/*
 *  Copyright 2015-2018 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.webpki.mobileid.keyprovider;

import java.io.IOException;

import java.security.SecureRandom;

import java.util.logging.Logger;

import java.net.URLEncoder;

import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.keygen2.ServerState;

import org.webpki.localized.LocalizedStrings;

import org.webpki.util.Base64URL;

import org.webpki.webutil.ServletUtil;

// This is the Home/Initialization servlet

public class KeyProviderInitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(KeyProviderInitServlet.class.getCanonicalName());

    static final String ANDROID_WEBPKI_VERSION_TAG      = "VER";
    static final String ANDROID_WEBPKI_VERSION_MACRO    = "$VER$";  // KeyGen2 Android PoC
    
    static final String KEYGEN2_SESSION_ATTR            = "keygen2";
    
    static final String SERVER_STATE_ISSUER             = "issuer";
    static final String SERVER_STATE_USER               = "user";

    static final String INIT_TAG                        = "init";     // Note: This is currently also a part of the KeyGen2 client!
    static final String ABORT_TAG                       = "abort";
    static final String PARAM_TAG = "msg";
    static final String ERROR_TAG = "err";
    
    static final String DEFAULT_NAME                    = "Luke Skywalker";

    static final String RA_NAME_PARAM                   = "name.mid";  // No general auto complete please
    static final String RA_ISSUER_PARAM                 = "issuer";
    
    static final int    ID_STRING_LENGTH                = 12;    // Fictitious identity universe
    static final String ID_STRING_NAME                  = "ID";  // Card label of ID is "ID"

    static final String DISMISSED_FOOTER                = "dismiss";
    static final int    DISMISS_TIME                    = 60 * 60 * 8; // 8 hours

    private static final String WEB_LINK =
            "<a href=\"" +
            LocalizedStrings.URL_TO_DESCRIPTION + 
            "\" target=\"_blank\">Mobile&nbsp;ID</a>";

    static String keygen2EnrollmentUrl;
    
    synchronized void initGlobals(HttpServletRequest request) throws IOException {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Get KeyGen2 protocol entry
        ////////////////////////////////////////////////////////////////////////////////////////////
        keygen2EnrollmentUrl = ServletUtil.getContextURL(request) + "/getkeys";

    }
    
    class UserData {

        String userName;
        String userId;
        String cardImage;
        String issuerName;

        UserData(String userName, String userId, KeyProviderService.IssuerHolder issuer) {
            this.userName = userName;
            this.userId = userId;
            StringBuilder idString = new StringBuilder(ID_STRING_NAME + ":&#x2009;");
            for (int q = 0; q < ID_STRING_LENGTH; q += 4) {
                if (q != 0) {
                    idString.append(' ');
                }
                idString.append(userId.substring(q, q + 4));
            }
            this.issuerName = issuer.commonName;
            this.cardImage = issuer.cardImage.replace("@n", userName)
                                             .replace("@i", idString);
            if (userName.length() > 26) {
                cardImage = cardImage.replace("font-size=\"20\"", "font-size=\"14\"");
            }
        }
        
        @Override
        public String toString() {
            return "Name=" + userName + " ID=" + userId + " Issuer=" + issuerName;
        }
    }

    String getParameter(HttpServletRequest request, String name) throws IOException {
        String value = request.getParameter(name);
        if (value == null) {
            throw new IOException ("Missing parameter: " + name);
        }
        return value;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // This is where a user's credentials should be verified
        // Note: in the demo we don't verify credentials
        if (keygen2EnrollmentUrl == null) {
            initGlobals(request);
        }
        request.setCharacterEncoding("utf-8");
        String userName = getParameter(request, RA_NAME_PARAM).trim(); 
        if (userName.length() == 0) {  // This is a demo...
            userName = DEFAULT_NAME;
        }
        String issuerName = getParameter(request, RA_ISSUER_PARAM);
        KeyProviderService.IssuerHolder issuer = KeyProviderService.issuers.get(issuerName);
        if (issuer == null) {
            throw new IOException("No such issuer: " + issuerName);
        }
        // Since we doesn't have a citizen registry we fake one :-)
        long randomUserId = new SecureRandom().nextLong() & 0x7fffffffffffffffl;
        StringBuilder userId = new StringBuilder();
        for (int q = 0; q < ID_STRING_LENGTH; q++) {
            userId.append(randomUserId % 10);
            randomUserId /= 10;
        }
        // We have a user
        UserData userData = new UserData(userName, userId.toString(), issuer);
        // We have a session to connect the user to
        HttpSession session = request.getSession(true);
        if (KeyProviderService.logging) {
            logger.info(userData.toString() + " Session ID=" + session.getId());
        }
        // Setup KeyGen2 using a session cookie for keeping state
        ServerState serverState = new ServerState(new KeyGen2SoftHSM(issuer.keyManagementKey));
        serverState.setServiceSpecificObject(SERVER_STATE_ISSUER, issuer);
        serverState.setServiceSpecificObject(SERVER_STATE_USER, userData);
        session.setAttribute(KEYGEN2_SESSION_ATTR, serverState);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // The following is the actual contract between an issuing server and a KeyGen2 client.
        // The "cookie" element is optional while the HTTP GET "url" argument is mandatory.
        // The "url" argument bootstraps the protocol.
        //
        // The "init" element on the bootstrap URL is a local Mobile RA convention.
        // The purpose of the random element is suppressing caching of bootstrap data.
        ////////////////////////////////////////////////////////////////////////////////////////////

        String extra = "cookie=JSESSIONID%3D" +
                     session.getId() +
                     "&url=" + URLEncoder.encode(keygen2EnrollmentUrl + "?" +
                     INIT_TAG + "=" + Base64URL.generateURLFriendlyRandom(8) +
                     (KeyProviderService.grantedVersions == null ? "" : "&" + ANDROID_WEBPKI_VERSION_TAG + "=" + ANDROID_WEBPKI_VERSION_MACRO), "UTF-8");
        response.sendRedirect("intent://keygen2?" + extra +
                              "#Intent;scheme=webpkiproxy;" +
                              "package=org.webpki.mobile.android;end");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        boolean footerDismissed = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            if (cookie.getName().equals(DISMISSED_FOOTER)) {
                footerDismissed = true;
            }
        }
        // We always start from zero
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        StringBuilder html = new StringBuilder(
            "<form name=\"shoot\" method=\"POST\" action=\"home\">" +
            "<div class=\"header\">" + LocalizedStrings.ENROLLMENT_HEADER + "</div>" +
            "<div style=\"padding:20pt 0 10pt 0;display:flex;justify-content:center;align-items:center\">" +
                "<div class=\"label\">" + LocalizedStrings.YOUR_NAME + ":&nbsp;</div>" +
                "<div><input type=\"text\" placeholder=\"" +
                LocalizedStrings.DEFAULT + ": " +
                DEFAULT_NAME + "\" maxlength=\"50\" " +
                "style=\"background-color:#def7fc\" class=\"label\" name=\"" +
                RA_NAME_PARAM + "\"></div>" +
            "</div>" + 
            "<div class=\"label\" style=\"display:flex;justify-content:center;align-items:center;padding-bottom:20pt;text-align:left\">" +
            "<div>" + LocalizedStrings.SELECTED_ISSUER + ":</div>" +
            "<div style=\"display:flex;flex-direction:column\">");
            boolean first = true;
            for (String issuer : KeyProviderService.issuers.keySet()) {
                html.append("<div style=\"display:flex;align-items:center\"><div>" +
                            "<input name=\"" + RA_ISSUER_PARAM + "\" type=\"radio\" value=\"")
                    .append(issuer)
                    .append("\"")
                    .append(first ? " checked" : "")
                    .append("></div><div>")
                    .append(KeyProviderService.issuers.get(issuer).commonName)
                    .append("</div></div>");
                first = false;
            }
        html.append(
                "</div>" +
            "</div>" +
            "<div id=\"command\" class=\"stdbtn\" onclick=\"enroll()\">" +
            LocalizedStrings.START_ENROLLMENT +
            "</div></form>");
        String javaScript;
        if (footerDismissed) {
            javaScript = "";
        } else {
            html.append("</div>" +
                        "<div id=\"sitefooter\" class=\"sitefooter\">" +
                        "<img src=\"images/x.svg\" class=\"xicon\" alt=\"x\" title=\"" +
                        LocalizedStrings.CLOSE_VIEW +
                        "\" onclick=\"closeDescription()\">" +
                        "<div style=\"padding:0.3em 1em 0.3em 0\">")
                .append(LocalizedStrings.DEMO_TEXT
                   .replace("@", WEB_LINK))
                .append("</div>");
            javaScript = "function closeDescription() {\n" +
                         "  let sitefooter = document.getElementById('sitefooter');\n" +
                         "  sitefooter.style.visibility = 'hidden';\n" +
                         "  document.cookie = '" + DISMISSED_FOOTER + 
                         " = true;Max-Age=" + DISMISS_TIME + "';\n" +
                         "}\n";
        }
        HTML.resultPage(response,
                        javaScript +
                        "function enroll() {\n" +
                        "  console.log('Mobile application is supposed to start here');\n" +
                        "  document.forms.shoot.submit();\n" +
                        "}\n",
                        false,
                        html);
    }
}

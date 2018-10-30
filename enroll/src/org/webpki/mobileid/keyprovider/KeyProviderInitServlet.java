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
    
    static final String KG2_SESSION_ATTR                = "keygen2";
    
    static final String SERVER_STATE_ISSUER             = "issuer";
    static final String SERVER_STATE_USER               = "user";

    static final String KG2_INIT_TAG                    = "init";     // Note: This is currently also a part of the KeyGen2 client!
    static final String KG2_ABORT_TAG                   = "abort";
    static final String KG2_PARAM_TAG                   = "msg";
    static final String KG2_ERROR_TAG                   = "err";
    
    static final String DEFAULT_USER_NAME               = "Luke Skywalker";

    static final String USER_NAME_PARAM                 = "name.mid";  // No general auto complete please
    static final String ISSUER_NAME_PARAM               = "issuer";
    static final String DESKTOP_MODE_PARAM              = "desktop";
    static final String TARGET_PLATFORM_PARAM           = "target";
    
    static final String DISMISSED_FOOTER                = "dismiss";
    static final int    DISMISS_TIME                    = 60 * 60 * 8; // 8 hours
    
    static final int    MINIMUM_CHROME_VERSION          = 67;

    static final String WEB_LINK =
            "<a href=\"" +
            LocalizedStrings.URL_TO_DESCRIPTION + 
            "\" target=\"_blank\">Mobile&nbsp;ID</a>";
    
    static String keygen2EnrollmentUrl;
    
    synchronized void initGlobals(HttpServletRequest request) throws IOException {
        // Get KeyGen2 protocol entry
        keygen2EnrollmentUrl = ServletUtil.getContextURL(request) + "/getkeys";
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
        // Check that we can make it at all
        TargetPlatforms targetPlatform = TargetPlatforms.valueOf(getParameter(request, TARGET_PLATFORM_PARAM));
        if (!targetPlatform.supported) {
            incompatibleBrowser(response,
                                LocalizedStrings.UNSUPPORTED_PLATFORM
                                    .replace("@", targetPlatform.name));
            return;
        }

        // This is where a user's credentials should be verified
        // Note: in the demo we don't verify credentials
        if (keygen2EnrollmentUrl == null) {
            initGlobals(request);
        }
        request.setCharacterEncoding("utf-8");
        String userName = getParameter(request, USER_NAME_PARAM).trim(); 
        if (userName.length() == 0) {  // This is a demo...
            userName = DEFAULT_USER_NAME;
        }
        String issuerName = getParameter(request, ISSUER_NAME_PARAM);
        KeyProviderService.IssuerHolder issuer = KeyProviderService.issuers.get(issuerName);
        if (issuer == null) {
            throw new IOException("No such issuer: " + issuerName);
        }
        // Since we doesn't have a citizen registry we fake one :-)
        long randomUserId = new SecureRandom().nextLong() & 0x7fffffffffffffffl;
        StringBuilder userId = new StringBuilder();
        for (int q = 0; q < UserData.ID_STRING_LENGTH; q++) {
            userId.append(randomUserId % 10);
            randomUserId /= 10;
        }
        // We have a user
        UserData userData = new UserData(userName, 
                                         userId.toString(),
                                         issuer,
                                         targetPlatform);
        // We have a session to connect the user to
        HttpSession session = request.getSession(true);
        if (KeyProviderService.logging) {
            logger.info(userData.toString() + " Session ID=" + session.getId());
        }
        // Setup KeyGen2 using a session cookie for keeping state
        ServerState serverState = new ServerState(new KeyGen2SoftHSM(issuer.keyManagementKey));
        serverState.setServiceSpecificObject(SERVER_STATE_ISSUER, issuer);
        serverState.setServiceSpecificObject(SERVER_STATE_USER, userData);
        session.setAttribute(KG2_SESSION_ATTR, serverState);

        // Now to big question, are we on a [suitable] mobile phone or on a desktop?
        if (new Boolean(getParameter(request, DESKTOP_MODE_PARAM))) {
            response.sendRedirect("qrinit");
            return;
        }
        
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
                     KG2_INIT_TAG + "=" + Base64URL.generateURLFriendlyRandom(8) +
                     (KeyProviderService.grantedVersions == null ? "" : "&" + ANDROID_WEBPKI_VERSION_TAG + "=" + ANDROID_WEBPKI_VERSION_MACRO), "UTF-8");
        response.sendRedirect("intent://keygen2?" + extra +
                              "#Intent;scheme=webpkiproxy;" +
                              "package=org.webpki.mobile.android;end");
    }
    
    void incompatibleBrowser(HttpServletResponse response, String reason) throws IOException, ServletException {
        StringBuilder html = new StringBuilder(
                "<div class=\"header\">" +
                "Incompatible Browser" +
                "</div>" +
                "<div class=\"label\" style=\"padding-top:20pt\">")
            .append(reason)
            .append("</div>");
        HTML.resultPage(response, null, false, html);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // We always start from zero
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Investigate which browser/platform we are using
        TargetPlatforms targetPlatform = TargetPlatforms.ANDROID;
        boolean desktopMode = true;
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.contains("Android ")) {
            int i = userAgent.indexOf(" Chrome/");
            if (i > 0) {
                String chromeVersion = userAgent.substring(i + 8, userAgent.indexOf('.', i));
                if (Integer.parseInt(chromeVersion) < MINIMUM_CHROME_VERSION) {
                    incompatibleBrowser(response,
                                        "Found Chrome version=" +  chromeVersion +
                                        ", min version=" + MINIMUM_CHROME_VERSION);
                    return;
                }
            } else {
                HTML.output(response,
                    "<!DOCTYPE html><html><head>" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                    "<title>Unsupported Browser</title>" +
                    "</head><body>" +
                    "&quot;Chrome&quot; is currently the only supported browser on Android" +
                    "</body></html>");
                return;
            }
            desktopMode = false;
        } else if (userAgent.contains(" Mobile/") &&
                   userAgent.contains(" Safari/") &&
                   userAgent.contains(" iPhone")) {
            targetPlatform = TargetPlatforms.IPHONE;
            return;
        }

        // Check if we need showing the footer
        boolean footerDismissed = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            if (cookie.getName().equals(DISMISSED_FOOTER)) {
                footerDismissed = true;
            }
        }

        // Create the actual HTML
        StringBuilder html = new StringBuilder(
            "<form name=\"shoot\" method=\"POST\" action=\"home\">" +
            "<input type=\"hidden\" name=\"" + DESKTOP_MODE_PARAM + "\" value=\"")
        .append(desktopMode)
        .append(
            "\">" +
            "<input type=\"hidden\" name=\"" + TARGET_PLATFORM_PARAM + "\" value=\"")
        .append(targetPlatform.toString())
        .append(
            "\">" +
            "<div class=\"header\">" + LocalizedStrings.ENROLLMENT_HEADER + "</div>" +
            "<div style=\"padding:20pt 0 10pt 0;display:flex;justify-content:center;align-items:center\">" +
                "<div class=\"label\">" + LocalizedStrings.YOUR_NAME + ":&nbsp;</div>" +
                "<div><input type=\"text\" placeholder=\"" +
                LocalizedStrings.DEFAULT + ": " +
                DEFAULT_USER_NAME + "\" maxlength=\"50\" " +
                "style=\"background-color:#def7fc\" class=\"label\" name=\"" +
                USER_NAME_PARAM + "\"></div>" +
            "</div>" + 
            "<div class=\"label\" style=\"display:flex;justify-content:center;align-items:center;padding-bottom:20pt;text-align:left\">" +
            "<div>" + LocalizedStrings.SELECTED_ISSUER + ":</div>" +
            "<div style=\"display:flex;flex-direction:column\">");
            boolean first = true;
            for (String issuer : KeyProviderService.issuers.keySet()) {
                html.append("<div style=\"display:flex;align-items:center\"><div>" +
                            "<input name=\"" + ISSUER_NAME_PARAM + "\" type=\"radio\" value=\"")
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

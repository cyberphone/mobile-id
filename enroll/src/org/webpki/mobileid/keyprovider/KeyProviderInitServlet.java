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
import java.io.OutputStream;
import java.util.logging.Logger;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.keygen2.ServerState;
import org.webpki.localized.LocalizedStrings;
import org.webpki.util.Base64URL;
import org.webpki.webutil.ServletUtil;

public class KeyProviderInitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(KeyProviderInitServlet.class.getCanonicalName());

    static final String ANDROID_WEBPKI_VERSION_TAG     = "VER";
    static final String ANDROID_WEBPKI_VERSION_MACRO   = "$VER$";  // KeyGen2 Android PoC
    
    static final String KEYGEN2_SESSION_ATTR           = "keygen2";
    
    static final String SERVER_STATE_ISSUER            = "issuer";

    static final String INIT_TAG = "init";     // Note: This is currently also a part of the KeyGen2 client!
    static final String ABORT_TAG = "abort";
    static final String PARAM_TAG = "msg";
    static final String ERROR_TAG = "err";
    
    static final String DEFAULT_NAME                   ="Luke Skywalker";

    static String keygen2EnrollmentUrl;
    
    synchronized void initGlobals(String baseUrl) throws IOException {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Get KeyGen2 protocol entry
        ////////////////////////////////////////////////////////////////////////////////////////////
        keygen2EnrollmentUrl = baseUrl + "/getkeys";

    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (keygen2EnrollmentUrl == null) {
            initGlobals(ServletUtil.getContextURL(request));
        }
        String name = request.getParameter("name").trim(); 
        if (name.length() == 0) {
            name = DEFAULT_NAME;
        }
        logger.info(name);
        HttpSession session = request.getSession(true);
        logger.info("Created ID=" + session.getId() + " Int" + session.getMaxInactiveInterval());
        // Temporary issuer selector
        KeyProviderService.IssuerHolder issuer = KeyProviderService.issuers.get("laposte");
        ServerState serverState = new ServerState(new KeyGen2SoftHSM(issuer.keyManagementKey));
        serverState.setServiceSpecificObject(SERVER_STATE_ISSUER, issuer);
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
/*
               output(response, 
               getHTML(null,
//                       null,
                       "onload=\"document.location.href='intent://keygen2?" + extra +
                           "#Intent;scheme=webpkiproxy;" +
                           "package=org.webpki.mobile.android;end'\"",
                       "<tr><td align=\"center\"><table>" +
                       "<tr><td align=\"center\">Here the App is supposed to start...</td></tr>" +
                       "</table></td></tr>"));
*/
        response.sendRedirect("intent://keygen2?" + extra +
                              "#Intent;scheme=webpkiproxy;" +
                              "package=org.webpki.mobile.android;end");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
/*
        if (!request.getHeader("User-Agent").contains("Android")) {
            output(response, 
                    getHTML(null,
                            null,
                            "<tr><td width=\"100%\" align=\"center\" valign=\"middle\">" +
                            LocalizedStrings.ANDROID_ONLY +
                            "</td></tr>"));
            return;
        }
*/
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        HTML.resultPage(response,
                        "function enroll() {\n" +
                        "  console.log('Mobile application is supposed to start here');\n" +
                        "  document.forms.shoot.submit();\n" +
                        "}\n",
                        false,
                        new StringBuilder(
            "<form name=\"shoot\" method=\"POST\" action=\"home\">" +
            "<div style=\"text-align:left\">This proof-of-concept system provisions secure payment " +
                 "credentials to be used in the Android version of Mobile ID</div>" +
            "<div style=\"padding:20pt 0 10pt 0;display:flex;justify-content:center;align-items:center\">" +
                 "<div class=\"header\">Your name:&nbsp;</div>" +
                 "<div><input type=\"text\" placeholder=\"default: " +
                 DEFAULT_NAME +
                 "\" style=\"background-color:#ffffe0\" class=\"header\" name=\"name\"></div></div>" + 
            "<div class=\"header\" style=\"display:flex;justify-content:center;align-items:center;padding-bottom:20pt;text-align:left\">" +
                 "<div>Selected Issuer:</div>" +
                    "<div style=\"display:flex;flex-direction:column\">" +
                      "<div style=\"display:flex;align-items:center\"><div><input type=\"radio\"></div><div>La Poste</div></div>" +
                      "<div style=\"display:flex;align-items:center\"><div><input type=\"radio\"></div><div>BankID ltd.</div></div>" +
                    "</div>" +
            "</div>" +
            "<div id=\"command\" class=\"stdbtn\" onclick=\"enroll()\">" +
                        LocalizedStrings.START_ENROLLMENT +
                  "</div></form>"));
    }
}

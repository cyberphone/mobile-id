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

package org.webpki.mobileid.egovernment;

import java.io.IOException;
import java.util.logging.Logger;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

// This is a servlet which through QR + mobile browser invokes the WebAuth protocol

public class AndroidBootstrapServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(AndroidBootstrapServlet.class.getCanonicalName());

    static final String TOMCAT_SESSION_COOKIE           = "JSESSIONID";
    
    static String createIntent(HttpSession session) throws IOException {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // The following is the actual contract between an authentication server and a WebAuth client.
        // The "cookie" argument holds the session in progress while the "url" argument holds
        // an address to a protocol bootstrap service to be invoked by an HTTPS GET operation.
        ////////////////////////////////////////////////////////////////////////////////////////////

        String authUrlEnc = URLEncoder.encode(LoginServlet.authenticationUrl, "utf-8");
        return new StringBuilder("intent://webauth?cookie=" + TOMCAT_SESSION_COOKIE + "%3D").append(session.getId())
            .append("&url=").append(authUrlEnc)
            .append("&ver=").append(eGovernmentService.grantedVersions)
            .append("&init=").append(authUrlEnc)
            .append("&cncl=").append(URLEncoder.encode(
                    WebAuthServlet.getResultUrl(AuthResultServlet.Status.USER_ABORT,
                                                (String)session.getAttribute(QRInitServlet.QR_SESSION_ID_ATTR)), "utf-8"))
            .append("#Intent;scheme=webpkiproxy;" +
                    "package=org.webpki.mobile.android;end").toString();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = QRSessions.getHttpSession(ProtectedServlet
                .getParameter(request, QRSessions.QR_SESSION_ID));
        if (session == null) {
            WebAuthServlet.returnResult(response, AuthResultServlet.Status.TIMEOUT, null);
            return;
        }
        HTML.resultPage(response,
                        null,
                        false,
                        "  document.location.href = '" + createIntent(session) + "';\n",
                        null,
                        new StringBuilder(
                            "<div class=\"header\">Android QR &quot;Bootstrap&quot;</div>"));
    }
}

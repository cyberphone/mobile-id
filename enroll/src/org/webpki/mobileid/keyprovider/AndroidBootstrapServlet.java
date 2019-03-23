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

import java.util.logging.Logger;

import java.net.URLEncoder;

import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


// This is a servlet which through QR + mobile browser invokes the KeyGen2 protocol

public class AndroidBootstrapServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(AndroidBootstrapServlet.class.getCanonicalName());

    static final String TOMCAT_SESSION_COOKIE           = "JSESSIONID";
       
    static String createIntent(HttpSession session) throws IOException {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // The following is the actual contract between an issuing server and a KeyGen2 client.
        // The "cookie" argument holds the session in progress while the "url" argument holds
        // an address to a protocol bootstrap service to be invoked by an HTTPS GET operation.
        ////////////////////////////////////////////////////////////////////////////////////////////
        String urlEncoded = URLEncoder.encode(KeyProviderInitServlet.keygen2EnrollmentUrl, "utf-8");
        return new StringBuilder("intent://keygen2?cookie=" + TOMCAT_SESSION_COOKIE + "%3D").append(session.getId())
            .append("&url=").append(urlEncoded)
            .append("&ver=").append(KeyProviderService.grantedVersions)
            .append("&init=").append(urlEncoded).append("%3F" + KeyProviderInitServlet.KG2_INIT_TAG + "%3Dtrue")
            .append("&cncl=").append(urlEncoded).append("%3F" + KeyProviderInitServlet.KG2_ABORT_TAG + "%3Dtrue")
            .append("#Intent;scheme=webpkiproxy;" +
                    "package=org.webpki.mobile.android;end").toString();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = QRSessions.getHttpSession(KeyProviderInitServlet
                .getParameter(request, QRSessions.QR_SESSION_ID));
        if (session == null) {
            throw new IOException("QR Session timeout");
        }
        response.addCookie(new Cookie(TOMCAT_SESSION_COOKIE, session.getId()));
        HTML.resultPage(response,
                        null,
                        false,
                        "  document.location.href = '" + createIntent(session) + "';\n", 
                        new StringBuilder(
                            "<div class=\"header\">Android QR &quot;Bootstrap&quot;</div>"));
    }
}

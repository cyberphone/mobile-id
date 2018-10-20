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

package org.webpki.mobileid.authenticate;

import java.io.IOException;

import Logger;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.webauth.AuthenticationRequestEncoder;

import org.webpki.json.JSONOutputFormats;

import java.util.logging.Logger;

public class AuthenticationBootstrapServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger
            .getLogger(AuthenticationBootstrapServlet.class);

    private boolean versionCheckSucceeded(HttpServletRequest request)
            throws IOException, ServletException {
        if (LoginService.version_check == null) {
            return true;
        }
        String version = request.getParameter(PluginTriggerServlet.ANDROID_WEBPKI_VERSION_TAG);
        for (String ok_version : LoginService.version_check) {
            if (ok_version.equals(version)) {
                return true;
            }
        }
        return false;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (!versionCheckSucceeded(request)) {
            response.sendRedirect(LoginService.application_url
                    + AbortedServlet.ABORT_SERVLET
                    + "You%20need%20to%20update%20the%20webpkisuite-4-android%20application!");
            return;
        }
        AuthenticationRequestEncoder auth_req = AuthenticationDB
                .getAuthenticationRequest(PluginTriggerServlet.getID(request));
        if (auth_req == null) {
            response.sendRedirect(LoginService.application_url
                    + AbortedServlet.ABORT_SERVLET + "No%20such%20session!");
            return;
        }
        response.addHeader("EXPIRES", "Thu, 01 Jan 1970 00:00:00 GMT");
        response.addHeader("Pragma", "No-Cache");
        response.setContentType("application/json");
        byte[] sent_data = auth_req
                .serializeJSONDocument(JSONOutputFormats.PRETTY_PRINT);
        response.getOutputStream().write(sent_data);
        if (LoginService.debug) {
            log.info("Sent authentication request:\n"
                    + new String(sent_data, "UTF-8"));
        }
    }
}

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

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.json.JSONDecoder;
import org.webpki.json.JSONOutputFormats;

import org.webpki.webauth.AuthenticationRequestEncoder;
import org.webpki.webutil.ServletUtil;

// This is core WebAuth servlet

public class WebAuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(WebAuthServlet.class.getCanonicalName());

    static final String JSON_CONTENT_TYPE               = "application/json";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IOException("Session timeout");
        }
        AuthenticationRequestEncoder authReq = (AuthenticationRequestEncoder) session.getAttribute(LoginServlet.AUTH_REQ);
        byte[] jsonData = authReq.serializeJSONDocument(JSONOutputFormats.PRETTY_PRINT);
        if (eGovernmentService.logging) {
            logger.info("Sent message\n" + new String(jsonData, "UTF-8"));
        }
        response.setContentType(JSON_CONTENT_TYPE);
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(jsonData);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // This where we are supposed to get the authentication response
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IOException("Missing session");
        }
        if (session.getAttribute(UserData.USER_DATA) != null) {
            throw new IOException("Session weirdness");
        }
        byte[] jsonData = ServletUtil.getData(request);
        if (!request.getContentType().equals(JSON_CONTENT_TYPE)) {
            throw new IOException("Wrong \"Content-Type\": " + request.getContentType());
        }
        if (eGovernmentService.logging) {
            logger.info("Received message:\n" + new String(jsonData, "UTF-8"));
        }
        JSONDecoder jsonObject = eGovernmentService.webAuth2JSONCache.parse(jsonData);
    }
}

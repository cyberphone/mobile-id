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

import java.security.cert.X509Certificate;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.crypto.CertificateInfo;

import org.webpki.localized.LocalizedStrings;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    static final String LOGIN_TARGET = "target";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        StringBuilder html = new StringBuilder(
            "<form name=\"shoot\" method=\"POST\" action=\"login\">" +
            "<input type=\"hidden\" name=\"" + LoginServlet.LOGIN_TARGET + "\" value=\"")
        .append(request.getParameter(LOGIN_TARGET))
        .append(
            "\">" +
            "<div class=\"header\">" +
            LocalizedStrings.REQUIRES_LOGIN +
            "<br>" +
            LocalizedStrings.SELECT_LOGIN_METHOD +
            "</div>" +
            "<img src=\"images/mobileidlogo.svg\" title=\"Mobile ID\" alt=\"Mobile ID\"" +
             " onclick=\"document.forms.shoot.submit()\" class=\"loginbtn\">" +
            "<div class=\"footer\">" +
             LocalizedStrings.ONLY_ONE_LOGIN_METHOD + 
             "...</div>" +
            "</form>");
        HTML.resultPage(response, null, html);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        X509Certificate certificate = eGovernmentService.demoCertificate;
        CertificateInfo certInfo = new CertificateInfo(certificate);
        session.setAttribute(UserData.USER_DATA, 
                             new UserData(session,
                                          certInfo.getSubjectCommonName(), 
                                          certInfo.getSubjectSerialNumber(),
                                          certificate));
        response.sendRedirect(request.getParameter(LOGIN_TARGET));
    }
}

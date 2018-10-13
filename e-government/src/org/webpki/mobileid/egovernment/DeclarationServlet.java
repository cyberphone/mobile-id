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

import java.util.GregorianCalendar;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.localized.LocalizedStrings;

public class DeclarationServlet extends ProtectedServlet {

    private static final long serialVersionUID = 1L;
    
    private static int referenceId = 567;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        UserData userData = ProtectedServlet.loginCheck(request, response);
        if (userData == null) {
            return;
        }
        StringBuilder html = AvailableServices.DECLARATION.addSelfForm()
        .append(
            "<div class=\"header\">")
        .append(LocalizedStrings.DECLARATION_HEADER
            .replace("@", 
                     Integer.toString(new GregorianCalendar().get(GregorianCalendar.YEAR) - 1)))
        .append(
            "</div>" +
            "<div>" +
            "<div class=\"stdbtn\" onclick=\"document.forms.shoot.submit()\">Submit</div>" +
            "</div></form>");
        HTML.resultPage(response, userData, html);
    }

    @Override
    void protectedPost(UserData userData, 
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        String emailSubject = userData.userName.toLowerCase().replace(' ', '.');
        StringBuilder html = new StringBuilder(
            "<div class=\"header\">" +
            LocalizedStrings.DECLARATION_RECEIVED +
            "</div>" +
            "<div><table class=\"tftable\"><tr><th>" +
            LocalizedStrings.TIME_STAMP +
            "</th><td>")
        .append(getDateString(new GregorianCalendar().getTime()))
        .append(
            "</td></tr>" +
            "<tr><th>" +
            LocalizedStrings.REFERENCE_ID +
            "</th><td>")
        .append(String.format("%08d", referenceId++))
        .append(
            "</td></tr></table></div>" +
            "<div class=\"footer\"><table class=\"msgtable\"><tr><td>" +
            LocalizedStrings.CONFIRMATION + 
            ":</td></tr><tr><td style=\"white-space:nowrap;font-weight:bold\">")
        .append(emailSubject)
        .append("@gmail.com</td></tr></table></div>");
        HTML.resultPage(response, userData, html);
    }
}

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

import org.webpki.util.ISODateTime;

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
        HTML.declarationPage(response, userData);
    }

    @Override
    void protectedPost(UserData userData, 
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<table id=\"content\" style=\"position:absolute\">" +
             "<tr><td class=\"header\">Declaration Received</td></tr>" +
             "<tr><td><table class=\"tftable\"><tr><th>Time Stamp</th><td>")
        .append(ISODateTime.formatDateTime(new GregorianCalendar(), true)
                     .replace('T', ' ').replace("Z", " UTC"))
        .append("</td></tr><tr><th>Reference ID</th><td>")
        .append(String.format("%08d", referenceId++))
        .append("</td></tr></table></td></tr>" +
                "<tr><td style=\"text-align:center\">" +
                "<table style=\"display:inline-block;margin-top:10pt;text-align:left\">" +
                "<tr><td>A confirmation has been sent to:</td></tr>" +
                "<tr><td><b>")
        .append("luke.skywalker@gmail.com")
        .append("</b></td></tr></table></td></tr></table>");
        HTML.resultPage(response, userData, s.toString());
    }
}

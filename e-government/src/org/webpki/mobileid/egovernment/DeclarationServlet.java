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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        HTML.declarationPage(response, userData, new GregorianCalendar().get(GregorianCalendar.YEAR - 1));
    }

    @Override
    void protectedPost(UserData userData, 
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        String emailSubject = userData.user.toLowerCase().replace(' ', '.');
        StringBuilder s = new StringBuilder("<table id=\"content\" style=\"position:absolute\">" +
             "<tr><td class=\"header\">Declaration Received</td></tr>" +
             "<tr><td><table class=\"tftable\"><tr><th>Time Stamp</th><td>")
        .append(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' CET'").format(new Date()))
        .append("</td></tr><tr><th>Reference ID</th><td>")
        .append(String.format("%08d", referenceId++))
        .append("</td></tr></table></td></tr>" +
                "<tr><td style=\"text-align:center\">" +
                "<div class=\"emailmsg\">" +
                "A confirmation has been sent to:<br><b>")
        .append(emailSubject)
        .append("@gmail.com</b></div></td></tr></table>");
        HTML.resultPage(response, userData, s.toString());
    }
}

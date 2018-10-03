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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SubmitMessageServlet extends ProtectedServlet {

    private static final long serialVersionUID = 1L;
    
    enum MessageTypes {
        NOT_SELECTED      ("Select subject..."),
        TAX_QUESTION      ("Question regarding taxation"),
        FILE_COMPLAINT    ("File a complaint"),
        TECHNICAL_SUPPORT ("Technical support"),
        OTHER             ("Other topic");

        String userText;

        MessageTypes(String userText) {
            this.userText = userText;

        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        UserData userData = ProtectedServlet.loginCheck(request, response);
        if (userData == null) {
            return;
        }
        HTML.submitMessagePage(response, userData);
    }

    @Override
    void protectedPost(UserData userData, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<table id=\"content\" style=\"position:absolute\">" +
             "<tr><td class=\"header\">Message Received</td></tr>" +
             "<tr><td>Thank you for your input!</td></tr></table>");
        HTML.resultPage(response, userData, s.toString());  }
}

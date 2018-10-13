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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.localized.LocalizedStrings;

public class SendMessageServlet extends ProtectedServlet {

    private static final long serialVersionUID = 1L;
    
    static Logger logger = Logger.getLogger(SendMessageServlet.class.getCanonicalName());

    enum MessageTypes {

        NOT_SELECTED      (LocalizedStrings.NO_SELECT),
        TAX_QUESTION      (LocalizedStrings.SELECT_TAX_QUESTION),
        FILE_COMPLAINT    (LocalizedStrings.SELECT_FILE_COMPLAINT),
        TECHNICAL_SUPPORT (LocalizedStrings.SELECT_TECH_SUPPORT),
        OTHER             (LocalizedStrings.SELECT_OTHER_TOPIC);

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
        StringBuilder html = AvailableServices.SEND_MESSAGE.addSelfForm()
        .append(
            "<div class=\"header\">" +
            LocalizedStrings.SEND_MESSAGE +
            "</div>" +
            "<div><table style=\"display:inline-block\">" +
            "<tr><td style=\"text-align:left;padding-bottom:2pt\">" +
            "<select id=\"type\" autofocus name=\"type\">");
        for (MessageTypes type : MessageTypes.values()) {
            html.append("<option value=\"")
                .append(type.toString())
                .append("\">")
                .append(type.userText)
                .append("</option>");
        }
        html.append(
            "</select></td></tr>" +
            "<tr><td><textarea id=\"message\" name=\"message\" placeholder=\"" +
            LocalizedStrings.YOUR_MESSAGE_TIP +
            "...\" style=\"box-sizing:border-box\" rows=\"10\" cols=\"10\"></textarea>" +
            "</td></tr></table></div><div style=\"padding-top:10pt\">" +
            "<div class=\"stdbtn\" onclick=\"verify(this)\">" +
            LocalizedStrings.SUBMIT_BTN +
            "</div></div>" +
            "</form>");
        HTML.resultPage(response,
            "function verify(fromElement) {\n" +
            "  let type = document.getElementById('type');\n" +
            "  if (type.value == '" +
                        MessageTypes.NOT_SELECTED.toString() + "') {\n" +
            "    type.focus();\n" +
            "    toast('" +
                       HTML.javaScript(LocalizedStrings.NO_SELECT) +
                       "', fromElement);\n" +
            "    return;\n" +
            "  }\n" +
            "  let message = document.getElementById('message');\n\n" +
            "  if (message.value.trim().length == 0) {\n" +
            "    message.focus();\n" +
            "    toast('" +
                          HTML.javaScript(LocalizedStrings.NO_MESSAGE) +
                      "', fromElement);\n" +
            "    return;\n" +
            "  }\n" +
            "  document.forms.shoot.submit();\n" +
            "}\n"
                        , 
                        true, null, userData, html);
    }

    @Override
    void protectedPost(UserData userData,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        String type = getString(request, "type");
        String message = getString(request, "message");
        if (eGovernmentService.logging) {
            logger.info(type + "\n" + message);
        }

        StringBuilder html = new StringBuilder(
            "<div class=\"header\">" +
            LocalizedStrings.MESSAGE_RECEIVED +
            "</div>" +
            "<div><table class=\"msgtable\"><tr><td>")
        .append(LocalizedStrings.THANKS_FOR_MESSAGE)
        .append(
            "</td></tr></table></div>");
        HTML.resultPage(response, userData, html);  
    }
}

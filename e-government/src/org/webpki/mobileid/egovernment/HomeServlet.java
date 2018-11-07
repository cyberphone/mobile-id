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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.localized.LocalizedStrings;

public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final String DISMISSED_FOOTER = "dismiss";
    static final int DISMISS_TIME        = 60 * 60 * 8; // 8 hours
    
    private static final String WEB_LINK =
            "<a href=\"" +
            LocalizedStrings.URL_TO_DESCRIPTION + 
            "\" target=\"_blank\">Mobile&nbsp;ID</a>";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // MSIE doesn't support the UI
        String msieTest = request.getHeader("User-Agent");
        if (msieTest.contains("Mozilla/5") && (msieTest.contains(" MSIE ") || msieTest.contains("Trident/"))) {
            LoginServlet.incompatibiltyIssues(response, 
                                              LocalizedStrings.UNSUPPORTED_PLATFORM
                                                  .replace("@", "&quot;Internet Explorer&quot;"));
            return;
        }
        boolean footerDismissed = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            if (cookie.getName().equals(DISMISSED_FOOTER)) {
                footerDismissed = true;
            }
        }
        StringBuilder html = new StringBuilder(
                "<div class=\"header\">" +
                LocalizedStrings.SELECT_SERVICE +
                "</div>" +
                "<div><table style=\"display:inline-block\">");
        boolean first = true;
        for (AvailableServices service : AvailableServices.values()){
            html.append("<tr><td><div class=\"multibtn\" onclick=\"document.location.href='")
                .append(service.urlPath)
                .append("'\" title=\"")
                .append(service.userText);
            if (first) {
                html.append("\" style=\"margin-top:0px");
                first = false;
            }
            html.append("\">")
                .append(service.userText)
                .append("</div></td></tr>");
        }
        html.append("</table></div>");
        String javaScript;
        if (footerDismissed) {
            javaScript = null;
        } else {
            html.append("</div>" +
                        "<div id=\"sitefooter\" class=\"sitefooter\">" +
                        "<img src=\"images/x.svg\" class=\"xicon\" alt=\"x\" title=\"" +
                        LocalizedStrings.CLOSE_VIEW +
                        "\" onclick=\"closeDescription()\">" +
                        "<div>")
                .append(LocalizedStrings.DEMO_TEXT
                   .replace("@", WEB_LINK))
                .append("</div>");
            javaScript = "function closeDescription() {\n" +
                         "  let sitefooter = document.getElementById('sitefooter');\n" +
                         "  sitefooter.style.visibility = 'hidden';\n" +
                         "  document.cookie = '" + DISMISSED_FOOTER + 
                         " = true;Max-Age=" + DISMISS_TIME + "';\n" +
                         "}\n";
        }
        HTML.resultPage(response,
                        javaScript,
                        false,
                        null,
                        UserData.getUserData(request),
                        html);
    }
}

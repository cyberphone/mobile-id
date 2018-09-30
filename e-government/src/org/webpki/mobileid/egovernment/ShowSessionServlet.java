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

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShowSessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        StringBuilder s = new StringBuilder(
            "<div style=\"background-color:#f8f8f8\">" +
            "<div class=\"dialog\" style=\"font-size:8pt\"><img src=\"images/x.svg\" class=\"xicon\" alt=\"x\" title=\"Close session wiew\""+
            " onclick=\"document.getElementById('session').style.visibility='hidden'\"></div>" + 
            "<div class=\"dialog\">&nbsp;&nbsp;Session Data</div>" +
            "</div><div style=\"padding:10pt;background-color:white\">");
        UserData userData = UserData.getUserData(request);
        if (userData == null) {
            s.append("The session appears to have terminated");
        } else {
            s.append("<table class=\"tftable\">" +
                     "<tr><th>Session ID</th><td>")
             .append(userData.sessionId)
             .append(
                     "</td></tr><tr><th>Start Time</th><td>")
             .append(ProtectedServlet.getDateString(new Date(userData.creationTime)))
             .append("</td></tr></table>");
//            s.append("<pre>" + userData.certificate.toString() + "</pre>");
        }
        HTML.output(response, s.append("</div>").toString());
    }
}

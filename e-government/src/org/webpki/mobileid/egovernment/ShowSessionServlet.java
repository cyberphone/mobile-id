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

import org.webpki.crypto.CertificateInfo;

import org.webpki.localized.LocalizedStrings;

public class ShowSessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private void addEntry(StringBuilder s, String header, String argument) {
        s.append("<tr><th>")
         .append(header)
         .append("</th><td>")
         .append(argument)
         .append("</td></tr>");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        StringBuilder s = new StringBuilder(
            "<div>" +
            "<div class=\"dialog\" style=\"font-size:8pt;cursor:pointer\"" +
            " onclick=\"document.getElementById('session').style.visibility='hidden'\">" +
            "<img src=\"images/x.svg\" class=\"xicon\" alt=\"x\" title=\"Close session wiew\"></div>" + 
            "<div class=\"dialog\">Session Data</div>" +
            "</div>" +
            "<div style=\"padding:10pt\">");
        UserData userData = UserData.getUserData(request);
        if (userData == null) {
            s.append("The session appears to have terminated");
        } else {
            s.append("<table class=\"tftable\">" +
                     "<tr><th>Session&nbsp;ID</th><td>")
             .append(userData.sessionId)
             .append("</td></tr><tr><th>Start&nbsp;Time</th><td>")
             .append(ProtectedServlet.getDateString(new Date(userData.creationTime)))
             .append("</td></tr></table>" +
                     "<div style=\"padding:10pt 0px 3pt 0px\">" +
            		 LocalizedStrings.LS_USER_CERTIFICATE +
            		 ":</div>" +
                     "<table class=\"tftable\">");
            CertificateInfo certInfo = new CertificateInfo(userData.certificate);
            addEntry(s, LocalizedStrings.LS_SERIAL_NUMBER, certInfo.getSerialNumber() +
                     " (" + certInfo.getSerialNumberInHex() + ")");
            addEntry(s, LocalizedStrings.LS_ISSUER_NAME, certInfo.getIssuer());
            addEntry(s, LocalizedStrings.LS_SUBJECT_NAME, certInfo.getSubject());
            addEntry(s, LocalizedStrings.LS_VALIDITY,
                     ProtectedServlet.getDateString(userData.certificate.getNotBefore()) + 
                     " - " +
                     ProtectedServlet.getDateString(userData.certificate.getNotAfter()));
            s.append("</table>");
        }
        HTML.output(response, s.append("</div>").toString());
    }
}

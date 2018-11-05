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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.localized.LocalizedStrings;

import org.webpki.webutil.ServletUtil;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    static final String LOGIN_TARGET  = "target";
    static final String MOBILE_ID_APP = "Mobile ID App";

    static String baseUrl;
    static String authenticationUrl;

    synchronized void initGlobals(HttpServletRequest request) throws IOException {
        baseUrl = ServletUtil.getContextURL(request);
        authenticationUrl = baseUrl + "/webauth";
    }

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
        .append(ProtectedServlet.getParameter(request, LOGIN_TARGET))
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

        if (baseUrl == null) {
            initGlobals(request);
        }
        // Create a session.
        HttpSession session = request.getSession(true);
        // But make sure i is empty
        session.removeAttribute(UserData.USER_DATA);
        // Save where to go when auth is ready
        session.setAttribute(LOGIN_TARGET,
                             baseUrl+ "/" + ProtectedServlet.getParameter(request, LOGIN_TARGET));
        
        // Demo only
        if (eGovernmentService.demoCertificate != null) {
            demoAuthentication(request, response);
            return;
        }
        
        // Real authentication
        String userAgent = request.getHeader("User-Agent");
        TargetPlatforms targetPlatform = userAgent.contains("Android") ? 
                                               TargetPlatforms.ANDROID : TargetPlatforms.DESKTOP_MODE;

        // Now to big question, are we on a [suitable] mobile phone or on a desktop?
        if (targetPlatform == TargetPlatforms.DESKTOP_MODE) {
            response.sendRedirect(QRInitServlet.QR_INIT_SERVLET_NAME);
            return;
        }
        
        // iOS code is not yet in place...
        response.sendRedirect(AndroidBootstrapServlet.createIntent(session));
    }

    void demoAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        StringBuilder html = new StringBuilder("<table style=\"border-color:red;" +
  "border-style:solid;border-width:2pt;border-collapse:collapse;background-color:white\">" +
   "<tr><td title=\"" +
     LocalizedStrings.UI_DEMO_HT_REQUEST_DOMAIN +
     "\" style=\"background-color:black;color:white;text-align:left;padding:3pt 5pt\">" +
     LocalizedStrings.UI_DEMO_TOP_URL + "</td></tr>" +
   "<tr><td><img style=\"height:14pt;padding:4pt;display:block;margin-right:auto\"" +
     " src=\"images/mobileidlogo.svg\" alt=\"Mobile ID\" title=\"" +
     MOBILE_ID_APP + "\"></td></tr>" +
   "<tr><td class=\"uidheader\">" +
     LocalizedStrings.UI_DEMO_AUTH_TO + "</td></tr>" +
   "<tr><td>" + LocalizedStrings.UI_DEMO_SELECTED_CRED + "</td></tr>" +
   "<tr><td>")
        .append(eGovernmentService.demoCard)
        .append("</td></tr>" +
   "<tr><td><div style=\"display:flex;justify-content:center\"><div>PIN</div><div>Field</div><div>PIN</div>" +
     "</div></td></tr>" +
   "<tr><td>")
        .append(eGovernmentService.pinKeyboard)
        .append("</td></tr>" +
   "</table>");
        HTML.resultPage(response, null, html);
    }
}

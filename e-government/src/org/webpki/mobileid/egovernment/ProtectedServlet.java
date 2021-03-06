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

import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.localized.LocalizedStrings;

public abstract class ProtectedServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    abstract void protectedPost(UserData userData,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException, ServletException;

    @Override
    public final void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        UserData userData = UserData.getUserData(request);
        if (userData == null) {
            HTML.resultPage(response, userData,
                new StringBuilder(
                    "<div style=\"color:red;font-weight:bold\">" +
                    LocalizedStrings.SESSION_TIMOUT +
                    "..." +
                    "</div>"));
            return;
        }
        protectedPost(userData, request, response);
    }

    static UserData loginCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserData userData = UserData.getUserData(request);
        if (userData == null) {
            String path = request.getServletPath();
            response.sendRedirect("login?" + LoginServlet.LOGIN_TO_APP_PARAM + "=" + path.substring(path.lastIndexOf('/') + 1));
        }
        return userData;
    }

    static String getDateString(GregorianCalendar dateTime) {
        return new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' CET'").format(dateTime.getTime());
    }
    
    static String getParameter(HttpServletRequest request, String name) throws IOException {
        String result = request.getParameter(name);
        if (result == null) {
            throw new IOException("\"" + name + "\" missing");
        }
        return result;
    }
}

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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

// This is servlet is called for QR auth and for errors

public class AuthResultServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    static Logger logger = Logger.getLogger(AuthResultServlet.class.getCanonicalName());

    static final String STATUS_TAG               = "status";
    static final String OPTIONAL_DATA_TAG        = "optional";
    
    static final String AUTH_RESULT_SERVLET_NAME = "authresult";

    enum Status {

        USER_ABORT ("Aborted by the User"),
        QR_NORMAL  ("Authentication Succeded"),
        TIMEOUT    ("Authentication Timeout"),
        OTHER      ("Authentication Error");
        
        String header;
        
        Status(String header) {
            this.header = header;
        }
    }    

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        String statusTag = request.getParameter(STATUS_TAG);
        StringBuilder html = new StringBuilder("<div class=\"header\">");
        if (statusTag == null) {
            HTML.resultPage(response,
                            null,
                            html.append("Malformed Request</div>"));
            return;
        }
        Status status = Status.valueOf(statusTag);
        String optional = request.getParameter(OPTIONAL_DATA_TAG);
        if (status == Status.USER_ABORT) {
            QRSessions.cancelSession(optional);
        }
        HTML.resultPage(response,
                        null,
                        html.append(status.header)
                            .append("</div>"));
    }
}

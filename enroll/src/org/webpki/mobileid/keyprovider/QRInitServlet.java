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

package org.webpki.mobileid.keyprovider;

import java.io.IOException;

import java.util.logging.Logger;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.keygen2.ServerState;

import org.webpki.localized.LocalizedStrings;

import org.webpki.util.Base64URL;

import org.webpki.webutil.ServletUtil;

// This is the "Desktop" initialization servlet using QR code

public class QRInitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(QRInitServlet.class.getCanonicalName());

    @Override
    public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String id = new String(ServletUtil.getData(request), "UTF-8");
        if (KeyProviderService.logging) {
            logger.info("QR DISP=" + id);
        }
        QRSessions.Synchronizer synchronizer = QRSessions.getSynchronizer(id);
        if (synchronizer == null) {
            sendResult(response, QRSessions.QR_RETURN);
        } else if (synchronizer.perform(QRSessions.COMET_WAIT)) {
            QRSessions.removeSession(id);
            sendResult(response, QRSessions.QR_SUCCESS);
        } else {
            if (KeyProviderService.logging) {
                logger.info("QR Continue");
            }
            sendResult(response, synchronizer.isInProgress() ? QRSessions.QR_PROGRESS : QRSessions.QR_CONTINUE);
        }
    }

    void sendResult(HttpServletResponse response, String result) throws IOException {
        response.setContentType("text/plain");
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(result.getBytes("UTF-8"));
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        StringBuilder html = new StringBuilder(
                "<div class=\"header\">QR Activation Mode</div>" +
                "<div class=\"label\" style=\"padding-top:15pt\">" +
                "Since enrollment was not initiated from Android, you " +
                "need a QR code to &quot;boostrap&quot; the enrollment process.</div>");
        HTML.resultPage(response,
                        null,
                        false,html);
    }
}

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

import java.net.URLEncoder;

import java.util.logging.Logger;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.localized.LocalizedStrings;

import org.webpki.util.Base64;

import org.webpki.webutil.ServletUtil;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

// This is the "Desktop" initialization servlet using QR code

public class QRInitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final String QR_SESSION_ID_ATTR     = "qrsess";
    
    static final String QR_INIT_SERVLET_NAME   = "qrinit";
    
    static Logger logger = Logger.getLogger(QRInitServlet.class.getCanonicalName());

    @Override
    public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String id = new String(ServletUtil.getData(request), "UTF-8");
        if (eGovernmentService.logging) {
            logger.info("QR DISP=" + id);
        }
        QRSessions.Synchronizer synchronizer = QRSessions.getSynchronizer(id);
        if (synchronizer == null) {
            sendResult(response, QRSessions.QR_DIED);
        } else if (synchronizer.perform(QRSessions.COMET_WAIT)) {
            QRSessions.removeSession(id);
            sendResult(response, QRSessions.QR_SUCCESS);
        } else {
            if (eGovernmentService.logging) {
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
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IOException("Missing session");
        }
        String loginTarget = (String)session.getAttribute(LoginServlet.LOGIN_TO_APP_PARAM);
        String qrSessionId = QRSessions.createSession(session);
        session.setAttribute(QR_SESSION_ID_ATTR, qrSessionId);
        String url = "webpki.org=" + URLEncoder.encode(LoginServlet.baseUrl + "/androidbootstrap?" +
                                                       QRSessions.QR_SESSION_ID  + "=" + qrSessionId,
                                                       "UTF-8");
        logger.info("URL=" + url + " SID=" + session.getId());
        String qrImage = new Base64(false).getBase64StringFromBinary(QRCode.from(url)
                .to(ImageType.PNG).withSize(200, 200).stream().toByteArray());
        StringBuilder supportedTargets = new StringBuilder();
        boolean next = false;
        for (TargetPlatforms targetPlatform : TargetPlatforms.getSupportedMobilePlatforms()) {
            if (next) {
                supportedTargets.append('/');
            }
            next = true;
            supportedTargets.append(targetPlatform.name);
        }
        StringBuilder html = new StringBuilder(
                "<div class=\"header\" style=\"padding-bottom:15pt\">" +
                LocalizedStrings.QR_AUTHENTICATION_HEADER +
                "</div><div id=\"qr\">" +
                "<div class=\"label\" style=\"padding-bottom:15pt;text-align:left\">")
            .append(LocalizedStrings.QR_BOOTSTRAP.replace("@", supportedTargets.toString()))
            .append(
                "</div>" +
                "<div class=\"label\">" +
                LocalizedStrings.QR_START_APPLICATION
                    .replace("@",
                             "<img src=\"images/qr_launcher.png\" onclick=\"toast('" +
                               HTML.javaScript(LocalizedStrings.QR_APP_LOCATING) +
                              "', this)\" " +
                              "style=\"border-width:1px;border-style:solid;" +
                              "border-color:blue;cursor:pointer\" alt=\"image\">") +
                "</div>" +
                "<img src=\"data:image/png;base64,")
            .append(qrImage)
            .append(
                "\" style=\"cursor:none\" alt=\"image\"></div>" +
                "<div style=\"display:flex;justify-content:center;align-items:center\">" +
                  "<div class=\"label\">" +
                  LocalizedStrings.QR_SESSION_STATUS + ":&nbsp;</div>" +
                  "<div style=\"width:100px;" +
                              "border-width:1px;border-style:solid;" +
                              "border-color:grey\">" +
                    "<div id=\"life\" style=\"width:100%;height:15pt;background-color:#3fdaa8\"></div>" +
                 "</div>" +
                "</div>");
        HTML.resultPage(response, 
            "function startComet() {\n" +
            "  fetch('" + QR_INIT_SERVLET_NAME + "', {\n" +
            "     headers: {\n" +
            "       'Content-Type': 'text/plain'\n" +
            "     },\n" +
            "     method: 'POST',\n" +
            "     body: '" + qrSessionId + "'\n" +
            "  }).then(function (response) {\n" +
            "    return response.text();\n" +
            "  }).then(function (resultData) {\n" +
            "    console.log('Response', resultData);\n" +
            "    switch (resultData) {\n" +
            "      case '" + QRSessions.QR_DIED + "':\n" +
            "        document.location.href = 'home';\n" +
            "        break;\n" +
            "      case '" + QRSessions.QR_PROGRESS + "':\n" +
            "        document.getElementById('qr').outerHTML = " +
            "'<div class=\"label\" style=\"padding-bottom:15pt\">" +
            HTML.javaScript(LocalizedStrings.QR_WAITING_FOR_MOB_DEVICE) + 
            "</div>';\n" +
            "        initUi();\n" +
            "      case '" + QRSessions.QR_CONTINUE + "':\n" +
            "        startComet();\n" +
            "        break;\n" +
            "      default:\n" +
            "        document.location.href = '" + loginTarget + "';\n" +
            "    }\n" +
            "  }).catch (function(error) {\n" +
            "    console.log('Request failed', error);\n" +
            "  });\n" +                           
            "}\n" +
            "var life = 100;\n",
            true,
            "  startComet();\n" +
            "  setInterval(function(){ document.getElementById('life').style.width = life + '%'; life -= " +
            (100.0 / (QRSessions.MAX_SESSION / 1000)) + ";}, 1000);\n",
            null,
            html);
    }
}

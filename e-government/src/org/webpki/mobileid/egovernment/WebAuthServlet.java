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
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.crypto.AsymSignatureAlgorithms;
import org.webpki.crypto.CertificateFilter;
import org.webpki.crypto.HashAlgorithms;
import org.webpki.crypto.KeyStoreVerifier;
import org.webpki.crypto.VerifierInterface;
import org.webpki.json.JSONOutputFormats;
import org.webpki.webauth.AuthenticationRequestEncoder;
import org.webpki.webauth.AuthenticationResponseDecoder;
import org.webpki.webutil.ServletUtil;

// This is core WebAuth servlet

public class WebAuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(WebAuthServlet.class.getCanonicalName());

    static final String JSON_CONTENT_TYPE               = "application/json";
    static final String AUTH_REQ                        = "areq";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IOException("Session timeout");
        }
        AuthenticationRequestEncoder authReqEnc = new AuthenticationRequestEncoder(LoginServlet.authenticationUrl, null);
        authReqEnc.addSignatureAlgorithm(AsymSignatureAlgorithms.ECDSA_SHA256);
        authReqEnc.setExtendedCertPath(true);
        authReqEnc.addCertificateFilter(new CertificateFilter()
            .setPolicyRules(new String[]{"1.2.250.33"}));  // AFNOR - French ISO
        session.setAttribute(AUTH_REQ, authReqEnc);
        byte[] jsonData = authReqEnc.serializeJSONDocument(JSONOutputFormats.PRETTY_PRINT);
        if (eGovernmentService.logging) {
            logger.info("Sent message\n" + new String(jsonData, "UTF-8"));
        }
        response.setContentType(JSON_CONTENT_TYPE);
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(jsonData);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // This where we are supposed to get the authentication response
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new IOException("Missing session");
            }
            if (session.getAttribute(UserData.USER_DATA) != null) {
                throw new IOException("Session weirdness");
            }
            AuthenticationRequestEncoder authReqEnc = (AuthenticationRequestEncoder) session.getAttribute(AUTH_REQ);
            byte[] jsonData = ServletUtil.getData(request);
            if (!request.getContentType().equals(JSON_CONTENT_TYPE)) {
                throw new IOException("Wrong \"Content-Type\": " + request.getContentType());
            }
            if (eGovernmentService.logging) {
                logger.info("Received message:\n" + new String(jsonData, "UTF-8"));
            }
            AuthenticationResponseDecoder authReqDec = (AuthenticationResponseDecoder)
                    eGovernmentService.webAuth2JSONCache.parse(jsonData);
                authReqEnc.checkRequestResponseIntegrity(authReqDec, 
                                                         HashAlgorithms.SHA256.digest(
                                                                 eGovernmentService.tlsCertificate.getEncoded()));
            VerifierInterface verifier = new KeyStoreVerifier(eGovernmentService.trustedIssuers);
            verifier.setTrustedRequired(true);
            authReqDec.verifySignature(verifier);
            session.setAttribute(UserData.USER_DATA,
                                 new UserData(session, verifier.getSignerCertificatePath()[0]));
            String qrSessionId = (String) session.getAttribute(QRInitServlet.QR_SESSION_ID_ATTR);
            logger.info("Auth OK=" + qrSessionId);
            if (qrSessionId == null) {
                response.sendRedirect((String) session.getAttribute(LoginServlet.LOGIN_TARGET));
            } else {
                QRSessions.optionalSessionSetReady(qrSessionId);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }
}

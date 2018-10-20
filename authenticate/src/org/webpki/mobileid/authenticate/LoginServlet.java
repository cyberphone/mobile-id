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

package org.webpki.mobileid.authenticate;

import java.io.IOException;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.GeneralSecurityException;

import java.security.cert.X509Certificate;

import java.net.URLEncoder;

import org.webpki.net.HTTPSWrapper;

import org.webpki.webutil.ServletUtil;

import org.webpki.crypto.KeyStoreVerifier;
import org.webpki.crypto.HashAlgorithms;

import org.webpki.webauth.AuthenticationResponseDecoder;
import org.webpki.webauth.AuthenticationRequestEncoder;


import org.webpki.json.JSONDecoder;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(LoginServlet.class);

    static byte[] tls_certificate_fingerprint;

    private byte[] getTLSCertificateFingerPrint() throws IOException {
        if (tls_certificate_fingerprint == null
                && LoginService.application_url.startsWith("https://")) {
            HTTPSWrapper https_wrapper = new HTTPSWrapper();
            https_wrapper.allowInvalidCert(true);
            https_wrapper.makeGetRequest(LoginService.internal_url);
            https_wrapper.getData();
            try {
                tls_certificate_fingerprint = HashAlgorithms.SHA256
                        .digest(https_wrapper.getServerCertificate()
                                .getEncoded());
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
        return tls_certificate_fingerprint;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        byte[] received_data = ServletUtil.getData(request);
        if (LoginService.debug) {
            log.info("Received authentication response:\n"
                    + new String(received_data, "UTF-8"));
        }
        JSONDecoder auth_resp = LoginService.json_decoder_cache
                .parse(received_data);
        if (!(auth_resp instanceof AuthenticationResponseDecoder)) {
            throw new IOException("Unexpected response type: " + auth_resp);
        }
        String id = ((AuthenticationResponseDecoder) auth_resp).getID();
        AuthenticationRequestEncoder auth_req = AuthenticationDB
                .getAuthenticationRequest(id);
        if (auth_req == null) {
            response.sendRedirect(LoginService.application_url
                    + AbortedServlet.ABORT_SERVLET + "Session%20not%20found!");
            return;
        }
        try {
            auth_req.checkRequestResponseIntegrity(
                    (AuthenticationResponseDecoder) auth_resp,
                    getTLSCertificateFingerPrint());
            KeyStoreVerifier verifier = new KeyStoreVerifier(
                    LoginService.trust_store);
            // To create an error, use the following line:
            // KeyStoreVerifier verifier = new KeyStoreVerifier
            // (org.webpki.crypto.test.DemoKeyStore.getCAKeyStore ());
            verifier.setTrustedRequired(true);
            ((AuthenticationResponseDecoder) auth_resp)
                    .verifySignature(verifier);
            X509Certificate certificate = verifier.getSignerCertificatePath()[0];
            log.info("Login ID=" + id + ", Subject DN='"
                    + certificate.getSubjectX500Principal().getName()
                    + "' succeeded");
            AuthenticationDB.setUserCertificate(id, certificate);
            response.sendRedirect(LoginService.application_url + "/account?"
                    + AuthenticationDB.ID + "=" + id);
        } catch (IOException e) {
            AuthenticationDB.removeAuthenticationObject(id);
            response.sendRedirect(LoginService.application_url
                    + AbortedServlet.ABORT_SERVLET
                    + URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        PluginTriggerServlet.pluginTrigger(response,
                AuthenticationDB.createAuthenticationRequest(false));
    }
}

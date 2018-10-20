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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.security.GeneralSecurityException;

import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import org.webpki.webauth.AuthenticationRequestEncoder;

import org.webpki.crypto.CertificateFilter;
import org.webpki.crypto.HashAlgorithms;
import org.webpki.crypto.AsymSignatureAlgorithms;

import org.webpki.util.Base64URL;


public class AuthenticationDB {
    static class AuthenticationInProgress {
        long expiry_time;
        String id;
        AuthenticationRequestEncoder authentication_request;
        X509Certificate certificate;
        boolean qr_flag;
        Synchronizer synchronizer; // For QR
    }

    static final long CYCLE_TIME = 60000L;

    static final String ID = "ID";

    private static final Logger log = Logger.getLogger(AuthenticationDB.class);

    private static LinkedHashMap<String, AuthenticationInProgress> authentications_in_progress = new LinkedHashMap<String, AuthenticationInProgress>();

    private static Looper looper;

    private static class Looper extends Thread {
        public void run() {
            while (true) {
                try {
                    sleep(CYCLE_TIME);
                    synchronized (AuthenticationDB.class) {
                        if (authentications_in_progress.isEmpty()) {
                            log.debug("Timeout thread died");
                            looper = null;
                            break;
                        }
                        long current_time = System.currentTimeMillis();
                        Iterator<AuthenticationInProgress> list = authentications_in_progress
                                .values().iterator();
                        while (list.hasNext()) {
                            AuthenticationInProgress auth_in_progress = list
                                    .next();
                            if (current_time > auth_in_progress.expiry_time) {
                                log.info("Removed due to timeout, Authentication ID="
                                        + auth_in_progress.id);
                                if (auth_in_progress.qr_flag) {
                                    auth_in_progress.synchronizer
                                            .haveData4You();
                                }
                                list.remove();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    static synchronized String createAuthenticationRequest(boolean qr_flag)
            throws IOException {
        AuthenticationInProgress auth_in_progress = new AuthenticationInProgress();
        AuthenticationRequestEncoder auth_req = new AuthenticationRequestEncoder(
                LoginService.application_url + "/login",
                LoginService.application_url + AbortedServlet.ABORT_SERVLET
                        + "The%20user%20terminated%20the%20process");
        // null);
        auth_in_progress.id = "auth." + Long.toHexString(new Date().getTime())
                + Base64URL.generateURLFriendlyRandom(20);
        auth_in_progress.expiry_time = System.currentTimeMillis() + 300000L;
        auth_req.setID(auth_in_progress.id);
        if (LoginService.extended_cert_path) {
            auth_req.setExtendedCertPath(true);
        }
        for (AsymSignatureAlgorithms sig_alg : LoginService.signature_algorithms) {
            auth_req.addSignatureAlgorithm(sig_alg);
        }
        auth_in_progress.authentication_request = auth_req;
        auth_in_progress.qr_flag = qr_flag;
        if (qr_flag) {
            auth_in_progress.synchronizer = new Synchronizer();
        }
        authentications_in_progress.put(auth_in_progress.id, auth_in_progress);
        log.info("Created Authentication ID=" + auth_in_progress.id);
        if (looper == null) {
            log.debug("Timeout thread started");
            (looper = new Looper()).start();
        }
        if (LoginService.trust_store != null) {
            for (X509Certificate cert : LoginService.getTrustedCACerts()) {
                try {
                    auth_req.addCertificateFilter(new CertificateFilter()
                            .setFingerPrint(
                                    HashAlgorithms.SHA256.digest(cert
                                            .getEncoded()))
                            .setExtendedKeyUsageRules(
                                    new String[] { "1.3.6.1.5.5.7.3.2",
                                            "1.3.6.1.5.5.7.3.4" })
                    // .setExtendedKeyUsageRules (new
                    // String[]{"1.3.6.1.5.5.7.3.2"})
                    // .setKeyUsageRules
                    // ("digitalsignature, nonRepudiation,-keyAgreement")
                    // .setKeyUsageRules (new
                    // KeyUsageBits[]{KeyUsageBits.DIGITAL_SIGNATURE,
                    // KeyUsageBits.NON_REPUDIATION},
                    // new KeyUsageBits[]{KeyUsageBits.KEY_AGREEMENT})
                    // .setIssuer (cert.getIssuerX500Principal())
                    );
                } catch (GeneralSecurityException e) {
                    throw new IOException(e);
                }
            }
            // Testing
            // auth_req.setTargetKeyContainerList (new
            // KeyContainerTypes[]{KeyContainerTypes.SOFTWARE});
            // auth_req.setPreferredLanguages (new String[]{"en","de","fr"});
        }
        return auth_in_progress.id;
    }

    static synchronized AuthenticationRequestEncoder getAuthenticationRequest(
            String id) {
        AuthenticationInProgress auth = authentications_in_progress.get(id);
        return auth == null ? null : auth.authentication_request;
    }

    static void setUserCertificate(String id, X509Certificate certificate) {
        authentications_in_progress.get(id).certificate = certificate;
    }

    static X509Certificate getUserCertificateAndRemoveAuthentication(String id) {
        AuthenticationInProgress auth = authentications_in_progress.get(id);
        if (auth == null) {
            return null;
        }
        removeAuthenticationObject(id);
        return auth.certificate;
    }

    static synchronized void removeAuthenticationObject(String id) {
        authentications_in_progress.remove(id);
    }

    static boolean isQRLogin(String id) {
        AuthenticationInProgress auth = authentications_in_progress.get(id);
        if (auth == null) {
            return false;
        }
        return auth.qr_flag;
    }

    static Synchronizer getSynchronizer(String id) {
        AuthenticationInProgress auth = authentications_in_progress.get(id);
        if (auth == null) {
            return null;
        }
        return auth.synchronizer;
    }
}

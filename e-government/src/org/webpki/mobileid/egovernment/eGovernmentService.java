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
import java.io.InputStream;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import java.security.cert.X509Certificate;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.CustomCryptoProvider;
import org.webpki.crypto.KeyStoreVerifier;

import org.webpki.json.JSONArrayReader;
import org.webpki.json.JSONDecoderCache;
import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONParser;
import org.webpki.json.JSONX509Verifier;

import org.webpki.keygen2.CredentialDiscoveryResponseDecoder;
import org.webpki.keygen2.InvocationResponseDecoder;
import org.webpki.keygen2.KeyCreationResponseDecoder;
import org.webpki.keygen2.ProvisioningFinalizationResponseDecoder;
import org.webpki.keygen2.ProvisioningInitializationResponseDecoder;

import org.webpki.util.ArrayUtil;

import org.webpki.webutil.InitPropertyReader;

public class eGovernmentService extends InitPropertyReader implements ServletContextListener {

    static Logger logger = Logger.getLogger(eGovernmentService.class.getCanonicalName());
    
    static final String VERSION_CHECK         = "android_webpki_versions";

    static final String KEYSTORE_PASSWORD     = "key_password";

    static final String BOUNCYCASTLE_FIRST    = "bouncycastle_first";

    static final String ISSUER_JSON           = "issuer";

    static final String DEMO                  = "demo";  // No user auth

    static final String LOGGING               = "logging";

    static JSONDecoderCache keygen2JSONCache;
    
    static String[] grantedVersions;
    
    static JSONX509Verifier trustedIssuers;
    
    static boolean demoMode;
    
    static boolean logging;

    static X509Certificate demoCertificate;

    void addIssuer(KeyStore keyStore, JSONObjectReader issuerObject) throws IOException, GeneralSecurityException {
        String issuerBase = issuerObject.getString(ISSUER_JSON);
        keyStore.setCertificateEntry (issuerBase,
                CertificateUtil.getCertificateFromBlob(getResourceBytes(issuerBase + "-root-ca.cer")));        
    }

    InputStream getResource(String name) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(name);
        if (is == null) {
            throw new IOException("Resource fail for: " + name);
        }
        return is;
    }

    byte[] getResourceBytes(String name) throws IOException {
        return ArrayUtil.getByteArrayFromInputStream(getResource(name));
    }

    String getResourceString(String name) throws IOException {
        return new String(getResourceBytes(name), "UTF-8");
    }
    
    JSONObjectReader readJSONFile(String name) throws IOException {
        return JSONParser.parse(getResourceBytes(name));        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initProperties (sce);
        try {
            CustomCryptoProvider.forcedLoad(getPropertyBoolean(BOUNCYCASTLE_FIRST));

            ////////////////////////////////////////////////////////////////////////////////////////////
            // KeyGen2
            ////////////////////////////////////////////////////////////////////////////////////////////
            keygen2JSONCache = new JSONDecoderCache();
            keygen2JSONCache.addToCache(InvocationResponseDecoder.class);
            keygen2JSONCache.addToCache(ProvisioningInitializationResponseDecoder.class);
            keygen2JSONCache.addToCache(CredentialDiscoveryResponseDecoder.class);
            keygen2JSONCache.addToCache(KeyCreationResponseDecoder.class);
            keygen2JSONCache.addToCache(ProvisioningFinalizationResponseDecoder.class);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Trusted issuers
            ////////////////////////////////////////////////////////////////////////////////////////////
            JSONArrayReader issuerArray = readJSONFile("issuers.json").getJSONArrayReader();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            do {
                addIssuer(keyStore, issuerArray.getObject());
            } while (issuerArray.hasMore());
            trustedIssuers = new JSONX509Verifier(new KeyStoreVerifier(keyStore));

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Android WebPKI version check
            ////////////////////////////////////////////////////////////////////////////////////////////
            grantedVersions = getPropertyStringList(VERSION_CHECK);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Are we in demo mode?
            ////////////////////////////////////////////////////////////////////////////////////////////
            demoMode = getPropertyBoolean(DEMO);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Are we logging?
            ////////////////////////////////////////////////////////////////////////////////////////////
            logging = getPropertyBoolean(LOGGING);

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Demo certificate in the waiting for the full solution
            ////////////////////////////////////////////////////////////////////////////////////////////
            demoCertificate = CertificateUtil.getCertificateFromBlob(getResourceBytes("lukeskywalker.cer"));

            logger.info("Mobile ID eGovernment server initiated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
        }
    }
}

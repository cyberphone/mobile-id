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
import java.io.InputStream;

import java.security.cert.X509Certificate;

import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.CustomCryptoProvider;

import org.webpki.json.JSONArrayReader;
import org.webpki.json.JSONDecoderCache;
import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONParser;

import org.webpki.keygen2.CredentialDiscoveryResponseDecoder;
import org.webpki.keygen2.InvocationResponseDecoder;
import org.webpki.keygen2.KeyCreationResponseDecoder;
import org.webpki.keygen2.ProvisioningFinalizationResponseDecoder;
import org.webpki.keygen2.ProvisioningInitializationResponseDecoder;

import org.webpki.util.ArrayUtil;

import org.webpki.webutil.InitPropertyReader;

public class KeyProviderService extends InitPropertyReader implements ServletContextListener {

    static Logger logger = Logger.getLogger(KeyProviderService.class.getCanonicalName());
    
    static final String VERSION_CHECK         = "android_webpki_versions";

    static final String KEYSTORE_PASSWORD     = "key_password";

    static final String TLS_CERTIFICATE       = "server_tls_certificate";

    static final String BOUNCYCASTLE_FIRST    = "bouncycastle_first";

    static final String LOGGING               = "logging";

    static JSONDecoderCache keygen2JSONCache;
    
    static X509Certificate tlsCertificate;

    static String[] grantedVersions;

    static boolean logging;

    static Vector<IssuerHolder> issuers = new Vector<IssuerHolder>();
    
    class IssuerHolder {
        
        private final static String ISSUER_JSON = "issuer";
        
        KeyStoreEnumerator keyManagementKey;
        KeyStoreEnumerator subCA;
        String cardImage;

        public IssuerHolder(JSONObjectReader issuerObject) throws IOException {
            String issuerBase = issuerObject.getString(ISSUER_JSON);
            keyManagementKey = new KeyStoreEnumerator(getResource(issuerBase + "-kmk.p12"),
                                                        getPropertyString(KEYSTORE_PASSWORD));
            subCA = new KeyStoreEnumerator(getResource(issuerBase + "-sub-ca.p12"),
                                           getPropertyString(KEYSTORE_PASSWORD));
            cardImage = getResourceString(issuerBase + "-card.svg");
            issuerObject.checkForUnread();
        }
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
            // Issuers
            ////////////////////////////////////////////////////////////////////////////////////////////
            JSONArrayReader issuerArray = readJSONFile("issuers.json").getJSONArrayReader();
            do {
                issuers.add(new IssuerHolder(issuerArray.getObject()));
            } while (issuerArray.hasMore());

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Android WebPKI version check
            ////////////////////////////////////////////////////////////////////////////////////////////
            grantedVersions = getPropertyStringList(VERSION_CHECK);
 
            ////////////////////////////////////////////////////////////////////////////////////////////
            // Get TLS server certificate
            ////////////////////////////////////////////////////////////////////////////////////////////
            tlsCertificate = CertificateUtil.getCertificateFromBlob(getResourceBytes(getPropertyString(TLS_CERTIFICATE)));

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Are we logging?
            ////////////////////////////////////////////////////////////////////////////////////////////
            logging = getPropertyBoolean(LOGGING);

            logger.info("Mobile ID KeyProvider Server initiated: " + tlsCertificate.getSubjectX500Principal().getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
        }
    }
}

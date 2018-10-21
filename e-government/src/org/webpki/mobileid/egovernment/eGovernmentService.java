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

    static final String UIDEMO                = "uidemo";  // No user auth

    static final String LOGGING               = "logging";

    static JSONDecoderCache keygen2JSONCache;
    
    static String[] grantedVersions;
    
    static JSONX509Verifier trustedIssuers;
    
    static boolean logging;

    static X509Certificate demoCertificate; // UI demo mode flag as well
    
    static String demoCard;

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

/*    
    StringBuilder createCoreCard(String svgHeader) {
    	StringBuilder 
    }
*/

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
            if (getPropertyBoolean(UIDEMO)) {
                demoCertificate = CertificateUtil.getCertificateFromBlob(getResourceBytes("democert.cer"));
                UserData userData = new UserData(demoCertificate);
                String card = getResourceString("democard.svg");
                	// Removing the SVG header excluding the >
                card = card.substring(card.indexOf('>'))
                	// Remove the local title object
                    .replaceFirst("<title>.*<\\/title>\\s+", "")
                    // Insert user name
                	.replace("@n", userData.getUserCommonName())
                	// And the associated identity string
                    .replace("@i", userData.getUserIdHTMLString());

                StringBuilder svg = new StringBuilder(
                    "<svg style=\"height:90pt;padding:0 10pt 0 8pt;margin-right:auto;margin-left:auto;display:block\" viewBox=\"0 0 310 190\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                    "<defs>\n" +
                    " <clipPath id=\"cardClip\">\n" +
                    "  <rect rx=\"15\" ry=\"15\" height=\"180\" width=\"300\" y=\"0\" x=\"0\"/>\n" +
                    " </clipPath>\n" +
                    " <filter id=\"dropShaddow\">\n" +
                    "  <feGaussianBlur stdDeviation=\"2.4\"/>\n" +
                    " </filter>\n" +
                    " <linearGradient y1=\"0\" x1=\"0\" y2=\"1\" x2=\"1\" id=\"innerCardBorder\">\n" +
                    "  <stop offset=\"0\" stop-opacity=\"0.6\" stop-color=\"white\"/>\n" +
                    "  <stop offset=\"0.48\" stop-opacity=\"0.6\" stop-color=\"white\"/>\n" +
                    "  <stop offset=\"0.52\" stop-opacity=\"0.6\" stop-color=\"#b0b0b0\"/>\n" +
                    "  <stop offset=\"1\" stop-opacity=\"0.6\" stop-color=\"#b0b0b0\"/>\n" +
                    " </linearGradient>\n" +
                    " <linearGradient y1=\"0\" x1=\"0\" y2=\"1\" x2=\"1\" id=\"outerCardBorder\">\n" +
                    "  <stop offset=\"0\" stop-color=\"#b0b0b0\"/>\n" +
                    "  <stop offset=\"0.48\" stop-color=\"#b0b0b0\"/>\n" +
                    "  <stop offset=\"0.52\" stop-color=\"#808080\"/>\n" +
                    "  <stop offset=\"1\" stop-color=\"#808080\"/>\n" +
                    " </linearGradient>\n" +
                    "</defs>\n" +
                    "<rect filter=\"url(#dropShaddow)\" rx=\"16\" ry=\"16\" " +
                      "height=\"182\" width=\"302\" y=\"4\" x=\"4\" fill=\"#c0c0c0\"/>\n" +
                    "<svg x=\"1\" y=\"1\" clip-path=\"url(#cardClip)\"");
                svg.append(card)
                   .append(
                    "<rect x=\"2\" y=\"2\" " +
                    "width=\"298\" height=\"178\" " +
                    "rx=\"14.7\" ry=\"14.7\" " +
                    "fill=\"none\" " +
                    "stroke=\"url(#innerCardBorder)\" stroke-width=\"2.7\"/>\n" +
                    "<rect x=\"0.5\" y=\"0.5\" " +
                    "width=\"301\" height=\"181\" " +
                    "rx=\"16\" ry=\"16\" fill=\"none\" stroke=\"url(#outerCardBorder)\"/>\n" +
                    "</svg>\n");
                demoCard = svg.toString();
                
                svg.delete(0, demoCard.indexOf('>'))
                   .insert(0,
                	       "<svg width=\"410\" height=\"290\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                           "<title>Mobile ID - Virtual Card Credential</title>\n" +
                           "<svg x=\"50\" y=\"50\"")
                   .append("</svg>\n");
                logger.info(svg.toString());
            }

            ////////////////////////////////////////////////////////////////////////////////////////////
            // Are we logging?
            ////////////////////////////////////////////////////////////////////////////////////////////
            logging = getPropertyBoolean(LOGGING);

            logger.info("Mobile ID eGovernment server initiated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
        }
    }
}

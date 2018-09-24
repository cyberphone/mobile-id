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

import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import java.security.cert.X509Certificate;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpki.crypto.AlgorithmPreferences;
import org.webpki.crypto.KeyStoreReader;
import org.webpki.crypto.AsymSignatureAlgorithms;
import org.webpki.crypto.CustomCryptoProvider;

import org.webpki.webauth.AuthenticationResponseDecoder;
import org.webpki.webutil.InitPropertyReader;

import org.webpki.json.JSONDecoderCache;

public class LoginService extends InitPropertyReader implements	ServletContextListener {

	private static Logger log = Logger.getLogger(LoginService.class.getName());

	static final String BASE_URL             = "base-url"; // Optional
	static final String TRUST_STORE          = "truststore"; // Optional
	static final String TRUST_PASS           = "trustpass"; // Optional
	static final String DEBUG                = "debug"; // Optional
	static final String SIGNATURE_ALGORITHMS = "signature-algorithms"; // Optional
	static final String EXTENDED_CERT_PATH   = "extended-cert-path"; // Optional
	static final String COMET_DELAY          = "comet-delay"; // Optional
	static final String VERSION_CHECK        = "version-check"; // Optional

	static String application_url;

	static String internal_url;

	static KeyStore trust_store;

	static int comet_delay;

	static boolean debug;

	static boolean extended_cert_path;

	static String[] version_check;

	static Vector<AsymSignatureAlgorithms> signature_algorithms = new Vector<AsymSignatureAlgorithms>();

	static String administrator;

	static JSONDecoderCache json_decoder_cache;

	static String port_map;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// //////////////////////////////////////////////////////////////////////////////////////////
		// One-time system initialization
		// //////////////////////////////////////////////////////////////////////////////////////////
		try {
			// //////////////////////////////////////////////////////////////////////////////////////////
			// We need BC
			// //////////////////////////////////////////////////////////////////////////////////////////
			CustomCryptoProvider.forcedLoad(false);

			// //////////////////////////////////////////////////////////////////////////////////////////
			// Property fetching
			// //////////////////////////////////////////////////////////////////////////////////////////
			initProperties(event);

			// //////////////////////////////////////////////////////////////////////////////////////////
			// Initialization of core variables
			// //////////////////////////////////////////////////////////////////////////////////////////
			application_url = getApplicationURL(getPropertyStringConditional(BASE_URL));

			if (hasProperty(PORT_MAP)) {
				URL url = new URL(application_url);
				internal_url = new URL(url.getProtocol(), url.getHost(),
						getPropertyInt(PORT_MAP), url.getFile())
						.toExternalForm();
			} else {
				internal_url = application_url;
			}

			port_map = getPropertyStringConditional(PORT_MAP);

			comet_delay = hasProperty(COMET_DELAY) ? getPropertyInt(COMET_DELAY)
					: 30000;

			if (hasProperty(TRUST_STORE)) {
				trust_store = KeyStoreReader.loadKeyStore(
						getPropertyString(TRUST_STORE),
						getPropertyString(TRUST_PASS));
				for (X509Certificate cert : getTrustedCACerts()) {
					log.info("Trusted CA: "
							+ cert.getSubjectX500Principal().getName());
				}
			}

			if (hasProperty(DEBUG)) {
				debug = getPropertyBoolean(DEBUG);
			}
			// //////////////////////////////////////////////////////////////////////////////////////////
			// Registering the sole JSON decoder
			// //////////////////////////////////////////////////////////////////////////////////////////
			json_decoder_cache = new JSONDecoderCache();
			json_decoder_cache.addToCache(AuthenticationResponseDecoder.class);

			// //////////////////////////////////////////////////////////////////////////////////////////
			// User database
			// //////////////////////////////////////////////////////////////////////////////////////////
			emfactory = Persistence.createEntityManagerFactory("webpki-login");

			// //////////////////////////////////////////////////////////////////////////////////////////
			// Checking compatibility
			// //////////////////////////////////////////////////////////////////////////////////////////
			if (hasProperty(VERSION_CHECK)) {
				version_check = getPropertyStringList(VERSION_CHECK);
				log.info("Require versions: "
						+ getPropertyString(VERSION_CHECK));
			}

			// //////////////////////////////////////////////////////////////////////////////////////////
			// Do we want the EE-cert only or the rest of the path from the
			// client?
			// //////////////////////////////////////////////////////////////////////////////////////////
			if (hasProperty(EXTENDED_CERT_PATH)) {
				extended_cert_path = getPropertyBoolean(EXTENDED_CERT_PATH);
			}

			// //////////////////////////////////////////////////////////////////////////////////////////
			// Requested signature algorithms
			// //////////////////////////////////////////////////////////////////////////////////////////
			if (hasProperty(SIGNATURE_ALGORITHMS)) {
				for (String sig_alg : getPropertyStringList(SIGNATURE_ALGORITHMS)) {
					signature_algorithms.add(AsymSignatureAlgorithms
							.getAlgorithmFromId(sig_alg,
									AlgorithmPreferences.JOSE_ACCEPT_PREFER));
				}
			} else {
				signature_algorithms.add(AsymSignatureAlgorithms.ECDSA_SHA256);
				signature_algorithms.add(AsymSignatureAlgorithms.RSA_SHA256);

			}
			for (AsymSignatureAlgorithms sig_alg : signature_algorithms) {
				log.info("Signature algorithm: "
						+ sig_alg
								.getAlgorithmId(AlgorithmPreferences.JOSE_ACCEPT_PREFER));
			}

			// //////////////////////////////////////////////////////////////////////////////////////////
			// Optional administrator
			// //////////////////////////////////////////////////////////////////////////////////////////
			administrator = getPropertyStringConditional(ADMINISTRATOR);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Couldn't initialize", e);
			return;
		}
		log.info("WebPKI.org Demo Login Service Started");
	}

	static List<X509Certificate> getTrustedCACerts() throws IOException {
		Vector<X509Certificate> certs = new Vector<X509Certificate>();
		try {
			Enumeration<String> aliases = trust_store.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				if (trust_store.isCertificateEntry(alias)) {
					certs.add((X509Certificate) trust_store
							.getCertificate(alias));
				}
			}
			return certs;
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
}

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
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.math.BigInteger;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.asn1.cert.DistinguishedName;
import org.webpki.ca.CA;
import org.webpki.ca.CertSpec;
import org.webpki.crypto.AsymKeySignerInterface;
import org.webpki.crypto.AsymSignatureAlgorithms;
import org.webpki.crypto.KeyAlgorithms;
import org.webpki.crypto.SignatureWrapper;
import org.webpki.keygen2.ServerState;
import org.webpki.keygen2.KeySpecifier;
import org.webpki.keygen2.KeyGen2URIs;
import org.webpki.keygen2.InvocationResponseDecoder;
import org.webpki.keygen2.ProvisioningInitializationResponseDecoder;
import org.webpki.keygen2.CredentialDiscoveryResponseDecoder;
import org.webpki.keygen2.KeyCreationResponseDecoder;
import org.webpki.keygen2.ProvisioningFinalizationResponseDecoder;
import org.webpki.keygen2.InvocationRequestEncoder;
import org.webpki.keygen2.ProvisioningInitializationRequestEncoder;
import org.webpki.keygen2.CredentialDiscoveryRequestEncoder;
import org.webpki.keygen2.KeyCreationRequestEncoder;
import org.webpki.keygen2.ProvisioningFinalizationRequestEncoder;
import org.webpki.localized.LocalizedStrings;
import org.webpki.sks.AppUsage;
import org.webpki.sks.PassphraseFormat;
import org.webpki.util.MIMETypedObject;
import org.webpki.webutil.ServletUtil;
import org.webpki.json.JSONEncoder;
import org.webpki.json.JSONDecoder;
import org.webpki.json.JSONOutputFormats;

// A KeyGen2 protocol runner that creates MobileID keys.

public class KeyProviderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    static Logger log = Logger.getLogger(KeyProviderServlet.class.getCanonicalName());
    
    static final String JSON_CONTENT_TYPE = "application/json";

    static String success_image_and_message;
    
    void returnKeyGen2Error(HttpServletResponse response, String errorMessage) throws IOException, ServletException {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // Server errors are returned as HTTP redirects taking the client out of its KeyGen2 mode
        ////////////////////////////////////////////////////////////////////////////////////////////
        response.sendRedirect(KeyProviderInitServlet.keygen2EnrollmentUrl + 
                              "?" +
                              KeyProviderInitServlet.ERROR_TAG +
                              "=" +
                              URLEncoder.encode(errorMessage, "UTF-8"));
    }
    
    void keygen2JSONBody(HttpServletResponse response, JSONEncoder object) throws IOException {
        byte[] jsonData = object.serializeJSONDocument(JSONOutputFormats.PRETTY_PRINT);
        if (KeyProviderService.logging) {
            log.info("Sent message\n" + new String(jsonData, "UTF-8"));
        }
        response.setContentType(JSON_CONTENT_TYPE);
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(jsonData);
    }

    void requestKeyGen2KeyCreation(HttpServletResponse response, ServerState keygen2State)
            throws IOException {
        ServerState.PINPolicy standardPinPolicy = 
            keygen2State.createPINPolicy(PassphraseFormat.NUMERIC,
                                         4,
                                         8,
                                         3,
                                         null);
        keygen2State.createKey(AppUsage.SIGNATURE,
                               new KeySpecifier(KeyAlgorithms.NIST_P_256),
                               standardPinPolicy);
        keygen2JSONBody(response, 
                        new KeyCreationRequestEncoder(keygen2State,
                                                      KeyProviderInitServlet.keygen2EnrollmentUrl));
      }

    String certificateData(X509Certificate certificate) {
        return ", Subject='" + certificate.getSubjectX500Principal().getName() +
               "', Serial=" + certificate.getSerialNumber();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException {
        executeRequest(request, response, null, false);
    }

    void executeRequest(HttpServletRequest request,
                        HttpServletResponse response,
                        String versionMacro,
                        boolean init)
         throws IOException, ServletException {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                log.info("Header=" + headerName + " value=" + headers.nextElement());
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            log.info("Cookie: Name=" + cookie.getName() +
                    " Value=" + cookie.getValue() +
                    " Path=" + cookie.getPath() +
                    " isecure= " + cookie.getSecure() +
                    " http=" + cookie.isHttpOnly() +
                    " maxage=" + cookie.getMaxAge());
        }
        String keygen2EnrollmentUrl = KeyProviderInitServlet.keygen2EnrollmentUrl;
        HttpSession session = request.getSession(false);
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////
            // Check that the request is properly authenticated
            ////////////////////////////////////////////////////////////////////////////////////////////
            if (session == null) {
                returnKeyGen2Error(response, "Session timed out");
                return;
             }
            ServerState keygen2State =
                (ServerState) session.getAttribute(KeyProviderInitServlet.KEYGEN2_SESSION_ATTR);
            if (keygen2State == null) {
                throw new IOException("Server state missing");
            }
            KeyProviderService.IssuerHolder issuer = 
                    (KeyProviderService.IssuerHolder) keygen2State.getServiceSpecificObject(KeyProviderInitServlet.SERVER_STATE_ISSUER);
            ////////////////////////////////////////////////////////////////////////////////////////////
            // Check if it is the first (trigger) message from the client
            ////////////////////////////////////////////////////////////////////////////////////////////
            if (init) {
                boolean found = false;;
                for (String version : KeyProviderService.grantedVersions) {
                    if (version.equals(versionMacro)) {
                        found = true;
                        break;
                      }
                }
                if (!found) {
                    returnKeyGen2Error(response, "Wrong version of the WebPKI app, you need to update");
                    return;
                }
                InvocationRequestEncoder invocationRequest =
                    new InvocationRequestEncoder(keygen2State,
                                                 keygen2EnrollmentUrl,
                                                 null);
                invocationRequest.setAbortUrl(keygen2EnrollmentUrl +
                                                  "?" +
                                                  KeyProviderInitServlet.ABORT_TAG +
                                                  "=true");
                keygen2State.addImageAttributesQuery(KeyGen2URIs.LOGOTYPES.LIST);
                keygen2JSONBody(response, invocationRequest);
                return;
              }

            ////////////////////////////////////////////////////////////////////////////////////////////
            // It should be a genuine KeyGen2 response.  Note that the order is verified!
            ////////////////////////////////////////////////////////////////////////////////////////////
            byte[] jsonData = ServletUtil.getData(request);
            if (!request.getContentType().equals(JSON_CONTENT_TYPE)) {
                throw new IOException("Wrong \"Content-Type\": " + request.getContentType());
            }
            if (KeyProviderService.logging) {
                log.info("Received message:\n" + new String(jsonData, "UTF-8"));
            }
            JSONDecoder jsonObject = KeyProviderService.keygen2JSONCache.parse(jsonData);
            switch (keygen2State.getProtocolPhase()) {
                case INVOCATION:
                  InvocationResponseDecoder invocationResponse = (InvocationResponseDecoder) jsonObject;
                  keygen2State.update(invocationResponse);

                  // Now we really start doing something
                  ProvisioningInitializationRequestEncoder provisioningInitRequest =
                      new ProvisioningInitializationRequestEncoder(keygen2State,
                                                                   keygen2EnrollmentUrl,
                                                                   1000,
                                                                   (short)50);
                  provisioningInitRequest.setKeyManagementKey(issuer.keyManagementKey.getPublicKey());
                  keygen2JSONBody(response, provisioningInitRequest);
                  return;

                case PROVISIONING_INITIALIZATION:
                  ProvisioningInitializationResponseDecoder provisioningInitResponse = (ProvisioningInitializationResponseDecoder) jsonObject;
                  keygen2State.update(provisioningInitResponse, KeyProviderService.tlsCertificate);

                  log.info("Device Certificate=" + certificateData(keygen2State.getDeviceCertificate()));
                  CredentialDiscoveryRequestEncoder credentialDiscoveryRequest =
                      new CredentialDiscoveryRequestEncoder(keygen2State, keygen2EnrollmentUrl);
                  credentialDiscoveryRequest.addLookupDescriptor(issuer.keyManagementKey.getPublicKey());
                  keygen2JSONBody(response, credentialDiscoveryRequest);
                  return;

                case CREDENTIAL_DISCOVERY:
                  CredentialDiscoveryResponseDecoder credentiaDiscoveryResponse = (CredentialDiscoveryResponseDecoder) jsonObject;
                  keygen2State.update(credentiaDiscoveryResponse);
                  for (CredentialDiscoveryResponseDecoder.LookupResult lookupResult : credentiaDiscoveryResponse.getLookupResults()) {
                      for (CredentialDiscoveryResponseDecoder.MatchingCredential matchingCredential : lookupResult.getMatchingCredentials()) {
                          X509Certificate endEntityCertificate = matchingCredential.getCertificatePath()[0];
                          keygen2State.addPostDeleteKey(matchingCredential.getClientSessionId(), 
                                                        matchingCredential.getServerSessionId(),
                                                        endEntityCertificate,
                                                        issuer.keyManagementKey.getPublicKey());
                          log.info("Deleting key=" + certificateData(endEntityCertificate));
                      }
                  }
                  requestKeyGen2KeyCreation(response, keygen2State);
                  return;

                case KEY_CREATION:
                  KeyCreationResponseDecoder keyCreationResponse = (KeyCreationResponseDecoder) jsonObject;
                  keygen2State.update(keyCreationResponse);
                  ServerState.Key key = keygen2State.getKeys()[0];

                  // Certification of key starts here
                  KeyProviderInitServlet.UserData userData = 
                          (KeyProviderInitServlet.UserData) keygen2State.getServiceSpecificObject(KeyProviderInitServlet.SERVER_STATE_USER);
                  CertSpec certSpec = new CertSpec();
                  certSpec.setSubject("cn=" + userData.userName + ", serialNumber=" + userData.userId);
                  CA ca = new CA();
                  X509Certificate[] caCertPath = issuer.subCA.getCertificatePath();
                  DistinguishedName issuerName = DistinguishedName.subjectDN(caCertPath[0]);
                  BigInteger serialNumber = new BigInteger(String.valueOf(new Date().getTime()));
                  GregorianCalendar validity = new GregorianCalendar();
                  validity.add(GregorianCalendar.YEAR, 2);
                  X509Certificate[] certPath = new X509Certificate[caCertPath.length + 1];
                  int q = 0;
                  for (X509Certificate cert : caCertPath) {
                      certPath[++q] = cert;
                  }
                  certPath[0] = ca.createCert(certSpec,
                                              issuerName,
                                              serialNumber,
                                              new Date(System.currentTimeMillis() - 600000), // Ten minutes backward...
                                              validity.getTime(),
                                              AsymSignatureAlgorithms.ECDSA_SHA256,
                                              new AsymKeySignerInterface() {

                      @Override
                      public PublicKey getPublicKey() throws IOException {
                          return issuer.subCA.getPublicKey();
                      }
    
                      @Override
                      public byte[] signData(byte[] data,
                                               AsymSignatureAlgorithms certAlg)
                                throws IOException {
                          try {
                              return new SignatureWrapper(certAlg, issuer.subCA.getPrivateKey())
                                        .setEcdsaSignatureEncoding(true)
                                        .update(data)
                                        .sign();
                          } catch (GeneralSecurityException e) {
                              throw new IOException(e);
                          }
                      }
                  },
                                              key.getPublicKey());
                  // Certification of key ends here

                  key.setCertificatePath(certPath);
                  
                  // Get the personalized card image
                  key.addLogotype(KeyGen2URIs.LOGOTYPES.CARD, new MIMETypedObject() {

                      @Override
                       public byte[] getData() throws IOException {
                          return userData.cardImage.getBytes("utf-8");
                      }
    
                      @Override
                      public String getMimeType() throws IOException {
                          return "image/svg+xml";
                      }
                  });
                  keygen2JSONBody(response,
                                  new ProvisioningFinalizationRequestEncoder(keygen2State,
                                                                             keygen2EnrollmentUrl));
                  return;

                case PROVISIONING_FINALIZATION:
                  ProvisioningFinalizationResponseDecoder provisioningFinalResponse =
                      (ProvisioningFinalizationResponseDecoder) jsonObject;
                  keygen2State.update(provisioningFinalResponse);
                  log.info("Successful KeyGen2 run");

                  ////////////////////////////////////////////////////////////////////////////////////////////
                  // We are done, return an HTTP redirect taking the client out of its KeyGen2 mode
                  ////////////////////////////////////////////////////////////////////////////////////////////
                  response.sendRedirect(keygen2EnrollmentUrl);
                  return;

                default:
                  throw new IOException("Unxepected state");
            }
        } catch (Exception e) {
            if (session != null) {
                session.invalidate();
            }
            log.log(Level.SEVERE, "KeyGen2 failure", e);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter printerWriter = new PrintWriter(baos);
            e.printStackTrace(printerWriter);
            printerWriter.flush();
            returnKeyGen2Error(response, baos.toString("UTF-8"));
        }
    }

    boolean foundData(HttpServletRequest request, StringBuilder result, String tag) {
        String value = request.getParameter(tag);
        if (value == null) {
            return false;
        }
        result.append(value);
        return true;
    }
    
    StringBuilder successPage(HttpSession session) {
        StringBuilder svg = new StringBuilder(
            "<div class=\"header\">" +
            LocalizedStrings.RESULT_MESSAGE_HEADER +
            "</div>" +
            "<svg id=\"cardimage\" style=\"width:100pt;padding-top:15pt\" " +
            "viewBox=\"0 0 318 190\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
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
              "height=\"182\" width=\"302\" y=\"4\" x=\"12\" fill=\"#c0c0c0\"/>\n" +
            "<svg x=\"9\" y=\"1\" clip-path=\"url(#cardClip)\"");
        svg.append(((KeyProviderInitServlet.UserData) ((ServerState)session
                .getAttribute(KeyProviderInitServlet.KEYGEN2_SESSION_ATTR))
                .getServiceSpecificObject(KeyProviderInitServlet.SERVER_STATE_USER))
                    .cardImage)
           .append(
            "<rect x=\"10\" y=\"2\" " +
            "width=\"298\" height=\"178\" " +
            "rx=\"14.7\" ry=\"14.7\" " +
            "fill=\"none\" " +
            "stroke=\"url(#innerCardBorder)\" stroke-width=\"2.7\"/>\n" +
            "<rect x=\"8.5\" y=\"0.5\" " +
            "width=\"301\" height=\"181\" " +
            "rx=\"16\" ry=\"16\" fill=\"none\" stroke=\"url(#outerCardBorder)\"/>\n" +
            "</svg>\n");
        return svg;
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException {
        if (request.getParameter(KeyProviderInitServlet.INIT_TAG) != null) {
            executeRequest(request,
                           response,
                           request.getParameter(KeyProviderInitServlet.ANDROID_WEBPKI_VERSION_TAG),
                           true);
            return;
        }
        HttpSession session = request.getSession(false);
        StringBuilder html = new StringBuilder();
        StringBuilder result = new StringBuilder();
        if (foundData(request, result, KeyProviderInitServlet.ERROR_TAG)) {
            html.append("<b>Failure Report:</b><pre><font color=\"red\">")
                .append(result)
                .append("</font></pre>");
        } else if (foundData(request, result, KeyProviderInitServlet.PARAM_TAG)) {
            html.append(result);
        } else if (foundData(request, result, KeyProviderInitServlet.ABORT_TAG)) {
            log.info("KeyGen2 run aborted by the user");
            html.append("<b>Aborted by the user!</b>");
        } else {
            if (session == null) {
                html.append("<b>You need to restart the session</b>");
            } else {
                html = successPage(session);
            }
        }
        if (session != null) {
            session.invalidate();
        }
       HTML.resultPage(response, null, false, html);
    }
}

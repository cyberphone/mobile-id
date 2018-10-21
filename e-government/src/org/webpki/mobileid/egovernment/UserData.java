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
import java.io.Serializable;

import java.security.cert.X509Certificate;

import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.webpki.crypto.CertificateInfo;

import org.webpki.localized.LocalizedStrings;

public class UserData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    static final String USER_DATA = "userdata";
    
    static final int ID_STRING_LENGTH    = 12;    // Fits most nations
    static final String ID_STRING_NAME   = "ID";  // Could also be SSN (Social Security Number), 
                                                  // PN (Person Number, etc.
    
    long creationTime;
    String sessionId;
    String userCommonName;
    String userId;
    private X509Certificate userCertificate;
    
    public UserData(HttpSession session, X509Certificate userCertificate) throws IOException {
    	this(userCertificate);
        this.creationTime = session.getCreationTime();
        this.sessionId = session.getId();
    }

    public String getSessionId() {
    	return sessionId;
    }

    public GregorianCalendar getCreationTime() {
    	GregorianCalendar dateTime = new GregorianCalendar();
    	dateTime.setTimeInMillis(creationTime);
    	return dateTime;
    }

    public UserData(X509Certificate userCertificate) throws IOException {
    	CertificateInfo certInfo = new CertificateInfo(userCertificate);
        this.userCommonName = certInfo.getSubjectCommonName();
        this.userId = certInfo.getSubjectSerialNumber();
        this.userCertificate = userCertificate;
    }

    public X509Certificate getUserCertificate() {
    	return userCertificate;
    }

    // The actual user identity used by the information systems
    public String getUserId() {
    	return userId;
    }

    // For usage in UIs
    public String getUserIdHTMLString() {
        StringBuilder idString = new StringBuilder(ID_STRING_NAME + ":&#x2009;");
        for (int q = 0; q < ID_STRING_LENGTH; q += 4) {
            if (q != 0) {
                idString.append(' ');
            }
            idString.append(userId.substring(q, q + 4));
        }
    	return idString.toString();
    }

    // Human-friendly name of the user
    public String getUserCommonName() {
    	return userCommonName;
    }

    // This is fake, you usually get such data elsewhere
    public String getUserEmailAddress() {
    	return userCommonName.toLowerCase().replace(' ', '.') +
    		"@" + LocalizedStrings.DEMO_EMAIL_PROVIDER;
    }

    static UserData getUserData(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (UserData)session.getAttribute(USER_DATA);
    }
}

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

import java.io.Serializable;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UserData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    static final String USER_DATA = "userdata";
    
    long creationTime;
    String sessionId;
    String userName;
    String citizenId;
    X509Certificate certificate;
    
    public UserData(HttpSession session, 
                    String userName,
                    String citizenId, 
                    X509Certificate certificate) {
        this.creationTime = session.getCreationTime();
        this.sessionId = session.getId();
        this.userName = userName;
        this.citizenId = citizenId;
        this.certificate = certificate;
    }

    static UserData getUserData(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (UserData)session.getAttribute(USER_DATA);
    }
}

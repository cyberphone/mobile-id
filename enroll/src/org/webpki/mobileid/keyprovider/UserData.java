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

// This class holds the data to be certified

public class UserData {

    static final int    ID_STRING_LENGTH = 12;    // Fictitious identity universe
    static final String ID_STRING_NAME   = "ID";  // Card label of ID is "ID"

    String userName;
    String userId;
    String cardImage;
    String issuerName;
    TargetPlatforms targetPlatform;

    UserData(String userName,
             String userId,
             KeyProviderService.IssuerHolder issuer,
             TargetPlatforms targetPlatform) {
        this.userName = userName;
        this.userId = userId;
        this.targetPlatform = targetPlatform;
        StringBuilder idString = new StringBuilder(ID_STRING_NAME + ":&#x2009;");
        for (int q = 0; q < ID_STRING_LENGTH; q += 4) {
            if (q != 0) {
                idString.append(' ');
            }
            idString.append(userId.substring(q, q + 4));
        }
        this.issuerName = issuer.commonName;
        this.cardImage = issuer.cardImage.replace("@n", userName)
                                         .replace("@i", idString);
        if (userName.length() > 26) {
            cardImage = cardImage.replace("font-size=\"20\"", "font-size=\"14\"");
        }
    }
    
    @Override
    public String toString() {
        return "Name=" + userName + " ID=" + userId + " Issuer=" + issuerName;
    }
}


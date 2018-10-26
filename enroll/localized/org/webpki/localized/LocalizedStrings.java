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

package org.webpki.localized;

public interface LocalizedStrings {
    
    String LANGUAGE_CODE              = "en";

    String TITLE                      = "Mobile ID Enrollment Demo";
    String URL_TO_DESCRIPTION         = "http://cyberphone.github.io/doc/mobile-id";
	String HT_TO_MOBILEID_LOGO        = "Mobile ID documentation";

    // Description
    String DESCRIPTION                = "This is a proof of concept site " +
                                        "hosting an enrollment application " +
                                        "for the Mobile ID system. " +
                                        "Note that in a real enrollment system " +
                                        "you would also need to authenticate in a " +
                                        "way defined by the issuer.";
    
    // Your name label
    String YOUR_NAME                  = "Your Name";
    
    // Name hint
    String DEFAULT                    = "default";

    // Issuer radio button label
    String SELECTED_ISSUER            = "Selected Issuer";

    // Button text
    String START_ENROLLMENT           = "Start Enrollment";

    // Limitations
    String ANDROID_ONLY               = "This demo currently only supports Android";

	String RESULT_MESSAGE_HEADER      = "Your new digital identity is now securely " +
	                                    "stored in your Android device!";

 }


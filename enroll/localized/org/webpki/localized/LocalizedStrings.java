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
    String HT_HOME                    = "Home sweet home...";

    // Description
    String DEMO_TEXT                  = "This is a proof of concept site " +
                                        "illustrating enrollment using @. " +
                                        "Note: in a real enrollment scenario " +
                                        "users would also need to authenticate!";

    // First header
    String ENROLLMENT_HEADER          = "Enrolling a Mobile Identity";
    
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

	// The note on the initial page can be closed
	String CLOSE_VIEW                 = "Click to close";

	String OPERATION_FAILED_HEADER    = "Operation Failed";

	String ABORTED_BY_USER_HEADER     = "Aborted by the User";

	String SESSION_TIMED_OUT_HEADER   = "Session Timed Out";
 }


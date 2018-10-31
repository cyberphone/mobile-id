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

    String DO_YOU_HAVE_MOBILE_ID      = "If you have not already installed " +
                                        "Mobile ID, this is the time to do it!";

    String TEST_URL_HERE              = "You may now try it out at";

    String QR_ACTIVATION_HEADER       = "QR Activation Mode";

    String QR_BOOTSTRAP               = "Since enrollment was not initiated from an @ " +
                                        "device you need a QR code to &quot;boostrap&quot; " +
                                        "the enrollment process.";

    String QR_START_APPLICATION       = "Use the @ application to start!";
    
    String QR_APP_LOCATING            = "You get it automatically when you " +
                                        "install Mobile ID, just look for the icon!";

    String FOUND_CHROME_VERSION       = "Found &quot;Chrome&quot; version";

    String UNSUPPORTED_PLATFORM       = "@ is currently not supported";

    String INCOMPATIBILITY_ISSUES     = "Incompatibility Issues";

    String UNSUPPORTED_ANDROID_BROWSER = "&quot;Chrome&quot; is currently the " +
                                        "only supported browser on Android";

    String WRONG_APP_VERSION           = "Incorrect version of the Mobile ID app, " +
                                         "you need to update";
}


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

    String TITLE                      = "Mobile ID/eGovernment Demo";
    String URL_TO_DESCRIPTION         = "http://cyberphone.github.io/doc/mobile-id";

    // Demo data
    String DEMO_TEXT                  = "This is a proof of concept site " +
                                        "hosting a minute e-government application " +
                                        "powered by the @ system.";
    String DEMO_EMAIL_PROVIDER        = "google.com";  // Pure UI demo feature

    // App UI emulator only
    String UI_DEMO_TOP_URL            = "taxdepartment.gov";
    String UI_DEMO_AUTH_TO            = "Authenticate to Service";
    String UI_DEMO_SELECTED_CRED      = "Selected Credential";
    String UI_DEMO_HT_REQUEST_DOMAIN  = "Requesting domain";

    // Main
    String SELECT_SERVICE             = "Select Service";
    String INCOME_DECLARATION         = "Income Declaration";
    String TAXATION_HISTORY           = "Taxation History";
    String SEND_MESSAGE               = "Send Message";
    String THANK_YOU_WELCOME_BACK     = "Thank you, welcome back!";

    // Declaration
    String DECLARATION_HEADER         = "Declaration Year @";  // @ = Year placeholder
    String DECLARATION_RECEIVED       = "Declaration Received";
    
    String CONFIRMATION               = "A confirmation message has been sent to"; // Followed by : email

    // Send Message
    String MESSAGE_RECEIVED           = "Message Received";

    String THANKS_FOR_MESSAGE         = "Thank you for your message, " +
                                         "we will process it as fast we can!";
    String MSG_NO_SELECT              = "Please select message subject";
    String MSG_SELECT_TAX_QUESTION    = "Question regarding taxation";
    String MSG_SELECT_FILE_COMPLAINT  = "File a complaint";
    String MSG_SELECT_TECH_SUPPORT    = "Technical support";
    String MSG_SELECT_OTHER_TOPIC     = "Other topic";
    String MSG_NO_MESSAGE             = "Please provide a message";
    String MSG_YOUR_MESSAGE_TIP       = "Your message";
    String SUBMIT_BTN                 = "Submit";

    // General purpose
    String REFERENCE_ID               = "Reference ID";  // SHORT table header
    String TIME_STAMP                 = "Time Stamp";    // SHORT table header
    String CLOSE_VIEW                 = "Click to close";

    // Login, logout, home
    String HT_SESSION                 = "Click to show session data";
    String HT_HOME                    = "Home sweet home...";
    String HT_LOGOUT                  = "Click to logout";
    String REQUIRES_LOGIN             = "This Service Requires Login";
    String SELECT_LOGIN_METHOD        = "Select Login Method";
    String ONLY_ONE_LOGIN_METHOD      = "In this demo there is only one method";
    
    // Session data
    String SESSION_DATA               = "Session Data";
    String SESSION_ID                 = "Session ID";
    String START_TIME                 = "Start Time";
    String SERIAL_NUMBER              = "Serial Number";
    String ISSUER_NAME                = "Issuer Name";
    String SUBJECT_NAME               = "Subject Name";
    String VALIDITY                   = "Validity";
    String USER_CERTIFICATE           = "User Certificate";
    String SESSION_TERMINATED         = "The session appears to have terminated";
    String SESSION_TIMOUT             = "Session Timed Out";
    
    String DO_YOU_HAVE_MOBILE_ID      = "If you have not already installed " +
            "Mobile ID, this is the time to do it!";

    String QR_AUTHENTICATION_HEADER   = "QR Authentication Mode";
    
    String QR_BOOTSTRAP               = "Since authentication was not initiated from an @ " +
                "device<br>you need a QR code to &quot;boostrap&quot; " +
                "the process.";
    
    String QR_START_APPLICATION       = "Use the @ application to start!";
    
    String QR_APP_LOCATING            = "You get it automatically when you " +
                                        "install Mobile ID, just look for the icon!";
    
    String QR_WAITING_FOR_MOB_DEVICE  = "Now waiting on your interactions with the mobile device...";
    
    String QR_SESSION_STATUS          = "Session Status";
    
    String FOUND_CHROME_VERSION       = "Found &quot;Chrome&quot; version";
    
    String UNSUPPORTED_PLATFORM       = "@ is currently not supported";
    
    String INCOMPATIBILITY_ISSUES     = "Incompatibility Issues";
    
    String UNSUPPORTED_ANDROID_BROWSER = "&quot;Chrome&quot; is currently the " +
                                         "only supported browser on Android";
    
    String WRONG_APP_VERSION          = "Incorrect version of the Mobile ID app, " +
                                        "you need to update";
 }


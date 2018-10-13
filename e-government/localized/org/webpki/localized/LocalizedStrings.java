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

    // Declaration
    String LS_DECLARATION_HEADER   = "Declaration Year @";  // @ = Year placeholder
    String LS_DECLARATION_RECEIVED = "Declaration Received";
    
    String LS_CONFIRMATION         = "A confirmation message has been sent to"; // Followed by : email

    // Submit Message
    String LS_MESSAGE_RECEIVED     = "Message Received";

    String LS_THANKS_FOR_MESSAGE   = "Thank you for your message, " +
                                     "we will process it as fast we can!";
    String LS_NO_SELECT            = "Please select message subject";
    String LS_NO_MESSAGE           = "Please provide a message";

    // General purpose
    String LS_REFERENCE_ID         = "Reference ID";  // SHORT table header
    String LS_TIME_STAMP           = "Time Stamp";    // SHORT table header

    // Login, logout, home
    String LS_HT_SESSION           = "Click to show session data";
    String LS_HT_HOME              = "Home sweet home...";
    String LS_HT_LOGOUT            = "Click to logout";
	String LS_REQUIRES_LOGIN       = "This Service Requires Login";
	String LS_SELECT_LOGIN_METHOD  = "Select Login Method";
    
    // Session data
	String LS_SERIAL_NUMBER        = "Serial Number";
	String LS_ISSUER_NAME          = "Issuer Name";
	String LS_SUBJECT_NAME         = "Subject Name";
	String LS_VALIDITY             = "Validity";
	String LS_USER_CERTIFICATE     = "User Certificate";
 
}


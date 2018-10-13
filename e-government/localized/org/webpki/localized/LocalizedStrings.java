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

	// Main
	String SELECT_SERVICE           = "Select Service";
	String INCOME_DECLARATION       = "Income Declaration";
	String TAXATION_HISTORY         = "Taxation History";
	String SEND_MESSAGE             = "Send Message";
	String THANK_YOU_WELCOME_BACK   = "Thank you, welcome back!";

	// Declaration
    String DECLARATION_HEADER       = "Declaration Year @";  // @   = Year placeholder
    String DECLARATION_RECEIVED     = "Declaration Received";
    
    String CONFIRMATION             = "A confirmation message has been sent to"; // Followed by : email

    // Send Message
    String MESSAGE_RECEIVED         = "Message Received";

    String THANKS_FOR_MESSAGE       = "Thank you for your message, " +
                                      "we will process it as fast we can!";
    String NO_SELECT                = "Please select message subject";
    String NO_MESSAGE               = "Please provide a message";
	String YOUR_MESSAGE_TIP         = "Your message";

    // General purpose
    String REFERENCE_ID             = "Reference ID";  // SHORT table header
    String TIME_STAMP               = "Time Stamp";    // SHORT table header

    // Login, logout, home
    String HT_SESSION               = "Click to show session data";
    String HT_HOME                  = "Home sweet home...";
    String HT_LOGOUT                = "Click to logout";
	String REQUIRES_LOGIN           = "This Service Requires Login";
	String SELECT_LOGIN_METHOD      = "Select Login Method";
	String ONLY_ONE_LOGIN_METHOD    = "In this demo there is only one method";
    
    // Session data
	String SERIAL_NUMBER            = "Serial Number";
	String ISSUER_NAME              = "Issuer Name";
	String SUBJECT_NAME             = "Subject Name";
	String VALIDITY                 = "Validity";
	String USER_CERTIFICATE         = "User Certificate";
 }


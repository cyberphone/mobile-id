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

package org.webpki.mobileid.authenticate;

import java.io.IOException;

import javax.servlet.ServletException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.webutil.ServletUtil;

public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		HTML.printResultPage(
				response,
				"<table>"
						+ "<tr><td align=\"center\" style=\"font-weight:bolder;font-size:10pt;font-family:arial,verdana\">Secure Login Demo<br>&nbsp;</td></tr>"
						+ "<tr><td align=\"left\">This is a demo application where you can login using an Android device<br>provisioned "
						+ "with a key received through <a href=\""
						+ ServletUtil.getServerRootURL(request)
						+ "scc\">Secure Credential Cloning</a>.<br>&nbsp;</td></tr>"
						+ "<tr><td align=\"left\">Example 1: <a href=\"qrdisplay\">Login from a PC</a> using the Android device like a wireless<br>PKI token. How does it actually work? Click "
						+ "<a href=\"https://cyberphone.github.io/openkeystore/resources/docs/QR-ID-presentation.pdf\" target=\"_blank\">here</a> for a description.<br>&nbsp;</td></tr>"
						+ "<tr><td align=\"left\">Example 2: <a href=\"login\">Login from Android</a> using the <i>mobile browser.<br>&nbsp;</i></td></tr>"
						+ "<tr><td align=\"left\">Note that you if you get a popup question from Android - Select the browser!</td></tr>"
						+ "</table>");
	}
}

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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AbortedServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String ERROR = "ERR";

	static final String ABORT_SERVLET = "/aborted?" + ERROR + "=";

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String message = request.getParameter(ERROR);
		if (message == null) {
			message = "Unknown error";
		}
		HTML.printResultPage(response, "<b>" + message
				+ "</b><br>&nbsp;<br><a href=\"home\">Try again</a>");
	}
}

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

import java.net.URLEncoder;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PluginTriggerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String ANDROID_WEBPKI_VERSION_MACRO = "$VER$";

	static final String ANDROID_WEBPKI_VERSION_TAG = "VER";

	static String getID(HttpServletRequest request) throws IOException {
		String id = request.getParameter(AuthenticationDB.ID);
		if (id == null)
			throw new IOException("\"" + AuthenticationDB.ID + "\" missing");
		return id;
	}

	static void pluginTrigger(HttpServletResponse response, String id)
			throws IOException, ServletException {
		String version = LoginService.version_check == null ? "" : "&"
				+ ANDROID_WEBPKI_VERSION_TAG + "="
				+ ANDROID_WEBPKI_VERSION_MACRO;
		String url = "webpkiproxy://webauth?url="
				+ URLEncoder.encode(LoginService.application_url + "/bootstrap"
						+ "?" + AuthenticationDB.ID + "=" + id + version,
						"UTF-8");
		response.setHeader("Cache-Control",
				"no-cache, max-age=0, must-revalidate, no-store");
		HTML.startPlugin(response, url);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		pluginTrigger(response, getID(request));
	}
}

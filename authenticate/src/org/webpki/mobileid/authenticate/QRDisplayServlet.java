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

import org.apache.log4j.Logger;

import net.glxn.qrgen.QRCode;

import net.glxn.qrgen.image.ImageType;


public class QRDisplayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(QRDisplayServlet.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String id = request.getParameter(AuthenticationDB.ID);
		if (LoginService.debug) {
			log.info(id);
		}
		String result = "<done/>";
		if (id != null) {
			Synchronizer synchronizer = AuthenticationDB.getSynchronizer(id);
			if (synchronizer != null
					&& !synchronizer.perform(LoginService.comet_delay)) {
				result = "<continue/>";
			}
		}
		response.setContentType("text/xml; charset=utf-8");
		response.setHeader("Pragma", "No-Cache");
		response.setDateHeader("EXPIRES", 0);
		response.getOutputStream().write(result.getBytes("UTF-8"));
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String self_url = request.getRequestURL().toString();
		self_url = self_url.substring(0, self_url.lastIndexOf('/'));
		String id = AuthenticationDB.createAuthenticationRequest(true);
		response.setHeader("Cache-Control",
				"no-cache, max-age=0, must-revalidate, no-store");
		HTML.printQRCode(
				response,
				QRCode.from(
						"webpki.org="
								+ URLEncoder.encode(
										LoginService.application_url
												+ "/plugin?"
												+ AuthenticationDB.ID + "="
												+ id, "UTF-8"))
						.to(ImageType.PNG).withSize(200, 200).stream()
						.toByteArray(), self_url + "/qrdisplay?"
						+ AuthenticationDB.ID + "=" + id, self_url
						+ "/account?QR=true&" + AuthenticationDB.ID + "=" + id);
	}
}

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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// Just a simple WAR to put when an application is not available

public class HomeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	static String getHTML(String content) {
		StringBuilder s = new StringBuilder(
				"<!DOCTYPE html>"
		                + "<head>"
						+ "<meta charset=\"utf-8\">"
						+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
						+ "<style>"
						+ "</style>"
						+ "<title>UNAVAILABLE</title>"
						+ "</head><body>")
		.append(content)
		.append("</body></html>");
		return s.toString();
	}

	static void output(HttpServletResponse response, String html)
			throws IOException, ServletException {
		response.setContentType("text/html; charset=utf-8");
		response.setHeader("Pragma", "No-Cache");
		response.setDateHeader("EXPIRES", 0);
		byte[] data = html.getBytes("UTF-8");
		response.setContentLength(data.length);
		ServletOutputStream servletOutputStream = response.getOutputStream();
		servletOutputStream.write(data);
		servletOutputStream.flush();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
	}
}

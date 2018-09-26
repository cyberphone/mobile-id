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

package org.webpki.mobileid.egovernment;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.webpki.util.Base64;

public class HTML {
	static final String SIGNUP_BGND_COLOR = "#F4FFF1";
	static final String SIGNUP_EDIT_COLOR = "#FFFA91";
	static final String SIGNUP_BAD_COLOR = "#F78181";
	static final String BOX_SHADDOW = "box-shadow:5px 5px 5px #C0C0C0";
	static final String KG2_DEVID_BASE = "Field";

	static final String HTML_INIT = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">"
			+ "<html><head><link rel=\"shortcut icon\" href=\"favicon.png\">"
			+ "<meta name=\"viewport\" content=\"initial-scale=1.0\"/>"
			+ "<title>Secure Login Demo</title>"
			+ "<style type=\"text/css\">html {overflow:auto} html, body {margin:0px;padding:0px;height:100%} "
			+ "body {font-size:8pt;color:#000000;font-family:verdana,arial;background-color:white} "
			+ "h2 {font-weight:bold;font-size:12pt;color:#000000;font-family:arial,verdana,helvetica} "
			+ "h3 {font-weight:bold;font-size:11pt;color:#000000;font-family:arial,verdana,helvetica} "
			+ "a:link {font-weight:bold;font-size:8pt;color:blue;font-family:arial,verdana;text-decoration:none} "
			+ "a:visited {font-weight:bold;font-size:8pt;color:blue;font-family:arial,verdana;text-decoration:none} "
			+ "a:active {font-weight:bold;font-size:8pt;color:blue;font-family:arial,verdana} "
			+ "input {font-weight:normal;font-size:8pt;font-family:verdana,arial} "
			+ "td {font-size:8pt;font-family:verdana,arial} "
			+ ".smalltext {font-size:6pt;font-family:verdana,arial} "
			+ "button {font-weight:normal;font-size:8pt;font-family:verdana,arial;padding-top:2px;padding-bottom:2px} "
			+ ".headline {font-weight:bolder;font-size:10pt;font-family:arial,verdana} "
			+ ".dbTR {border-width:1px 1px 1px 0;border-style:solid;border-color:black;padding:4px} "
			+ ".dbTL {border-width:1px 1px 1px 1px;border-style:solid;border-color:black;padding:4px} "
			+ ".dbNL {border-width:0 1px 1px 1px;border-style:solid;border-color:black;padding:4px} "
			+ ".dbNR {border-width:0 1px 1px 0;border-style:solid;border-color:black;padding:4px} "
			+ "</style>";

	static String encode(String val) {
		if (val != null) {
			StringBuffer buf = new StringBuffer(val.length() + 8);
			char c;

			for (int i = 0; i < val.length(); i++) {
				c = val.charAt(i);
				switch (c) {
				case '<':
					buf.append("&lt;");
					break;
				case '>':
					buf.append("&gt;");
					break;
				case '&':
					buf.append("&amp;");
					break;
				case '\"':
					buf.append("&#034;");
					break;
				case '\'':
					buf.append("&#039;");
					break;
				default:
					buf.append(c);
					break;
				}
			}
			return buf.toString();
		} else {
			return new String("");
		}
	}

	static String getHTML(String javascript, String bodyscript, String box) {
		StringBuffer s = new StringBuffer(HTML_INIT);
		if (javascript != null) {
			s.append("<script type=\"text/javascript\">").append(javascript)
					.append("</script>");
		}
		s.append("</head><body");
		if (bodyscript != null) {
			s.append(' ').append(bodyscript);
		}
		s.append(
				"><a href=\"http://primekey.se\" title=\"PrimeKey Solutions\" style=\"position:absolute;top:15px;left:15px;z-index:5;visibility:visible\">"
						+ "<img src=\"images/logotype.svg\" border=\"0\"></a>"
						+ "<table cellapdding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">")
				.append(box).append("</table></body></html>");
		return s.toString();
	}

	private static void output(HttpServletResponse response, String html)
			throws IOException, ServletException {
		response.setContentType("text/html; charset=utf-8");
		response.setHeader("Pragma", "No-Cache");
		response.setDateHeader("EXPIRES", 0);
		response.getOutputStream().write(html.getBytes("UTF-8"));
	}

	public static void printQRCode(HttpServletResponse response,
			byte[] qr_image, String comet_url, String target_url)
			throws IOException, ServletException {
		HTML.output(
				response,
				HTML.getHTML(
						"var xmlhttp = new XMLHttpRequest ();\n"
								+ "xmlhttp.onreadystatechange=function ()\n"
								+ "  {\n"
								+ "    if (xmlhttp.readyState == 4 && xmlhttp.status == 200)\n"
								+ "      {\n"
								+ "        if (xmlhttp.responseText == '<done/>')\n"
								+ "          {\n"
								+ "            document.location.href = '"
								+ target_url + "';\n" + "            return;\n"
								+ "          }\n" + "        startCOMET ();\n"
								+ "      }\n" + "  }\n"
								+ "function startCOMET ()\n" + "  {\n"
								+ "    xmlhttp.open ('POST', '" + comet_url
								+ "', true);\n" + "    xmlhttp.send ();\n"
								+ "  }\n",

						"onload=\"startCOMET ()\"",

						"<tr><td width=\"100%\" align=\"center\" valign=\"middle\" id=\"progress\"><table cellpadding=\"5\" cellspacing=\"0\">"
								+ "<tr><td align=\"left\">Now use the QR ID&trade; <a href=\"javascript:alert ('You get it automatically when you install the &quot;WebPKI&nbsp;Suite&quot;, just look for the icon!')\"><img border=\"1\" src=\"images/qr_launcher.png\"></a> application to retrieve the web-address<br>"
								+ "that starts the actual authentication process in the mobile device</span></td></tr>"
								+ "<tr><td align=\"center\"><img src=\"data:image/png;base64,"
								+ new Base64(false)
										.getBase64StringFromBinary(qr_image)
								+ "\"></td></tr>"
								+ "<tr><td align=\"center\"><img src=\"images/loading.gif\"></td></tr>"
								+ "</table></td></tr>"));
	}

	public static void printResultPage(HttpServletResponse response,
			String message) throws IOException, ServletException {
		HTML.output(response, HTML.getHTML(null, null,
				"<tr><td width=\"100%\" align=\"center\" valign=\"middle\">"
						+ message + "</td></tr>"));
	}

	public static void printResultPageWithLogout(HttpServletResponse response,
			String message) throws IOException, ServletException {
		HTML.output(
				response,
				HTML.getHTML(
						null,
						"><a href=\"home\" title=\"Logout\" style=\"position:absolute;top:15px;right:15px;z-index:5;visibility:visible\">Logout</a",
						"<tr><td width=\"100%\" align=\"center\" valign=\"middle\">"
								+ message + "</td></tr>"));
	}

	public static void startPlugin(HttpServletResponse response, String url)
			throws IOException, ServletException {
		HTML.output(
				response,
				HTML.getHTML(
						null,
						"onload=\"document.location.href='" + url + "'\"",
						"<tr><td width=\"100%\" align=\"center\" valign=\"middle\"><b>Please wait while authentication plugin starts...</b></td></tr>"));

	}

	public static void printNFCCode(HttpServletResponse response, String url,
			String comet_url, String target_url) throws IOException,
			ServletException {
		HTML.output(
				response,
				HTML.getHTML(
						"\n"
								+ "\"use strict\";\n"
								+ "var xmlhttp = new XMLHttpRequest ();\n"
								+ "xmlhttp.onreadystatechange=function ()\n"
								+ "  {\n"
								+ "    if (xmlhttp.readyState == 4 && xmlhttp.status == 200)\n"
								+ "      {\n"
								+ "        if (xmlhttp.responseText == '<done/>')\n"
								+ "          {\n"
								+ "            document.location.href = '"
								+ target_url
								+ "';\n"
								+ "            return;\n"
								+ "          }\n"
								+ "        startCOMET ();\n"
								+ "      }\n"
								+ "  }\n"
								+ "function startCOMET ()\n"
								+ "  {\n"
								+ "    xmlhttp.open ('POST', '"
								+ comet_url
								+ "', true);\n"
								+ "    xmlhttp.send ();\n"
								+ "  }\n"
								+ "var nfcPort;\n"
								+ "function activateNfc() {\n"
								+ "    navigator.tapConnect('webauth.demo', {callme:'"
								+ url
								+ "'}).then(function(port) {\n"
								+ "        nfcPort = port;\n"
								+ "    }, function(err) {\n"
								+ "        console.debug(err);\n"
								+ "    });"
								+ "    startCOMET ();\n"
								+ "}\n"
								+ "window.addEventListener(\"beforeunload\", function(event) {\n"
								+ "    nfcPort.disconnect();\n"
								+ "    nfcPort = null;\n" + "});\n",

						"onload=\"activateNfc()\"",

						"<tr><td width=\"100%\" align=\"center\" valign=\"middle\" id=\"progress\"><table cellpadding=\"5\" cellspacing=\"0\">"
								+ "<tr><td align=\"center\"><b>Please tap your phone to start authenticating</b>&nbsp;<br></td></tr>"
								+ "<tr><td align=\"center\"><img id=\"state\" title=\"Please tap your mobile wallet!\" "
								+ "src=\"images/NFC-N-Mark-Logo.svg\" style=\"height:120pt;margin-top:10pt\"></td></tr></table></td></tr>"));
	}
}

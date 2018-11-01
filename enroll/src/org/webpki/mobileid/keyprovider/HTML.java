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

package org.webpki.mobileid.keyprovider;

import java.io.IOException;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.webpki.localized.LocalizedStrings;

public class HTML {

    static Logger logger = Logger.getLogger(HTML.class.getCanonicalName());

    static final String STICK_TO_HOME_URL            =
            "history.pushState(null, null, 'home');\n" +
            "window.addEventListener('popstate', function(event) {\n" +
            "    history.pushState(null, null, 'home');\n" +
            "});\n";

    static String getHTML(String customJavaScript, 
                          boolean toasterSupport,
                          String onloadCode,
                          String content) {
        StringBuilder s = new StringBuilder(
            "<!DOCTYPE html>"+
            "<html lang=\"" +
            LocalizedStrings.LANGUAGE_CODE +
            "\"><head>" +
            "<meta charset=\"utf-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
            // iOS thinks all numbers are telephone numbers!
            "<meta name=\"format-detection\" content=\"telephone=no\">" +
            "<link rel=\"icon\" href=\"mobileid.png\" sizes=\"192x192\">"+
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">" +
            "<title>" +
            LocalizedStrings.TITLE +
            "</title>" +
            "<script>\n" +
            "\"use strict\";\n" +
            STICK_TO_HOME_URL +
            "function initUi() {\n" +
            "  let cardimage = document.getElementById('cardimage');\n" +
            "  if (cardimage) {\n" + 
            "    let width = window.innerWidth;\n" +
            "    if (width > window.innerHeight) width = window.innerHeight;\n" +
            "    cardimage.style.width = (width * 0.8) + 'px';\n" +
            "  }\n" +
            "  let content = document.getElementById('content');\n" +
            "  if (content) {\n" + 
            "    let top = (window.innerHeight - content.offsetHeight) / 2;\n" +
            "    if (top < 0) top = 0;\n" +
            "    content.style.top = top + 'px';\n" +
            "    let left = (window.innerWidth - content.offsetWidth) / 2;\n" +
            "    if (left < 0) left = 0;\n" +
            "    content.style.left = left + 'px';\n" +
            "    content.style.visibility = 'visible';\n" +
            "  }\n" +
            "}\n" +
            "window.addEventListener('resize', () => { initUi() });\n" +
            "function initApplication() {\n" +
            "  initUi();\n")
        .append(onloadCode == null ? "" : onloadCode)
        .append(
            "}\n");
        if (customJavaScript != null) {
            s.append(customJavaScript);
        }
        if (toasterSupport){
            s.append(
                "function toast(message, fromElement) {\n" +
                "  let toaster = document.getElementById('toaster');\n" +
                "  toaster.innerHTML = message;\n" +
                "  if (fromElement == undefined) {\n" +
                "    toaster.style.top = ((window.innerHeight - toaster.offsetHeight) / 2) + 'px';\n" +
                "  } else {\n" +
                "    toaster.style.top = (fromElement.getBoundingClientRect().top - toaster.offsetHeight - 20) + 'px';\n" +
                "  }\n" +
                "  toaster.style.left = ((window.innerWidth - toaster.offsetWidth) / 2) + 'px';\n" +
                "  toaster.style.visibility = 'visible';\n" +
                "  setTimeout(function () {\n" +
                "    toaster.style.visibility = 'hidden';\n" +
                "  }, 1000);\n" +
                "}\n");
        }
        s.append(
            "</script></head><body onload=\"initApplication()\">" +
            "<img alt=\"home\" onclick=\"document.location.href='home'\"  " +
            "title=\"" +
            LocalizedStrings.HT_HOME +
            "\" class=\"mobileidlogo\" src=\"images/mobileidlogo.svg\">");
        if (toasterSupport) {
            s.append("<div id=\"toaster\" class=\"toaster\"></div>");
        }
        s.append(
            "<div id=\"content\" class=\"content\">")
         .append(content)
         .append("</div></body></html>");
        return s.toString();
    }

    static void output(HttpServletResponse response, String html) throws IOException, ServletException {
        if (KeyProviderService.logging) {
            logger.info(html);
        }
        response.setContentType("text/html; charset=utf-8");
        response.setHeader("Pragma", "No-Cache");
        response.setDateHeader("EXPIRES", 0);
        byte[] data = html.getBytes("UTF-8");
        response.setContentLength(data.length);
        ServletOutputStream servletOutputStream = response.getOutputStream();
        servletOutputStream.write(data);
        servletOutputStream.flush();
    }

    static void resultPage(HttpServletResponse response,
                           String customJavaScript,
                           boolean toasterSupport,
                           StringBuilder stringBuilder) throws IOException, ServletException {
        resultPage(response,
                   customJavaScript,
                   toasterSupport,
                   null,
                   stringBuilder);
    }

    static void resultPage(HttpServletResponse response,
                           String customJavaScript,
                           boolean toasterSupport,
                           String onloadCode,
                           StringBuilder stringBuilder) throws IOException, ServletException {
        HTML.output(response, 
             HTML.getHTML(customJavaScript,
                          toasterSupport,
                          onloadCode,
                          stringBuilder.toString()));
    }

    static String javaScript(String string) {
        StringBuilder s = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (c == '\n') {
                s.append("\\n");
            } else if (c == '\'') {
                s.append("\\'");
            } else if (c == '\\') {
                s.append("\\\\");
            } else {
                s.append(c);
            }
        }
        return s.toString();
    }
}

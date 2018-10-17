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
                          String customOnLoad, 
                          String content, 
                          UserData userData) {
        StringBuilder s = new StringBuilder(
            "<!DOCTYPE html>"+
            "<html lang=\"" +
            LocalizedStrings.LANGUAGE_CODE +
            "\"><head>" +
            "<meta charset=\"utf-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
            // iOS thinks all numbers are telephone numbers!
            "<meta name=\"format-detection\" content=\"telephone=no\">" +
            "<link rel=\"icon\" href=\"favicon.png\" sizes=\"192x192\">"+
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">" +
            "<title>" +
            LocalizedStrings.TITLE +
            "</title>" +
            "<script>\n" +
            "\"use strict\";\n" +
            STICK_TO_HOME_URL +
            "function initUi() {\n" +
            "  let message = document.getElementById('message');\n" +
            "  if (message) {\n" +
            "    message.style.width = (window.innerWidth * 96) / 100 + 'px';\n" +
            "  }\n" +
            "  let content = document.getElementById('content');\n" +
            "  if (content) {\n" +
            "    let minTop = document.getElementById('egovlogo').getBoundingClientRect().bottom;\n" +
            "    let top = (window.innerHeight - content.offsetHeight) / 2;\n" +
            "    if (top < minTop) top = minTop;\n" +
            "    content.style.top = top + 'px';\n" +
            "    let left = (window.innerWidth - content.offsetWidth) / 2;\n" +
            "    if (left < 0) left = 0;\n" +
            "    content.style.left = left + 'px';\n" +
            "    content.style.visibility = 'visible';\n" +
            "  }\n" +
            "}\n" +
            "window.addEventListener('resize', () => { initUi() });\n" +
            "function initApplication() {\n" +
            "  initUi();\n");
        if (customOnLoad != null) {
            s.append("  ")
             .append(customOnLoad)
             .append(";\n");
        }
        s.append("}\n");
        if (userData != null) {
            s.append(
                "function showSession() {\n" +
                "  fetch('showsession', {\n" +
                "         method: 'GET',\n" +
                "         credentials: 'same-origin'\n" +
                "  }).then(function(response) {\n" +
                "    return response.text();\n" +
                "  }).then(function(html) {\n" +
                "    let session = document.getElementById('session');\n" +
                "    session.innerHTML = html;\n" +
                "    let top = (window.innerHeight - session.offsetHeight) / 2;\n" +
                "    if (top < 5) top = 5;\n" +
                "    session.style.top = top + 'px';\n" +
                "    let left = (window.innerWidth - session.offsetWidth) / 2;\n" +
                "    if (left < 5) left = 5;\n" +
                "    session.style.left = left + 'px';\n" +
                "    session.style.visibility = 'visible';\n" +
                "  }).catch(function(error) {\n" +
                "    console.log('Request failed', error);\n" +
                "  });\n" + 
                "}\n");
              
        }
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
            "<img alt=\"home\" onclick=\"document.location.href='home'\"" +
            " title=\"" +
            LocalizedStrings.HT_HOME +
            "\" id=\"egovlogo\" class=\"egovlogo\" src=\"images/egovlogo.svg\">");
        if (userData != null) {
            StringBuilder id = new StringBuilder();
            for (int q = 0; q < 12; q += 4) {
                if (q != 0) {
                    id.append('\u2009');
                }
                id.append(userData.citizenId.substring(q, q + 4));
            }
            s.append(
                "<div id=\"session\" class=\"sessionview\"></div>" +
                "<div class=\"loginlogoutgroup\">" +
                "<div class=\"login\" title=\"" +
                LocalizedStrings.HT_SESSION +
                "\" onclick=\"showSession()\">" +
                "<div style=\"padding-bottom:4px;white-space:nowrap\">")
             .append(userData.userName)
             .append(
                "</div><div>ID:\u2009")
             .append(id)
             .append(
                "</div></div><div class=\"logout\" title=\"" +
                LocalizedStrings.HT_LOGOUT +
                "\" onclick=\"document.location.href='logout'\">" +
                "<img src=\"images/logout.svg\" class=\"logouticon\" alt=\"logout\">" +
                "</div></div>");
        }
        if (toasterSupport) {
            s.append("<div id=\"toaster\" class=\"toaster\"></div>");
        }
        s.append("<div id=\"content\" class=\"content\">")
         .append(content)
         .append("</div></body></html>");
        return s.toString();
    }
    
    static void output(HttpServletResponse response, String html) throws IOException, ServletException {
        if (eGovernmentService.logging) {
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

    static void resultPage(HttpServletResponse response,
                           UserData userData,
                           StringBuilder stringBuilder) throws IOException, ServletException {
        resultPage(response,
                   null,
                   false,
                   null,
                   userData,
                   stringBuilder);
    }

    static void resultPage(HttpServletResponse response,
                           String customJavaScript,
                           boolean toasterSupport,
                           String customOnLoad,
                           UserData userData,
                           StringBuilder stringBuilder)
    throws IOException, ServletException {
        HTML.output(response, 
            HTML.getHTML(customJavaScript,
                         toasterSupport,
                         customOnLoad,
                         stringBuilder.toString(),
                         userData));
    }

    /*

    static void w2nbWalletPay(HttpServletResponse response,
                              boolean firefox,
                              SavedShoppingCart savedShoppingCart, 
                              boolean tapConnectMode,
                              boolean debugMode,
                              String walletRequest) throws IOException, ServletException {
        String connectMethod = tapConnectMode ? "tapConnect" : "nativeConnect";
        StringBuilder s = currentOrder(savedShoppingCart);
      
        if (tapConnectMode) {
            s.append("<tr><td align=\"center\"><img id=\"state\" title=\"Please tap your mobile wallet!\" " +
                     "src=\"images/NFC-N-Mark-Logo.svg\" style=\"height:120pt;margin-top:10pt\"></td>");
        } else {
            s.append("<tr><td style=\"padding:20pt;text-align:center\" id=\"wallet\"><img src=\"images/waiting.gif\"></td>");
        }
        s.append("</tr></table>" +
                 "<form name=\"restore\" method=\"POST\" action=\"shop\">" +
                 "</form></td></tr>");
        
        StringBuilder temp_string = new StringBuilder("\n\n\"use strict\";\n\nvar invocationData = ")
            .append(walletRequest)
            .append(";\n\n" +

                    "var failureFlag = true; // Race condition between W2NB and location.href\n" +
                    "var nativePort = null;\n\n" +

                    "function closeWallet() {\n" +
                    "  if (nativePort) {\n" +
                    "    nativePort.disconnect();\n" +
                    "    nativePort = null;\n" +
                    "  }\n" +
                    "}\n\n" +

                    "function setFail(message) {\n" +
                    "  closeWallet();\n" +
                    "  alert(message);\n" +
                    "  document.location.href='home';\n" +
                    "}\n\n" +

                    "function activateWallet() {\n" +
                    "  var initMode = true;\n");
        if (firefox) {
            temp_string.append("  setTimeout(function() {\n");
        }
                    
        temp_string.append("  if (!navigator.")
             .append(connectMethod)
             .append(") {\n" +
                    "    setFail('\"navigator.")
             .append(connectMethod)
             .append("\" not found, \\ncheck browser extension install and settings');\n" +
                    "    return;\n" +
                    "  }\n" +
                    "  navigator.")
             .append(connectMethod)
             .append("('")
             .append(MerchantService.w2nbWalletName)
             .append("'");
        if (!tapConnectMode) {
            temp_string.append(",\n                          ")
             .append(ExtensionPositioning.encode(ExtensionPositioning.HORIZONTAL_ALIGNMENT.Center,
                                                 ExtensionPositioning.VERTICAL_ALIGNMENT.Center,
                                                 "wallet"));
        }
        temp_string.append(").then(function(port) {\n" +
                    "    nativePort = port;\n" +
                    "    port.addMessageListener(function(message) {\n" +
                    "      if (message['@context'] != '" + BaseProperties.SATURN_WEB_PAY_CONTEXT_URI + "') {\n" +
                    "        setFail('Wrong or missing \"@context\"');\n" +
                    "        return;\n" +
                    "      }\n" +
                    "      var qualifier = message['@qualifier'];\n" +
                    "      if ((initMode && qualifier != '" + Messages.PAYMENT_CLIENT_IS_READY.toString() + "')  ||\n" +
                    "          (!initMode && qualifier != '" +  Messages.PAYER_AUTHORIZATION.toString() + "')) {\n" +  
                    "        setFail('Wrong or missing \"@qualifier\"');\n" +
                    "        return;\n" +
                    "      }\n" +
                    "      if (initMode) {\n");
       if (debugMode) {
           temp_string.append(
                    "        console.debug(JSON.stringify(message));\n");
       }
       if (!tapConnectMode) {
           temp_string.append(
                    "        document.getElementById('wallet').style.height = message." + 
                                         BaseProperties.WINDOW_JSON + "." + BaseProperties.HEIGHT_JSON + " + 'px';\n");
       }
       temp_string.append(
                    "        initMode = false;\n" +
                    "        nativePort.postMessage(invocationData);\n" +
                    "      } else {\n" +
                    "// This is it...transfer the Wallet authorization data back to the Merchant server\n" +
                    "        fetch('authorize', {\n" +
                    "           headers: {\n" +
                    "             'Content-Type': 'application/json'\n" +
                    "           },\n" +
                    "           method: 'POST',\n" +
                    "           credentials: 'same-origin',\n" +
                    "           body: JSON.stringify(message)\n" +
                    "        }).then(function (response) {\n" +
                    "          return response.json();\n" +
                    "        }).then(function (resultData) {\n" +
                    "          if (typeof resultData == 'object' && !Array.isArray(resultData)) {\n" +
                    "            if (resultData['@qualifier'] == '" + Messages.PAYMENT_CLIENT_SUCCESS.toString() + "') {\n" +
                    "// \"Normal\" return\n" +
                    "              failureFlag = false;\n" +
                    "              document.location.href='result';\n" +
                    "            } else {\n" +
                    "// \"Exceptional\" return with error or RBA\n" +
                    "              nativePort.postMessage(resultData);\n" +
                    "            }\n" +
                    "          } else {\n" +
                    "            setFail('Unexpected wallet return data');\n" +
                    "          }\n" +
                    "        }).catch (function (error) {\n" +
                    "          console.log('Request failed', error);\n" +
                    "        });\n" +                           
                    "      }\n"+
                    "    });\n");
       if (tapConnectMode) {
           temp_string.append(
                   "    port.addConnectionListener(function(initialize) {\n" +
                   "      if (initialize) {\n" +
                   "        document.getElementById('state').src = 'images/loading-gears-animation-3.gif';\n" +
                   "      } else {\n" +
                   "        if (initMode) console.debug('Wallet prematurely closed!');\n" +
                   "        nativePort = null;\n" +
                   "        document.forms.restore.submit();\n" +
                   "      }\n" +
                   "    });\n");
       } else {
           temp_string.append(
                    "    port.addDisconnectListener(function() {\n" +
                    "      if (initMode) alert('Wallet application \"" + 
                                     MerchantService.w2nbWalletName + ".jar\" appears to be missing!');\n" +
                    "      nativePort = null;\n" +
                    "// User cancel\n" +
                    "      if (failureFlag) document.forms.restore.submit();\n" +
                    "    });\n");
       }
       temp_string.append(
                    "  }, function(err) {\n" +
                    "    console.debug(err);\n" +
                    "  });\n");
       if (firefox) {
           temp_string.append("}, 10);\n");
       }
       temp_string.append("}\n\n");

       if (!tapConnectMode) {
           temp_string.append(ExtensionPositioning.SET_EXTENSION_POSITION_FUNCTION_TEXT + "\n");
       }

       temp_string.append(
                    "window.addEventListener('beforeunload', function(event) {\n" +
                    "  closeWallet();\n" +
                    "});\n\n");
        HTML.output(response, HTML.getHTML(temp_string.toString(),
                              "onload=\"activateWallet()\"",
                              s.toString()));
    }

    static void shopResultPage(HttpServletResponse response,
                               boolean debugMode,
                               ResultData resultData) throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<tr><td width=\"100%\" align=\"center\" valign=\"middle\">")
            .append("<table>" +
                    "<tr><td style=\"text-align:center;font-weight:bolder;font-size:10pt;font-family:" + FONT_ARIAL)
            .append(resultData.transactionError == null ?
                      "\">Order Status" : ";color:red\">Failed = " + resultData.transactionError.toString())
            .append("<br>&nbsp;</td></tr>")
            .append(resultData.transactionError == null ?
                    "<tr><td style=\"text-align:center\">" +
                    "Dear customer, your order has been successfully processed!<br>&nbsp;</td></tr>" : "")
            .append(receiptCore(resultData, debugMode && resultData.optionalRefund == null))
            .append(optionalRefund(resultData))
            .append("</table></td></tr></table></td></tr>");
        HTML.output(response, HTML.getHTML(STICK_TO_HOME_URL, null, s.toString()));
    }

    static String updatePumpDisplay(FuelTypes fuelType) {
        return "function setDigits(prefix, value) {\n" +
               "  var q = 0;\n" +
               "  while(value) {\n" +
               "    var digit = value % 10;\n" +
               "    document.getElementById(prefix + (q++)).innerHTML = digit;\n" +
               "    value = (value - digit) / 10;\n" +
               "  }\n" +
               "}\n" +
               "function updatePumpDisplay(decilitres) {\n" +
               "  var priceX1000 = " + fuelType.pricePerLitreX100 + " * decilitres;\n" +
               "  var roundup = priceX1000 % " + GasStationServlet.ROUND_UP_FACTOR_X_10 + ";\n" +
               "  if (roundup) priceX1000 += " + GasStationServlet.ROUND_UP_FACTOR_X_10 + " - roundup;\n" +
               "  setDigits('pvol', decilitres);\n" +
               "  setDigits('ppri', priceX1000 / 10);\n" +
               "}\n";
    }
    static void gasStationResultPage(HttpServletResponse response,
                                     FuelTypes fuelType,
                                     int decilitres,
                                     boolean debugMode,
                                     ResultData resultData) throws IOException, ServletException {
        if (resultData.transactionError == null) {
            StringBuilder s = new StringBuilder()
                .append(gasStation("Thank You - Welcome Back!", true))
                .append(selectionButtons(new FuelTypes[]{fuelType}))
                .append("<tr><td style=\"height:15pt\"></td></tr>")
                .append(receiptCore(resultData, debugMode && resultData.optionalRefund == null))
                .append(optionalRefund(resultData))
                .append("</table></td></tr>");
            HTML.output(response, 
                        HTML.getHTML(STICK_TO_HOME_URL + updatePumpDisplay(fuelType),
                                     "onload=\"updatePumpDisplay(" + decilitres + ")\">" +
                                     GAS_PUMP_LOGO,
                                     s.toString()));
        } else {
            shopResultPage(response, debugMode, resultData);
        }
    }

    static String optionalRefund(ResultData resultData) {
        return resultData.optionalRefund == null ? "" :
            "<tr><td style=\"text-align:center;padding-top:20pt\">" +
            fancyButton("Please refund this transaction...", "Payback time has come...", "location.href='refund'") +
            "</td></tr>";
    }

    static void debugPage(HttpServletResponse response, String string, boolean clean) throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<tr><td width=\"100%\" align=\"center\" valign=\"middle\">" + 
                  "<table>" +
                  "<tr><td style=\"padding-top:50pt;text-align:center;font-weight:bolder;font-size:10pt;font-family:" + FONT_ARIAL +
                  "\">Payment Session Debug Information&nbsp;<br></td></tr><tr><td style=\"text-align:left\">")
          .append(string)
          .append("</td></tr></table></td></tr>");
        HTML.output(response, HTML.getHTML(clean ? null : STICK_TO_HOME_URL, null,s.toString()));
    }

    static void errorPage(HttpServletResponse response, String error, boolean system)
                                 throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<tr><td width=\"100%\" align=\"center\" valign=\"middle\">" + 
                 "<table>" +
                 "<tr><td style=\"text-align:center;font-weight:bolder;font-size:10pt;font-family:" + FONT_ARIAL +
                 "\">")
         .append(system ? "System " : "")
         .append("Failure&nbsp;<br></td></tr><tr><td style=\"text-align:center\">")
         .append(HTMLEncoder.encodeWithLineBreaks(error.getBytes("UTF-8")))
         .append("</td></tr></table></td></tr>");
        HTML.output(response, HTML.getHTML(STICK_TO_HOME_URL, null,s.toString()));
    }

    static void notification(HttpServletResponse response, String notification)
            throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<tr><td width=\"100%\" align=\"center\" valign=\"middle\">" + 
            "<table>" +
            "<tr><td style=\"font-size:10pt;font-family:" + FONT_ARIAL +
            "\">")
        .append(notification)
        .append("</td></tr></table></td></tr>");
        HTML.output(response, HTML.getHTML(STICK_TO_HOME_URL, null,s.toString()));
    }
    
    static StringBuilder fancyButton(String value, String title, String onclick) {
        return new StringBuilder("<div class=\"stdbtn\" id=\"cmd\" title=\"")
            .append(title)
            .append("\" onclick=\"")
            .append(onclick)
            .append("\">")
            .append(value)
            .append("</div>");
     }

    static void userChoosePage(HttpServletResponse response,
                               SavedShoppingCart savedShoppingCart,
                               boolean android) throws IOException, ServletException {
        StringBuilder s = currentOrder(savedShoppingCart)
            .append("<tr><td style=\"padding-top:15pt\"><table style=\"margin-left:auto;margin-right:auto\">" +
                    "<tr><td style=\"padding-bottom:10pt;text-align:center;font-weight:bolder;font-size:10pt;font-family:" +
                    FONT_ARIAL + "\">Select Payment Method</td></tr>")
            .append(MerchantService.desktopWallet || android ?
                      "<tr><td><img title=\"Saturn\" style=\"cursor:pointer\" src=\"images/paywith-saturn.png\" onclick=\"document.forms.shoot.submit()\"></td></tr>" 
                                                                :
                      "")
            .append(MerchantService.desktopWallet || !android ?
                      "<tr><td style=\"padding-top:10pt\"><img title=\"Saturn QR\" style=\"cursor:pointer\" src=\"images/paywith-saturn" +
                            (MerchantService.desktopWallet ? "qr" : "") +
                      ".png\" onclick=\"document.location.href='qrdisplay'\"></td></tr>"
                                                               :
                      "")
            .append("<tr><td style=\"padding: 10pt 0 10pt 0\"><img title=\"VISA &amp; MasterCard\" style=\"cursor:pointer\" src=\"images/paywith-visa-mc.png\" onclick=\"noSuchMethod(this)\"></td></tr>" +
                    "<tr><td><img title=\"PayPal\" style=\"cursor:pointer\" src=\"images/paywith-paypal.png\" onclick=\"noSuchMethod(this)\"></td></tr>" +
                    "<tr><td style=\"text-align:center;padding:15pt\">")
            .append(fancyButton("Return to shop..", "Changed your mind?", "document.forms.restore.submit()"))
            .append("</td></tr></table></td></tr></table></td></tr>");

        HTML.output(response, HTML.getHTML(
                STICK_TO_HOME_URL +
                "\nfunction noSuchMethod(element) {\n" +
                        "    document.getElementById('notimplemented').style.top = (element.getBoundingClientRect().top + window.scrollY - document.getElementById('notimplemented').offsetHeight * 1.5) + 'px';\n" +
                        "    document.getElementById('notimplemented').style.left = ((window.innerWidth - document.getElementById('notimplemented').offsetWidth) / 2) + 'px';\n" +
                        "    document.getElementById('notimplemented').style.visibility = 'visible';\n" +
                        "    setTimeout(function() {\n" +
                        "        document.getElementById('notimplemented').style.visibility = 'hidden';\n" +
                        "    }, 1000);\n" +
                        "}\n\n",
                "><form name=\"shoot\" method=\"POST\" action=\"" + 
                (android ? "androidplugin" : "w2nbwallet") +
                "\"></form><form name=\"restore\" method=\"POST\" action=\"shop\">" +
                "</form><div id=\"notimplemented\" class=\"toasting\">This demo only supports Saturn!</div",
                s.toString()));
    }
    
    static StringBuilder pumpDigit(int digit, String prefix) {
        return new StringBuilder("<td><div style=\"background-color:white;padding:0pt 3pt 1pt 3pt;font-size:14pt;border-radius:2pt\" id=\"")
            .append(prefix)
            .append(digit)
            .append("\">0</div></td>");
    }
    
    static StringBuilder pumpDisplay(int digits, int decimals, String leader, String trailer, String prefix) {
        StringBuilder s = new StringBuilder("<table cellspacing=\"5\"><tr><td style=\"color:white\">")
            .append(leader)
            .append("</td>");
        while (digits-- > 0) {
            s.append(pumpDigit(digits + decimals, prefix));
        }
        s.append("<td style=\"color:white\">&#x2022;</td>");
        while (decimals-- > 0) {
            s.append(pumpDigit(decimals, prefix));
        }
        return s.append("<td style=\"color:white\">")
                .append(trailer)
                .append("</td></tr></table>");
    }
    
    static String gasStation(String header, boolean visiblePumpDisplay) {
        StringBuilder s = new StringBuilder("<tr><td width=\"100%\" align=\"center\" valign=\"middle\"><table>" +
          "<tr><td id=\"phase\" style=\"padding-bottom:10pt;text-align:center;" +
                "font-weight:bolder;font-size:11pt;font-family:" +
                FONT_ARIAL + "\">")
            .append(header)
            .append("</td></tr>");
        if (visiblePumpDisplay) {
            s.append("<tr><td style=\"padding-bottom:15pt\" align=\"center\"><table title=\"This is [sort of] a pump display\"><tr>"+
                     "<td align=\"center\" style=\"box-shadow:5pt 5pt 5pt #c0c0c0;background:linear-gradient(135deg, #516287 0%,#5697e2 71%,#5697e2 71%,#516287 100%);border-radius:4pt\">" +
                     "<div style=\"padding:3pt;font-size:12pt;color:white\">Pump O'Matic</div>")
             .append(pumpDisplay(3, 1, "Volume", "Litres", "pvol"))
             .append(pumpDisplay(3, 2, "To Pay", "&#8364;", "ppri"))
             .append("</td></tr></table></td></tr>");
        }
        return s.toString();

    }

    static String cometJavaScriptSupport(String id, 
                                         HttpServletRequest request,
                                         String returnAction,
                                         String progressAction,
                                         String successAction) {
        return new StringBuilder(
            "\"use strict\";\n" +
            STICK_TO_HOME_URL +
            "function setQRDisplay(turnOn) {" +
            "  var displayArg = turnOn ? 'table-row' : 'none';\n" +
            "  document.getElementById('qr1').style.display = displayArg;\n" +
            "  document.getElementById('qr2').style.display = displayArg;\n" +
            "  document.getElementById('waiting').style.display = turnOn ? 'none' : 'table-row';\n" +
            "}\n" +
            "function flashQRInfo() {\n" +
            "  document.getElementById('qridflasher').style.top = ((window.innerHeight - document.getElementById('qridflasher').offsetHeight) / 2) + 'px';\n" +
            "  document.getElementById('qridflasher').style.left = ((window.innerWidth - document.getElementById('qridflasher').offsetWidth) / 2) + 'px';\n" +
            "  document.getElementById('qridflasher').style.visibility = 'visible';\n" +
            "  setTimeout(function() {\n" +
            "    document.getElementById('qridflasher').style.visibility = 'hidden';\n" +
            "  }, 2000);\n" +
            "}\n\n" +
            "function startComet() {\n" +
            "  fetch('")
        .append(request.getRequestURL().toString())
        .append(
            "', {\n" +
            "     headers: {\n" +
            "       'Content-Type': 'text/plain'\n" +
            "     },\n" +
            "     method: 'POST',\n" +
            "     body: '" + id + "'\n" +
            "  }).then(function (response) {\n" +
            "    return response.text();\n" +
            "  }).then(function (resultData) {\n" +
            "    console.log('Response', resultData);\n" +
            "    switch (resultData) {\n" +
            "      case '" + QRSessions.QR_PROGRESS + "':\n")
        .append(progressAction)
        .append("      document.getElementById('authhelp').style.display = 'table-row';\n")
        .append(
            "        setQRDisplay(false);\n" +
            "      case '" + QRSessions.QR_CONTINUE + "':\n" +
            "        startComet();\n" +
            "        break;\n" +
            "      case '" + QRSessions.QR_RETURN + "':\n" +
            "        ")
        .append(returnAction)
        .append(";\n" +
            "        break;\n" +
            "      default:\n" +
            "        ")
        .append(successAction)
        .append(";\n" +
            "    }\n" +
            "  }).catch (function(error) {\n" +
            "    console.log('Request failed', error);\n" +
            "  });\n" +                           
            "}\n").toString();
    }
    
    static String bodyStartQR(String optional) {
        return "onload=\"startComet()\">" + optional + 
               "<div id=\"qridflasher\" class=\"toasting\">" +
               "You get it automatically when you install the<br>&quot;WebPKI&nbsp;Suite&quot;, just look for the icon!</div";       
    }
    
    static StringBuilder bodyEndQR(byte[] qrImage, boolean qrInitOn) {
        String display = qrInitOn ? "" : " style=\"display:none\"";
        return new StringBuilder()
            .append(
                "<tr")
            .append(display)
            .append(
                " id=\"qr1\"><td style=\"padding-top:10pt\" align=\"left\">Now use the QR ID&trade; <a href=\"javascript:flashQRInfo()\">" +
                "<img border=\"1\" src=\"images/qr_launcher.png\"></a> application to start the Wallet</td></tr>" +
                "<tr")
             .append(display)
             .append(" id=\"qr2\"><td align=\"center\"><img title=\"Do NOT put the cursor here because then the QR reader won't work\" src=\"data:image/png;base64,")
             .append(new Base64(false).getBase64StringFromBinary(qrImage))
             .append("\"></td></tr>"+
                     "<tr id=\"authhelp\" style=\"display:none\"><td style=\"padding-top:15pt;text-align:center\">"+
                     "Waiting for you to <i>authorize</i> the transaction in the mobile device...</td></tr>" +    
                     "<tr id=\"waiting\" style=\"display:none\"><td style=\"padding-top:10pt;text-align:center\">"+
                     "<img title=\"Something is running...\" src=\"images/waiting.gif\"></td></tr></table></td></tr>");     
    }

    static void printQRCode4Shop(HttpServletResponse response,
                                 SavedShoppingCart savedShoppingCart,
                                 byte[] qrImage,
                                 HttpServletRequest request,
                                 String id) throws IOException, ServletException {
        HTML.output(response, HTML.getHTML(
            cometJavaScriptSupport(id, request, 
                                   "document.forms.restore.submit()",
                                   "",
                                   "document.location.href = 'result'"),
            bodyStartQR("<form name=\"restore\" method=\"POST\" action=\"shop\"></form>"),
            currentOrder(savedShoppingCart).toString() +
            bodyEndQR(qrImage, true)));
    }
    
    static String selectionButtons(FuelTypes[] fuelTypes) throws IOException {
        StringBuilder s = new StringBuilder("<tr><td><table cellpadding=\"0\" cellspacing=\"0\" align=\"center\">" +
                                          "<tr style=\"text-align:center\"><td>Fuel Type</td><td>Price/Litre</td></tr>");
        for (FuelTypes fuelType : fuelTypes) {
            s.append("<tr id=\"")
             .append(fuelType.toString())
             .append(".\"><td colspan=\"2\" style=\"height:6pt\"></td></tr><tr title=\"Selected fuel type\" id=\"")
             .append(fuelType.toString())
             .append("\" style=\"box-shadow:3pt 3pt 3pt #D0D0D0;text-align:center;background:")
             .append(fuelType.background);
            if (fuelTypes.length > 1) {
                s.append(";cursor:pointer\" onclick=\"selectFuel('")
                 .append(fuelType.toString())
                 .append("', this)");
            }
            s.append("\"><td style=\"font-size:11pt;font-family:" + FONT_ARIAL + ";padding:6pt;min-width:10em\">")
             .append(fuelType.commonName)
             .append("</td><td style=\"font-size:11pt;font-family:" + FONT_ARIAL + ";padding:6pt 12pt 6pt 12pt\">")
             .append(fuelType.displayPrice())
             .append("</td></tr>");
        }
        return s.append("</table></td></tr>").toString();
    }

    static void printQRCode4GasStation(HttpServletResponse response,
                                       byte[] qrImage,
                                       HttpServletRequest request,
                                       String id) throws IOException, ServletException {
        StringBuilder s = new StringBuilder("function selectFuel(fuelType, element) {\n");
        for (FuelTypes fuelType : FuelTypes.values()) {
            s.append("  if (fuelType == '")
             .append(fuelType.toString())
             .append("') {\n" +
             "    document.getElementById('" + GasStationServlet.FUEL_TYPE_FIELD + "').value = fuelType;\n" +
             "    element.style.cursor = 'default';\n" +
             "   } else {\n" +
             "    document.getElementById('")
             .append(fuelType.toString())
             .append("').style.display = 'none';\n" +
             "    document.getElementById('")
            .append(fuelType.toString())
            .append(".').style.display = 'none';\n" +
            "}\n");
        }
        HTML.output(response, HTML.getHTML(
            cometJavaScriptSupport(id, request, 
                                  "document.location.href='home'",
                                  "      document.getElementById('phase').innerHTML = '3. User Authorization';\n" +
                                  "      document.getElementById('authtext').style.display = 'table-row';\n",
                                  "document.forms.fillgas.submit()") +
            s.append(
            "  setQRDisplay(true);\n" +
            "  document.getElementById('phase').innerHTML = '2. Initiate Payment';\n" +
            "}\n").toString(),
            bodyStartQR("<form name=\"fillgas\" method=\"POST\" action=\"gasstation\">" +
                        "<input name=\"" + GasStationServlet.FUEL_TYPE_FIELD + "\" " +
                        "id=\"" + GasStationServlet.FUEL_TYPE_FIELD + "\" type=\"hidden\"></form>" +
                        GAS_PUMP_LOGO + ">"),
            gasStation("1. Select Fuel Type", false) +
            "<tr id=\"authtext\" style=\"display:none\">" +
            "<td style=\"width:40em;padding-bottom:15pt\">Since the quantity of fuel is usually not known in an advance, " +
            "automated fueling stations require <i>pre-authorization</i> of a fixed maximum amount of money (" +
            Currencies.EUR.amountToDisplayString(new BigDecimal(GasStationServlet.STANDARD_RESERVATION_AMOUNT_X_100 / 100), true) +
            " in the demo), while only the actual amount " +
            "needed is eventually withdrawn from the client's account. This should be reflected in the Wallet's " +
            "authorization display as well.</td></tr>" +
            selectionButtons(FuelTypes.values()) +
            bodyEndQR(qrImage, false)));
    }

    static void androidPluginActivate(HttpServletResponse response, String url) throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setHeader("Pragma", "No-Cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        response.setDateHeader("EXPIRES", 0);
        response.getOutputStream().write(
            HTML.getHTML(null,
                         "onload=\"document.location.href='" + url + "'\"" ,
                         "<tr><td width=\"100%\" align=\"center\" valign=\"middle\">" +
                         "<b>Please wait while the Wallet plugin starts...</b></td></tr>").getBytes("UTF-8"));
    }

    static void autoPost(HttpServletResponse response, String url) throws IOException, ServletException {
        HTML.output(response, "<html><body onload=\"document.forms.posting.submit()\">Redirecting..." +
                              "<form name=\"posting\" action=\"" + url + "\" method=\"POST\"></form></body></html>");
    }

    static void androidPage(HttpServletResponse response) throws IOException, ServletException {
        HTML.output(response, HTML.getHTML(null, null,
            "<tr><td width=\"100%\" align=\"center\" valign=\"middle\">" +
            "<table class=\"tighttable\" style=\"max-width:600px;\" cellpadding=\"4\">" +
            "<tr><td style=\"text-align:center;font-weight:bolder;font-size:10pt;font-family:" + 
            FONT_ARIAL + "\">Android Wallet<br>&nbsp;</td></tr>" +
            "<tr><td style=\"text-align:left\">" +
              "Note: The Android Wallet is a <i>proof-of-concept implementation</i> rather than a product.</td></tr>" +
            "<tr><td>Installation: <a href=\"https://play.google.com/store/apps/details?id=org.webpki.mobile.android\">" +
              "https://play.google.com/store/apps/details?id=org.webpki.mobile.android</a></td></tr>" +
            "<tr><td>Enroll payment <i>test credentials</i> " +
              "by surfing (with the Android device...) to: <a href=\"https://mobilepki.org/webpay-keyprovider\">" +
              "https://mobilepki.org/webpay-keyprovider</td></tr>" +
            (MerchantService.desktopWallet ?
              "<tr><td>Unlike the desktop (Windows, Linux, and OS/X-based) Wallet, " +
              "the Android version also supports remote operation using QR codes.  This mode is " +
              "indicated by the following image in the Merchant Web application:</td></tr>" +
              "<tr><td align=\"center\"><img src=\"images/paywith-saturnqr.png\"></td></tr>" : "") +
            "</table></td></tr>"));       
    }

    static void gasFillingPage(HttpServletResponse response, FuelTypes fuelType, int maxVolume) throws IOException, ServletException {
        HTML.output(response, HTML.getHTML(
            STICK_TO_HOME_URL +
            updatePumpDisplay(fuelType) +
            "var decilitres = 0;\n" +
            "var timer = null;\n" +
            "function finishPumping() {\n" +
            "  document.getElementById('waiting').style.display = 'table-row';\n" +
            "  clearInterval(timer);\n" +
            "  document.getElementById('pumpbtn').innerHTML = 'Please wait while we are finalizing the payment...';\n" +
            "  setTimeout(function() {\n" +
            "    doneFilling();\n" +
            "  }, 1000);\n" +
            "}\n" +
            "function execute() {\n" +
            "  if (timer) {\n"+
            "    finishPumping();\n" +
            "  } else {\n" +
            "    timer = setInterval(function () {\n" +
            "      updatePumpDisplay(++decilitres);\n" +
            "      if (decilitres == " + maxVolume + ") {\n" +
            "        finishPumping();\n" +
            "      }\n" +
            "    }, 100);\n" +
            "    document.getElementById('waiting').style.display = 'table-row';\n" +
            "    setTimeout(function() {\n" +
            "      document.getElementById('cmd').innerHTML = 'Click here to <i style=\"color:red;font-weight:bolder\">stop</i> pumping...';\n" +
            "      document.getElementById('waiting').style.display = 'none';\n" +
            "    }, 1000);\n" +
            "  }\n" +
            "}\n" +
            "function doneFilling() {\n" +
            "  document.getElementById('" + GasStationServlet.FUEL_TYPE_FIELD + "').value = '" + fuelType.toString() + "';\n" +
            "  document.getElementById('" + GasStationServlet.FUEL_DECILITRE_FIELD + "').value = decilitres;\n" +
            "  document.forms.finish.submit();\n" +
            "}\n", 
            ">" + GAS_PUMP_LOGO + "><form name=\"finish\" action=\"result\" method=\"POST\">" +
            "<input name=\"" + GasStationServlet.FUEL_TYPE_FIELD + "\" " +
            "id=\"" + GasStationServlet.FUEL_TYPE_FIELD + "\" type=\"hidden\">" +
            "<input name=\"" + GasStationServlet.FUEL_DECILITRE_FIELD + "\" " +
            "id=\"" + GasStationServlet.FUEL_DECILITRE_FIELD + "\" type=\"hidden\">" +
            "</form",
            gasStation("4. Fill Tank", true) +
            selectionButtons(new FuelTypes[]{fuelType}) +
            "<tr><td style=\"padding-top:20pt;text-align:center\" id=\"pumpbtn\">" +
            fancyButton("Click here to <i style=\"color:green;font-weight:bolder\">start</i> pumping!",
                        "Since we don't have a real pump we &quot;simulate&quot; one",
                        "execute()") +
            "</td></tr>" +
            "<tr id=\"waiting\" style=\"display:none\"><td style=\"padding-top:10pt\" align=\"center\">" +
            "<img title=\"Something is running...\" src=\"images/waiting.gif\"></td></tr>" +
            "</table></td></tr>"));
    }

    static void refundResultPage(HttpServletResponse response, boolean debugMode, ResultData resultData) throws IOException, ServletException {
        StringBuilder s = new StringBuilder("<tr><td width=\"100%\" align=\"center\" valign=\"middle\">")
            .append("<table>" +
                "<tr><td style=\"text-align:center;font-weight:bolder;font-size:10pt;font-family:" + FONT_ARIAL +
                "\">Refund Result<br>&nbsp;</td></tr>" +
                 "<tr><td style=\"text-align:center;padding-bottom:15pt;font-size:10pt\">" +
                "Dear customer, your payment has been refunded!</td></tr>")
            .append(receiptCore(resultData, debugMode))
            .append("</table></td></tr></table></td></tr>");
        HTML.output(response, HTML.getHTML(STICK_TO_HOME_URL, null, s.toString()));
    }
    */
}

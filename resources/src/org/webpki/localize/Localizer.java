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
package org.webpki.localize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.reflect.Field;

import java.util.Properties;

import org.webpki.localized.LocalizedStrings;

import org.webpki.util.ArrayUtil;

public class Localizer {

    public static void main(String[] args) {
        // 0 pathToLocalizedApplication
        // 1 pathToPropertyFile
        Properties properties = new Properties();
        StringBuilder java = new StringBuilder("package org.webpki.localized;\n" +
        "// Auto localized...\n" +
        "public interface LocalizedStrings {\n");
        try {
            properties.load(new InputStreamReader(new FileInputStream(args[1])));
            for (Field field : LocalizedStrings.class.getDeclaredFields()) {
                String property = field.getName();
                String value = properties.getProperty(property);
                if (value == null) {
                    throw new IOException("Missing property: " + property);
                }
                properties.remove(property);
                java.append("    String ")
                    .append(property);
                for (int q = property.length(); q < 30; q++) {
                    java.append(' ');
                }
                java.append(" = \"")
                    .append(value)
                    .append("\";\n");
            }
            for (String key : properties.stringPropertyNames()) {
                throw new IOException("Unexpected property: " + key);
            }
            ArrayUtil.writeFile(args[0] + 
                                File.separatorChar +
                                "localized" +
                                File.separatorChar +
                                "org" +
                                File.separatorChar +
                                "webpki" +
                                File.separatorChar +
                                "localized" +
                                File.separatorChar +
                                "LocalizedStrings.java",
                                java.append("}\n").toString().getBytes("utf-8"));
            System.out.println(args[0]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(3);
        }
    }
 }

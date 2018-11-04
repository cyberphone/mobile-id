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

import java.util.Vector;

// Known Mobile ID platforms

public enum TargetPlatforms {

    DESKTOP_MODE (null, true, null, null),

    ANDROID ("Android", true, "google-play-badge.png", 
             "https://play.google.com/store/apps/details?id=org.webpki.mobile.android"),

    IPAD    ("iPad",    false, null, null),
    IPHONE  ("iPhone",  false, "iphone-not-ready.png", "https://apple.com");
    
    String name;
    boolean supported;
    String logotype;
    String url;
    
    TargetPlatforms(String name, boolean supported, String logotype, String url) {
        this.name = name;
        this.supported = supported;
        this.logotype = logotype;
        this.url = url;
    }

    static TargetPlatforms[] getSupportedMobilePlatforms() {
        Vector<TargetPlatforms> supportedPlatforms = new Vector<TargetPlatforms>();
        for (TargetPlatforms platform : TargetPlatforms.values()) {
            if (platform.supported && platform != TargetPlatforms.DESKTOP_MODE) {
                supportedPlatforms.add(platform);
            }
        }
        return supportedPlatforms.toArray(new TargetPlatforms[0]);
    }
}

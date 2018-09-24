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

class Synchronizer {
	boolean touched;

	synchronized boolean perform(int timeout) {
		boolean timeout_flag = false;
		while (!touched && !timeout_flag) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				return false;
			}
			timeout_flag = true;
		}
		return touched;
	}

	synchronized void haveData4You() {
		touched = true;
		notify();
	}
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib;

import java.io.File;

public class AppUtil {

	private static final String USER_HOME;

	private static String appName;

	static {

		USER_HOME = System.getProperty("user.home");

	}

	public static void init(String appName) {
		AppUtil.appName = appName;
	}

	public static File getUserHome() {
		checkInit();
		return USER_HOME != null ? new File(USER_HOME) : null;
	}

	public static File getAppHome() {
		checkInit();
		if (USER_HOME == null) {
			return null;
		}

		File home = new File(USER_HOME + "/." + appName);
		home.mkdirs();

		return home;
	}

	public static File getAppHome(String subdir) {
		checkInit();
		if (USER_HOME == null) {
			return null;
		}

		File home = new File(getAppHome().getPath() + "/" + subdir);
		home.mkdirs();

		return home;
	}

	private static void checkInit() {
		if (appName == null) {
			throw new IllegalStateException("Must call init() on AppUtil");
		}
	}

	public static String getAppName() {
		return appName;
	}
}

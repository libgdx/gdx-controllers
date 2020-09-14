/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.controllers.test.ios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosrobovm.IOSGraphics;
import com.badlogic.gdx.controllers.IosControllerManager;
import com.badlogic.gdx.controllers.test.ControllersTest;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.Selector;

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication () {
		IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
		ControllersTest testApp = new ControllersTest() {
			@Override
			public void create() {
				UIViewController uiViewController = ((IOSApplication) Gdx.app).getUIViewController();
				IosControllerManager.enableICade(uiViewController, Selector.register("keyPress:"));
				super.create();
			}
		};
		return new IOSApplication(testApp, configuration) {
			@Override
			protected IOSGraphics.IOSUIViewController createUIViewController(IOSGraphics graphics) {
				return new MyUIViewController(this, graphics);
			}
		};
	}

	public static void main (String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSLauncher.class);
		pool.close();
	}
}

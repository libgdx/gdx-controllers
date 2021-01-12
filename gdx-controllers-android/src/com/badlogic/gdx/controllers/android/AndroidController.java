/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.controllers.android;

import android.view.InputDevice;
import android.view.InputDevice.MotionRange;
import android.view.MotionEvent;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;

import java.util.UUID;

public class AndroidController implements Controller {
	private final int deviceId;
	private boolean attached;
	private final String name;
	protected final IntIntMap buttons = new IntIntMap();
	protected final float[] axes;
	protected final int[] axesIds;
	protected float povX = 0f;
	protected float povY = 0f;
	private boolean povAxis;
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	private String uuid;
	public boolean connected;

	public AndroidController(int deviceId, String name) {
		this.deviceId = deviceId;
		this.name = name;
		this.uuid = UUID.randomUUID().toString();
		this.connected = true;

		InputDevice device = InputDevice.getDevice(deviceId);
		int numAxes = 0;
		for (MotionRange range : device.getMotionRanges()) {
			if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
				if (range.getAxis() != MotionEvent.AXIS_HAT_X && range.getAxis() != MotionEvent.AXIS_HAT_Y) {
					numAxes += 1;
				} else {
					povAxis = true;
				}
			}
		}

		axesIds = new int[numAxes];
		axes = new float[numAxes];
		int i = 0;
		for (MotionRange range : device.getMotionRanges()) {
			if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
				if (range.getAxis() != MotionEvent.AXIS_HAT_X && range.getAxis() != MotionEvent.AXIS_HAT_Y) {
					axesIds[i++] = range.getAxis();
				}
			}
		}
	}

	public boolean isAttached () {
		return attached;
	}

	public boolean hasPovAxis() {
		return povAxis;
	}

	public void setAttached (boolean attached) {
		this.attached = attached;
	}

	public int getDeviceId () {
		return deviceId;
	}
	
	@Override
	public void addListener (ControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener (ControllerListener listener) {
		this.listeners.removeValue(listener, true);
	}
	
	public Array<ControllerListener> getListeners() {
		return this.listeners;
	}

	@Override
	public boolean getButton (int buttonIndex) {
		return buttons.containsKey(buttonIndex);
	}

	@Override
	public float getAxis (int axisIndex) {
		if(axisIndex < 0 || axisIndex >= axes.length) return 0;
		return axes[axisIndex];
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public boolean canVibrate() {
		return false;
	}

	@Override
	public boolean isVibrating() {
		return false;
	}

	@Override
	public void startVibration(int duration, float strength) {
		// not supported
	}

	@Override
	public void cancelVibration() {
		// not supported
	}

	@Override
	public String getUniqueId() {
		return uuid;
	}

	@Override
	public boolean supportsPlayerIndex() {
		return false;
	}

	@Override
	public int getPlayerIndex() {
		return Controller.PLAYER_IDX_UNSET;
	}

	@Override
	public void setPlayerIndex(int index) {

	}

	@Override
	public int getMinButtonIndex() {
		return 0;
	}

	@Override
	public int getMaxButtonIndex() {
		// see Android's KeyEvent class
		return 300;
	}

	@Override
	public int getAxisCount() {
		return axes.length;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public ControllerMapping getMapping() {
		return AndroidControllerMapping.getInstance();
	}

	@Override
	public ControllerPowerLevel getPowerLevel() {
		return ControllerPowerLevel.POWER_UNKNOWN;
	}
}
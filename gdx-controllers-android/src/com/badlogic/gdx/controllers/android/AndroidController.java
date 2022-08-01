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

import java.util.ArrayList;
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
	protected float lTrigger = 0f;
	protected float rTrigger = 0f;
	private boolean triggerAxis;
	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	private String uuid;
	public boolean connected;

	public AndroidController(int deviceId, String name) {
		this.deviceId = deviceId;
		this.name = name;
		this.uuid = UUID.randomUUID().toString();
		this.connected = true;

		InputDevice device = InputDevice.getDevice(deviceId);
		ArrayList<Integer> axesIDList = new ArrayList<>();
		for (MotionRange range : device.getMotionRanges()) {
			if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
				axesIDList.add(range.getAxis());
			}
		}

		//remove pov axis as it will be mapped to buttons
		if (axesIDList.contains(MotionEvent.AXIS_HAT_X) && axesIDList.contains(MotionEvent.AXIS_HAT_Y)){
			povAxis = true;
			axesIDList.remove((Integer)MotionEvent.AXIS_HAT_X);
			axesIDList.remove((Integer)MotionEvent.AXIS_HAT_Y);
		}

		if (AndroidControllers.useNewAxisLogic){
			//remove trigger axis as it will be mapped to buttons
			if (axesIDList.contains(MotionEvent.AXIS_LTRIGGER) && axesIDList.contains(MotionEvent.AXIS_RTRIGGER)){
				triggerAxis = true;
				axesIDList.remove((Integer)MotionEvent.AXIS_LTRIGGER);
				axesIDList.remove((Integer)MotionEvent.AXIS_RTRIGGER);
			}

			//move left and right sticks to indices 0-3, to match default controller mapping
			if (axesIDList.contains(MotionEvent.AXIS_X) && axesIDList.contains(MotionEvent.AXIS_Y)){
				axesIDList.remove((Integer)MotionEvent.AXIS_X);
				axesIDList.remove((Integer)MotionEvent.AXIS_Y);
				axesIDList.add(0, MotionEvent.AXIS_X);
				axesIDList.add(1, MotionEvent.AXIS_Y);
			}
			if (axesIDList.contains(MotionEvent.AXIS_Z) && axesIDList.contains(MotionEvent.AXIS_RZ)){
				axesIDList.remove((Integer)MotionEvent.AXIS_Z);
				axesIDList.remove((Integer)MotionEvent.AXIS_RZ);
				axesIDList.add(2, MotionEvent.AXIS_Z);
				axesIDList.add(3, MotionEvent.AXIS_RZ);
			}
		}

		axesIds = new int[axesIDList.size()];
		axes = new float[axesIDList.size()];

		for (int i = 0; i < axesIds.length; i++){
			axesIds[i] = axesIDList.get(i);
		}

	}

	public boolean isAttached () {
		return attached;
	}

	public boolean hasPovAxis() {
		return povAxis;
	}

	public boolean hasTriggerAxis(){
		return triggerAxis;
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
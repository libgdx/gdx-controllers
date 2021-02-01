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

package com.badlogic.gdx.controllers.gwt;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.controllers.gwt.support.Gamepad;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.TimeUtils;

public class GwtController implements Controller {

	private int index;
	private String name;
	private boolean standardMapping;
	protected final float[] axes;
	protected final IntFloatMap buttons = new IntFloatMap();
	boolean connected = true;

	private final Array<ControllerListener> listeners = new Array<ControllerListener>();
	private final int buttonCount;
	private long vibrationEndMs;

	public GwtController(int index, String name) {
		this.index = index;
		this.name = name;
		
		Gamepad gamepad = Gamepad.getGamepad(index);
		axes = new float[gamepad.getAxes().length()];
		buttonCount = gamepad.getButtons().length();
		standardMapping = gamepad.getMapping().equals("standard");
	}
	
	public int getIndex() {
		return index;
	}

	public boolean isStandardMapping() {return standardMapping; }

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean getButton(int buttonCode) {
		return buttons.get(buttonCode, 0) >= 0.5f;
	}

	public float getButtonValue(int buttonCode) {
		return buttons.get(buttonCode, 0);
	}
	
	@Override
	public float getAxis(int axisIndex) {
		if(axisIndex < 0 || axisIndex >= axes.length) return 0;
		return axes[axisIndex];
	}

	@Override
	public boolean canVibrate() {
		return Gamepad.getGamepad(index).canVibrate();
	}

	@Override
	public boolean isVibrating() {
		return canVibrate() && TimeUtils.millis() < vibrationEndMs;
	}

	@Override
	public void startVibration(int duration, float strength) {
		Gamepad.getGamepad(index).doVibrate(duration, strength);
		vibrationEndMs = TimeUtils.millis() + duration;
	}

	@Override
	public void cancelVibration() {
		startVibration(0, 0);
	}

	@Override
	public String getUniqueId() {
		// UUID is not available on GWT, but according to W3C, indices are not reassigned. So
		// it is somehow safe to use the index.
		return String.valueOf(index);
	}

	@Override
	public boolean supportsPlayerIndex() {
		return false;
	}

	@Override
	public int getPlayerIndex() {
		// not supported
		return PLAYER_IDX_UNSET;
	}

	@Override
	public void setPlayerIndex(int index) {
		// not supported
	}

	@Override
	public int getMinButtonIndex() {
		return 0;
	}

	@Override
	public int getMaxButtonIndex() {
		return buttonCount - 1;
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
		return WebMapping.getInstance();
	}

	@Override
	public ControllerPowerLevel getPowerLevel() {
		return ControllerPowerLevel.POWER_UNKNOWN;
	}

	@Override
	public void addListener(ControllerListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(ControllerListener listener) {
		this.listeners.removeValue(listener, true);
	}
	
	public Array<ControllerListener> getListeners() {
		return listeners;
	}
}
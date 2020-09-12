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

package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

/** Represents a connected controller. Provides methods to query the state of buttons, axes and more information on
 * the controller. Multiple {@link ControllerListener} instances can be registered with the Controller to receive events in case
 * the controller's state changes. Listeners will be invoked on the rendering thread.
 * 
 * @author Nathan Sweet */
public interface Controller {
	/**
	 * returned by {@link #getPlayerIndex()} if no player index was set or feature is not supported
	 */
	int PLAYER_IDX_UNSET = -1;

	/** @param buttonCode
	 * @return whether the button is pressed. */
	public boolean getButton (int buttonCode);

	/** @param axisCode
	 * @return the value of the axis, between -1 and 1 */
	public float getAxis (int axisCode);

	/** @return the device name */
	public String getName ();

	/**
	 * @return unique ID to recognize this controller if more than one of the same controller models are connected.
	 * Use this to map a controller to a player, but do not use it to save a button mapping.
	 */
	String getUniqueId();

	/**
	 * @return the minimum button index code that can be queried
	 */
	int getMinButtonIndex();

	/**
	 * @return the maximum button index code that can be queried
	 */
	int getMaxButtonIndex();

	/**
	 * @return number of axis of this controller. Axis indices start at 0, so the maximum axis index
	 * is one under this value.
	 */
	int getAxisCount();

	/**
	 * @return true when this Controller is still connected, false if it already disconnected
	 */
	boolean isConnected();

	/**
	 * @return whether the connected controller or the current controller implementation can rumble.
	 * Note that this is no guarantee that the connected controller itself can vibrate.
	 */
	boolean canVibrate();

	/**
	 * @return if the controller is currently rumbling
	 */
	boolean isVibrating();

	/**
	 * Starts vibrating this controller, if possible.
	 *
	 * @param duration duration, in milliseconds
	 * @param strength value between 0f and 1f
	 */
	void startVibration(int duration, float strength);

	/**
	 * Cancel any running vibration. May not be supported by implementations
	 */
	void cancelVibration();

	/**
	 * @return whether the connected controller (or the implementation) can return and set the current player index
	 */
	boolean supportsPlayerIndex();

	/**
	 * @return 0-based player index of this controller, or PLAYER_IDX_UNSET if none is set
	 */
	int getPlayerIndex();

	/**
	 * Sets the player index of this controller. Please note that this does not always set
	 * indication lights of controllers, it is just an internal representation on some platforms
	 *
	 * @param index 0 typically 0 to 3 for player indices, and PLAYER_IDX_UNSET for unset
	 */
	void setPlayerIndex(int index);

	/**
	 * @return button and axis mapping for this controller (or platform). The connected controller
	 * might not support all features.
	 */
	ControllerMapping getMapping();

	/** Adds a new {@link ControllerListener} to this {@link Controller}. The listener will receive calls in case the state of the
	 * controller changes. The listener will be invoked on the rendering thread.
	 * @param listener */
	public void addListener (ControllerListener listener);

	/** Removes the given {@link ControllerListener}
	 * @param listener */
	public void removeListener (ControllerListener listener);
}

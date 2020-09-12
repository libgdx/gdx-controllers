package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * AbstractController to be used by new Controller implementations. Provides listener notification
 * implementations and default return values
 */
abstract class AbstractController implements Disposable, Controller {
	private final Array<ControllerListener> listeners = new Array<>();
	private boolean connected = true;

	@Override
	public void dispose() {
		synchronized (listeners) {
			listeners.clear();
		}
		connected = false;
	}

	protected void notifyListenersButtonUp(int button) {
		Array<ControllerListener> managerListeners = Controllers.getListeners();
		synchronized (managerListeners) {
			for (ControllerListener listener : managerListeners) {
				if (listener.buttonUp(this, button))
					break;
			}
		}

		synchronized (this.listeners) {
			for (ControllerListener listener : this.listeners) {
				if (listener.buttonUp(this, button))
					break;
			}
		}
    }

	protected void notifyListenersButtonDown(int button) {
		Array<ControllerListener> managerListeners = Controllers.getListeners();
		synchronized (managerListeners) {
			for (ControllerListener listener : managerListeners) {
				if (listener.buttonDown(this, button))
					break;
			}
		}

		synchronized (listeners) {
			for (ControllerListener listener : listeners) {
				if (listener.buttonDown(this, button))
					break;
			}
		}
    }

	protected void notifyListenersAxisMoved(int axisNum, float value) {
		Array<ControllerListener> managerListeners = Controllers.getListeners();
		synchronized (managerListeners) {
			for (ControllerListener listener : managerListeners) {
				if (listener.axisMoved(this, axisNum, value))
					break;
			}
		}

		synchronized (listeners) {
			for (ControllerListener listener : listeners) {
				if (listener.axisMoved(this, axisNum, value))
					break;
			}
		}
	}

	@Override
	public void addListener(ControllerListener controllerListener) {
		synchronized (listeners) {
			if (!listeners.contains(controllerListener, true))
				listeners.add(controllerListener);
		}
	}

	@Override
	public void removeListener(ControllerListener controllerListener) {
		synchronized (listeners) {
			listeners.removeValue(controllerListener, true);
		}
	}

	// methods from advanced interface that are not supported by most controllers

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

	}

	@Override
	public void cancelVibration() {

	}

	@Override
	public boolean supportsPlayerIndex() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public int getPlayerIndex() {
		return Controller.PLAYER_IDX_UNSET;
	}

	@Override
	public void setPlayerIndex(int index) {

	}
}

package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

public class ICadeController extends AbstractController {

	protected static final String KEYS_AXIS = "aqdcwexz";
	protected static final String KEYS_BUTTONS = "hrufytjnimkplvog";
	public final static String KEYS_TO_HANDLE = KEYS_BUTTONS + KEYS_AXIS;
	private final boolean[] buttonPressed;

	public ICadeController() {
		buttonPressed = new boolean[KEYS_TO_HANDLE.length() / 2];
	}

	@Override
	public String getUniqueId() {
		return "icade";
	}

	@Override
	public int getMinButtonIndex() {
		return 0;
	}

	@Override
	public int getMaxButtonIndex() {
		return (KEYS_BUTTONS.length() / 2) - 1;
	}

	@Override
	public int getAxisCount() {
		return (KEYS_AXIS.length() / 4);
	}

	@Override
	public int getPovCount() {
		// not supported
		return 0;
	}

	@Override
	public boolean getButton(int i) {
		if (i <= getMaxButtonIndex())
			return buttonPressed[i];
		else
			return false;
	}

	@Override
	public float getAxis(int i) {
		if (i < getAxisCount()) {
			int offset = getMaxButtonIndex() + 1 + i * 2;

			if (buttonPressed[offset] && !buttonPressed[offset + 1])
				return -1;
			else if (!buttonPressed[offset] && buttonPressed[offset + 1])
				return 1;
			else return 0;
		}

		return 0;
	}

	@Override
	public PovDirection getPov(int i) {
		// not supported
		return PovDirection.center;
	}

	@Override
	public ControllerMapping getMapping() {
		return ICadeMapping.getInstance();
	}

	@Override
	public String getName() {
		return "iCade device";
	}

	public void handleKeyPressed(String keyPressed) {
		int index = KEYS_TO_HANDLE.indexOf(keyPressed);

		if (index >= 0) {
			int buttonNum = index / 2;
			boolean buttonDown = index % 2 == 0;

			if (buttonPressed[buttonNum] != buttonDown) {
				buttonPressed[buttonNum] = buttonDown;

				if (buttonNum <= getMaxButtonIndex()) {
					if (buttonDown)
						notifyListenersButtonDown(buttonNum);
					else
						notifyListenersButtonUp(buttonNum);
				} else {
					int axisNum = (buttonNum - getMaxButtonIndex() - 1) / 2;
					notifyListenersAxisMoved(axisNum, getAxis(axisNum));
				}
			}
		}
	}
}

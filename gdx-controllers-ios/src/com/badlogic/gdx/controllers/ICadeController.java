package com.badlogic.gdx.controllers;

import com.badlogic.gdx.math.Vector3;

public class ICadeController extends AbstractController {

	public final static String KEYS_TO_HANDLE = "wexzaqdchrufytjnimkplvog";
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
		return KEYS_TO_HANDLE.length() - 1;
	}

	@Override
	public int getAxisCount() {
		// not supported
		return 0;
	}

	@Override
	public int getPovCount() {
		// not supported
		return 0;
	}

	@Override
	public boolean getButton(int i) {
		return buttonPressed[i];
	}

	@Override
	public float getAxis(int i) {
		// not supported
		return 0;
	}

	@Override
	public PovDirection getPov(int i) {
		// not supported
		return PovDirection.center;
	}

	@Override
	public boolean getSliderX(int i) {
		// not supported
		return false;
	}

	@Override
	public boolean getSliderY(int i) {
		// not supported
		return false;
	}

	@Override
	public Vector3 getAccelerometer(int i) {
		// not supported
		return Vector3.Zero;
	}

	@Override
	public void setAccelerometerSensitivity(float v) {
		// not supported

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

				if (buttonDown)
					notifyListenersButtonDown(buttonNum);
				else
					notifyListenersButtonUp(buttonNum);
			}
		}
	}
}

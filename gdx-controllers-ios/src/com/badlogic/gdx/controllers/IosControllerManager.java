package com.badlogic.gdx.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.gamecontroller.GCController;
import org.robovm.apple.uikit.UIKeyCommand;
import org.robovm.apple.uikit.UIKeyModifierFlags;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.Selector;
import org.robovm.objc.block.VoidBlock1;

public class IosControllerManager extends AbstractControllerManager {
	private final Array<ControllerListener> listeners = new Array<>();
	private boolean initialized = false;
	private ICadeController iCadeController;

	public IosControllerManager() {
	}

	public static void enableICade(UIViewController controller, Selector action) {
		for (int i = 0; i < ICadeController.KEYS_TO_HANDLE.length(); i++) {
			controller.addKeyCommand(new UIKeyCommand(Character.toString(ICadeController.KEYS_TO_HANDLE.charAt(i)),
					UIKeyModifierFlags.None, action));
		}

		controller.becomeFirstResponder();
		Gdx.app.log("Controllers", "iCade support activated");
	}

	public static void keyPress(UIKeyCommand sender) {
		//a key for ICadeController was pressed
		// instantiate it, if not already available
		IosControllerManager controllerManager = (IosControllerManager) Controllers.managers.get(Gdx.app);

		if (controllerManager != null)
			controllerManager.handleKeyPressed(sender);
	}

	private void handleKeyPressed(UIKeyCommand sender) {
		if (iCadeController == null) {
			Gdx.app.log("Controllers", "iCade key was pressed, adding iCade controller.");

			iCadeController = new ICadeController();
			controllers.add(iCadeController);

			synchronized (listeners) {
				for (ControllerListener listener : listeners)
					listener.connected(iCadeController);
			}
		}

		iCadeController.handleKeyPressed(sender.getInput());
	}

	protected boolean isSupportedController(GCController controller) {
		return controller.getExtendedGamepad() != null || controller.getGamepad() != null;
	}

	@Override
	public Array<Controller> getControllers() {
		initializeControllerArray();

		return super.getControllers();
	}

	private void initializeControllerArray() {
		if (!initialized && Foundation.getMajorSystemVersion() >= 7) {
			initialized = true;

			NSArray<GCController> controllers = GCController.getControllers();

			for (GCController controller : controllers) {
				if (isSupportedController(controller))
					this.controllers.add(new IosController(controller));
			}

			GCController.Notifications.observeDidConnect(new VoidBlock1<GCController>() {
				@Override
				public void invoke(GCController gcController) {
					onControllerConnect(gcController);
				}
			});

			GCController.Notifications.observeDidDisconnect(new VoidBlock1<GCController>() {
				@Override
				public void invoke(GCController gcController) {
					onControllerDisconnect(gcController);
				}
			});

		}
	}

	protected void onControllerConnect(GCController gcController) {
		if (!isSupportedController(gcController))
			return;

		boolean alreadyInList = false;
		for (Controller controller : controllers) {
			if (controller instanceof IosController && ((IosController) controller).getController() == gcController) {
				alreadyInList = true;
				break;
			}
		}

		if (!alreadyInList) {
			IosController iosController = new IosController(gcController);
			controllers.add(iosController);

			synchronized (listeners) {
				for (ControllerListener listener : listeners)
					listener.connected(iosController);
			}
		}
	}

	protected void onControllerDisconnect(GCController gcController) {
		IosController oldReference = null;
		for (Controller controller : controllers) {
			if (controller instanceof IosController && ((IosController) controller).getController() == gcController) {
				oldReference = (IosController) controller;
			}
		}

		if (oldReference != null) {
			controllers.removeValue(oldReference, true);

			synchronized (listeners) {
				for (ControllerListener listener : listeners)
					listener.disconnected(oldReference);
			}

			oldReference.dispose();
		}
	}

	@Override
	public void addListener(ControllerListener controllerListener) {
		initializeControllerArray();

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

	@Override
	public Array<ControllerListener> getListeners() {
		return listeners;
	}

	@Override
	public void clearListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
}


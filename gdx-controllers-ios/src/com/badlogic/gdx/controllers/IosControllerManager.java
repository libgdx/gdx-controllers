package com.badlogic.gdx.controllers;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.gamecontroller.GCController;
import org.robovm.apple.uikit.UIKeyCommand;
import org.robovm.apple.uikit.UIKeyModifierFlags;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.rt.bro.annotation.Callback;

public class IosControllerManager implements ControllerManager {
	private final Array<Controller> controllers = new Array<>();
	private final Array<ControllerListener> listeners = new Array<>();
	private boolean initialized = false;
	private ICadeController iCadeController;

	public IosControllerManager() {
	}

	/**
	 * you need to call this method to register IosControllerManager with libGDX before your first call to
	 * Controllers.getControllers().
	 */
	public static void initializeIosControllers(boolean enableICade) {
		ObjectMap<Application, ControllerManager> managers = Controllers.managers;

		// this is a copy from Controllers class. A hack to get IosControllerManager to work with libGDX
		if (Foundation.getMajorSystemVersion() < 7) {
			Gdx.app.log("Controllers", "IosControllerManager not added, needs iOS 7+.");
		} else if (!managers.containsKey(Gdx.app)) {
			IosControllerManager manager = new IosControllerManager();

			managers.put(Gdx.app, manager);
			final Application app = Gdx.app;
			Gdx.app.addLifecycleListener(new LifecycleListener() {
				public void resume() {
				}

				public void pause() {
				}

				public void dispose() {
					Controllers.managers.remove(app);
					Gdx.app.log("Controllers", "removed manager for application, " + Controllers.managers.size + " managers active");
				}
			});
			Gdx.app.log("Controllers", "added manager for application, " + managers.size + " managers active");

			if (enableICade) {
				manager.enableICade(((IOSApplication) Gdx.app).getUIViewController());
			}
		} else {
			Gdx.app.log("Controllers", "IosControllerManager not added, manager already active. ");
		}
	}

	public void enableICade(UIViewController controller) {
		Selector action = Selector.register("keyPress:");
		for (int i = 0; i < ICadeController.KEYS_TO_HANDLE.length(); i++) {
			controller.addKeyCommand(new UIKeyCommand(ICadeController.KEYS_TO_HANDLE.substring(i, 1),
					UIKeyModifierFlags.None, action));
		}
	}

	@Callback
	@BindSelector("keyPress:")
	private static void keyPress(UIKeyCommand sender) {
		//a key for ICadeController was pressed
		// instantiate it, if not already available
		IosControllerManager controllerManager = (IosControllerManager) Controllers.managers.get(Gdx.app);

		if (controllerManager != null)
			controllerManager.handleKeyPressed(sender);
	}

	private void handleKeyPressed(UIKeyCommand sender) {
		if (iCadeController == null) {
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

		return controllers;
	}

	private void initializeControllerArray() {
		if (!initialized) {
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


package com.badlogic.gdx.controllers;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.gamecontroller.GCController;
import org.robovm.objc.block.VoidBlock1;

public class IosControllerManager implements ControllerManager {
	private final Array<Controller> controllers = new Array<>();
	private final Array<ControllerListener> listeners = new Array<>();
	private boolean initialized = false;

	public IosControllerManager() {
	}

	/**
	 * you need to call this method to register IosControllerManager with libGDX before your first call to
	 * Controllers.getControllers().
	 */
	public static void initializeIosControllers() {
		ObjectMap<Application, ControllerManager> managers = Controllers.managers;

		// this is a copy from Controllers class. A hack to get IosControllerManager to work with libGDX
		if (Foundation.getMajorSystemVersion() < 7) {
			Gdx.app.log("Controllers", "IosControllerManager not added, needs iOS 7+.");
		} else if (!managers.containsKey(Gdx.app)) {
			ControllerManager manager = new IosControllerManager();

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
		} else {
			Gdx.app.log("Controllers", "IosControllerManager not added, manager already active. ");
		}
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
		IosController iosController = new IosController(gcController);

		if (!controllers.contains(iosController, false)) {
			controllers.add(iosController);

			for (ControllerListener listener : listeners)
				listener.connected(iosController);
		} else
			iosController.dispose();
	}

	protected void onControllerDisconnect(GCController gcController) {
		IosController oldReference = null;
		for (Controller controller : controllers) {
			if (((IosController) controller).getController() == gcController) {
				oldReference = (IosController) controller;
			}
		}

		if (oldReference != null) {
			controllers.removeValue(oldReference, true);

			for (ControllerListener listener : listeners) {
				listener.disconnected(oldReference);
			}

			oldReference.dispose();
		}
	}

	@Override
	public void addListener(ControllerListener controllerListener) {
		initializeControllerArray();

		if (!listeners.contains(controllerListener, true))
			listeners.add(controllerListener);
	}

	@Override
	public void removeListener(ControllerListener controllerListener) {
		listeners.removeValue(controllerListener, true);
	}

	@Override
	public Array<ControllerListener> getListeners() {
		return listeners;
	}

	@Override
	public void clearListeners() {
		listeners.clear();
	}
}


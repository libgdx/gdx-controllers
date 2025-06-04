package com.badlogic.gdx.controllers.desktop;

import com.badlogic.gdx.controllers.ControllerManager;

/**
 * Class defining the configuration of a {@link ControllerManager}.
 *
 * @author Benjamin Schulte
 */
public class Configuration {
    /**
     * The max number of controllers the ControllerManager should deal with
     */
    public int maxNumControllers = 4;

    /**
     * Use RawInput implementation instead of XInput on Windows, if applicable. Enable this if you
     * need to use more than four XInput controllers at once. Comes with drawbacks.
     */
    public boolean useRawInput = false;

    /**
     * Disable this to skip loading of the native library. Can be useful if an application wants
     * to use a loader other than {@link com.badlogic.gdx.utils.SharedLibraryLoader}.
     */
    public boolean loadNativeLibrary = true;

    /**
     * Disable this to return to legacy temporary file loading of database file.
     */
    public boolean loadDatabaseInMemory = true;
}


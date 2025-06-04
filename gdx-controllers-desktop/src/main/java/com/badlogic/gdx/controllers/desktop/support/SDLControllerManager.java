package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.controllers.desktop.Configuration;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.sdl.SDL_Event;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.sdl.SDLHints.*;
import static org.lwjgl.sdl.SDLInit.*;
import static org.lwjgl.sdl.SDLEvents.*;
import static org.lwjgl.sdl.SDLGamepad.*;
import static org.lwjgl.sdl.SDLJoystick.*;
import static org.lwjgl.sdl.SDLError.*;
import static org.lwjgl.sdl.SDLIOStream.*;

/**
 * This class handles initializing the native library, connecting to controllers, and managing the
 * list of controllers.
 *
 * Generally, after creating a ControllerManager object and calling initSDLGamepad() on it, you
 * would access the states of the attached gamepads by calling getState().
 *
 * For some applications (but probably very few), getState may have a performance impact. In this
 * case, it may make sense to use the getControllerIndex() method to access the objects used
 * internally by  ControllerManager.
 *
 * @author William Hartman
 */
public class SDLControllerManager {

    private final SDL_Event sdlEvent = SDL_Event.create(); // Shared temp obj
    private final Configuration configuration;
    private String mappingsPath;
    private boolean isInitialized;
    private ControllerIndex[] controllers;

    /**
     * Default constructor. Makes a manager for 4 controllers with the built in mappings from here:
     * https://github.com/gabomdq/SDL_GameControllerDB
     */
    public SDLControllerManager() {
        this(new Configuration(), "/gamecontrollerdb.txt");
    }

    /**
     * Constructor. Uses built-in mappings from here: https://github.com/gabomdq/SDL_GameControllerDB
     *
     * @param configuration see {@link Configuration and its fields}
     */
    public SDLControllerManager(Configuration configuration) {
        this(configuration, "/gamecontrollerdb.txt");
    }

    /**
     * Constructor.
     *
     * @param mappingsPath The path to a file containing SDL controller mappings.
     * @param configuration see {@link Configuration and its fields}
     */
    public SDLControllerManager(Configuration configuration, String mappingsPath) {
        this.configuration = configuration;
        this.mappingsPath = mappingsPath;
        isInitialized = false;
        controllers = new ControllerIndex[configuration.maxNumControllers];
    }

    /**
     * Initialize the ControllerIndex library. This loads the native library and initializes SDL
     * in the native code.
     *
     * @throws IllegalStateException If the native code fails to initialize or if SDL is already initialized
     */
    public void initSDLGamepad() throws IllegalStateException {
        if(isInitialized) {
            throw new IllegalStateException("SDL is already initialized!");
        }

        if (!configuration.useRawInput)
            SDL_SetHint(SDL_HINT_JOYSTICK_RAWINPUT, "0");
        if (!SDL_Init(SDL_INIT_EVENTS | SDL_INIT_JOYSTICK | SDL_INIT_GAMEPAD))
            throw new IllegalStateException("SDL init failed!");

        //We don't want any controller connections events (which are automatically generated at init)
        //since they interfere with us detecting new controllers, so we go through all events and clear them.
        while (SDL_PollEvent(sdlEvent));

        isInitialized = true;

        //Set controller mappings. The possible exception is caught, since stuff will still work ok
        //for most people if mapping aren't set.
        try {
            addMappingsFromFile(mappingsPath);
        } catch (IOException | IllegalStateException e) {
            System.err.println("Failed to load mapping with original location \"" + mappingsPath + "\", " +
                    "Falling back of SDL's built in mappings");
            e.printStackTrace();
        }

        //Connect and keep track of the controllers
        for(int i = 0; i < controllers.length; i++) {
            controllers[i] = new ControllerIndex(i);
        }
    }

    /**
     * This method quits all the native stuff. Call it when you're done with Jamepad.
     */
    public void quitSDLGamepad() {
        for(ControllerIndex c: controllers) {
            c.close();
        }
        SDL_Quit();
        controllers = new ControllerIndex[0];
        isInitialized = false;
    }

    /**
     * Return the state of a controller at the passed index. This is probably the way most people
     * should use this library. It's simpler and less verbose, and controller connections and
     * disconnections are automatically handled.
     *
     * Also, no exceptions are thrown here (unless Jamepad isn't initialized), so you don't need
     * to have a million try/catches or anything.
     *
     * The returned state is immutable. This means an object is allocated every time you call this
     * (unless the controller is disconnected). This shouldn't be a big deal (even for games) if your
     * GC is tuned well, but if this is a problem for you, you can go directly through the internal
     * ControllerIndex objects using getControllerIndex().
     *
     * update() is called each time this method is called. Buttons are also queried, so values
     * returned from isButtonJustPressed() in ControllerIndex may not be what you expect. Calling
     * this method will have side effects if you are using the ControllerIndex objects yourself.
     * This should be fine unless you are mixing and matching this method with ControllerIndex
     * objects, which you probably shouldn't do anyway.
     *
     * @param index The index of the controller to be checked
     * @return The state of the controller at the passed index.
     * @throws IllegalStateException if Jamepad was not initialized
     */
    public ControllerState getState(int index) throws IllegalStateException {
        verifyInitialized();

        if(index < controllers.length && index >= 0) {
            update();
            return ControllerState.getInstanceFromController(controllers[index]);
        } else {
            return ControllerState.getDisconnectedControllerInstance();
        }
    }

    /**
     * Starts vibrating the controller at this given index. If this fails for one reason or another (e.g.
     * the controller at that index doesn't support haptics, or if there is no controller at that index),
     * this method will return false.
     *
     * Each call to this function cancels any previous rumble effect, and calling it with 0 intensity stops any rumbling.
     *
     * @param index The index of the controller that will be vibrated
     * @param leftMagnitude The intensity of the left rumble motor (0-1)
     * @param rightMagnitude The intensity of the rught rumble motor (0-1)
     * @return Whether or not vibration was successfully started
     * @throws IllegalStateException if Jamepad was not initialized
     */
    public boolean doVibration(int index, float leftMagnitude, float rightMagnitude, int duration_ms) throws IllegalStateException {
        verifyInitialized();

        if(index < controllers.length && index >= 0) {
            try {
                return controllers[index].doVibration(leftMagnitude, rightMagnitude, duration_ms);
            } catch (ControllerUnpluggedException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Returns a the ControllerIndex object with the passed index (0 for p1, 1 for p2, etc.).
     *
     * You should only use this method if you're worried about the object allocations from getState().
     * If you decide to do things this way, your code will be a good bit more verbose and you'll
     * need to deal with potential exceptions.
     *
     * It is generally safe to store objects returned from this method. They will only change internally
     * if you call quitSDLGamepad() followed by a call to initSDLGamepad().
     *
     * Calling update() will run through all the controllers to check for newly plugged in or unplugged
     * controllers. You could do this from your code, but keep that in mind.
     *
     * @param index the index of the ControllerIndex that will be returned
     * @return The internal ControllerIndex object for the passed index.
     * @throws IllegalStateException if Jamepad was not initialized
     */
    public ControllerIndex getControllerIndex(int index) {
        verifyInitialized();
        return controllers[index];
    }

    /**
     * Return the number of controllers that are actually connected. This may disagree with
     * the ControllerIndex objects held in here if something has been plugged in or unplugged
     * since update() was last called.
     *
     * @return the number of connected controllers.
     * @throws IllegalStateException if Jamepad was not initialized
     */
    public int getNumControllers() {
        verifyInitialized();
        IntBuffer joysticks = SDL_GetJoysticks();
        if (joysticks == null)
            return 0;

        int numGamepads = 0;
        for (int i = 0; i < joysticks.remaining(); i++) {
            int joystickId = joysticks.get(i);
            if(SDL_IsGamepad(joystickId))
                numGamepads++;

        }

        return numGamepads;
    }

    /**
     * Refresh the connected controllers in the controller list if something has been connected or
     * unplugged.
     *
     * If there hasn't been a change in whether controller are connected or not, nothing will happen.
     *
     * @return True if the controller list was refreshed, false otherwise
     * @throws IllegalStateException if Jamepad was not initialized
     */
    public boolean update() {
        verifyInitialized();
        SDL_UpdateJoysticks();
        while (SDL_PollEvent(sdlEvent)) {
            if (sdlEvent.type() == SDL_EVENT_JOYSTICK_ADDED || sdlEvent.type() == SDL_EVENT_JOYSTICK_REMOVED) {
                for (int i = 0; i < controllers.length; i++) {
                    controllers[i].reconnectController();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * This method adds mappings held in the specified file. The file is copied to the temp folder so
     * that it can be read by the native code (if running from a .jar for instance)
     *
     * @param path The path to the file containing controller mappings.
     * @throws IOException if the file cannot be read, copied to a temp folder, or deleted.
     * @throws IllegalStateException if the mappings cannot be applied to SDL
     */
    public void addMappingsFromFile(String path) throws IOException, IllegalStateException {
        InputStream source = getClass().getResourceAsStream(path);
        if(source==null) source = ClassLoader.getSystemResourceAsStream(path);
        if(source==null && new File(path).exists()) source = new FileInputStream(path);
        if(source==null) throw new IOException("Cannot open resource from classpath "+path);

        if(configuration.loadDatabaseInMemory) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int read;
            byte[] data = new byte[4096];

            while((read = source.read(data, 0, data.length)) != -1) {
                out.write(data, 0, read);
            }
            source.close();

            byte[] b = out.toByteArray();

            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(data);
            buffer.flip();

            long stream = SDL_IOFromMem(buffer);
            if (stream == 0)
                throw new IllegalStateException("Failed to create SDLIOStream: " + getLastNativeError());

            int error = SDL_AddGamepadMappingsFromIO(stream, true);
            if (error < 0)
                throw new IllegalStateException("Failed to load mappings from SDLIOStream, error: " + error + ", msg: " + getLastNativeError() + "! Falling back to build in SDL mappings.");
        }
        else {
            /*
            Copy the file to a temp folder. SDL can't read files held in .jars, and that's probably how
            most people would use this library.
             */
            Path extractedLoc =  Files.createTempFile(null, null).toAbsolutePath();

            Files.copy(source, extractedLoc, StandardCopyOption.REPLACE_EXISTING);

            int err = SDL_AddGamepadMappingsFromFile(extractedLoc.toString());
            if(err < 0) {
                throw new IllegalStateException("Failed to load SDL controller mappings from" + extractedLoc + " with error-code " + err + " and message " + getLastNativeError() +"!"
                        + " Falling back to build in SDL mappings.");
            }

            Files.delete(extractedLoc);
        }
    }

    /**
     * @return last error message logged by the native lib. Use this for debugging purposes.
     */
    public String getLastNativeError() {
        return SDL_GetError();
    }

    private boolean verifyInitialized() throws IllegalStateException {
        if(!isInitialized) {
            throw new IllegalStateException("SDL_GameController is not initialized!");
        }
        return true;
    }
}


package de.golfgl.gdx.controllers.jamepad.support;

public class JamePadExceptions {
    public static UnsupportedOperationException notSupported(String operation) {
        return new UnsupportedOperationException("Jamepad does not support: " + operation);
    }
}

package com.badlogic.gdx.controllers;

public class ICadeMapping extends ControllerMapping {
    private static ICadeMapping instance;

    ICadeMapping() {
        super(0, 1, UNDEFINED, UNDEFINED,
                0, 1, 2, 3, 6, 7,
                4, UNDEFINED, 5, UNDEFINED, UNDEFINED, UNDEFINED,
                UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
    }

    static ICadeMapping getInstance() {
        if (instance == null)
            instance = new ICadeMapping();

        return instance;
    }
}

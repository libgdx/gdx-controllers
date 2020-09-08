package com.badlogic.gdx.controllers.gwt;

import com.badlogic.gdx.controllers.ControllerMapping;

public class WebMapping extends ControllerMapping {
    private static WebMapping instance;

    WebMapping() {
        super(0, 1, 2, 3,
                0, 1, 2, 3, 8, 9,
                4, 6, 5, 7, 10, 11);
    }

    static WebMapping getInstance() {
        if (instance == null)
            instance = new WebMapping();

        return instance;
    }
}

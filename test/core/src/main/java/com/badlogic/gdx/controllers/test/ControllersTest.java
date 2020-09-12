package com.badlogic.gdx.controllers.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.graphics.Color.RED;
import static com.badlogic.gdx.graphics.Color.WHITE;

public class ControllersTest extends ApplicationAdapter {
    public final Array<Label> axisLabelArray = new Array<>();
    public Table axisTable;
    public Label buttonDpadUp;
    public Label buttonDpadDown;
    public Label buttonDpadLeft;
    public Label buttonDpadRight;
    public Label buttonA;
    public Label buttonB;
    public Label buttonX;
    public Label buttonY;
    public Label buttonBack;
    public Label buttonStart;
    public Label buttonL1;
    public Label buttonL2;
    public Label buttonR1;
    public Label buttonR2;
    public Label buttonLeftStick;
    public Label buttonRightStick;
    private Stage stage;
    private Array<String> controllerNames = new Array<>();
    private Label indexLabel;

    private Controller selectedController;
    private SelectBox<String> controllerList;
    private ControllerListener controllerListener;
    private Skin skin;
    private Label callbackLabel;

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        controllerList = new SelectBox<>(skin);

        controllerListener = new ControllerAdapter() {
            @Override
            public void connected(final Controller controller) {
                Gdx.app.log("Controller", "Controller connected: " + controller.getName()
                        + "/" + controller.getUniqueId());
            }

            @Override
            public void disconnected(Controller controller) {
                Gdx.app.log("Controller", "Controller disconnected: " + controller.getName()
                        + "/" + controller.getUniqueId());
            }
        };

        refreshControllersList();

        Controllers.addListener(new ControllerAdapter() {
            @Override
            public void connected(final Controller controller) {
                Gdx.app.log("Controllers", "Controller connected: " + controller.getName()
                        + "/" + controller.getUniqueId());
                refreshControllersList();
            }

            @Override
            public void disconnected(Controller controller) {
                Gdx.app.log("Controllers", "Controller disconnected: " + controller.getName()
                        + "/" + controller.getUniqueId());
                refreshControllersList();
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {
                if (controller == selectedController) {
                    callbackLabel.setText(String.valueOf(buttonIndex));
                    callbackLabel.setColor(RED);
                }
                return true;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonIndex) {
                if (controller == selectedController) {
                    callbackLabel.setText(String.valueOf(buttonIndex));
                    callbackLabel.setColor(WHITE);
                }
                return true;
            }

            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {
                if (controller == selectedController) {
                    callbackLabel.setColor(WHITE);
                    callbackLabel.setText(axisIndex + "/" + value);
                }
                return true;
            }
        });

        controllerList.setItems(controllerNames);
        controllerList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int index = controllerList.getSelectedIndex();
                axisTable.clearChildren();
                axisLabelArray.clear();
                if (index == 0) {
                    selectedController = null;
                } else {
                    selectedController = Controllers.getControllers().get(index - 1);
                    selectedController.startVibration(200, 1);
                }
                addAxisLabels();
            }
        });

        Table buttonTable = new Table(skin);
        buttonA = addControllerButtonLabel(buttonTable, "buttonA          ");
        buttonB = addControllerButtonLabel(buttonTable, "buttonB          ");
        buttonX = addControllerButtonLabel(buttonTable, "buttonX          ");
        buttonY = addControllerButtonLabel(buttonTable, "buttonY          ");
        buttonBack = addControllerButtonLabel(buttonTable, "buttonBack       ");
        buttonStart = addControllerButtonLabel(buttonTable, "buttonStart      ");
        buttonL1 = addControllerButtonLabel(buttonTable, "buttonL1         ");
        buttonL2 = addControllerButtonLabel(buttonTable, "buttonL2         ");
        buttonR1 = addControllerButtonLabel(buttonTable, "buttonR1         ");
        buttonR2 = addControllerButtonLabel(buttonTable, "buttonR2  ");
        buttonDpadUp = addControllerButtonLabel(buttonTable, "buttonDpadUp      ");
        buttonDpadDown = addControllerButtonLabel(buttonTable, "buttonDpadDown        ");
        buttonDpadLeft = addControllerButtonLabel(buttonTable, "buttonDpadLeft       ");
        buttonDpadRight = addControllerButtonLabel(buttonTable, "buttonDpadRight        ");
        buttonLeftStick = addControllerButtonLabel(buttonTable, "buttonLeftStick  ");
        buttonRightStick = addControllerButtonLabel(buttonTable, "buttonRightStick ");

        axisTable = new Table(skin);
        addAxisLabels();

        Table moreInfoTable = new Table(skin);
        moreInfoTable.row().padTop(20);
        moreInfoTable.add("Player index").padRight(10);
        indexLabel = new Label("", skin);
        indexLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (selectedController != null) {
                    selectedController.setPlayerIndex(selectedController.getPlayerIndex() + 1);
                }
            }
        });
        moreInfoTable.add(indexLabel);

        moreInfoTable.row().padTop(20);
        callbackLabel = new Label("", skin);
        moreInfoTable.add("Callback:");
        moreInfoTable.add(callbackLabel).width(150);

        stage = new Stage(new FitViewport(640, 480));

        Table columnRight = new Table();
        columnRight.add(axisTable).row();
        columnRight.add(moreInfoTable).row();

        Table table = new Table(skin);
        table.add("Controller:");
        table.add(controllerList).padBottom(10).row();
        table.add(buttonTable).padRight(10);
        table.add(columnRight).row();

        table.setFillParent(true);

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    private void addAxisLabels() {
        // add labels for controller axis
        ControllerMapping mapping = selectedController == null ? null : selectedController.getMapping();
        int axisCount = selectedController == null ? 0 : selectedController.getAxisCount();
        for (int i = 0; i < axisCount; i++) {
            String name;
            if (mapping.axisLeftX == i)
                name = "leftX";
            else if (mapping.axisLeftY == i)
                name = "leftY";
            else if (mapping.axisRightX == i)
                name = "rightX";
            else if (mapping.axisRightY == i)
                name = "rightY";
            else
                name = "Axis " + i;
            Label label = addControllerAxisLabel(axisTable, name);
            axisLabelArray.add(label);
        }

        // add labels in case of non-existing defaults
        if (mapping == null || mapping.axisLeftX == ControllerMapping.UNDEFINED || mapping.axisLeftX >= axisCount)
            addControllerAxisLabel(axisTable, "leftX");
        if (mapping == null || mapping.axisLeftY == ControllerMapping.UNDEFINED || mapping.axisLeftY >= axisCount)
            addControllerAxisLabel(axisTable, "leftY");
        if (mapping == null || mapping.axisRightX == ControllerMapping.UNDEFINED || mapping.axisRightX >= axisCount)
            addControllerAxisLabel(axisTable, "rightX");
        if (mapping == null || mapping.axisRightY == ControllerMapping.UNDEFINED || mapping.axisRightY >= axisCount)
            addControllerAxisLabel(axisTable, "rightY");

    }

    private Label addControllerButtonLabel(Table table, String name) {
        Label label = new Label(name.trim(), skin);
        table.add(label).row();
        return label;
    }

    private Label addControllerAxisLabel(Table table, String name) {
        Label label = new Label("0.0", skin);
        label.setColor(Color.DARK_GRAY);
        table.add(name.trim()).padRight(10);
        table.add(label).width(100).row();
        return label;
    }

    private void refreshControllersList() {
        controllerNames = new Array<>();
        Array<Controller> controllers = new Array<>();
        controllerNames.add("Select...");
        Gdx.app.log("Controllers", Controllers.getControllers().size + " controllers connected.");
        for (int i = 0; i < Controllers.getControllers().size; i++) {
            Controller controller = Controllers.getControllers().get(i);
            String name = controller.getName();
            Gdx.app.log("Controllers", name + "/" + controller.getUniqueId());
            if (name.length() > 30)
                name = name.substring(0, 28) + "...";
            controllerNames.add(name);
            controllers.add(controller);
            controller.addListener(controllerListener);
        }
        controllerList.setItems(controllerNames);
        controllerList.setSelectedIndex(0);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        updateStateOfButtons();
        updateStateOfAxis();
        indexLabel.setText(selectedController != null ? String.valueOf(selectedController.getPlayerIndex()) : "");

        stage.act();
        stage.draw();
    }

    private void updateStateOfAxis() {
        if (selectedController != null) {
            for (int i = 0; i < axisLabelArray.size; i++) {
                updateAxisLabel(axisLabelArray.get(i), i);
            }
        }
    }

    private void updateAxisLabel(Label axisLabel, int axisNum) {
        float value = selectedController.getAxis(axisNum);
        axisLabel.setColor(value == 0 ? WHITE : RED);
        axisLabel.setText(String.valueOf(value));
    }

    private void updateButtonLabel(Label buttonLabel, int buttonNum) {
        if (buttonNum == ControllerMapping.UNDEFINED || buttonNum > selectedController.getMaxButtonIndex()
                || buttonNum < selectedController.getMinButtonIndex()) {
            buttonLabel.setColor(Color.DARK_GRAY);
        } else {
            boolean pressed = selectedController.getButton(buttonNum);
            buttonLabel.setColor(pressed ? RED : WHITE);
        }
    }

    private void updateStateOfButtons() {
        updateButtonLabel(buttonA, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonA);
        updateButtonLabel(buttonB, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonB);
        updateButtonLabel(buttonX, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonX);
        updateButtonLabel(buttonY, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonY);
        updateButtonLabel(buttonBack, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonBack);
        updateButtonLabel(buttonStart, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonStart);
        updateButtonLabel(buttonL1, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonL1);
        updateButtonLabel(buttonL2, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonL2);
        updateButtonLabel(buttonR1, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonR1);
        updateButtonLabel(buttonR2, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonR2);
        updateButtonLabel(buttonLeftStick, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonLeftStick);
        updateButtonLabel(buttonRightStick, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonRightStick);
        updateButtonLabel(buttonDpadUp, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonDpadUp);
        updateButtonLabel(buttonDpadDown, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonDpadDown);
        updateButtonLabel(buttonDpadLeft, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonDpadLeft);
        updateButtonLabel(buttonDpadRight, selectedController == null ? ControllerMapping.UNDEFINED : selectedController.getMapping().buttonDpadRight);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height);
        }
    }
}

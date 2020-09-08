package de.golfgl.gdx.controllers.jamepad;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.controllers.AdvancedController;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;

import static com.badlogic.gdx.graphics.Color.RED;
import static com.badlogic.gdx.graphics.Color.WHITE;

public class JamepadTest extends ApplicationAdapter {
    private Stage stage;
    private Array<String> controllerNames = new Array<>();
    private Array<Controller> controllers = new Array<>();
    private ObjectMap<ControllerButton, Label> buttonToLabel = new ObjectMap<>(ControllerButton.values().length);
    private ObjectMap<ControllerAxis, Label> axisToLabel = new ObjectMap<>(ControllerAxis.values().length);
    private Label indexLabel;
    private Controller selectedController;
    private SelectBox<String> controllerList;
    private ControllerListener controllerListener;

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 640;
        config.height = 480;

        new LwjglApplication(new JamepadTest(), config);
    }

    @Override
    public void create() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        controllerList = new SelectBox<>(skin);

        controllerListener = new ControllerAdapter() {
            @Override
            public void connected(final Controller controller) {
                Gdx.app.log("Controller", "Controller connected: " + controller.getName()
                        + "/" + ((AdvancedController) controller).getUniqueId());
            }

            @Override
            public void disconnected(Controller controller) {
                Gdx.app.log("Controller", "Controller disconnected: " + controller.getName()
                        + "/" + ((AdvancedController) controller).getUniqueId());
            }
        };

        refreshControllersList();

        Controllers.addListener(new ControllerAdapter() {
            @Override
            public void connected(final Controller controller) {
                Gdx.app.log("Controllers", "Controller connected: " + controller.getName()
                        + "/" + ((AdvancedController) controller).getUniqueId());
                refreshControllersList();
            }

            @Override
            public void disconnected(Controller controller) {
                Gdx.app.log("Controllers", "Controller disconnected: " + controller.getName()
                        + "/" + ((AdvancedController) controller).getUniqueId());
                refreshControllersList();
            }
        });

        controllerList.setItems(controllerNames);
        controllerList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int index = controllerList.getSelectedIndex();
                if (index == 0) {
                    selectedController = null;
                } else {
                    selectedController = Controllers.getControllers().get(index - 1);
                    ((AdvancedController) selectedController).startVibration(200, 1);
                }
            }
        });

        Table buttonTable = new Table(skin);
        for (ControllerButton button : ControllerButton.values()) {
            Label label = new Label(button.name(), skin);
            buttonToLabel.put(button, label);
            buttonTable.add(label).row();
        }

        Table axisTable = new Table(skin);
        for (ControllerAxis axis : ControllerAxis.values()) {
            Label label = new Label("0.0", skin);
            axisToLabel.put(axis, label);
            axisTable.add(axis.name()).padRight(10);
            axisTable.add(label).row();
        }
        axisTable.add("Player index").padRight(10);
        indexLabel = new Label("", skin);
        indexLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (selectedController != null) {
                    ((AdvancedController) selectedController).
                            setPlayerIndex(((AdvancedController) selectedController).getPlayerIndex() + 1);
                }
            }
        });
        axisTable.add(indexLabel).row();

        stage = new Stage();
        stage.setViewport(new ScreenViewport());


        Table table = new Table(skin);
        table.add("Controller:");
        table.add(controllerList).padBottom(10).row();
        table.add(axisTable).padRight(10);
        table.add(buttonTable).row();

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    private void refreshControllersList() {
        controllerNames = new Array<>();
        controllers = new Array<>();
        controllerNames.add("Select...");
        Gdx.app.log("Controllers", Controllers.getControllers().size + " controllers connected.");
        for (int i = 0; i < Controllers.getControllers().size; i++) {
            Controller controller = Controllers.getControllers().get(i);
            Gdx.app.log("Controllers", controller.getName() + "/" + ((AdvancedController) controller).getUniqueId());
            controllerNames.add(controller.getName());
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
        indexLabel.setText(selectedController != null ? String.valueOf(((AdvancedController) selectedController).getPlayerIndex()) : "");

        stage.act();
        stage.draw();
    }

    private void updateStateOfAxis() {
        for (ObjectMap.Entry<ControllerAxis, Label> entry : axisToLabel) {
            if (selectedController == null) {
                entry.value.setColor(Color.DARK_GRAY);
                entry.value.setText("0.0");
            } else {
                float value = selectedController.getAxis(entry.key.ordinal());
                entry.value.setColor(value == 0 ? WHITE : RED);
                entry.value.setText(String.valueOf(value));
            }
        }
    }

    private void updateStateOfButtons() {
        for (ObjectMap.Entry<ControllerButton, Label> entry : buttonToLabel.entries()) {
            if (selectedController == null) {
                entry.value.setColor(Color.DARK_GRAY);
            } else {
                boolean pressed = selectedController.getButton(entry.key.ordinal());
                entry.value.setColor(pressed ? RED : WHITE);
            }
        }
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

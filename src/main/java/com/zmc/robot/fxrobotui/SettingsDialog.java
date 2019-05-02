package com.zmc.robot.fxrobotui;

import java.util.Optional;

import com.zmc.robot.simulator.Settings;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Dialog;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.control.ButtonType;

import javafx.scene.image.ImageView;

public class SettingsDialog {

    private Settings mSettings;
    private Dialog<Boolean> dialog;

    private SettingsPane settingsPane;

    // private TextField kpField, kiField, kdField;
    // private TextField atObstacleField, unsafeField, followWallField;
    // private TextField velocityField, maxRPMField, minRPMField, radiusField,
    // wheelDistanceField;
    // private GridPane grid;

    public SettingsDialog(Settings settings) {
        this.mSettings = settings;
        init();
    }

    private void init() {

        dialog = new Dialog<>();
        dialog.setTitle("Settings ");
        dialog.setHeaderText("Settings dialog");

        // Set the icon (must be included in the project).
        // dialog.setGraphic(new
        // ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        settingsPane = new SettingsPane();

        settingsPane.setSettings(mSettings);

        dialog.getDialogPane().setContent(settingsPane.getMainPane());

        // Request focus on the username field by default.
        // Platform.runLater(() -> kpField.requestFocus());

        // Convert the result to a username-password-pair when the login button is
        // clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Boolean(true);
            }
            return false;
        });
    }

    public Boolean showAndWait() {

        Optional<Boolean> result = dialog.showAndWait();
        if (result.get()) {
            mSettings = settingsPane.getSettings();
            // mSettings.kp = Double.valueOf(kpField.getText());
            // mSettings.ki = Double.valueOf(kiField.getText());
            // mSettings.kd = Double.valueOf(kdField.getText());
        }

        System.out.println(result);
        return result.get();
    }
}
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

    private TextField kpField, kiField, kdField;
    private TextField atObstacleField, unsafeField, followWallField;
    private TextField velocityField, maxRPMField, minRPMField, radiusField, wheelDistanceField;
    private GridPane grid;

    public SettingsDialog(Settings settings) {
        this.mSettings = settings;
        init();
    }

    private void init() {

        dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

        // Set the icon (must be included in the project).
        // dialog.setGraphic(new
        // ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER); // Override default
        grid.setHgap(10);
        grid.setVgap(12);

        // Use column constraints to set properties for columns in the grid
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.RIGHT); // Override default
        grid.getColumnConstraints().add(column1);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.LEFT); // Override default
        grid.getColumnConstraints().add(column2);

        kpField = new TextField();
        kiField = new TextField();
        kdField = new TextField();
        atObstacleField = new TextField();
        unsafeField = new TextField();
        followWallField = new TextField();

        velocityField = new TextField();
        maxRPMField = new TextField();
        minRPMField = new TextField();
        radiusField = new TextField();
        wheelDistanceField = new TextField();

        int rowIdx = 0;
        Label label = new Label("PID parameters     ");
        grid.add(label, 0, 0, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(kpField, 1, rowIdx);

        rowIdx++;
        label = new Label("KI:");
        grid.add(label, 0, rowIdx);
        grid.add(kiField, 1, rowIdx);

        rowIdx++;
        label = new Label("KD:");
        grid.add(label, 0, rowIdx);
        grid.add(kdField, 1, rowIdx);

        rowIdx++;
        label = new Label("Obstacle parameters     ");
        grid.add(label, 0, rowIdx, 2, 2);

        rowIdx += 2;
        label = new Label("At Obstacle:");
        grid.add(label, 0, rowIdx);
        grid.add(atObstacleField, 1, rowIdx);

        rowIdx++;
        label = new Label("Unsafe distance:");
        grid.add(label, 0, rowIdx);
        grid.add(unsafeField, 1, rowIdx);

        rowIdx++;
        label = new Label("Follow wall distance:");
        grid.add(label, 0, rowIdx);
        grid.add(followWallField, 1, rowIdx);

        rowIdx++;
        label = new Label("Robot settings       ");
        grid.add(label, 0, rowIdx, 2, 2);

        rowIdx += 2;
        label = new Label("Velocity:");
        grid.add(label, 0, rowIdx);
        grid.add(velocityField, 1, rowIdx);

        rowIdx++;
        label = new Label("Min RPM:");
        grid.add(label, 0, rowIdx);
        grid.add(minRPMField, 1, rowIdx);

        rowIdx++;
        label = new Label("Max RPM:");
        grid.add(label, 0, rowIdx);
        grid.add(maxRPMField, 1, rowIdx);

        rowIdx++;
        label = new Label("Wheel radius(R):");
        grid.add(label, 0, rowIdx);
        grid.add(radiusField, 1, rowIdx);

        rowIdx++;
        label = new Label("Wheel distabce(L):");
        grid.add(label, 0, rowIdx);
        grid.add(wheelDistanceField, 1, rowIdx);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> kpField.requestFocus());

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
        System.out.println(result);
        return true;
    }
}
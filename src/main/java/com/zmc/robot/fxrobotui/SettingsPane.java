package com.zmc.robot.fxrobotui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

public class SettingsPane {

    private TextField kpField, kiField, kdField;
    private TextField atObstacleField, unsafeField, followWallField;
    private TextField velocityField, maxRPMField, minRPMField, radiusField, wheelDistanceField;
    private final Button btnApply = new Button("Apply");
    private final Button btnReset = new Button("Reset");

    private final GridPane grid;

    public Pane getMainPane() {
        return grid;

    }

    public SettingsPane() {

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

        TilePane tileButtons = new TilePane(Orientation.HORIZONTAL);

        tileButtons.setPadding(new Insets(20, 10, 20, 0));
        tileButtons.setHgap(10.0);
        tileButtons.setVgap(8.0); // In case window is reduced and buttons
                                  // require another row
        tileButtons.getChildren().addAll(btnApply, btnReset);

        rowIdx++;
        grid.add(tileButtons, 1, rowIdx);

    }

}
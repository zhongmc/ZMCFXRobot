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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import com.zmc.robot.simulator.Settings;

public class SettingsPane {

    private TextField d_kpField, d_kiField, d_kdField;
    private TextField p_kpField, p_kiField, p_kdField;
    private TextField t_kpField, t_kiField, t_kdField;

    private TextField atObstacleField, unsafeField, followWallField;
    private TextField velocityField, maxRPMField, minRPMField, radiusField, wheelDistanceField;
    private final Button btnApply = new Button("Apply");
    private final Button btnReset = new Button("Reset");
    private Settings mSettings;

    private final BorderPane border;

    public Pane getMainPane() {
        return border;

    }

    public void setSettings(Settings settings) {
        mSettings = settings;
        if (d_kpField == null)
            return;
        d_kpField.setText(String.format("%.2f", settings.kp));
        d_kiField.setText(String.format("%.2f", settings.ki));
        d_kdField.setText(String.format("%.2f", settings.kd));

        p_kpField.setText(String.format("%.2f", settings.pkp));
        p_kiField.setText(String.format("%.2f", settings.pki));
        p_kdField.setText(String.format("%.2f", settings.pkd));

        t_kpField.setText(String.format("%.2f", settings.tkp));
        t_kiField.setText(String.format("%.2f", settings.tki));
        t_kdField.setText(String.format("%.2f", settings.tkd));

        atObstacleField.setText(String.format("%.2f", settings.atObstacle));
        unsafeField.setText(String.format("%.2f", settings.unsafe));
        followWallField.setText(String.format("%.2f", settings.dfw));

        velocityField.setText(String.format("%.2f", settings.velocity));
        maxRPMField.setText(String.format("%d", settings.max_rpm));
        minRPMField.setText(String.format("%d", settings.min_rpm));
        radiusField.setText(String.format("%.3f", settings.wheelRadius));
        wheelDistanceField.setText(String.format("%.3f", settings.wheelDistance));
    }

    public Settings getSettings() {
        mSettings.kp = Double.valueOf(d_kpField.getText());
        mSettings.ki = Double.valueOf(d_kiField.getText());
        mSettings.kd = Double.valueOf(d_kdField.getText());

        mSettings.pkp = Double.valueOf(p_kpField.getText());
        mSettings.pki = Double.valueOf(p_kiField.getText());
        mSettings.pkd = Double.valueOf(p_kdField.getText());

        mSettings.tkp = Double.valueOf(t_kpField.getText());
        mSettings.tki = Double.valueOf(t_kiField.getText());
        mSettings.tkd = Double.valueOf(t_kdField.getText());

        mSettings.atObstacle = Double.valueOf(atObstacleField.getText());
        mSettings.unsafe = Double.valueOf(unsafeField.getText());
        mSettings.dfw = Double.valueOf(followWallField.getText());

        mSettings.velocity = Double.valueOf(velocityField.getText());
        mSettings.max_rpm = Integer.valueOf(maxRPMField.getText());
        mSettings.min_rpm = Integer.valueOf(minRPMField.getText());
        mSettings.wheelRadius = Double.valueOf(radiusField.getText());
        mSettings.wheelDistance = Double.valueOf(wheelDistanceField.getText());

        return mSettings;
    }

    public SettingsPane() {

        border = new BorderPane();
        border.setPadding(new Insets(20, 30, 20, 10));

        TabPane tabs = new TabPane();

        Tab tabDPID = new Tab();
        tabDPID.setText("PID-D");
        tabDPID.setContent(dirPIDPane());

        Tab tabPPID = new Tab();
        tabPPID.setText("PID-P");
        tabPPID.setContent(positionPIDPane());

        Tab tabTPID = new Tab();
        tabTPID.setText("PID-T");
        tabTPID.setContent(thetaPIDPane());

        Tab tabObstacle = new Tab();
        tabObstacle.setText("Obstacle");
        tabObstacle.setContent(obstaclePane());

        Tab tabRobot = new Tab();
        tabRobot.setText("Robot");
        tabRobot.setContent(robotPane());

        tabs.getTabs().addAll(tabDPID, tabPPID, tabTPID, tabObstacle, tabRobot);

        border.setCenter(tabs);

        TilePane tileButtons = new TilePane(Orientation.HORIZONTAL);

        tileButtons.setPadding(new Insets(20, 10, 20, 0));
        tileButtons.setHgap(10.0);
        tileButtons.setVgap(8.0); // In case window is reduced and buttons
                                  // require another row
        tileButtons.getChildren().addAll(btnApply, btnReset);

        // border.setBottom(tileButtons);

    }

    private Pane dirPIDPane() {

        GridPane grid = new GridPane();
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

        d_kpField = new TextField();
        d_kiField = new TextField();
        d_kdField = new TextField();

        int rowIdx = 0;
        Label label = new Label("PID parameters for direction   ");
        grid.add(label, 0, 0, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(d_kpField, 1, rowIdx);

        rowIdx++;
        label = new Label("KI:");
        grid.add(label, 0, rowIdx);
        grid.add(d_kiField, 1, rowIdx);

        rowIdx++;
        label = new Label("KD:");
        grid.add(label, 0, rowIdx);
        grid.add(d_kdField, 1, rowIdx);

        return grid;
    }

    private Pane positionPIDPane() {

        GridPane grid = new GridPane();
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

        p_kpField = new TextField();
        p_kiField = new TextField();
        p_kdField = new TextField();

        int rowIdx = 0;
        Label label = new Label("PID parameters for position    ");
        grid.add(label, 0, 0, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(p_kpField, 1, rowIdx);

        rowIdx++;
        label = new Label("KI:");
        grid.add(label, 0, rowIdx);
        grid.add(p_kiField, 1, rowIdx);

        rowIdx++;
        label = new Label("KD:");
        grid.add(label, 0, rowIdx);
        grid.add(p_kdField, 1, rowIdx);

        return grid;
    }

    private Pane thetaPIDPane() {

        GridPane grid = new GridPane();
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

        t_kpField = new TextField();
        t_kiField = new TextField();
        t_kdField = new TextField();

        int rowIdx = 0;
        Label label = new Label("PID parameters for Theta    ");
        grid.add(label, 0, 0, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(t_kpField, 1, rowIdx);

        rowIdx++;
        label = new Label("KI:");
        grid.add(label, 0, rowIdx);
        grid.add(t_kiField, 1, rowIdx);

        rowIdx++;
        label = new Label("KD:");
        grid.add(label, 0, rowIdx);
        grid.add(t_kdField, 1, rowIdx);

        return grid;
    }

    private Pane obstaclePane() {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER); // Override default
        grid.setHgap(10);
        grid.setVgap(12);

        atObstacleField = new TextField();

        unsafeField = new TextField();

        followWallField = new TextField();

        // Use column constraints to set properties for columns in the grid
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.RIGHT); // Override default
        grid.getColumnConstraints().add(column1);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.LEFT); // Override default
        grid.getColumnConstraints().add(column2);

        // t_kpField = new TextField();
        // t_kiField = new TextField();
        // t_kdField = new TextField();

        int rowIdx = 0;
        Label label = new Label("Obstacle parameters     ");
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

        return grid;
    }

    private Pane robotPane() {

        GridPane grid = new GridPane();
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

        velocityField = new TextField();

        maxRPMField = new TextField();

        minRPMField = new TextField();

        radiusField = new TextField();

        wheelDistanceField = new TextField();

        int rowIdx = 0;
        Label label = new Label("Robot settings       ");
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
        return grid;
    }

}
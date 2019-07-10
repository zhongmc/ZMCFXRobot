package com.zmc.robot.fxrobotui;

import java.util.concurrent.CompletableFuture;

import com.zmc.robot.simulator.ControllerInfo;
import com.zmc.robot.simulator.RearDriveRobot;
import com.zmc.robot.simulator.RobotState;
import com.zmc.robot.simulator.Settings;
import com.zmc.robot.simulator.Supervisor;
import com.sun.javafx.geom.Point2D;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.beans.value.ObservableValue;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import javafx.scene.layout.StackPane;
import javafx.scene.control.CheckBox;

/**
 * not used see DrivePane
 */
public class LocalSimulatorPane implements Runnable {

    // private RobotCanvasView robotView;

    private RobotView robotView;
    private ScenseView scenseView;

    private BorderPane border;

    private Button homeButton;
    private Button startStopButton;

    private TextField velocityField, angleField;
    private CheckBox traceRouteCheckBox;
    private Button setRouteButton;

    private Supervisor supervisor = new Supervisor();
    private RearDriveRobot robot = new RearDriveRobot();

    private boolean isGoing = false;
    private boolean isPause = false;
    private boolean setRoute = false;
    private boolean setRouteStarted = false;

    private float[][] mRoutes = new float[2000][2];
    private int routeSize = 0;
    private double drag_x0, drag_y0;

    private double home_x = 0, home_y = 0, home_theta = (float) Math.PI / 4;

    private int mMode = 0;

    private Logger log = Logger.getLogger("Local");

    private Thread timmerThread = null; // = new Thread(this );
    private boolean mStopTimer = false;

    public Pane getMainPane() {
        return border;
    }

    private boolean draged = false;

    public LocalSimulatorPane() {

        supervisor.setMode(0); // goto goal mode
        supervisor.setRobot(robot);
        border = new BorderPane();
        border.setPadding(new Insets(20, 0, 10, 5));

        robotView = new RobotView(1024, 800);
        scenseView = new ScenseView(1024, 800);
        robotView.setObstacles(scenseView.getObstacles());

        robotView.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                draged = false;

                if (setRoute && !setRouteStarted) {
                    setRouteStarted = true;
                    drag_x0 = event.getX();
                    drag_y0 = event.getY();
                    routeSize = 0;
                    Point2D p = scenseView.startRoutes(drag_x0, drag_y0);
                    mRoutes[0][0] = p.x;
                    mRoutes[0][1] = p.y;
                    routeSize++;

                }
            }
        });

        robotView.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (setRoute) {
                    Point2D p = scenseView.addRoutePoint(event.getX(), event.getY());
                    mRoutes[routeSize][0] = p.x;
                    mRoutes[routeSize][1] = p.y;
                    routeSize++;
                }
            }
        });

        robotView.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                draged = true;
            }
        });

        robotView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            public void handle(MouseEvent t) {

                if (t.getClickCount() == 1) {
                    // System.out.println("Single Click");
                    latestClickRunner = new ClickRunner(() -> {

                        if (draged || setRoute) {
                            latestClickRunner = null;
                            draged = false;
                            return;
                        }
                        double x = t.getX();
                        double y = t.getY();
                        robotView.setTarget(x, y);

                        Point2D p = robotView.getTarget();
                        double angle = 0;
                        String angleStr = angleField.getText();
                        if (angleStr != null && !angleStr.isEmpty()) {
                            try {

                                int in = Integer.valueOf(angleStr);

                                if (in <= 180)
                                    angle = (in * Math.PI) / 180;
                                else {
                                    in = in - 360;
                                    angle = (in * Math.PI) / 180;
                                }

                            } catch (Exception e) {

                            }
                        }

                        log.info("Set target to:" + p.x + "," + p.y + ": " + angle);
                        supervisor.setGoal(p.x, p.y, angle);

                        latestClickRunner = null;
                    });

                    CompletableFuture.runAsync(latestClickRunner);
                }
                if (t.getClickCount() == 2) {
                    // System.out.println("Double Click");
                    if (latestClickRunner != null) {
                        // System.out.println("-> Abort Single Click");
                        latestClickRunner.abort();
                        latestClickRunner = null;
                    }
                    double x = t.getX();
                    double y = t.getY();
                    robotView.setRobotPosition(x, y);
                    scenseView.setRobotPosition(x, y);

                    Point2D p = robotView.getRobotPosition();
                    home_x = p.x;
                    home_y = p.y;
                    robot.setPosition(home_x, home_y, home_theta);

                    double[] distances = robotView.getIrDistances();
                    supervisor.setIrDistances(distances);
                    // home_theta = angle;
                    log.info("move robot to:" + home_x + ", " + home_y);

                }
            }
        });

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(scenseView, robotView);

        ScrollPane s1 = new ScrollPane();
        s1.setContent(stackPane);

        border.setCenter(s1);
        // border.setRight(createLeftPane());

        ScrollPane s2 = new ScrollPane();
        s2.setContent(createLeftPane());
        border.setRight(s2);

        timmerThread = new Thread(this);
        timmerThread.start();
        log.info("Create the local simulator...");

        resetRobot();

    }

    /*
     * Creates a column of buttons and makes them all the same width as the largest
     * button.
     */
    private VBox createLeftPane() {

        homeButton = new Button("Home");

        homeButton.setOnAction((ActionEvent) -> {
            resetRobot();

        });

        startStopButton = new Button("Go");

        startStopButton.setOnAction((ActionEvent) -> {
            if (!isGoing) {
                startRobot();
            } else {
                stopRobot();
            }

        });

        homeButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startStopButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startStopButton.setMinWidth(Control.USE_PREF_SIZE);

        VBox vbButtons = new VBox();
        vbButtons.setSpacing(10);
        vbButtons.setPadding(new Insets(0, 20, 10, 20));

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

        Settings settings = robot.getSettings();

        velocityField = new TextField(String.valueOf(settings.velocity));

        Label label = new Label("Velocity:");
        grid.add(label, 0, 0);
        grid.add(velocityField, 1, 0);

        label = new Label("Goal angle:");
        angleField = new TextField("45");
        grid.add(label, 0, 1);
        grid.add(angleField, 1, 1);

        traceRouteCheckBox = new CheckBox("Trace route");

        traceRouteCheckBox.selectedProperty()
                .addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                    setTraceRouteMode(new_val);
                });

        setRouteButton = new Button("Start set route");

        setRouteButton.setOnAction((ActionEvent) -> {
            if (!setRoute) {
                setRouteStarted = false;
                setRouteButton.setText("Stop set route");
                setRoute = true;
            } else {
                setRouteButton.setText("Start set route");
                setRoute = false;
            }

        });

        Slider slider = new Slider();
        slider.setMin(50);
        slider.setMax(200);
        slider.setValue(100);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(10);
        slider.setBlockIncrement(20);
        slider.setSnapToTicks(true);

        final Label scalingValue = new Label("100%");

        slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            double scale = new_val.doubleValue();
            zoomRobotView(scale);
            scalingValue.setText(String.format("%.2f", scale) + "%");
        });

        Button optionButton = new Button("Options");

        optionButton.setOnAction((ActionEvent) -> {
            SettingsDialog dialog = new SettingsDialog(null);
            dialog.showAndWait();
        });

        vbButtons.getChildren().addAll(homeButton, startStopButton, grid, setRouteButton, traceRouteCheckBox, slider,
                scalingValue, optionButton);

        return vbButtons;
    }

    private void zoomRobotView(double scale) {
        robotView.setScale(scale);
        scenseView.setScale(scale);
    }

    private static final int SINGLE_CLICK_DELAY = 350;
    private ClickRunner latestClickRunner = null;

    private class ClickRunner implements Runnable {

        private final Runnable onSingleClick;
        private boolean aborted = false;

        public ClickRunner(Runnable onSingleClick) {
            this.onSingleClick = onSingleClick;
        }

        public void abort() {
            this.aborted = true;
        }

        public void run() {
            try {
                Thread.sleep(SINGLE_CLICK_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!aborted) {
                // System.out.println("Execute Single Click");
                Platform.runLater(() -> onSingleClick.run());
            }
        }
    }

    private void startRobot() {
        // mRobotView.setRecoverPoint(0, 0);
        if (isPause) {
            isPause = false;
            startStopButton.setText("Pause");
            // supervisor.reset();
            isGoing = true;
            return;

        }

        if (this.mMode == 0) // goto goal mode
        {
            startStopButton.setText("Pause");
            isGoing = true;

        } else if (this.mMode == 2) // trace route mode
        {
            if (routeSize < 5) {

                Alert information = new Alert(Alert.AlertType.INFORMATION, "Pless set the route first!");
                information.setTitle("information");
                information.showAndWait();
                return;
            }

            float x, y;
            x = mRoutes[0][0];
            y = mRoutes[0][1];

            double u_x = mRoutes[1][0] - mRoutes[0][0];
            double u_y = mRoutes[1][1] - mRoutes[0][1];
            double theta_g = Math.atan2(u_y, u_x);
            robotView.setRobotPosition(x, y, (float) theta_g, 0.3);

            double irDistances[] = robotView.getIrDistances();
            supervisor.setIrDistances(irDistances);

            supervisor.setRoute(mRoutes, routeSize);

            home_x = x;
            home_y = y;
            home_theta = theta_g;

        }

        startStopButton.setText("Pause");
        // supervisor.reset();
        isGoing = true;

    }

    private void stopRobot() {

        isPause = true;
        isGoing = false;
        startStopButton.setText("Go");
        // supervisor.reset();

    }

    public void setTraceRouteMode(boolean new_val) {
        if (new_val) {
            log.info("Change to trace route mode...");
            mMode = 2;
        } else
            mMode = 0;
        isGoing = false;
        isPause = false;
        supervisor.setMode(mMode);
    }

    private void resetRobot() {

        // mRobotView.resetRobot();
        robotView.setRobotPosition(home_x, home_y, home_theta, 0);
        robot.setPosition(home_x, home_y, home_theta);

        scenseView.resetRobotPosition(home_x, home_y);

        double irDistances[] = robotView.getIrDistances();
        supervisor.setIrDistances(irDistances);

        supervisor.reset();
        isPause = false;
    }

    public void stop() {
        mStopTimer = true;
        log.info("required to stop...");
    }

    @Override
    public void run() {

        while (true) {
            if (mStopTimer) // stop
                break;

            try {

                Thread.sleep(20);
                Platform.runLater(timmerHandler);
            } catch (Exception e) {

            }
        }
    }

    private Runnable timmerHandler = new Runnable() {

        @Override
        public void run() {
            if (isGoing && !supervisor.atGoal()) {

                supervisor.execute(0, 0, 0.02);
                RobotState state = supervisor.getRobotState();
                robotView.setRobotPosition(state.x, state.y, state.theta, state.velocity);
                scenseView.setRobotPosition(state.x, state.y, state.theta, state.velocity);
                ControllerInfo ctrlInfo = supervisor.getControllerInfo();
                robotView.setControllerInfo(ctrlInfo);

                double irDistances[] = robotView.getIrDistances();
                supervisor.setIrDistances(irDistances);

            }
        }

    };

}
package com.zmc.robot.fxrobotui;

import java.util.concurrent.CompletableFuture;

import com.zmc.robot.simulator.RearDriveRobot;
import com.zmc.robot.simulator.RobotState;
import com.zmc.robot.simulator.Supervisor;
import com.sun.javafx.geom.Point2D;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
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

public class LocalSimulatorPane implements Runnable {

    private RobotCanvasView robotView;
    private BorderPane border;

    private Button homeButton;
    private Button startStopButton;

    private TextField velocityField, angleField;

    private Supervisor supervisor = new Supervisor();
    private RearDriveRobot robot = new RearDriveRobot();

    private boolean isGoing = false;
    private boolean isPause = true;
    private boolean setRoute = false;
    private float[][] mRoutes = new float[2000][2];
    private int routeSize = 0;
    private double home_x = 0, home_y = 0, home_theta = (float) Math.PI / 4;

    private int mMode = 0;

    private Logger log = Logger.getLogger("Local");

    private Thread timmerThread = null; // = new Thread(this );
    private boolean mStopTimer = false;
    private static int MSG_TIMMER = 1;

    public Pane getMainPane() {
        return border;
    }

    public LocalSimulatorPane() {

        supervisor.setMode(0); // goto goal mode
        supervisor.setRobot(robot);
        border = new BorderPane();
        border.setPadding(new Insets(20, 0, 10, 5));

        robotView = new RobotCanvasView(1024, 800);

        robotView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            public void handle(MouseEvent t) {

                if (t.getClickCount() == 1) {
                    // System.out.println("Single Click");
                    latestClickRunner = new ClickRunner(() -> {
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
                    }
                    double x = t.getX();
                    double y = t.getY();
                    robotView.setRobotPosition(x, y);

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

        ScrollPane s1 = new ScrollPane();
        // s1.setPrefSize(800, 600);
        s1.setContent(robotView);

        border.setCenter(s1);
        border.setRight(createLeftPane());

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

        velocityField = new TextField("0.3");
        Label label = new Label("Velocity:");
        grid.add(label, 0, 0);
        grid.add(velocityField, 1, 0);

        label = new Label("Goal angle:");
        angleField = new TextField("45");
        grid.add(label, 0, 1);
        grid.add(angleField, 1, 1);

        vbButtons.getChildren().addAll(homeButton, startStopButton, grid);

        return vbButtons;
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
            startStopButton.setText("Stop");
            // supervisor.reset();
            isGoing = true;
            return;

        }

        if (this.mMode == 2) // trace route mode
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

        startStopButton.setText("Stop");
        // supervisor.reset();
        isGoing = true;

    }

    private void stopRobot() {

        isPause = true;

        startStopButton.setText("Go");
        // supervisor.reset();
        isGoing = false;

    }

    private void resetRobot() {

        // mRobotView.resetRobot();
        robotView.setRobotPosition(home_x, home_y, home_theta, 0);

        robot.setPosition(home_x, home_y, home_theta);
        double irDistances[] = robotView.getIrDistances();
        supervisor.setIrDistances(irDistances);

        supervisor.reset();
        isPause = true;
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

                double irDistances[] = robotView.getIrDistances();
                supervisor.setIrDistances(irDistances);

            }
        }

    };

}
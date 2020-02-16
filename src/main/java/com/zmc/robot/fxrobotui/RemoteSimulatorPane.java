package com.zmc.robot.fxrobotui;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.sun.javafx.geom.Point2D;
import com.zmc.robot.simulator.Settings;
import com.zmc.robot.utils.SerialPortUtil;

import org.apache.log4j.Logger;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Slider;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.geometry.HPos;
import javafx.geometry.Pos;

import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

public class RemoteSimulatorPane {

    private final RobotView robotView;
    private final ScenseView scenseView;

    private final BorderPane border;

    private Button homeButton;
    private Button startStopButton;
    private Button connectButton;
    private Button closeButton;
    private Button refreshButton;

    private CheckBox simulateModeCheckBox, simulateWithObstacleCheckBox;
    // private ToggleGroup commGroup;

    private ChoiceBox<String> commChoiceBox;
    private ChoiceBox<String> baundRateChouceBox;

    private TextField cmdField;
    private TextField velocityField, angleField;

    private SerialPortUtil serialPortUtil;
    private String selectedCom;

    private boolean isGoing = false;
    private double home_x = 0, home_y = 0;
    private final double home_theta = (float) Math.PI / 4;

    private final Logger log = Logger.getLogger("Simulator");
    private final Logger logRemote = Logger.getLogger("Remote");

    private boolean simulateWithObstacle = false;

    private final Settings m_settings = new Settings();

    private final Stage mPrimaryStage;

    private ProgressFrom mProgressFrom;

    public Pane getMainPane() {
        return border;
    }

    public RemoteSimulatorPane(final Stage primaryStage) {

        mPrimaryStage = primaryStage;

        // mProgressFrom = new ProgressFrom(null, primaryStage);

        border = new BorderPane();
        border.setPadding(new Insets(20, 0, 10, 5));
        scenseView = new ScenseView(1024, 800);
        robotView = new RobotView(1024, 800);
        robotView.setObstacles(scenseView.getObstacles());
        robotView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            public void handle(final MouseEvent t) {

                if (t.getClickCount() == 1) {
                    // System.out.println("Single Click");
                    latestClickRunner = new ClickRunner(() -> {
                        final double x = t.getX();
                        final double y = t.getY();
                        robotView.setTarget(x, y);

                        final Point2D p = robotView.getTarget();
                        double angle = 0, v = 0.15;

                        final String angleStr = angleField.getText();
                        if (angleStr != null && !angleStr.isEmpty()) {
                            try {

                                int in = Integer.valueOf(angleStr);

                                if (in <= 180)
                                    angle = (in * Math.PI) / 180;
                                else {
                                    in = in - 360;
                                    angle = (in * Math.PI) / 180;
                                }

                            } catch (final Exception e) {

                            }
                        }

                        final String vStr = velocityField.getText();
                        if (vStr != null && !vStr.isEmpty()) {
                            v = Double.valueOf(vStr);
                        }

                        log.info("Set target to (x,y; v, angle):" + p.x + "," + p.y + ": " + v + "," + angle);

                        setRemoteGoal(p.x, p.y, angle, v);

                        latestClickRunner = null;
                    });

                    CompletableFuture.runAsync(latestClickRunner);
                }
                if (t.getClickCount() == 2) {
                    System.out.println("Double Click");
                    if (latestClickRunner != null) {
                        // System.out.println("-> Abort Single Click");
                        latestClickRunner.abort();
                    }
                    final double x = t.getX();
                    final double y = t.getY();
                    robotView.setRobotPosition(x, y);
                    scenseView.setRobotPosition(x, y);

                    final Point2D p = robotView.getRobotPosition();
                    home_x = p.x;
                    home_y = p.y;
                    setRemoteRobotPosition(home_x, home_y, home_theta);
                    // home_theta = angle;
                    log.info("move robot to:" + home_x + ", " + home_y);

                }
            }
        });

        final StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(scenseView, robotView);

        final ScrollPane s1 = new ScrollPane();
        // s1.setPrefSize(800, 600);
        s1.setContent(stackPane);

        border.setCenter(s1);
        // border.setRight(createLeftPane());
        final ScrollPane s2 = new ScrollPane();
        s2.setContent(createLeftPane());
        border.setRight(s2);

    }

    /*
     * Creates a column of buttons and makes them all the same width as the largest
     * button.
     */
    private VBox createLeftPane() {

        homeButton = new Button("Home");
        startStopButton = new Button("Go");

        homeButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startStopButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startStopButton.setMinWidth(Control.USE_PREF_SIZE);

        final VBox vbButtons = new VBox();
        vbButtons.setSpacing(10);
        vbButtons.setPadding(new Insets(0, 20, 10, 20));

        vbButtons.getChildren().addAll(homeButton, startStopButton);

        homeButton.setOnAction((ActionEvent) -> {
            resetRobot();

        });

        startStopButton.setOnAction((ActionEvent) -> {
            if (!isGoing) {
                startRobot();
            } else {
                stopRobot();
            }

        });

        final GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER); // Override default
        grid.setHgap(10);
        grid.setVgap(12);

        // Use column constraints to set properties for columns in the grid
        final ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.RIGHT); // Override default
        grid.getColumnConstraints().add(column1);

        final ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.LEFT); // Override default
        grid.getColumnConstraints().add(column2);

        velocityField = new TextField(String.valueOf(0.12));
        Label label = new Label("Velocity:");
        grid.add(label, 0, 0);
        grid.add(velocityField, 1, 0);

        label = new Label("Goal angle:");
        angleField = new TextField("0");
        grid.add(label, 0, 1);
        grid.add(angleField, 1, 1);
        vbButtons.getChildren().add(grid);

        final Slider slider = new Slider();
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

        vbButtons.getChildren().add(slider);

        vbButtons.getChildren().add(scalingValue);

        slider.valueProperty().addListener(
                (final ObservableValue<? extends Number> ov, final Number old_val, final Number new_val) -> {
                    final double scale = new_val.doubleValue();
                    zoomRobotView(scale);
                    final double sv = slider.getValue();
                    System.out.println("Scale value:" + sv);

                    scalingValue.setText(String.format("%.2f", scale) + "%");
                });

        final Button clearButton = new Button("Clear");
        clearButton.setOnAction((ActionEvent) -> {
            clear();
        });

        final Button optionButton = new Button("Options");

        optionButton.setOnAction((ActionEvent) -> {
            // Settings settings = new Settings();
            loadSettings();
            // mProgressFrom.activateProgressBar();
        });

        final HBox hb11 = new HBox();
        hb11.setSpacing(15);
        hb11.getChildren().addAll(clearButton, optionButton);

        vbButtons.getChildren().add(hb11);

        simulateModeCheckBox = new CheckBox();
        simulateModeCheckBox.setText("simulate mode");

        simulateModeCheckBox.selectedProperty().addListener(
                (final ObservableValue<? extends Boolean> ov, final Boolean old_val, final Boolean new_val) -> {
                    if( new_val )
                        setSimulateMode(1);
                    else setSimulateMode(0);
                });

        simulateWithObstacleCheckBox = new CheckBox();
        simulateWithObstacleCheckBox.setText("Simulate with obstacle");
        simulateWithObstacleCheckBox.selectedProperty().addListener(
                (final ObservableValue<? extends Boolean> ov, final Boolean old_val, final Boolean new_val) -> {
                    if( new_val)
                        setSimulateMode(2);
                    else
                        setSimulateMode(0);
                    // simulateWithObstacle = new_val;
                });

        vbButtons.getChildren().add(simulateModeCheckBox);
        vbButtons.getChildren().add(simulateWithObstacleCheckBox);

        vbButtons.getChildren().add(new Label("Comm port："));

        final String[] baundRates = new String[] { "115200", "57600", "56000", "43000", "38400", "19200", "9600" };

        commChoiceBox = new ChoiceBox<String>();
        listComm();

        baundRateChouceBox = new ChoiceBox<String>();
        baundRateChouceBox.getItems().addAll(baundRates);
        baundRateChouceBox.getSelectionModel().select(0);

        refreshButton = new Button("R");
        // commChoiceBox.setOn

        refreshButton.setOnAction((final ActionEvent e) -> {
            listComm();
        });

        commChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (final ObservableValue<? extends Number> ov, final Number old_val, final Number new_val) -> {
                    selectedCom = comPorts.get(new_val.intValue());
                    enableButtons();
                    // connectToCom(selectedComm);
                });
        final HBox hbox = new HBox();

        hbox.setSpacing(10);
        hbox.setPadding(new Insets(10, 5, 10, 5));

        hbox.getChildren().addAll(commChoiceBox, refreshButton);

        vbButtons.getChildren().add(hbox);
        vbButtons.getChildren().add(baundRateChouceBox);

        final HBox hbox1 = new HBox();
        hbox1.setSpacing(10);
        hbox1.setPadding(new Insets(10, 5, 10, 5));

        connectButton = new Button("Connect");
        closeButton = new Button("Close");
        hbox1.getChildren().addAll(connectButton, closeButton);

        vbButtons.getChildren().add(hbox1);

        connectButton.setOnAction((final ActionEvent e) -> {
            connectToCom(selectedCom);
        });

        closeButton.setOnAction((final ActionEvent e) -> {
            closeCom();
        });

        vbButtons.getChildren().add(new Label("Send com comand："));

        cmdField = new TextField();
        cmdField.setOnKeyPressed((final KeyEvent e) -> {
            if (e.getCode() == KeyCode.ENTER)
                sendCmd(cmdField.getText());
        });

        vbButtons.getChildren().add(cmdField);

        final String tlpStr = "press ENTER to send!\n[gr;] get robot info.\n[od d1,d2,d3,d4,d5;]IR distance(*1000).\n[st;]Stop robot.\n[ci;] count info.\n[mm pwm;]start moto 1 sec.\n[sr pwm;]Step response.\n[sp pwm0,pwm1,step;]speed test.\n[pi kp,ki,kd;]PID param.\n[tl +-pwm;]turn around test +left -right.\n[mg x,y;]goto goal test.\n[sm 0/1;]simulate mode.\n[io 0/1;]ignore obstacle mode.\n[rs;]Reset robot.";
        // Tooltip t = new Tooltip(tlpStr);
        // Tooltip.install(cmdField, t);

        vbButtons.getChildren().add(new Label(tlpStr));

        this.enableButtons();

        return vbButtons;
    }

    private void zoomRobotView(final double scale) {
        robotView.setScale(scale);
        scenseView.setScale(scale);
    }

    private List<String> comPorts;

    private void listComm() {
        final List<String> its = commChoiceBox.getItems();

        commChoiceBox.getItems().removeAll(its);

        comPorts = SerialPortUtil.listAllPorts();
        if (comPorts.isEmpty()) {
            commChoiceBox.getItems().add("No device found!");
        } else {
            for (final String port : comPorts) {
                commChoiceBox.getItems().add(port);
            }
        }

    }

    private void enableButtons() {

        if (this.selectedCom != null && selectedCom.startsWith("COM")) {
            connectButton.setDisable(false);
        } else {
            connectButton.setDisable(true);
        }

        if (serialPortUtil != null) {
            homeButton.setDisable(false);
            startStopButton.setDisable(false);
            connectButton.setDisable(true);
            closeButton.setDisable(false);
            cmdField.setDisable(false);
            simulateModeCheckBox.setDisable(false);
            simulateWithObstacleCheckBox.setDisable( false );

        } else {
            simulateModeCheckBox.setDisable(true);
            homeButton.setDisable(true);
            startStopButton.setDisable(true);
            // connectButton.setDisable(true);
            closeButton.setDisable(true);
            cmdField.setDisable(true);

            simulateWithObstacleCheckBox.setDisable(true);

        }

    }

    private final SerialPortEventListener serialListener = new SerialPortEventListener() {

        @Override
        public void serialEvent(final SerialPortEvent event) {
            switch (event.getEventType()) {
            case SerialPortEvent.BI:/* Break interrupt */
            case SerialPortEvent.OE:/* Overrun error */
            case SerialPortEvent.FE:/* Framing error */
            case SerialPortEvent.PE:/* Parity error */
            case SerialPortEvent.CD:/* Carrier detect */
            case SerialPortEvent.CTS:/* Clear to send */
            case SerialPortEvent.DSR:/* Data set ready */
            case SerialPortEvent.RI:/* Ring indicator */
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:/* Output buffer is empty */
                break;

            case SerialPortEvent.DATA_AVAILABLE:/*
                                                 * Data available at the serial port
                                                 */
                dataAvailable();
                break;
            }

        }

    };

    private void startRobot() {
        if (!isGoing) {
            startStopButton.setText("Stop");
            sendCmd("go;");
            isGoing = true;
            return;
        }
    }

    private void clear() {
        scenseView.invalidate();
    }

    private void stopRobot() {
        startStopButton.setText("Go");
        sendCmd("st;");
        isGoing = false;
    }

    private void resetRobot() {
        // mRobotView.resetRobot();
        robotView.setRobotPosition(home_x, home_y, home_theta, 0);
        scenseView.resetRobotPosition(home_x, home_y);

        setRemoteRobotPosition(home_x, home_y, home_theta);
    }

    private void setSimulateMode(int value) {
        log.info("Set simulate mode: " + value);
        String cmdStr = "sm" + value + ";";
        if( value == 2)
            simulateWithObstacle = true;
        else
        simulateWithObstacle = false;
        sendCmd(cmdStr);
    }

    private void setRemoteGoal(final double x, final double y, final double angle, final double v) {

        DecimalFormat fmt = new DecimalFormat("0.####");
        String cmdStr = String.format("gg%s,%s,%s,%s;", 
                fmt.format(x),
                fmt.format(y),
                fmt.format(angle),
                fmt.format(v));
        sendCmd( cmdStr );
        log.info(cmdStr );

    }

    private void setRemoteRobotPosition(final double x, final double y, final double angle) {

        DecimalFormat fmt = new DecimalFormat("0.####");
        String cmdStr = String.format("rp%s,%s,%s;", 
                fmt.format(x),
                fmt.format(y),
                fmt.format(angle));
        sendCmd( cmdStr );
        log.info(cmdStr );
        setRemoteObDistance();
    }

    private void setRemoteObDistance() {
        if (!simulateWithObstacle)
            return;
            
        DecimalFormat fmt = new DecimalFormat("0.####");
        final double[] distances = robotView.getIrDistances();
        String cmdStr = "od";
        for (int i = 0; i < 5; i++) {
            cmdStr = cmdStr + fmt.format(distances[i]) + ",";
        }
        cmdStr = cmdStr + ";";
        if (simulateWithObstacle)
            sendCmd(cmdStr);

    }

    private final byte[] comBuffer = new byte[1024];
    private int bufOff = 0;

    private void dataAvailable() {
        final InputStream inputStream = serialPortUtil.getInputStream();
        try {
            while (inputStream.available() > 0) {
                final int readByte = inputStream.read();
                if (readByte == -1)
                    break;

                if (readByte == '\r' || readByte == '\n') {
                    comDataReaded(comBuffer, bufOff);
                    bufOff = 0;
                    continue;
                }
                comBuffer[bufOff++] = (byte) readByte;
                if (bufOff >= comBuffer.length) {
                    log.error("com buffer out off bound! " + bufOff);
                    log.info("Com msg: " + new String(comBuffer));
                    bufOff = 0;
                }

            }

        } catch (final Exception e) {
            log.error("Failed to read com:" + e, e);
        }

    }

    private void comDataReaded(final byte[] buf, final int len) {

        final String strValue = new String(buf, 0, len);
        if (strValue.startsWith("RP")) // robot position x,y,Q,w,v (double * 10000)
        {
            // log.info(strValue);
            positionDataReaded(strValue.substring(2));
        } else if (strValue.startsWith("ROP")) {
            settingsDataReaded(strValue.substring(3));
        } else if (strValue.startsWith("PID")) {
            pidSettingsDataReaded(strValue.substring(3));
        } else if( strValue.startsWith("READY")){
            sendCmd("cr;");
        }
        else if( strValue.startsWith("RD") 
            ||strValue.startsWith("IR")
            || strValue.startsWith("IM") )
            {
                return;
            } 
        else {
            if (len > 2)
            logRemote.info(strValue);
        }
    }

    private void settingsDataReaded(final String data) {
        final String[] datas = data.split(",");
        if (datas.length < 8) {
            log.info("Settings data error: " + data);
            return;
        }

        try {
            m_settings.sampleTime = Integer.valueOf(datas[0]);
            m_settings.min_rpm = Integer.valueOf(datas[1]);
            m_settings.max_rpm = Integer.valueOf(datas[2]);

            m_settings.wheelRadius = Double.valueOf(datas[3]);
            m_settings.wheelDistance = Double.valueOf(datas[4]);
            m_settings.atObstacle = Double.valueOf(datas[5]);
            m_settings.dfw = Double.valueOf(datas[6]);
            m_settings.unsafe = Double.valueOf(datas[7]);
            m_settings.max_w = Double.valueOf(datas[8].trim());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        // log("ROP%d,%d,%s,%s,%s,%s,%s\n", sett.min_rpm, sett.max_rpm,
        // floatToStr(0, sett.radius),
        // floatToStr(1, sett.length),

        // floatToStr(2, sett.atObstacle),
        // floatToStr(3, sett.dfw ),
        // floatToStr(4, sett.unsafe ),
        // floatToStr(5, sett.max_w )
        // );

    }

    private void pidSettingsDataReaded(final String data) {

        // log("PID1,%s,%s,%s\n", floatToStr(0, sett.kp),
        // floatToStr(1, sett.ki),
        // floatToStr(2, sett.kd));

        final String[] datas = data.split(",");
        if (datas.length < 4) {
            log.info("Settings data error: " + data);
            return;
        }

        final int type = Integer.parseInt(datas[0]);

        double p, i, d;
        try {
            p = Double.parseDouble(datas[1]);
            i = Double.parseDouble(datas[2]);
            d = Double.parseDouble(datas[3].trim());
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        switch (type) {

        case 1:
            m_settings.kp = p;
            m_settings.ki = i;
            m_settings.kd = d;
            break;
        case 2:
            m_settings.pkp = p;
            m_settings.pki = i;
            m_settings.pkd = d;
            break;
        case 3:
            m_settings.tkp = p;
            m_settings.tki = i;
            m_settings.tkd = d;
            break;
        case 4:
            m_settings.dkp = p;
            m_settings.dki = i;
            m_settings.dkd = d;

            // startOptionDialog();
            optionDialogHandler.settingsOk = true;

            Platform.runLater(optionDialogHandler);
            break;
        }

    }

    private void positionDataReaded(final String strValue) {

        // log("RP%d,%d,%d,%d,%d\n",
        // (int)(1000 * pos.x),
        // (int)(1000 * pos.y),
        // (int)(1000 * pos.theta),
        // (int)(1000 * 0), w
        // (int)(1000 * 0)); v

        final String[] datas = strValue.split(",");

        double x, y, theta, w, v;
        if (datas.length < 5)
            return;

        x = (double) Integer.valueOf(datas[0]) / 10000.0;
        y = (double) Integer.valueOf(datas[1]) / 10000.0;
        theta = (double) Integer.valueOf(datas[2]) / 10000.0;
        w = (double) Integer.valueOf(datas[3]) / 10000.0;
        v = (double) Integer.valueOf(datas[4]) / 10000.0;

        Platform.runLater(() -> {
            robotView.setRobotPosition(x, y, theta, v);
            scenseView.setRobotPosition(x, y, theta, v); // draw trails
        });

        setRemoteObDistance();
    }

    private void connectToCom(final String comStr) {
        if (comStr.startsWith("COM")) {
            String portName;
            final int idx = comStr.indexOf(',');
            portName = comStr.substring(0, idx);
            log.info("Try to connect to com: " + portName);
            try {
                serialPortUtil = SerialPortUtil.openPort(portName);
                serialPortUtil.setSerialPortEventListener(serialListener);
                enableButtons();
                // try{
                // Thread.currentThread().sleep(100);
                // }catch(Exception e)
                // {

                // }

                // loadSettings();

            } catch (final Exception e) {
                log.error("failed to open com port: " + comStr, e);
            }
        }
    }

    private void closeCom() {
        if (serialPortUtil != null) {
            log.info("Close com ...");
            serialPortUtil.close();
            serialPortUtil = null;
            enableButtons();
        }
    }

    private void sendCmd(final String cmdStr) {
        if (serialPortUtil == null)
            return;

        // log.info("Send cmd:" + cmdStr);
        try {
            serialPortUtil.write(cmdStr);
        } catch (final Exception e) {
            log.error("failed to write com!", e);
        }
    }

    private static final int SINGLE_CLICK_DELAY = 350;
    private ClickRunner latestClickRunner = null;

    private class ClickRunner implements Runnable {

        private final Runnable onSingleClick;
        private boolean aborted = false;

        public ClickRunner(final Runnable onSingleClick) {
            this.onSingleClick = onSingleClick;
        }

        public void abort() {
            this.aborted = true;
        }

        public void run() {
            try {
                Thread.sleep(SINGLE_CLICK_DELAY);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            if (!aborted) {
                // System.out.println("Execute Single Click");
                Platform.runLater(() -> onSingleClick.run());
            }
        }
    }

    public void stop() {
        // mStopTimer = true;
        log.info("required to stop...");
        closeCom();
    }

    private void sendSettings() {
        // sett.min_rpm = atoi( ptrs[0]);
        // sett.max_rpm = atoi(ptrs[1]);
        // sett.radius = atof( ptrs[2] );
        // sett.length = atof( ptrs[3] );
        // sett.atObstacle = atof( ptrs[4] );
        // sett.dfw = atof( ptrs[5] );
        // sett.unsafe = atof( ptrs[6] );
        // sett.max_w = atof( ptrs[7] );
        DecimalFormat fmt = new DecimalFormat("0.####"); 

        String cmd = String.format("sr%d,%d,%d,%s,%s,%s,%s,%s,%s\n", m_settings.sampleTime, m_settings.min_rpm, m_settings.max_rpm,
                fmt.format(m_settings.wheelRadius), 
                fmt.format(m_settings.wheelDistance), 
                fmt.format(m_settings.atObstacle), 
                fmt.format(m_settings.dfw),
                fmt.format(m_settings.unsafe), 
                fmt.format(m_settings.max_w));
        sendCmd(cmd);

        log.info(cmd);
        try{
            Thread.sleep(400);
        }catch(Exception e)
        {

        }

       
        cmd = String.format("pi1%s,%s,%s\n", fmt.format(m_settings.kp), fmt.format(m_settings.ki), fmt.format(m_settings.kd));
        sendCmd(cmd);
        log.info(cmd);

        cmd = String.format("pi2%s,%s,%s\n", fmt.format(m_settings.pkp), fmt.format(m_settings.pki), fmt.format(m_settings.pkd));

        sendCmd(cmd);
        log.info(cmd);

        try{
            Thread.sleep(400);
        }catch(Exception e)
        {

        }
        cmd = String.format("pi3%s,%s,%s\n", fmt.format(m_settings.tkp), fmt.format(m_settings.tki), fmt.format(m_settings.tkd));

        sendCmd(cmd);
        log.info(cmd);

        cmd = String.format("pi4%s,%s,%s\n",fmt.format(m_settings.dkp), fmt.format(m_settings.dki), fmt.format(m_settings.dkd));

        sendCmd(cmd);
        log.info(cmd);

    }


    private void loadSettings() {

        Alert mAlert = new Alert(Alert.AlertType.CONFIRMATION);
        mAlert.setTitle("Please wait...");
        mAlert.setHeaderText("Loading settings, please wait...");
        mAlert.setContentText(" press cancel to cancel.");
        mAlert.initOwner(mPrimaryStage);
        // mAlert.initModality(Modality.NONE); //Modality.APPLICATION_MODAL

        optionDialogHandler.mAlert = mAlert;
        optionDialogHandler.timeOut = false;
        optionDialogHandler.settingsOk = false;

        sendCmd("si;\n");

        // ButtonType buttonTypeOne = new ButtonType("OK");
        final ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        mAlert.getButtonTypes().setAll(buttonTypeCancel);

        TimmerHandler handle  = new TimmerHandler( mAlert );
        Thread thread = new Thread(handle);
        thread.start();
        final Optional<ButtonType> ret = mAlert.showAndWait();
        // if (ret.get() == buttonTypeCancel)
        //     return;
        
        if( optionDialogHandler.timeOut )  //time out....
            return;

        if( !optionDialogHandler.settingsOk ) //click cancel ????
        {
            log.info("Option canceled....");

            return;
        }


        log.info("Start Option....");
        startOptionDialog();
    }

    private void startOptionDialog() {

        final SettingsDialog dialog = new SettingsDialog(m_settings);
        final boolean ret = dialog.showAndWait();
        if (ret) {
            sendSettings();
        }

    }

    class OptionDialogHandler implements Runnable{
        
        Alert mAlert;
        boolean timeOut = false;
        boolean settingsOk = false;

        @Override
        public void run() {
            synchronized( this )
            {
                if( mAlert == null )
                    return;
            }
            if(settingsOk )
                log.info("Load settings finished...");
            if( timeOut )
                log.info("load settings time out.");

                //close the alert dialog
            for (final ButtonType bt : mAlert.getDialogPane().getButtonTypes()) {
                if (bt.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    final Button cancelButton = (Button) mAlert.getDialogPane().lookupButton(bt);
                    cancelButton.fire();
                    break;
                }
            }
            
            mAlert.close();
            mAlert = null;
        }
    }

    private OptionDialogHandler optionDialogHandler = new OptionDialogHandler();

    class TimmerHandler implements Runnable{

        Alert mAlert = null;
        public TimmerHandler(Alert alert)
        {
            mAlert = alert;
        }
        @Override
        public void run() {
            try {
                 Thread.sleep(2000);
                // Thread.currentThread().wait(2000);
            } catch (final Exception e)
            {

            }
            optionDialogHandler.timeOut = true;
            Platform.runLater(optionDialogHandler);

        }
    }

}
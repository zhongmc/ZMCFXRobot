package com.zmc.robot.fxrobotui;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.sun.javafx.geom.Point2D;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;

public class BalanceRobotPane {

    // private RobotView robotView;
    // private ScenseView scenseView;

    private CurveView curveView;

    private BorderPane border;

    private Button homeButton;
    private Button startStopButton;
    private Button connectButton;
    private Button closeButton;
    private Button refreshButton;

    private CheckBox simulateModeCheckBox;
    // private ToggleGroup commGroup;

    private ChoiceBox<String> commChoiceBox;
    private ChoiceBox<String> baundRateChouceBox;

    private CheckBox speedCheckBox, turnCheckBox;

    private TextField cmdField;

    private SerialPortUtil serialPortUtil;
    private String selectedCom;

    private TextField kpField, kdField; // kiField,
    private TextField skpField, skiField; // , skdField;
    private TextField tkpField;
    private GridPane grid;

    private TextField kgField, pwm0Field;

    private boolean isGoing = false;
    private double home_x = 0, home_y = 0, home_theta = (float) Math.PI / 4;

    private Logger log = Logger.getLogger("Balance");

    public Pane getMainPane() {
        return border;
    }

    public BalanceRobotPane() {

        border = new BorderPane();
        border.setPadding(new Insets(20, 0, 10, 5));
        curveView = new CurveView(900, 600);

        /*
         * scenseView = new ScenseView(1024, 800); robotView = new RobotView(1024, 800);
         * robotView.setObstacles(scenseView.getObstacles());
         * robotView.addEventHandler(MouseEvent.MOUSE_CLICKED, new
         * EventHandler<MouseEvent>() {
         * 
         * public void handle(MouseEvent t) {
         * 
         * if (t.getClickCount() == 1) { // System.out.println("Single Click");
         * latestClickRunner = new ClickRunner(() -> { double x = t.getX(); double y =
         * t.getY(); robotView.setTarget(x, y);
         * 
         * Point2D p = robotView.getTarget(); double angle = 0; // String angleStr =
         * angleField.getText(); // if (angleStr != null && !angleStr.isEmpty()) { //
         * try {
         * 
         * // int in = Integer.valueOf(angleStr);
         * 
         * // if (in <= 180) // angle = (in * Math.PI) / 180; // else { // in = in -
         * 360; // angle = (in * Math.PI) / 180; // }
         * 
         * // } catch (Exception e) {
         * 
         * // } // }
         * 
         * log.info("Set target to:" + p.x + "," + p.y + ": " + angle);
         * setRemoteGoal(p.x, p.y, angle);
         * 
         * latestClickRunner = null; });
         * 
         * CompletableFuture.runAsync(latestClickRunner); } if (t.getClickCount() == 2)
         * { System.out.println("Double Click"); if (latestClickRunner != null) { //
         * System.out.println("-> Abort Single Click"); latestClickRunner.abort(); }
         * double x = t.getX(); double y = t.getY(); robotView.setRobotPosition(x, y);
         * scenseView.setRobotPosition(x, y);
         * 
         * Point2D p = robotView.getRobotPosition(); home_x = p.x; home_y = p.y;
         * setRemoteRobotPosition(home_x, home_y, home_theta); // home_theta = angle;
         * log.info("move robot to:" + home_x + ", " + home_y);
         * 
         * } } });
         * 
         * StackPane stackPane = new StackPane();
         * stackPane.getChildren().addAll(scenseView, robotView);
         * 
         * ScrollPane s1 = new ScrollPane(); // s1.setPrefSize(800, 600);
         * s1.setContent(stackPane);
         * 
         * border.setCenter(s1);
         */
        ScrollPane s1 = new ScrollPane();
        s1.setContent(curveView);

        s1.setFitToHeight(true);
        s1.setFitToWidth(true);

        border.setCenter(s1);
        ScrollPane s2 = new ScrollPane();
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

        VBox vbButtons = new VBox();
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

        simulateModeCheckBox = new CheckBox();
        simulateModeCheckBox.setText("simulate mode");

        simulateModeCheckBox.selectedProperty()
                .addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                    setSimulateMode(new_val);
                });

        vbButtons.getChildren().add(simulateModeCheckBox);

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
        // kiField = new TextField();
        kdField = new TextField();

        int rowIdx = 0;
        Label label = new Label("PID for balance     ");
        grid.add(label, 0, 0, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(kpField, 1, rowIdx);

        // rowIdx++;
        // label = new Label("KI:");
        // grid.add(label, 0, rowIdx);
        // grid.add(kiField, 1, rowIdx);

        rowIdx++;
        label = new Label("KD:");
        grid.add(label, 0, rowIdx);
        grid.add(kdField, 1, rowIdx);
        rowIdx++;

        rowIdx++;
        Button rButton = new Button("Read");
        Button sButton = new Button("Set");

        rButton.setOnAction((ActionEvent e) -> {
            readBalancePID();
        });
        sButton.setOnAction((ActionEvent e) -> {
            setBalancePID();
        });

        grid.add(rButton, 0, rowIdx);
        grid.add(sButton, 1, rowIdx);

        skpField = new TextField();
        skiField = new TextField();
        // skdField = new TextField();

        rowIdx += 2;
        label = new Label("PID for Speed     ");
        grid.add(label, 0, rowIdx, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(skpField, 1, rowIdx);

        rowIdx++;
        label = new Label("KI:");
        grid.add(label, 0, rowIdx);
        grid.add(skiField, 1, rowIdx);

        rowIdx++;
        // label = new Label("KD:");
        // grid.add(label, 0, rowIdx);
        // grid.add(skdField, 1, rowIdx);
        // rowIdx++;

        speedCheckBox = new CheckBox("Add speed route");

        speedCheckBox.selectedProperty()
                .addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                    setSpeedRoute(new_val);
                });

        grid.add(speedCheckBox, 0, rowIdx, 2, 1);
        rowIdx++;

        Button srButton = new Button("Read");
        Button ssButton = new Button("Set");

        srButton.setOnAction((ActionEvent e) -> {
            readSpeedPID();
        });
        ssButton.setOnAction((ActionEvent e) -> {
            setSpeedPID();
        });

        grid.add(srButton, 0, rowIdx);
        grid.add(ssButton, 1, rowIdx);

        tkpField = new TextField();
        kgField = new TextField();
        pwm0Field = new TextField();

        rowIdx += 2;
        label = new Label("PID for Turning     ");
        grid.add(label, 0, rowIdx, 2, 2);

        rowIdx += 2;
        label = new Label("KP:");
        grid.add(label, 0, rowIdx);
        grid.add(tkpField, 1, rowIdx);

        rowIdx++;

        label = new Label("KG:");
        grid.add(label, 0, rowIdx);
        grid.add(kgField, 1, rowIdx);

        rowIdx++;
        label = new Label("PWM0:");
        grid.add(label, 0, rowIdx);
        grid.add(pwm0Field, 1, rowIdx);

        rowIdx++;

        Button trButton = new Button("Read");
        Button tsButton = new Button("Set");
        grid.add(trButton, 0, rowIdx);
        grid.add(tsButton, 1, rowIdx);

        trButton.setOnAction((ActionEvent e) -> {
            readTurnPID();
        });
        tsButton.setOnAction((ActionEvent e) -> {
            setTurnPID();
        });

        vbButtons.getChildren().add(grid);

        vbButtons.getChildren().add(new Label("Comm port："));

        String[] baundRates = new String[] { "115200", "57600", "56000", "43000", "38400", "19200", "9600" };

        commChoiceBox = new ChoiceBox<String>();
        listComm();

        baundRateChouceBox = new ChoiceBox<String>();
        baundRateChouceBox.getItems().addAll(baundRates);
        baundRateChouceBox.getSelectionModel().select(0);

        refreshButton = new Button("R");
        // commChoiceBox.setOn

        refreshButton.setOnAction((ActionEvent e) -> {
            listComm();
        });

        commChoiceBox.getSelectionModel().selectedIndexProperty()
                .addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
                    selectedCom = comPorts.get(new_val.intValue());
                    enableButtons();
                    // connectToCom(selectedComm);
                });
        HBox hbox = new HBox();

        hbox.setSpacing(10);
        hbox.setPadding(new Insets(10, 5, 10, 5));

        hbox.getChildren().addAll(commChoiceBox, refreshButton);

        vbButtons.getChildren().add(hbox);
        vbButtons.getChildren().add(baundRateChouceBox);

        HBox hbox1 = new HBox();
        hbox1.setSpacing(10);
        hbox1.setPadding(new Insets(10, 5, 10, 5));

        connectButton = new Button("Connect");
        closeButton = new Button("Close");
        hbox1.getChildren().addAll(connectButton, closeButton);

        vbButtons.getChildren().add(hbox1);

        connectButton.setOnAction((ActionEvent e) -> {
            connectToCom(selectedCom);
        });

        closeButton.setOnAction((ActionEvent e) -> {
            closeCom();
        });

        vbButtons.getChildren().add(new Label("Send com comand："));

        cmdField = new TextField();
        cmdField.setOnKeyPressed((KeyEvent e) -> {
            if (e.getCode() == KeyCode.ENTER)
                sendCmd(cmdField.getText());
        });

        vbButtons.getChildren().add(cmdField);

        String tlpStr = "press ENTER to send!\n[gr;] get robot info.\n[od d1,d2,d3,d4,d5;]IR distance(*1000).\n[st;]Stop robot.\n[ci;] count info.\n[mm pwm;]start moto 1 sec.\n[sr pwm;]Step response.\n[sp pwm0,pwm1,step;]speed test.\n[pi kp,ki,kd;]PID param.\n[tl +-pwm;]turn around test +left -right.\n[mg x,y;]goto goal test.\n[sm 0/1;]simulate mode.\n[io 0/1;]ignore obstacle mode.\n[rs;]Reset robot.";
        // Tooltip t = new Tooltip(tlpStr);
        // Tooltip.install(cmdField, t);

        vbButtons.getChildren().add(new Label(tlpStr));

        this.enableButtons();

        return vbButtons;
    }

    private void setSpeedRoute(boolean new_val) {
        String cmd;
        if (new_val)
            cmd = "cl01;";
        else
            cmd = "cl00;";

        log.info("Send cmd:" + cmd);
        this.sendCmd(cmd);
    }

    private void setTurnRoute(boolean new_val) {
        String cmd;
        if (new_val)
            cmd = "cl11;";
        else
            cmd = "cl10;";

        this.sendCmd(cmd);
    }

    private void readTurnPID() {
        this.sendCmd("PID4;");
    }

    private void setTurnPID() {
        String cmdStr = "PID4" + tkpField.getText() + "," + kgField.getText() + "," + pwm0Field.getText() + ";";
        sendCmd(cmdStr);
        log.info(cmdStr);
    }

    private void readSpeedPID() {
        this.sendCmd("PID3;");
    }

    private void setSpeedPID() {
        String cmdStr = "PID3" + skpField.getText() + "," + skiField.getText() + ",0;";
        sendCmd(cmdStr);
        log.info(cmdStr);
    }

    private void readBalancePID() {
        this.sendCmd("PID2;");

    }

    private void setBalancePID() {
        String cmdStr = "PID2" + kpField.getText() + ",0," + kdField.getText() + ";";
        sendCmd(cmdStr);
        log.info(cmdStr);
    }

    private void setPIDValueToTextField(String buf, int type) {

        int intVal;
        int idx1, idx2;
        idx1 = 0;
        String tmp;
        idx2 = buf.indexOf(',', idx1);
        tmp = buf.substring(idx1, idx2);
        String kpStr = getFormatDouble(tmp, 3);

        idx1 = idx2 + 1;
        idx2 = buf.indexOf(',', idx1);
        tmp = buf.substring(idx1, idx2);
        String kiStr = getFormatDouble(tmp, 3);

        tmp = buf.substring(idx2 + 1);
        String kdStr = getFormatDouble(tmp, 3);
        if (type == 2) {
            kpField.setText(kpStr);
            // kiField.setText(kiStr);
            kdField.setText(kdStr);
        } else if (type == 3) {
            skpField.setText(kpStr);
            skiField.setText(kiStr);
            // skdField.setText(kdStr);

        } else if (type == 4) {
            tkpField.setText(kpStr);
            kgField.setText(kiStr);
            pwm0Field.setText(kdStr);

        }

    }

    private String getFormatDouble(String valStr, int len) {
        if (valStr.length() > len) {
            int idx = valStr.length() - len;
            String retStr = valStr.substring(0, idx) + "." + valStr.substring(idx);
            return retStr;
        } else {
            String retStr = "0.";
            for (int i = 0; i < (len - valStr.length()); i++)
                retStr = retStr + "0";
            return retStr + valStr;
        }
    }

    private List<String> comPorts;

    private void listComm() {
        List<String> its = commChoiceBox.getItems();

        commChoiceBox.getItems().removeAll(its);

        comPorts = SerialPortUtil.listAllPorts();
        if (comPorts.isEmpty()) {
            commChoiceBox.getItems().add("No device found!");
        } else {
            for (String port : comPorts) {
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

        } else {
            simulateModeCheckBox.setDisable(true);
            homeButton.setDisable(true);
            startStopButton.setDisable(true);
            // connectButton.setDisable(true);
            closeButton.setDisable(true);
            cmdField.setDisable(true);

        }

    }

    private SerialPortEventListener serialListener = new SerialPortEventListener() {

        @Override
        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
            case SerialPortEvent.BI:/*
                                     * Break interrupt
                                     */
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

    private void stopRobot() {
        startStopButton.setText("Go");
        sendCmd("bs;");
        isGoing = false;
    }

    private void setSimulateMode(boolean value) {
        log.info("Set simulate mode: " + value);
        String cmdStr = "sm";
        if (value)
            cmdStr = cmdStr + "1;";
        else
            cmdStr = cmdStr + "0;";

        sendCmd(cmdStr);
    }

    private void resetRobot() {
        // robotView.setRobotPosition(home_x, home_y, home_theta, 0);
        // scenseView.resetRobotPosition(home_x, home_y);

        setRemoteRobotPosition(home_x, home_y, home_theta);
    }

    private void setRemoteGoal(double x, double y, double angle) {
        // todo
        int intv;
        intv = (int) (x * 1000.0);
        String cmdStr = "gg" + intv + ",";
        intv = (int) (y * 1000);
        cmdStr = cmdStr + intv + ",";
        intv = (int) (angle * 1000);
        cmdStr = cmdStr + intv + ";";
        this.sendCmd(cmdStr);

    }

    private void setRemoteRobotPosition(double x, double y, double angle) {
        // todo
        int intv;
        intv = (int) (x * 1000.0);
        String cmdStr = "rp" + intv + ",";
        intv = (int) (y * 1000);
        cmdStr = cmdStr + intv + ",";
        intv = (int) (angle * 1000);
        cmdStr = cmdStr + intv + ";";
        this.sendCmd(cmdStr);

        setRemoteObDistance();
    }

    private void setRemoteObDistance() {

        // double[] distances = robotView.getIrDistances();
        // String cmdStr = "od";
        // for (int i = 0; i < 5; i++) {
        // int intv = (int) (distances[i] * 1000);
        // cmdStr = cmdStr + intv + ",";
        // }

        // cmdStr = cmdStr + ";";

        // sendCmd(cmdStr);

    }

    private byte[] comBuffer = new byte[1024];
    private int bufOff = 0;

    private void dataAvailable() {
        InputStream inputStream = serialPortUtil.getInputStream();
        try {
            while (inputStream.available() > 0) {
                int readByte = inputStream.read();
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

        } catch (Exception e) {
            log.error("Failed to read com：", e);
            log.info(new String(comBuffer, 0, bufOff));
            bufOff = 0; // reset
        }

    }

    private double angleValue[] = new double[100];

    private void comDataReaded(byte[] buf, int len) {

        String strValue = new String(buf, 0, len);
        if (strValue.startsWith("RP")) // robot position
        {
            // log.info(strValue);
            int intVal;
            int idx1, idx2;
            idx1 = 2;
            idx2 = strValue.indexOf(',', idx1);
            String tmp = strValue.substring(idx1, idx2);

            intVal = Integer.valueOf(tmp);
            double x = (double) intVal / 10000;
            idx1 = idx2 + 1;
            idx2 = strValue.indexOf(',', idx1);
            tmp = strValue.substring(idx1, idx2);
            intVal = Integer.valueOf(tmp);
            double y = (double) intVal / 10000;

            idx1 = idx2 + 1;
            idx2 = strValue.indexOf(',', idx1);
            tmp = strValue.substring(idx1, idx2);
            intVal = Integer.valueOf(tmp);
            double theta = (double) intVal / 10000;

            tmp = strValue.substring(idx2 + 1);
            double v = Double.valueOf(tmp);

            // robotView.setRobotPosition(x, y, theta, v);
            // scenseView.setRobotPosition(x, y, theta, v); // draw trails

            // setRemoteObDistance();

        } else if (strValue.startsWith("MU")) {

            int intVal;
            int idx1, idx2;
            idx1 = 2;
            String tmp;
            int count = 0;
            while (true) {
                idx2 = strValue.indexOf(',', idx1);
                if (idx2 == -1)
                    tmp = strValue.substring(idx1);
                else
                    tmp = strValue.substring(idx1, idx2);
                intVal = Integer.valueOf(tmp);
                angleValue[count++] = (double) intVal / 100;
                if (idx2 == -1)
                    break;
                idx1 = idx2 + 1;
            }

            double curveData[] = new double[count];
            System.arraycopy(angleValue, 0, curveData, 0, count);

            Platform.runLater(() -> {
                curveView.addData(curveData, curveData.length);
            });

            // curveView.addData(angleValue, count);
        } else if (strValue.startsWith("PID")) {

            int type = buf[3] - '0';
            Platform.runLater(() -> {
                setPIDValueToTextField(strValue.substring(4), type);
            });

        }

        // else if (strValue.startsWith("PB")) {
        // Platform.runLater(() -> {
        // setPIDValueToTextField(strValue.substring(2), 2);

        // });
        // } else if (strValue.startsWith("PS")) {
        // Platform.runLater(() -> {
        // setPIDValueToTextField(strValue.substring(2), 3);

        // });
        // }

        else {
            if (len > 2)
                log.info(strValue);
        }
    }

    private class CurveRunner implements Runnable {

        private double[] curveData;

        public CurveRunner(double[] curveData) {
            this.curveData = curveData;
        }

        public void run() {
            curveView.addData(curveData, curveData.length);
        }
    }

    private void connectToCom(String comStr) {
        if (comStr.startsWith("COM")) {
            String portName;
            int idx = comStr.indexOf(',');
            portName = comStr.substring(0, idx);
            log.info("Try to connect to com: " + portName);
            try {
                serialPortUtil = SerialPortUtil.openPort(portName);
                serialPortUtil.setSerialPortEventListener(serialListener);
                enableButtons();
            } catch (Exception e) {
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

    private void sendCmd(String cmdStr) {
        if (serialPortUtil == null)
            return;

        // log.info("Send cmd:" + cmdStr);
        try {
            serialPortUtil.write(cmdStr);
        } catch (Exception e) {
            log.error("failed to write com!", e);
        }
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

    public void stop() {
        // mStopTimer = true;
        log.info("required to stop...");
        closeCom();
    }

}
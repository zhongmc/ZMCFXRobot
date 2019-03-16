package com.zmc.robot.fxrobot;

import com.zmc.robot.fxrobotui.BalanceRobotPane;
import com.zmc.robot.fxrobotui.LocalSimulatorPane;
import com.zmc.robot.fxrobotui.RemoteSimulatorPane;
import com.zmc.robot.fxrobotui.SettingsPane;
import com.zmc.robot.utils.TextAreaAppender;

import javafx.application.Application;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ZMCFXRobot extends Application {

    // private final Menu comMenu = new Menu("COMM Port");
    private final TextArea loggingView = new TextArea();
    // private SettingsPane settingsPane;

    private LocalSimulatorPane localSimulatorPane;
    private RemoteSimulatorPane remoteSimulatorPane;

    private BalanceRobotPane balancePane;

    // private RobotCanvasView robotView;

    @Override
    public void start(Stage primaryStage) throws Exception {

        TabPane tabs = new TabPane();

        localSimulatorPane = new LocalSimulatorPane();
        remoteSimulatorPane = new RemoteSimulatorPane();
        balancePane = new BalanceRobotPane();

        Tab tabSimulator = new Tab();
        tabSimulator.setText("Simulator");
        tabSimulator.setContent(localSimulatorPane.getMainPane());

        // Tab tabRemoteSimulator = new Tab();
        // tabRemoteSimulator.setText("Remote Simulator");
        // tabRemoteSimulator.setContent(remoteSimulatorPane.getMainPane());

        Tab tabRemoteSimulator = new Tab();
        tabRemoteSimulator.setText("Remote Simulator");
        tabRemoteSimulator.setContent(remoteSimulatorPane.getMainPane());

        Tab tabBalance = new Tab();
        tabBalance.setText("Balance");
        tabBalance.setContent(balancePane.getMainPane());

        // settingsPane = new SettingsPane();

        // Tab tabSettings = new Tab();
        // tabSettings.setText("Settings");
        // tabSettings.setContent(settingsPane.getMainPane());

        tabs.getTabs().addAll(tabSimulator, tabRemoteSimulator, tabBalance);// , tabSettings);

        SplitPane splitPane = new SplitPane();

        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8, 0.2);

        setupLogginView();

        splitPane.getItems().addAll(tabs, loggingView);

        // VBox vbox = new VBox(menuBar, splitPane);

        Scene scene = new Scene(splitPane, 1024, 600); // Manage scene size
        primaryStage.setTitle("ZMC FX Robot");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    @Override
    public void stop() throws Exception {

        // settingsPane.stop();

        localSimulatorPane.stop();
        remoteSimulatorPane.stop();
        balancePane.stop();

    }

    private void setupLogginView() {
        TextAreaAppender.setTextArea(loggingView);
        loggingView.setWrapText(true);
        loggingView.appendText("Starting ZMC FXRobot...\n");
        loggingView.setEditable(false);

        final MenuItem copyItem = new MenuItem("Copy ");
        MenuItem clearItem = new MenuItem("Clear");
        MenuItem selectAllItem = new MenuItem("Select All");
        ContextMenu ctxMenu = new ContextMenu();
        ctxMenu.getItems().add(clearItem);
        ctxMenu.getItems().add(new SeparatorMenuItem());
        ctxMenu.getItems().add(copyItem);
        ctxMenu.getItems().add(selectAllItem);

        loggingView.setContextMenu(ctxMenu);

        ctxMenu.setOnShowing((WindowEvent e) -> {
            String selT = loggingView.getSelectedText();
            if (selT != null && selT.length() > 0) {
                copyItem.setDisable(false);
            } else
                copyItem.setDisable(true);

        });

        clearItem.setOnAction((ActionEvent e) -> {
            loggingView.setText("");
        });

        selectAllItem.setOnAction((ActionEvent e) -> {
            loggingView.selectAll();
        });

        copyItem.setOnAction((ActionEvent e) -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(loggingView.getSelectedText());
            clipboard.setContent(content);
        });

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        launch(ZMCFXRobot.class, args);
    }

}

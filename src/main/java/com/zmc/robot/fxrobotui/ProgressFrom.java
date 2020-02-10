package com.zmc.robot.fxrobotui;

import org.apache.log4j.Logger;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class ProgressFrom {

    private static final Logger logger = Logger.getLogger(ProgressFrom.class);

    private Stage dialogStage;
    private ProgressIndicator progressIndicator;

    private int ret = 0;

    public ProgressFrom(final Task<?> task,Stage primaryStage) {

        dialogStage = new Stage();
        progressIndicator = new ProgressIndicator();

        // 窗口父子关系
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        // progress bar
        Label label = new Label("数据加载中, 请稍后...");
        label.setTextFill(Color.BLUE);
        //label.getStyleClass().add("progress-bar-root");
        progressIndicator.setProgress(-1F);
        //progressIndicator.getStyleClass().add("progress-bar-root");
        if( task != null )
         progressIndicator.progressProperty().bind(task.progressProperty());

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setBackground(Background.EMPTY);

        Button cancelButton = new Button("cancel");

        cancelButton.setOnAction((ActionEvent event)-> 
        {
            dialogStage.close(); 
            ret = 1;
        }); 

        vBox.getChildren().addAll(progressIndicator,label, cancelButton);

        Scene scene = new Scene(vBox);
        scene.setFill(null);
        dialogStage.setScene(scene);

        vBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            public void handle(MouseEvent t) {
                dialogStage.close();  
                ret = 1; 
            }
        });

        // Thread inner = new Thread(task);
        // inner.start();

        if( task != null )
        {
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                public void handle(WorkerStateEvent event) {
                    dialogStage.close();
                }
            });
        }
        logger.info("UI");
    }

    public void activateProgressBar() {
        dialogStage.show();
    }

    public Stage getDialogStage(){
        return dialogStage;
    }

    public void cancelProgressBar() {
        dialogStage.close();
    }

    public int getRet(){
        return ret;
    }
}

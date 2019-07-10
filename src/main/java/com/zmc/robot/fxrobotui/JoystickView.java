package com.zmc.robot.fxrobotui;

import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.sun.javafx.geom.Point2D;
import com.zmc.robot.simulator.ControllerInfo;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.net.URL;
import javafx.scene.input.MouseEvent;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.scene.image.Image;

public class JoystickView extends Canvas {

    private double width, height;
    private GraphicsContext gc;

    private boolean inTouch = false;

    // private Image image;

    // private boolean draged;
    private double tcx0, tcy0, tcx, tcy;
    // private double rotateAngle = 0, rotateRad = 0;

    private double angle = 0, throttle = 0;

    public JoystickView(double width, double height) {
        super(width, height);

        this.width = width;
        this.height = height;

        // image = new Image("/images/steer.png");

        gc = getGraphicsContext2D();
        draw(gc);
        init();
    }

    private EventHandler<JoystickEvent> handler;

    public void setActionHandler(EventHandler<JoystickEvent> handler) {
        this.handler = handler;
    }

    public void init() {

        addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                tcx0 = event.getX();
                tcy0 = event.getY();
                tcx = tcx0;
                tcy = tcy0;

                throttle = 0;
                angle = 0;
                inTouch = true;
                invalidate();

            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // rotateAngle = 0;
                // rotateRad = 0;
                inTouch = false;
                angle = 0;
                throttle = 0;
                invalidate();
                if (handler != null)

                {
                    // JoystickEvent jevent = new JoystickEvent();
                    // jevent.setAngle(angle);
                    // jevent.setThrottle(throttle);

                    handler.handle(new JoystickEvent(angle, throttle));
                }
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // draged = true;

                tcx = event.getX();
                tcy = event.getY();

                angle = -2 * (tcx - tcx0) / (width - 20);
                if (angle > 1)
                    angle = 1;
                else if (angle < -1)
                    angle = -1;

                throttle = -2 * (tcy - tcy0) / (width - 20);

                if (throttle < -1)
                    throttle = -1;
                else if (throttle > 1)
                    throttle = 1;

                invalidate();
                if (handler != null) {
                    // JoystickEvent jevent = new JoystickEvent();
                    // jevent.setAngle(angle);
                    // jevent.setThrottle(throttle);
                    // handler.handle(jevent);
                    handler.handle(new JoystickEvent(angle, throttle));
                }
            }
        });

    }

    public double getRotateRad() {
        return 0;
    }

    public void invalidate() {
        gc.clearRect(0, 0, width, height);
        draw(gc);
    }

    public void draw(GraphicsContext gc) {

        gc.setFill(Color.AZURE);
        gc.fillRect(2, 2, width - 4, height - 4);
        Font f = Font.font(null, FontWeight.THIN, 12);
        gc.setFont(f);
        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.DARKGRAY);
        gc.setLineWidth(1);
        gc.fillText("click/touch to use joystic", 50, height / 2 - 3);

        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.SNOW); // WHITESMOKE);
        gc.setLineWidth(2);

        double h = 15;
        gc.fillRoundRect(10, 10, width - 20, h, 10, 10);
        gc.fillRoundRect(10, 10 + h + 5, width - 20, h, 10, 10);

        if (!inTouch)
            return;

        double w;

        String angleStr = String.format("%.2f", angle);
        if (angle > 0) {
            gc.setFill(Color.GREEN);
            w = angle * (width / 2 - 10);
            gc.fillRect(width / 2, 10, w, h);
            gc.setFill(Color.WHITE);
            gc.fillText(angleStr, width / 2 + 10, 10 + h - 2);

        } else if (angle < 0) {
            gc.setFill(Color.RED);
            w = -angle * (width / 2 - 10);
            gc.fillRect(width / 2 - w, 10, w, h);
            gc.setFill(Color.WHITE);
            gc.fillText(angleStr, width / 2 - 40, 10 + h - 2);
        }

        String throttleStr = String.format("%.2f", throttle);
        if (throttle > 0) {
            gc.setFill(Color.GREEN);
            w = throttle * (width / 2 - 10);
            gc.fillRect(width / 2, 15 + h, w, h);
            gc.setFill(Color.WHITE);
            gc.fillText(throttleStr, width / 2 + 10, 15 + 2 * h - 2);
        } else if (throttle < 0) {
            gc.setFill(Color.RED);
            w = -throttle * (width / 2 - 10);
            gc.fillRect(width / 2 - w, 15 + h, w, h);
            gc.setFill(Color.WHITE);
            gc.fillText(throttleStr, width / 2 - 40, 15 + 2 * h - 2);
        }

        double d = width - 20;
        gc.setFill(Color.web("#E6E6FA", 0.6));
        gc.fillOval(tcx0 - d / 2, tcy0 - d / 2, d, d);

        gc.setFill(Color.web("#87CEFA", 0.6));
        d = width / 2;
        gc.fillOval(tcx - d / 2, tcy - d / 2, d, d);
    }

    @Override
    public double minHeight(double h) {
        return height;
    }

    @Override
    public double maxHeight(double h) {
        return height;
    }

    @Override
    public double prefHeight(double h) {
        return minHeight(h);
    }

    @Override
    public double minWidth(double w) {
        return width;
    }

    @Override
    public double maxWidth(double w) {
        return width;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {

        System.out.println("set to w " + width + ", h " + height);
        System.out.println("org w " + this.width + ", h " + this.height);

        super.setWidth(width);
        super.setHeight(height);
        this.width = width;
        this.height = height;
        this.invalidate();
        // paint();
    }

}
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

public class SteerView extends Canvas {

    private double width, height;
    private GraphicsContext gc;

    private Image image;

    // private boolean draged;
    private double tcx, tcy;
    private double rotateAngle = 0, rotateRad = 0;

    public SteerView(double width, double height) {
        super(width, height);

        this.width = width;
        this.height = height;

        image = new Image("/images/steer.png");

        gc = getGraphicsContext2D();
        draw(gc);
        init();
    }

    private EventHandler<ActionEvent> handler;

    public void setActionHandler(EventHandler<ActionEvent> handler) {
        this.handler = handler;
    }

    public void init() {

        addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                tcx = event.getX();
                tcy = event.getY();

            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                rotateAngle = 0;
                rotateRad = 0;
                invalidate();
                if (handler != null)
                    handler.handle(null);
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // draged = true;

                double cx = event.getX();
                double cy = event.getY();

                double x0, y0;

                x0 = width / 2;
                y0 = height / 2;

                double x, y;
                x = x0 - tcx;
                y = y0 - tcy;
                double b = Math.sqrt(x * x + y * y);
                x = x0 - cx;
                y = y0 - cy;
                double c = Math.sqrt(x * x + y * y);

                x = cx - tcx;
                y = cy - tcy;
                double a = Math.sqrt(x * x + y * y);

                if (b * c == 0)
                    return;

                double cos = (b * b + c * c - a * a) / (2 * b * c);
                double rad = Math.acos(cos);
                int ang = (int) (180 * rad / Math.PI);

                // Log.i(TAG, x0 + "," + y0 +";" + tcx + ", " + tcy + "; " + cx
                // + ", " + cy );
                // Log.i(TAG, a + ", " + b + ", " + c);
                // Log.i(TAG, "Touch Ang:" + ang );

                rad = -rad;

                if (tcx < x0 && cy > tcy) {
                    ang = -1 * ang;
                    rad = -rad;
                } else if (tcx > x0 && cy < tcy) {
                    ang = -1 * ang;
                    rad = -rad;
                }

                rotateAngle = ang;
                invalidate();
                if (handler != null && Math.abs(rotateRad - rad) > 0.01) {
                    rotateRad = rad;
                    handler.handle(null);
                }
            }
        });

    }

    public double getRotateRad() {
        return rotateRad;
    }

    public void invalidate() {
        gc.clearRect(0, 0, width, height);
        draw(gc);
    }

    public void draw(GraphicsContext gc) {

        gc.save();
        gc.translate(width / 2, height / 2);
        gc.rotate(rotateAngle);
        gc.translate(-width / 2, -height / 2);
        gc.drawImage(image, 0, 0, width, height);
        gc.restore();

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
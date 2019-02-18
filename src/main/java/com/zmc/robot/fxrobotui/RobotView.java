package com.zmc.robot.fxrobotui;

import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.sun.javafx.geom.Point2D;

public class RobotView extends Canvas {

    // private List<Obstacle> obstacles = new ArrayList<Obstacle>();

    private double x, y, theta;
    private double velocity;

    // public double width, height;

    // public double canvas_width, canvas_height;
    // private double scroll_x, scroll_y;
    // public double x_off = 0, y_off = 0;
    // private double scroll_width, scroll_height; //可滚动的宽度和高度

    private double targetX = -1, targetY = 1;
    private double mScale = 100;
    // the canvas dimension
    // private double cw = 5.5, ch = 7.5;

    private double width, height;
    private GraphicsContext gc;
    private RearDriveRobotUI robot;

    public RobotView(double width, double height) {
        super(width, height);

        this.width = width;
        this.height = height;

        robot = new RearDriveRobotUI();

        // robot.setObstacles(obstacles);

        robot.setCavasDimension(width, height);
        robot.setPosition(0.5, 0.5, Math.PI / 4);
        gc = getGraphicsContext2D();
        draw(gc);
    }

    public void invalidate() {
        gc.clearRect(0, 0, width, height);
        draw(gc);
    }

    public void draw(GraphicsContext gc) {

        gc.setStroke(Color.RED);//  设置红色  
        gc.setLineWidth(2);

        // gtg target
        gc.strokeLine((width / 2 + targetX * mScale - 20), (height / 2 - targetY * mScale),
                (width / 2 + targetX * mScale + 20), (height / 2 - targetY * mScale));
        gc.strokeLine((width / 2 + targetX * mScale), (height / 2 - targetY * mScale - 20),
                (width / 2 + targetX * mScale), (height / 2 - targetY * mScale + 20));

        robot.draw(gc);
    }

    public Point2D getTarget() {
        Point2D p = new Point2D((float) targetX, (float) targetY);
        return p;

    }

    public Point2D getRobotPosition() {
        Point2D p = new Point2D((float) robot.x, (float) robot.y);
        return p;

    }

    // 以屏幕坐标设定目标点
    public void setTarget(double x, double y) {
        targetX = (x - width / 2) / mScale;
        targetY = (height / 2 - y) / mScale;
        invalidate();

    }

    // 以屏幕坐标设定位置
    public void setRobotPosition(double x, double y) {

        double rx = (x - width / 2) / mScale;
        double ry = (height / 2 - y) / mScale;
        robot.setPosition(rx, ry, Math.PI / 4);
        this.invalidate();

    }

    public double[] getIrDistances() {
        return robot.getIrDistances();
    }

    // 使用robot的原始坐标
    public void setRobotPosition(double x, double y, double theta, double velocity) {

        double delt1, delt2, delt3;
        delt1 = Math.abs(x - this.x);
        delt2 = Math.abs(y - this.y);
        delt3 = Math.abs(theta - this.theta);

        if (delt1 < 0.0001 && delt2 < 0.0001 && delt3 < 0.0001)
            return;

        this.x = x;
        this.y = y;
        this.theta = theta;
        this.velocity = velocity;
        robot.setPosition(x, y, theta);
        this.invalidate();

    }

    public void setObstacles(List<Obstacle> obstacles) {
        robot.setObstacles(obstacles);
    }

}
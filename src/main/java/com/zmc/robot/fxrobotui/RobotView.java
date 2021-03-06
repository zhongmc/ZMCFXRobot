package com.zmc.robot.fxrobotui;

import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.sun.javafx.geom.Point2D;
import com.zmc.robot.simulator.ControllerInfo;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RobotView extends Canvas {

    // private List<Obstacle> obstacles = new ArrayList<Obstacle>();

    private double x, y, theta;
    private double velocity;
    private ControllerInfo mCtrlInfo = null;

    // public double width, height;

    // public double canvas_width, canvas_height;
    // private double scroll_x, scroll_y;
    // public double x_off = 0, y_off = 0;
    // private double scroll_width, scroll_height; //可滚动的宽度和高度

    private double targetX = -1, targetY = 1;
    private double mScale = 100;

    private double canvasWidth, canvasHeight; // 画布大小

    // the canvas dimension
    // private double cw = 5.5, ch = 7.5;

    private double width, height;
    private GraphicsContext gc;
    private RearDriveRobotUI robot;

    private String targetStr = "";

    public RobotView(double canvasWidth, double canvasHeight, double scale) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.mScale = scale;

        this.width = canvasWidth * mScale;
        this.height = canvasHeight * mScale;
        this.setWidth(width);
        this.setHeight(height);

        robot = new RearDriveRobotUI();

        // robot.setObstacles(obstacles);

        robot.setCavasDimension(width, height, scale);
        robot.setPosition(0.5, 0.5, Math.PI / 4, 0);
        gc = getGraphicsContext2D();
        draw(gc);
    }

    public void setScale(double scale) {
        this.mScale = scale;

        this.width = canvasWidth * mScale;
        this.height = canvasHeight * mScale;
        this.setWidth(width);
        this.setHeight(height);
        robot.setCavasDimension(width, height, scale);
        invalidate();
    }

    public RobotView(double width, double height) {
        super(width, height);

        this.width = width;
        this.height = height;
        canvasWidth = width / mScale;
        canvasHeight = height / mScale;

        robot = new RearDriveRobotUI();

        // robot.setObstacles(obstacles);

        robot.setCavasDimension(width, height, mScale);
        robot.setPosition(0.5, 0.5, Math.PI / 4, 0);
        gc = getGraphicsContext2D();
        draw(gc);
    }

    public void invalidate() {
        gc.clearRect(0, 0, width, height);
        draw(gc);
    }

    public void draw(GraphicsContext gc) {

        gc.setStroke(Color.RED);//  设置红色  
        gc.setLineWidth(1);

        double xs, ys, xe, ye;

        xs = width / 2 + targetX * mScale;
        ys = height / 2 - targetY * mScale;

        // gtg target
        gc.strokeLine((xs - 20), ys, (xs + 20), (ys));
        gc.strokeLine(xs, (ys - 20), xs, (ys + 20));

        Font f = Font.font(null, FontWeight.THIN, 12);
        gc.setFont(f);
        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.DARKGRAY);
        gc.setLineWidth(1);
        gc.fillText(targetStr, xs + 3, ys - 3);

        robot.draw(gc);

        if (mCtrlInfo == null)
            return;

        double x, y;
        x = robot.x;
        y = robot.y;

        xs = width / 2 + mScale * x;
        ys = height / 2 - mScale * y;

        if (mCtrlInfo.uAoidObstacle != null) {
            gc.setStroke(Color.GREEN);
            xe = (float) (width / 2 + mScale * (x + mCtrlInfo.uAoidObstacle.x));
            ye = (float) (height / 2 - mScale * (y + mCtrlInfo.uAoidObstacle.y));

            gc.strokeLine(xs, ys, xe, ye);

        }

        if (mCtrlInfo.uFollowWall != null) {
            gc.setStroke(Color.RED);
            xe = (float) (width / 2 + mScale * (x + mCtrlInfo.uFollowWall.x));
            ye = (float) (height / 2 - mScale * (y + mCtrlInfo.uFollowWall.y));

            gc.strokeLine(xs, ys, xe, ye);

        }

        if (mCtrlInfo.uGotoGoal != null) {
            gc.setStroke(Color.CYAN);
            xe = (float) (width / 2 + mScale * (x + mCtrlInfo.uGotoGoal.x));
            ye = (float) (height / 2 - mScale * (y + mCtrlInfo.uGotoGoal.y));

            gc.strokeLine(xs, ys, xe, ye);

        }

        if (mCtrlInfo.uFwP != null) {
            gc.setStroke(Color.BLUE);
            xe = (float) (width / 2 + mScale * (x + mCtrlInfo.uFwP.x));
            ye = (float) (height / 2 - mScale * (y + mCtrlInfo.uFwP.y));

            gc.strokeLine(xs, ys, xe, ye);

        }

        if (mCtrlInfo.p0 != null) {
            gc.setStroke(Color.CYAN);
            xs = (float) (width / 2 + mScale * (mCtrlInfo.p0.x));
            ys = (float) (height / 2 - mScale * (mCtrlInfo.p0.y));
            xe = (float) (width / 2 + mScale * (mCtrlInfo.p1.x));
            ye = (float) (height / 2 - mScale * (mCtrlInfo.p1.y));
            gc.strokeLine(xs, ys, xe, ye);
        }
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

        targetX = Math.round(targetX * 10) / 10.0;
        targetY = Math.round(targetY * 10) / 10.0;

        targetStr = String.format("%.2f,%.2f", targetX, targetY);
        invalidate();

    }

    // 以屏幕坐标设定位置
    public void setRobotPosition(double x, double y) {

        double rx = (x - width / 2) / mScale;
        double ry = (height / 2 - y) / mScale;
        robot.setPosition(rx, ry, Math.PI / 4, 0);
        this.invalidate();

    }

    public double[] getIrDistances() {
        return robot.getIrDistances();
    }

    // 请再setRobotPosition前调用
    public void setControllerInfo(ControllerInfo ctrlInfo) {
        this.mCtrlInfo = ctrlInfo;
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
        robot.setPosition(x, y, theta, velocity);
        this.invalidate();

    }

    public void setObstacles(List<Obstacle> obstacles) {
        robot.setObstacles(obstacles);
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

        System.out.println("resize: " + mScale);
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
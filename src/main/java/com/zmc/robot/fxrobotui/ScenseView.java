package com.zmc.robot.fxrobotui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.sun.javafx.geom.Point2D;

public class ScenseView extends Canvas {

    private List<Obstacle> obstacles = new ArrayList<Obstacle>();

    private double x, y, theta;
    private double velocity;

    // public double width, height;

    public double canvas_width, canvas_height;
    // private double scroll_x, scroll_y;
    // public double x_off = 0, y_off = 0;
    // private double scroll_width, scroll_height; //可滚动的宽度和高度

    private double targetX = -1, targetY = 1;
    private double mScale = 100;
    // the canvas dimension
    // private double cw = 5.5, ch = 7.5;

    // private int mTrailSize = 900;
    // private float[][] mTrails = new float[1000][2];
    // private int mTrailCount = 0;

    private double lastTrailX, lstTrailY;

    private float[][] mRoutes;
    private int routeSize = 0;

    private double width, height;
    private GraphicsContext gc;
    private RearDriveRobotUI robot;

    public ScenseView(double width, double height) {
        super(width, height);

        this.width = width;
        this.height = height;

        initObstacles();

        // robot = new RearDriveRobotUI();
        // robot.setObstacles(obstacles);
        // robot.setCavasDimension(width, height);
        // robot.setPosition(0.5, 0.5, Math.PI / 4);
        gc = getGraphicsContext2D();
        draw(gc);
    }

    public void invalidate() {
        gc.clearRect(0, 0, width, height);
        draw(gc);
    }

    public void draw(GraphicsContext gc) {

        drawLabel(gc);

        // gc.setStroke(Color.RED);//  设置红色  
        // gc.setLineWidth(2);
        // // gtg target
        // gc.strokeLine((width / 2 + targetX * mScale - 20), (height / 2 - targetY *
        // mScale),
        // (width / 2 + targetX * mScale + 20), (height / 2 - targetY * mScale));
        // gc.strokeLine((width / 2 + targetX * mScale), (height / 2 - targetY * mScale
        // - 20),
        // (width / 2 + targetX * mScale), (height / 2 - targetY * mScale + 20));

        gc.setFill(Color.YELLOWGREEN);
        gc.strokeOval(50, 80, 50, 50);
        gc.fillOval(100, 80, 50, 50);

        gc.strokeOval(500, 80, 50, 50);

        for (Obstacle obs : obstacles) {
            obs.draw(gc);
        }
        // // draw the trails of the robot
        // drawTrails(gc);
        // draw the route
        drawRoutes(gc);
        // robot.draw(gc);
    }

    private void drawRoutes(GraphicsContext gc) {

        if (mRoutes == null || routeSize == 0)
            return;

        // draw the route

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        double xs, ys, xe, ye;

        xs = (width / 2 + mRoutes[0][0] * mScale);
        ys = (height / 2 - mRoutes[0][1] * mScale);

        gc.strokeOval(xs, ys, 30, 30);

        gc.setLineWidth(1);
        for (int i = 1; i < routeSize - 1; i++) {
            xe = (width / 2 + mRoutes[i][0] * mScale);
            ye = (height / 2 - mRoutes[i][1] * mScale);
            gc.strokeLine(xs, ys, xe, ye);
            xs = xe;
            ys = ye;
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);

        double x0, y0;
        for (int i = 1; i < routeSize - 1; i++) {
            x0 = (width / 2 + mRoutes[i][0] * mScale);
            y0 = (height / 2 - mRoutes[i][1] * mScale);
            xs = x0 - 2;
            xe = x0 + 2;
            ys = y0;
            ye = y0;
            gc.strokeLine(xs, ys, xe, ye);
            xs = x0;
            xe = x0;
            ys = y0 - 2;
            ye = y0 + 2;
            gc.strokeLine(xs, ys, xe, ye);
        }

    }

    private void drawLabel(GraphicsContext gc) {

        gc.setLineWidth(1);

        double curPos = 0;
        double step = 0.1 * mScale;
        int idx = 0;
        double hh = 10;

        double w, h;

        w = width;
        h = height;

        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeLine(0.0, h / 2, w, h / 2);
        gc.strokeLine(w / 2, 0, w / 2, h);

        while (true) {
            idx++;
            curPos = curPos + step;
            if (curPos > w / 2)
                break;

            if (idx % 10 == 0) {
                gc.setStroke(Color.BLACK);
                hh = 20;
            } else {
                gc.setStroke(Color.GRAY);
                hh = 10;
            }

            gc.strokeLine((w / 2 + curPos), (h / 2 - hh), (w / 2 + curPos), (h / 2));
            gc.strokeLine((w / 2 - curPos), (h / 2 - hh), (w / 2 - curPos), (h / 2));
        }

        curPos = 0;
        step = 0.1 * mScale;
        idx = 0;

        while (true) {
            curPos = curPos + step;
            if (curPos > h / 2)
                break;

            idx++;
            if (idx % 10 == 0) {
                gc.setStroke(Color.BLACK);
                hh = 20;
            } else {
                gc.setStroke(Color.LIGHTGRAY);
                hh = 10;
            }

            gc.strokeLine((w / 2), (h / 2 + curPos), (w / 2 + hh), (h / 2 + curPos));
            gc.strokeLine((w / 2), (h / 2 - curPos), (w / 2 + hh), (h / 2 - curPos));

        }
    }

    public void setRoutes(float[][] routes, int routeSize) {
        this.mRoutes = routes;
        this.routeSize = routeSize;
        this.invalidate();
    }

    public void setRouteSize(int routeSize) {
        this.routeSize = routeSize;
        this.invalidate();
        // Log.i(TAG, "route size: " + routeSize );

    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void addObstacle(Obstacle obs) {
        obstacles.add(obs);
    }

    public void initObstacles() {
        try {
            Obstacle obstacle = new Obstacle(obstacle1);
            obstacle.setPosition(-0.8, 0, 0);
            obstacle.setCavasDimension(width, height);
            addObstacle(obstacle);

            obstacle = new Obstacle(obstacle3);
            obstacle.setPosition(-0.5, 0, 0);
            obstacle.setCavasDimension(width, height);
            addObstacle(obstacle);

            obstacle = new Obstacle(obstacle2);
            obstacle.setPosition(-1, -2, 0);
            obstacle.setCavasDimension(width, height);
            addObstacle(obstacle);

            obstacle = new Obstacle(obstacle4);
            obstacle.setPosition(-2, 1, 0);
            obstacle.setCavasDimension(width, height);
            addObstacle(obstacle);

            obstacle = new Obstacle(border, Color.RED, false);
            obstacle.setPosition(0, 0, 0);
            obstacle.setCavasDimension(width, height);
            addObstacle(obstacle);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    double obstacle1[][] = { { 0, 0, 1 }, { 0.35, 0, 1 }, { 0.35, -0.30, 1 }, { 0, -0.30, 1 } };

    // ����
    final double obstacle2[][] = { { 0, 0, 1 }, { 0, 0.8, 1 }, { 0.4, 0.8, 1 }, { 0.4, 0.4, 1 }, { 1.8, 0.4, 1 },
            { 1.8, 0, 1 }

    };

    // ����
    final double obstacle3[][] = { { 0, 1, 1 }, { 0, 2.2, 1 }, { 2.4, 2.2, 1 }, { 2.4, 0, 1 }, { 1.2, 0, 1 },
            { 1.2, 0.4, 1 }, { 2, 0.4, 1 }, { 2, 1.8, 1 }, { 0.4, 1.8, 1 }, { 0.4, 1, 1 } };

    final double obstacle4[][] = { { 0, 0, 1 }, { 0.3, 0, 1 }, { 0.3, 0.4, 1 }, { 0, 0.4, 1 } };

    final double border[][] = { { -3, -3.5, 1 }, { -3, 3.5, 1 }, { 3, 3.5, 1 }, { 3, -3.5, 1 } };

}
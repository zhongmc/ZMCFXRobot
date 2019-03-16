package com.zmc.robot.fxrobotui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.sun.javafx.geom.Point2D;

public class ScenseView extends Canvas {

    private List<Obstacle> obstacles = new ArrayList<Obstacle>();

    // private double x, y, theta;
    // private double velocity;

    // public double width, height;

    // public double canvas_width, canvas_height;
    // private double scroll_x, scroll_y;
    // public double x_off = 0, y_off = 0;
    // private double scroll_width, scroll_height; //可滚动的宽度和高度

    // private double targetX = -1, targetY = 1;
    // the canvas dimension
    // private double cw = 5.5, ch = 7.5;

    // private int mTrailSize = 900;
    // private float[][] mTrails = new float[1000][2];
    // private int mTrailCount = 0;

    private double trail_x0 = 0, trail_y0 = 0;

    private float[][] mRoutes = new float[2000][2];;
    private int routeSize = 0;

    private double width, height;
    private double mScale = 100;
    private double canvasWidth, canvasHeight; // 画布大小

    private GraphicsContext gc;
    // private RearDriveRobotUI robot;

    public ScenseView(double canvasWidth, double canvasHeight, double scale) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.mScale = scale;

        this.width = canvasWidth * mScale;
        this.height = canvasHeight * mScale;
        this.setWidth(width);
        this.setHeight(height);
        initObstacles();
        gc = getGraphicsContext2D();
        draw(gc);

    }

    public ScenseView(double width, double height) {
        super(width, height);

        this.width = width;
        this.height = height;

        canvasWidth = width / mScale;
        canvasHeight = height / mScale;

        initObstacles();

        // robot = new RearDriveRobotUI();
        // robot.setObstacles(obstacles);
        // robot.setCavasDimension(width, height);
        // robot.setPosition(0.5, 0.5, Math.PI / 4);
        gc = getGraphicsContext2D();
        draw(gc);
    }

    public void setScale(double scale) {

        System.out.println("Set scale: " + scale);
        System.out.println("w " + width + ", h " + height);
        this.mScale = scale;

        this.width = canvasWidth * scale;
        this.height = canvasHeight * scale;

        System.out.println("prefered: w " + width + ", h " + height);

        this.setWidth(width);
        this.setHeight(height);
        for (Obstacle obs : obstacles) {
            obs.setCavasDimension(width, height, scale);
        }

        System.out.println("after: w " + getWidth() + ", h " + getHeight());

        invalidate();
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

    // 双击鼠标，设定robot 位置
    public void setRobotPosition(double x, double y) {
        trail_x0 = (x - width / 2) / mScale;
        trail_y0 = (height / 2 - y) / mScale;
    }

    public void resetRobotPosition(double x, double y) {
        trail_x0 = x;
        trail_y0 = y;
        this.invalidate();

    }

    // 使用robot的原始坐标
    public void setRobotPosition(double x, double y, double theta, double velocity) {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setStroke(Color.RED);//  设置红色  
        gc.setLineWidth(1);
        double xs, ys, xe, ye;
        xs = (width / 2 + trail_x0 * mScale);
        ys = (height / 2 - trail_y0 * mScale);
        xe = (width / 2 + x * mScale);
        ye = (height / 2 - y * mScale);
        gc.strokeLine(xs, ys, xe, ye);
        trail_x0 = x;
        trail_y0 = y;

    }

    private double route_x, route_y;

    public Point2D startRoutes(double x, double y) {
        this.invalidate();

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        gc.strokeOval(x - 15, y - 15, 30, 30);
        route_x = x;
        route_y = y;

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);

        gc.strokeLine(x - 2, y, x + 2, y);
        gc.strokeLine(x, y - 2, x, y + 2);

        Point2D p = new Point2D();
        p.x = (float) ((x - width / 2) / mScale);
        p.y = (float) ((height / 2 - y) / mScale);
        mRoutes[0][0] = (float) p.x;
        mRoutes[0][1] = (float) p.y;
        routeSize = 1;

        return p;

    }

    public Point2D addRoutePoint(double x, double y) {
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(1);

        gc.strokeLine(route_x, route_y, x, y);

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);

        gc.strokeLine(x - 2, y, x + 2, y);
        gc.strokeLine(x, y - 2, x, y + 2);

        route_x = x;
        route_y = y;

        Point2D p = new Point2D();
        p.x = (float) ((x - width / 2) / mScale);
        p.y = (float) ((height / 2 - y) / mScale);
        mRoutes[routeSize][0] = (float) p.x;
        mRoutes[routeSize][1] = (float) p.y;
        routeSize++;

        return p;

    }

    private void drawRoutes1(GraphicsContext gc) {

        if (mRoutes == null || routeSize == 0)
            return;
        // draw the route

        gc.setStroke(Color.CYAN);
        gc.setLineWidth(3);
        double xs, ys, xe, ye;

        xs = mRoutes[0][0];
        ys = mRoutes[0][1];

        gc.strokeOval(xs - 15, ys - 15, 30, 30);

        gc.setLineWidth(1);
        for (int i = 1; i < routeSize; i++) {
            xe = mRoutes[i][0];
            ye = mRoutes[i][1];
            gc.strokeLine(xs, ys, xe, ye);
            xs = xe;
            ys = ye;
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(2);

        double x0, y0;
        for (int i = 0; i < routeSize; i++) {
            x0 = mRoutes[i][0];
            y0 = mRoutes[i][1];
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
            obstacle.setCavasDimension(width, height, mScale);
            addObstacle(obstacle);

            obstacle = new Obstacle(obstacle3);
            obstacle.setPosition(-0.5, 0, 0);
            obstacle.setCavasDimension(width, height, mScale);
            addObstacle(obstacle);

            obstacle = new Obstacle(obstacle2);
            obstacle.setPosition(-1, -2, 0);
            obstacle.setCavasDimension(width, height, mScale);
            addObstacle(obstacle);

            obstacle = new Obstacle(obstacle4);
            obstacle.setPosition(-2, 1, 0);
            obstacle.setCavasDimension(width, height, mScale);
            addObstacle(obstacle);

            obstacle = new Obstacle(border, Color.RED, false);
            obstacle.setPosition(0, 0, 0);
            obstacle.setCavasDimension(width, height, mScale);
            addObstacle(obstacle);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public double minHeight(double h) {
        return this.height;
    }

    @Override
    public double maxHeight(double h) {
        return this.height;
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
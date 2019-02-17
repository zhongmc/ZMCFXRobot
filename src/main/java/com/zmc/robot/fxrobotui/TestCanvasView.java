package com.zmc.robot.fxrobotui;

import java.util.ArrayList;
import java.util.List;

import com.zmc.robot.fxrobotui.Obstacle;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

public class TestCanvasView extends Canvas {

	private double width, height;

	private GraphicsContext gc;

	private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

	private RearDriveRobotUI robot;

	private double mScale = 100;

	public TestCanvasView(double width, double height) {
		super(width, height);

		this.width = width;
		this.height = height;
		initObstacles();

		robot = new RearDriveRobotUI();
		robot.setObstacles(obstacles);

		robot.setCavasDimension(width, height);
		robot.setPosition(0.5, 0.5, Math.PI / 4);
		gc = getGraphicsContext2D();
		draw(gc);

		this.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			public void handle(MouseEvent t) {
				if (t.getClickCount() > 1) {
					double x = t.getX();
					double y = t.getY();
					setRobotPosition(x, y);
				}
			}
		});

	}

	private void setRobotPosition(double x, double y) {

		double rx = (x - width / 2) / mScale;
		double ry = (height / 2 - y) / mScale;
		robot.setPosition(rx, ry, Math.PI / 4);
		this.invalidate();

	}

	public void invalidate() {
		gc.clearRect(0, 0, width, height);
		draw(gc);
	}

	public void draw(GraphicsContext gc) {

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

		gc.save();
		gc.setFill(Color.YELLOWGREEN);
		gc.strokeOval(50, 80, 50, 50);
		gc.fillOval(100, 80, 50, 50);

		gc.strokeOval(500, 80, 50, 50);
		gc.restore();

		for (Obstacle obs : obstacles) {
			obs.draw(gc);
		}

		robot.draw(gc);
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

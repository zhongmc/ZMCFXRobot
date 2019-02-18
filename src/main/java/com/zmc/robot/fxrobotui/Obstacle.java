package com.zmc.robot.fxrobotui;

import com.sun.javafx.geom.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

public class Obstacle {

	// the obstacle positionb
	private double x, y;
	private double ob_path[][];

	private double draw_path[][];

	// the canvas size
	private double width = 0, height = 0;
	private double mScale = 100;
	private boolean bSolid = true; // 是否为实习障碍物

	private Color mColor = Color.LIGHTSLATEGRAY;

	public Obstacle(double obPath[][]) throws Exception {
		if (obPath.length < 3)
			throw new Exception("Invalid obstacle path!");

		ob_path = obPath;
		// initPath();

	}

	public Obstacle(double obPath[][], Color color, boolean bSolid) throws Exception {
		if (obPath.length < 3)
			throw new Exception("Invalid obstacle path!");

		ob_path = obPath;
		this.bSolid = bSolid;
		this.mColor = color;
		// initPath();

	}

	public void draw(GraphicsContext gc) {
		if (draw_path == null)
			return;
		gc.setFill(mColor);
		gc.setStroke(mColor);
		gc.beginPath();
		gc.moveTo(draw_path[0][0], draw_path[0][1]);

		for (int i = 1; i < draw_path.length; i++) {
			gc.lineTo(draw_path[i][0], draw_path[i][1]);
		}
		gc.closePath();

		if (bSolid)
			gc.fill();
		else
			gc.stroke();
	}

	public void setPosition(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		// Log.i(TAG, "Set position to:" + x + "," + y);
		initPath();
	}

	public void setScale(double scale) {
		mScale = scale;
		initPath();
	}

	private void initPath() {

		if (width == 0 || height == 0)
			return;

		if (draw_path == null) {
			draw_path = new double[ob_path.length][2];
		}

		double x1, y1;

		x1 = width / 2 + (x + ob_path[0][0]) * mScale;
		y1 = height / 2 - (y + ob_path[0][1]) * mScale;
		draw_path[0][0] = x1;
		draw_path[0][1] = y1;

		for (int i = 1; i < ob_path.length; i++) {
			x1 = width / 2 + (x + ob_path[i][0]) * mScale;
			y1 = height / 2 - (y + ob_path[i][1]) * mScale;
			draw_path[i][0] = x1;
			draw_path[i][1] = y1;
		}
	}

	// PointF p0 = new PointF(),p1= new PointF(),p2 = new PointF(), p3 = new
	// PointF();

	public void getDistance(double x, double y, double theta, ObstacleCrossPoint ocp) {
		// double distance = 1000;

		Point2D p0 = new Point2D(), p1 = new Point2D(), p2 = new Point2D(), p3 = new Point2D();

		// PointF nearestPoint = null;
		if (ob_path == null)
			return;

		// p0 = new PointF((float)x, (float)y);
		// p1 = new PointF();
		// p2 = new PointF();

		// PointF p3;
		p0.x = (float) x;
		p0.y = (float) y;
		double d;
		for (int i = 0; i < ob_path.length - 1; i++) {
			p1.x = (float) (this.x + ob_path[i][0]);
			p1.y = (float) (this.y + ob_path[i][1]);

			p2.x = (float) (this.x + ob_path[i + 1][0]);
			p2.y = (float) (this.y + ob_path[i + 1][1]);
			// p1.set((float)(this.x + ob_path[i][0]), (float)(this.y + ob_path[i][1]));
			// p2.set((float)(this.x + ob_path[i+1][0]), (float)(this.y + ob_path[i+1][1]));

			p3 = getCrossPoint(p0, theta, p1, p2);
			if (p3 == null)
				continue;

			d = Math.sqrt(Math.pow(p3.x - p0.x, 2) + Math.pow(p3.y - p0.y, 2));

			if (d < ocp.distance) {
				ocp.distance = d;
				ocp.x = p3.x;
				ocp.y = p3.y;
			}
		}
		int idx = ob_path.length - 1;

		p1.x = (float) (this.x + ob_path[0][0]);
		p1.y = (float) (this.y + ob_path[0][1]);
		p2.x = (float) (this.x + ob_path[idx][0]);
		p2.y = (float) (this.y + ob_path[idx][1]);

		// p1.set((float)(this.x + ob_path[0][0]), (float)(this.y + ob_path[0][1]));
		// p2.set((float)(this.x + ob_path[idx][0]), (float)(this.y + ob_path[idx][1]));

		p3 = getCrossPoint(p0, theta, p1, p2);
		if (p3 != null) {
			d = Math.sqrt(Math.pow(p3.x - p0.x, 2) + Math.pow(p3.y - p0.y, 2));

			if (d < ocp.distance) {
				ocp.distance = d;
				ocp.x = p3.x;
				ocp.y = p3.y;
			}
			// Log.i(TAG, "Cross: " + p3 + " distance: " + d);
		}

		return;
	}

	// PointF pp0 = new PointF();
	// PointF pp1 = new PointF();
	// PointF pp2 = new PointF();

	private boolean doesVectorCross(Point2D P0, Point2D P1, Point2D Q0, Point2D Q1) {

		// Log.i(TAG, "getCross:" + P0 + ";" + P1 + ";" + Q0 + ";" + Q1 );

		// 矩形相交判断
		boolean val = (Math.max(P0.x, P1.x) >= Math.min(Q0.x, Q1.x)) && (Math.max(Q0.x, Q1.x) >= Math.min(P0.x, P1.x))
				&& (Math.max(P0.y, P1.y) >= Math.min(Q0.y, Q1.y)) && (Math.max(Q0.y, Q1.y) >= Math.min(P0.y, P1.y));

		if (!val)
			return false;

		// (p0-q0)*(q1-q0)
		double p1xq = CrossProduct(P0.x - Q0.x, P0.y - Q0.y, Q1.x - Q0.x, Q1.y - Q0.y);

		// (p1-q0)*(q1-q0)
		double p2xq = CrossProduct(P1.x - Q0.x, P1.y - Q0.y, Q1.x - Q0.x, Q1.y - Q0.y);
		// (q0-p0)*(p1-p0)
		double q1xp = CrossProduct(Q0.x - P0.x, Q0.y - P0.y, P1.x - P0.x, P1.y - P0.y);

		// (q1-p0)*(p1-p0)
		double q2xp = CrossProduct(Q1.x - P0.x, Q1.y - P0.y, P1.x - P0.x, P1.y - P0.y);

		return (p1xq * p2xq <= 0) && (q1xp * q2xp <= 0);
	}

	// 矢量叉积
	private double CrossProduct(double x1, double y1, double x2, double y2) {
		return x1 * y2 - x2 * y1;
	}

	// CrossProduct(x1,y1,x2,y2) =

	private Point2D getCrossPoint(Point2D p0, double theta, Point2D p1, Point2D p2) {

		Point2D p3 = new Point2D();

		p3.x = p0.x + 2 * (float) Math.cos(theta);
		p3.y = p0.y + 2 * (float) Math.sin(theta);

		if (!doesVectorCross(p0, p3, p1, p2))
			return null;

		double tahTheta = Math.tan(theta);

		double a1 = tahTheta;
		double b1 = p0.y - a1 * p0.x;

		if (p1.y == p2.y) {
			p3.x = (float) ((p1.y - b1) / a1);
			p3.y = p1.y;
			return p3;

			// return new PointF((float)((p1.y-b1)/a1), p1.y);
		}

		if (p1.x == p2.x) {
			p3.x = p1.x;
			p3.y = (float) (a1 * p1.x + b1);
			return p3;
			// return new PointF(p1.x, (float)(a1*p1.x + b1 ));
		}

		double a = (p2.y - p1.y) / (p2.x - p1.x);
		double b = p2.y - a * p2.x;

		double x1 = (b1 - b) / (a - a1);
		double y1 = a * x1 + b;

		p3.x = (float) x1;
		p3.y = (float) y1;
		return p3;
		// return new PointF((float)x1, (float)y1);
	}

	public void setCavasDimension(double width, double height) {
		this.width = width;
		this.height = height;
		initPath();
	}
}

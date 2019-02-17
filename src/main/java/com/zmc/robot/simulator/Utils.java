package com.zmc.robot.simulator;

import com.sun.javafx.geom.Point2D;

public class Utils {

	public Point2D getCrossPoint(Point2D p0, Point2D p3, Point2D p1, Point2D p2) {

		if (!doesVectorCross(p0, p3, p1, p2))
			return null;

		double a1 = (p3.y - p0.y) / (p3.x - p0.x);
		double b1 = p3.y - a1 * p3.x;

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
	}

	public Point2D getCrossPoint(Point2D p0, double theta, float d, Point2D p1, Point2D p2) {

		Point2D p3 = new Point2D();

		p3.x = p0.x + d * (float) Math.cos(theta);
		p3.y = p0.y + d * (float) Math.sin(theta);

		return getCrossPoint(p0, p3, p1, p2);

		// return new PointF((float)x1, (float)y1);
	}

	private boolean doesVectorCross(Point2D P0, Point2D P1, Point2D Q0, Point2D Q1) {

		// Log.i(TAG, "getCross:" + P0 + ";" + P1 + ";" + Q0 + ";" + Q1 );

		// �����ཻ�ж�
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

	// ʸ�����
	private double CrossProduct(double x1, double y1, double x2, double y2) {
		return x1 * y2 - x2 * y1;
	}
}

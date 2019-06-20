package com.zmc.robot.simulator;

public class IRSensor {

	static public float maxDistance = 0.8f; // 0.3f; //0.3f
	static public float minDistance = 0.1f; // 0.04f

	public double x_s, y_s, theta_s; // sensor position

	double distance; // obstacle distance
	double x, y; // the obtacle position in robot geometry
	double xw, yw; // the obtacle pos in real geometry

	public IRSensor() {
	}

	public IRSensor(double xs, double ys, double theta) {
		x_s = xs;
		y_s = ys;
		theta_s = theta;
	}

	public void setDistance(double dis) {
		distance = dis;

		x = x_s + distance * Math.cos(theta_s);
		y = y_s + distance * Math.sin(theta_s);
	}

	public void applyGeometry(double xc, double yc, double theta) // double sinTheta, double cosTheta)
	{
		double sinTheta, cosTheta;
		sinTheta = Math.sin(theta);
		cosTheta = Math.cos(theta);
		xw = xc + x * cosTheta - y * sinTheta;
		yw = yc + x * sinTheta + y * cosTheta;

	}

	public void getWallVector(Vector p, AbstractRobot robot, double d) {
		p.x = xw;
		p.y = yw;

		if (distance >= (maxDistance - 0.01)) {
			double dis = distance;
			setDistance(d);
			applyGeometry(robot.x, robot.y, robot.theta);
			p.x = xw;
			p.y = yw;
			setDistance(dis);
			applyGeometry(robot.x, robot.y, robot.theta);

		}
	}

	/*
	 * public Vector getWallVector(double xc, double yc, double theta, double d_fw)
	 * { Vector v = new Vector(xw, yw);
	 * 
	 * double dis = distance; if (distance >= (maxDistance - 0.01)) { double d; if
	 * (theta_s == 0) { d = d_fw; } else { d = Math.abs(d_fw / Math.sin(theta_s)); }
	 * 
	 * setDistance(d); applyGeometry(xc, yc, theta);
	 * 
	 * v.x = xw; v.y = yw;
	 * 
	 * // ��ԭ setDistance(dis); applyGeometry(xc, yc, theta); }
	 * 
	 * return v; }
	 * 
	 */
	public Vector getObstaclePos() {
		return new Vector(xw, yw);
	}

	public IRSensor clone() {
		IRSensor irSensor = new IRSensor(x_s, y_s, theta_s);
		return irSensor;
	}

}

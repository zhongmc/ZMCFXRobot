package com.zmc.robot.simulator;

public class Input {
	public double v, theta; // velocity and target direction
	public double x_g, y_g; // target x,y
	public double targetAngle;
	public double turning;

	public Input() {

	}

	public String toString() {
		return "v=" + v + ", Q=" + theta;
	}

}

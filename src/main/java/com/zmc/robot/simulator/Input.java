package com.zmc.robot.simulator;

public class Input {
	public double v, w; // velocity and target direction
	public double x_g, y_g; // target x,y
	public double targetAngle;

	public Input() {

	}

	public String toString() {
		return "v=" + v + ", W=" + w;
	}

}

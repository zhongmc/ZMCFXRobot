package com.zmc.robot.simulator;

public class Vector {

	public double x, y;

	public Vector() {

	}

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return String.format("[%.4f, %.4f]", x, y);
	}

}

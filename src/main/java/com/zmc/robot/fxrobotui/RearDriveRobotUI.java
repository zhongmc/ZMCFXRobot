package com.zmc.robot.fxrobotui;

import javafx.scene.paint.Color;

public class RearDriveRobotUI extends AbstractRobotUI {

	public RearDriveRobotUI() {

		super();

		ComponentInfo component;
		component = new ComponentInfo(Color.MEDIUMBLUE, qb_base_plate);
		addComponent(component);

		addArduino(0.061, 0);

		// addTwoCellBattery(-0.06, 0);

		component = new ComponentInfo(Color.RED, this.irDim);
		for (int i = 0; i < this.ir_thetas.length; i++) {
			addComponent(ir_positions[i][0], ir_positions[i][1], this.ir_thetas[i], component);
		}

		component = new ComponentInfo(Color.BLACK, this.wheel);
		// left wheel
		addComponent(0, 0.0675, component);
		// right whell
		addComponent(0, -0.0675, component);

	}

	// final double qb_base_plate[][] = {
	// {-0.03, -0.055, 1}, {-0.03, 0.055,1}, {0.061, 0.055, 1},
	// {0.117, 0.045, 1}, {0.177, 0.045, 1},
	// {0.182, 0.06, 1}, {0.192, 0.06, 1}, {0.197, 0.03, 1},
	// {0.197, -0.03, 1}, {0.192, -0.06, 1}, {0.182, -0.06, 1},
	// {0.177, -0.045, 1}, {0.117, -0.045,1},
	// {0.061, -0.055, 1}

	final double qb_base_plate[][] = { { -0.06, -0.055, 1 }, { -0.06, 0.055, 1 }, { 0.031, 0.055, 1 },
			{ 0.087, 0.045, 1 }, { 0.147, 0.045, 1 }, { 0.152, 0.06, 1 }, { 0.162, 0.06, 1 }, { 0.167, 0.03, 1 },
			{ 0.167, -0.03, 1 }, { 0.162, -0.06, 1 }, { 0.152, -0.06, 1 }, { 0.147, -0.045, 1 }, { 0.087, -0.045, 1 },
			{ 0.031, -0.055, 1 } };

	// {0.16, 0.045},{0.075, 0.035}
	final double ir_positions[][] = { { -0.045, 0.050 }, { 0.08, 0.04 }, { 0.162, 0.0 }, { 0.08, -0.04 },
			{ -0.045, -0.050 } };

	// double ir_positions[][] = {{-0.0582,0.0584}, {0.05725, 0.03555},{0.0686,
	// 0.0},{0.05725, -0.0355},{-0.0582, -0.0584}};
	// final double ir_positions[][] = {{0.07,0.050}, {0.190, 0.045},{0.192, 0.0},
	// {0.190, -0.045},{0.07, -0.050}};

	final double ir_thetas[] = { Math.PI / 2, Math.PI / 4, 0, -Math.PI / 4, -Math.PI / 2 };
	// double ir_thetas[] = {Math.PI/2, Math.PI/4, 0, -Math.PI/4, -Math.PI/2 };

	@Override
	double[][] getIrPositions() {
		return ir_positions;
	}

	@Override
	double[] getIrThetas() {
		return ir_thetas;
	}

	double wheel[][] = { { -0.0325, -0.01, 1 }, { -0.0325, 0.01, 1 }, { 0.0325, 0.01, 1 }, { 0.0325, -0.01, 1 } };

	double irDim[][] = { { -0.005, -0.015, 1 }, { -0.005, 0.015, 1 }, { 0.005, 0.015, 1 }, { 0.005, -0.015, 1 } };

}

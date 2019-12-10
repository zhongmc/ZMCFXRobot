package com.zmc.robot.fxrobotui;

import javafx.scene.paint.Color;

public class RearDriveRobotUI extends AbstractRobotUI {

	public RearDriveRobotUI() {

		super();

		ComponentInfo component;
		component = new ComponentInfo(Color.MEDIUMBLUE, qb_base_plate);
		addComponent(component);

		// addArduino(0.061, 0);
		addArduino(-0.07, 0);

		addTwoCellBattery(0, 0);
		// addTwoCellBattery(-0.06, 0);

		component = new ComponentInfo(Color.DARKGRAY, this.irDim);
		for (int i = 0; i < this.ir_thetas.length; i++) {
			addComponent(ir_positions[i][0], ir_positions[i][1], this.ir_thetas[i], component);
		}

		component = new ComponentInfo(Color.BLACK, this.wheel);
		// left wheel
		addComponent(0, 0.08, component);
		// right whell
		addComponent(0, -0.08, component);

	}

	// final double qb_base_plate[][] = {
	// {-0.03, -0.055, 1}, {-0.03, 0.055,1}, {0.061, 0.055, 1},
	// {0.117, 0.045, 1}, {0.177, 0.045, 1},
	// {0.182, 0.06, 1}, {0.192, 0.06, 1}, {0.197, 0.03, 1},
	// {0.197, -0.03, 1}, {0.192, -0.06, 1}, {0.182, -0.06, 1},
	// {0.177, -0.045, 1}, {0.117, -0.045,1},
	// {0.061, -0.055, 1}

	/**
	 * 白长版 final double qb_base_plate[][] = { { -0.06, -0.055, 1 }, { -0.06, 0.055,
	 * 1 }, { 0.031, 0.055, 1 }, { 0.087, 0.045, 1 }, { 0.147, 0.045, 1 }, { 0.152,
	 * 0.06, 1 }, { 0.162, 0.06, 1 }, { 0.167, 0.03, 1 }, { 0.167, -0.03, 1 }, {
	 * 0.162, -0.06, 1 }, { 0.152, -0.06, 1 }, { 0.147, -0.045, 1 }, { 0.087,
	 * -0.045, 1 }, { 0.031, -0.055, 1 } };
	 * 
	 * final double ir_positions[][] = { { -0.045, 0.050 }, { 0.08, 0.04 }, { 0.162,
	 * 0.0 }, { 0.08, -0.04 }, { -0.045, -0.050 } };
	 * 
	 */

	final double qb_base_plate[][] = { { -0.118, 0.065, 1 }, { -0.098, 0.075, 1 }, { -0.039, 0.075, 1 },
			{ -0.039, 0.06, 1 }, { 0.039, 0.06, 1 }, { 0.039, 0.075, 1 }, { 0.057, 0.075, 1 }, { 0.082, 0.05, 1 },
			{ 0.082, -0.05, 1 }, { 0.057, -0.075, 1 }, { 0.039, -0.075, 1 }, { 0.039, -0.06, 1 }, { -0.039, -0.06, 1 },
			{ -0.039, -0.075, 1 }, { -0.098, -0.075, 1 }, { -0.118, -0.065, 1 } };

	// final double ir_positions[][] = { { -0.073, 0.066, 1 }, { 0.061, 0.05, 1 }, { 0.072, 0, 1 }, { 0.061, -0.05, 1 },
	// 		{ -0.073, -0.066, 1 } };

	final double ir_positions[][] = { { -0.085, 0.067, 1 }, { 0.052,0.057, 1 }, { 0.063, 0, 1 }, 
			{ 0.052, -0.057, 1 }, { -0.085, -0.067, 1 } };

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

	double wheel[][] = { { -0.0325, -0.014, 1 }, { -0.0325, 0.014, 1 }, { 0.0325, 0.014, 1 }, { 0.0325, -0.014, 1 } };

	double irDim[][] = { { -0.009, -0.023, 1 }, { -0.009, 0.023, 1 }, { 0.009, 0.023, 1 }, { 0.009, -0.023, 1 } };

}

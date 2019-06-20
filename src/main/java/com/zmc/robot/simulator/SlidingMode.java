package com.zmc.robot.simulator;

public class SlidingMode extends Controller {

	Vector u_gtg = new Vector(), u_ao = new Vector(), u_fw_l = new Vector(), u_fw_r = new Vector();
	Vector sigma_l = new Vector(), sigma_r = new Vector();

	Vector p0 = new Vector(), p1 = new Vector();

	Vector p2 = new Vector(), p3 = new Vector();

	boolean leftObstacle = false;
	boolean rightObstacle = false;
	boolean slideLeft = false;
	boolean slideRight = false;

	double d_fw;

	// static double maxDistance = 0.3;

	static String TAG = "SlidingMode";

	public SlidingMode() {
	}

	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		double sensor_gains[] = { 1, 0.5, 1, 0.5, 1 };
		IRSensor[] irSensors = robot.getIRSensors();
		// double uao_x = 0, uao_y = 0;
		// for( int i=0; i<5; i++)
		// {
		// uao_x = uao_x + irSensors[i].xw * sensor_gains[i];
		// uao_y = uao_y + irSensors[i].yw * sensor_gains[i];
		// }

		double uao_x = 0, uao_y = 0;
		for (int i = 0; i < 5; i++) {
			uao_x = uao_x + (irSensors[i].xw - robot.x) * sensor_gains[i];
			uao_y = uao_y + (irSensors[i].yw - robot.y) * sensor_gains[i];
		}

		u_ao.x = uao_x;
		u_ao.y = uao_y;

		u_gtg.x = input.x_g - robot.x;
		u_gtg.y = input.y_g - robot.y;

		getWall(robot);
		/*
		 * //get the left wall int idx = 0; for( int i=1; i<3; i++ ) { if(
		 * irSensors[i].distance >= irSensors[idx].distance ) idx = i; }
		 * 
		 * switch(idx) { case 0: u_fw_l.x = irSensors[2].xw - irSensors[1].xw; u_fw_l.y
		 * = irSensors[2].yw - irSensors[1].yw; p1.x = irSensors[1].xw; p1.y =
		 * irSensors[1].yw; p0.x = irSensors[2].xw; p0.y = irSensors[2].yw;
		 * 
		 * break; case 1: u_fw_l.x = irSensors[2].xw - irSensors[0].xw; u_fw_l.y =
		 * irSensors[2].yw - irSensors[0].yw; p1.x = irSensors[0].xw; p1.y =
		 * irSensors[0].yw; p0.x = irSensors[2].xw; p0.y = irSensors[2].yw;
		 * 
		 * break; case 2: u_fw_l.x = irSensors[1].xw - irSensors[0].xw; u_fw_l.y =
		 * irSensors[1].yw - irSensors[0].yw; p1.x = irSensors[0].xw; p1.y =
		 * irSensors[0].yw; p0.x = irSensors[1].xw; p0.y = irSensors[1].yw;
		 * 
		 * break; } u_fw_l.x = p0.x - p1.x; u_fw_l.y = p0.y - p1.y;
		 * 
		 * 
		 * //get the right wall
		 * 
		 * idx = 2; for(int i = 3; i<5; i++) { if( irSensors[i].distance >
		 * irSensors[idx].distance ) idx = i;
		 * 
		 * }
		 * 
		 * switch(idx) { case 2: u_fw_r.x = irSensors[3].xw - irSensors[4].xw; u_fw_r.y
		 * = irSensors[3].yw - irSensors[4].yw; p3.x = irSensors[4].xw; p3.y =
		 * irSensors[4].yw; p2.x = irSensors[3].xw; p2.y = irSensors[3].yw;
		 * 
		 * break; case 3: u_fw_r.x = irSensors[2].xw - irSensors[4].xw; u_fw_r.y =
		 * irSensors[2].yw - irSensors[4].yw; p3.x = irSensors[4].xw; p3.y =
		 * irSensors[4].yw; p2.x = irSensors[2].xw; p2.y = irSensors[2].yw;
		 * 
		 * break; case 4: u_fw_r.x = irSensors[2].xw - irSensors[3].xw; u_fw_r.y =
		 * irSensors[2].yw - irSensors[3].yw; p3.x = irSensors[3].xw; p3.y =
		 * irSensors[3].yw; p2.x = irSensors[2].xw; p2.y = irSensors[2].yw;
		 * 
		 * break; }
		 * 
		 * u_fw_r.x = p2.x - p3.x; u_fw_r.y = p2.y - p3.y;
		 */

		leftObstacle = false;
		rightObstacle = false;

		double obsL = Math.min(irSensors[0].distance, irSensors[1].distance);
		double obsR = Math.min(irSensors[3].distance, irSensors[4].distance);

		if (obsL < obsR) {
			if (obsL < (IRSensor.maxDistance - 0.01))
				leftObstacle = true;
		} else {
			if (obsR < (IRSensor.maxDistance - 0.01))
				rightObstacle = true;

		}
		//
		// if( irSensors[0].distance < maxDistance || irSensors[1].distance <
		// maxDistance )
		// leftObstacle = true;
		//
		// if( irSensors[3].distance < maxDistance || irSensors[4].distance <
		// maxDistance )
		// rightObstacle = true;

		getSegma1(u_gtg, u_ao, u_fw_l, sigma_l);
		getSegma1(u_gtg, u_ao, u_fw_r, sigma_r);

		slideLeft = sigma_l.x > 0 && sigma_l.y > 0;
		slideRight = sigma_r.x > 0 && sigma_r.y > 0;

		return null;
	}

	/**
	 * 以离墙最近的两个点为墙
	 * 
	 * @param robot
	 */
	private void getWall(AbstractRobot robot) {

		IRSensor[] irSensors = robot.getIRSensors();

		double d = d_fw * 1.42;// 0.35;
		// double d_fw = 0.3;

		int idx = 0;

		for (int i = 1; i < 3; i++) {
			if (irSensors[i].distance >= irSensors[idx].distance)
				idx = i;
		}

		switch (idx) {
		case 0:

			irSensors[1].getWallVector(p1, robot, d);
			irSensors[2].getWallVector(p0, robot, d);
			break;
		case 1:
			irSensors[0].getWallVector(p1, robot, d);
			irSensors[2].getWallVector(p0, robot, d);
			break;
		case 2:
			irSensors[0].getWallVector(p1, robot, d);
			irSensors[1].getWallVector(p0, robot, d);
			break;
		}

		u_fw_l.x = p0.x - p1.x;
		u_fw_l.y = p0.y - p1.y;

		// get the right wall

		idx = 2;
		for (int i = 3; i < 5; i++) {
			if (irSensors[i].distance > irSensors[idx].distance)
				idx = i;

		}

		switch (idx) {
		case 2:
			irSensors[4].getWallVector(p3, robot, d);
			irSensors[3].getWallVector(p2, robot, d);
			break;
		case 3:
			irSensors[4].getWallVector(p3, robot, d);
			irSensors[2].getWallVector(p2, robot, d);
			break;
		case 4:
			irSensors[3].getWallVector(p3, robot, d);
			irSensors[2].getWallVector(p2, robot, d);
			break;
		}

		u_fw_r.x = p2.x - p3.x;
		u_fw_r.y = p2.y - p3.y;

	}

	// 解二元一次方程组 [Ugtg, Uao]*[sigma] = [Ufw]
	private void getSegma1(Vector u_gtg, Vector u_ao, Vector u_fw, Vector sigma) {

		double ad_bc = u_gtg.x * u_ao.y - u_ao.x * u_gtg.y;
		if (ad_bc == 0) {
			System.out.println("No result!");
			return;

		}

		sigma.x = (u_ao.y * u_fw.x - u_ao.x * u_fw.y) / ad_bc;
		sigma.y = (u_gtg.x * u_fw.y - u_gtg.y * u_fw.x) / ad_bc;

	}

	// private static void getSegma(Vector u_gtg, Vector u_ao, Vector u_fw, Vector
	// sigma) {

	// double a1, a2;
	// if (u_ao.y == 0 || u_gtg.x == 0) {
	// // Serial.println("Div by zero 1!");
	// return;
	// }
	// a1 = -u_ao.x / u_ao.y;
	// a2 = -u_gtg.y / u_gtg.x;

	// double fv1, fv2;

	// fv1 = (u_gtg.x + a1 * u_gtg.y);
	// fv2 = (u_ao.y + a2 * u_ao.x);
	// if (fv1 == 0 || fv2 == 0) {
	// // Serial.println("Div by zero 2!");
	// return;
	// }
	// sigma.x = (u_fw.x + a1 * u_fw.y) / fv1;
	// sigma.y = (u_fw.y + a2 * u_fw.x) / fv2;
	// }

	public boolean slidingLeft() {
		// return leftObstacle; // ||

		// if(!leftObstacle && !rightObstacle )
		// return false;
		//
		// if( slideLeft && slideRight )
		// return leftObstacle;
		// else
		// return slideLeft;

		return leftObstacle && slideLeft;

		// return (sigma_l.x > 0 && sigma_l.y > 0 );
	}

	public boolean slidingRight() {
		// if(!leftObstacle && !rightObstacle )
		// return false;

		// if( slideLeft && slideRight )
		// return rightObstacle;
		// else
		// return slideRight;

		return rightObstacle && slideRight;

		// return rightObstacle; // ||

		// return (sigma_r.x > 0 && sigma_r.y > 0 );
	}

	public boolean quitSlidingLeft() {
		boolean ret = false;

		if (!leftObstacle && !rightObstacle)
			ret = true;
		else
			ret = !(sigma_l.x > 0 && sigma_l.y > 0);

		// Log.i(TAG, "Quit fl left: " + ret);
		return ret;
	}

	public boolean quitSlidingRight() {

		boolean ret = false;

		if (!leftObstacle && !rightObstacle)
			ret = true;
		else
			ret = !(sigma_r.x > 0 && sigma_r.y > 0);

		// Log.i(TAG, "Quit fl right: " + ret);
		return ret;

		// if(!leftObstacle && !rightObstacle )
		// return true;
		//
		// return !(sigma_r.x > 0 && sigma_r.y > 0 );

	}

	@Override
	void reset() {

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uAoidObstacle = this.u_ao;
		state.uGotoGoal = this.u_gtg;
		state.p0 = p0;
		state.p1 = p1;

	}

}

package com.zmc.robot.simulator;

import org.apache.log4j.Logger;

public class FollowWall extends Controller {

	private final static String TAG = "GTG";

	Logger log = Logger.getLogger(TAG);

	public double d_fw;
	int dir;

	Vector u_fw_t = new Vector();
	Vector p1 = new Vector(), p0 = new Vector();

	Vector u_fw_tp = new Vector();
	Vector u_fw_p = new Vector();
	Vector u_a_p = new Vector();

	Vector u_fw_pp = new Vector();
	Vector u_fw = new Vector();

	public FollowWall() {

	}

	Output output = new Output();

	/**
	 * ����ǽ�����������Ϊǽ
	 * 
	 * @param robot
	 */
	private void getWall(AbstractRobot robot) {

		IRSensor[] irSensors = robot.getIRSensors();

		double d = d_fw * 1.42;// 0.32; // 0.35;

		int idx = 0;
		if (dir == 0) // follow left
		{

			for (int i = 1; i < 3; i++) {
				if (irSensors[i].distance >= irSensors[idx].distance)
					idx = i;
			}

			switch (idx) {
			case 0:

				irSensors[1].getWallVector(p1, robot, d);
				irSensors[2].getWallVector(p0, robot, d);

				// p1.x = irSensors[1].xw;
				// p1.y = irSensors[1].yw;
				// p0.x = irSensors[2].xw;
				// p0.y = irSensors[2].yw;
				// if( irSensors[2].distance >= IRSensor.maxDistance ) //ǽ��ֱ����
				// p0 = irSensors[2].getWallVector(robot.x, robot.y, robot.theta, d_fw);
				break;
			case 1:

				irSensors[0].getWallVector(p1, robot, d);
				irSensors[2].getWallVector(p0, robot, d);

				// p1.x = irSensors[0].xw;
				// p1.y = irSensors[0].yw;
				// p0.x = irSensors[2].xw;
				// p0.y = irSensors[2].yw;
				//
				// if( irSensors[2].distance >= IRSensor.maxDistance )
				// {
				// p0 = irSensors[2].getWallVector(robot.x, robot.y, robot.theta, d_fw - (d_fw -
				// irSensors[0].distance));
				//
				// }

				break;
			case 2:

				irSensors[0].getWallVector(p1, robot, d);
				irSensors[1].getWallVector(p0, robot, d);

				// p1.x = irSensors[0].xw;
				// p1.y = irSensors[0].yw;
				// p0.x = irSensors[1].xw;
				// p0.y = irSensors[1].yw;
				//
				// if( irSensors[1].distance >= IRSensor.maxDistance ) //ǽ��ֱ����
				// {
				// p0 = irSensors[1].getWallVector(robot.x, robot.y, robot.theta, d_fw - (d_fw -
				// irSensors[0].distance));
				// }

				break;
			}

		}

		else {
			// get the right wall

			idx = 2;
			for (int i = 3; i < 5; i++) {
				if (irSensors[i].distance > irSensors[idx].distance)
					idx = i;

			}

			switch (idx) {
			case 2:

				irSensors[4].getWallVector(p1, robot, d);
				irSensors[3].getWallVector(p0, robot, d);

				// p1.x = irSensors[4].xw;
				// p1.y = irSensors[4].yw;
				// p0.x = irSensors[3].xw;
				// p0.y = irSensors[3].yw;
				break;
			case 3:
				irSensors[4].getWallVector(p1, robot, d);
				irSensors[2].getWallVector(p0, robot, d);

				// p1.x = irSensors[4].xw;
				// p1.y = irSensors[4].yw;
				// p0.x = irSensors[2].xw;
				// p0.y = irSensors[2].yw;
				// if( irSensors[2].distance >= IRSensor.maxDistance ) //ǽ��ֱ����
				// p0 = irSensors[2].getWallVector(robot.x, robot.y, robot.theta, d_fw);
				break;
			case 4:

				irSensors[3].getWallVector(p1, robot, d);
				irSensors[2].getWallVector(p0, robot, d);
				// p1.x = irSensors[3].xw;
				// p1.y = irSensors[3].yw;
				// p0.x = irSensors[2].xw;
				// p0.y = irSensors[2].yw;
				// if( irSensors[2].distance >= IRSensor.maxDistance ) //ǽ��ֱ����
				// p0 = irSensors[2].getWallVector(robot.x, robot.y, robot.theta, d_fw);
				break;
			}
		}

	}

	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		getWall(robot);

		// getWallWithSideSensor(robot);

		u_fw_t.x = p0.x - p1.x; // irSensors[2].xw - irSensors[3].xw;
		u_fw_t.y = p0.y - p1.y; // irSensors[2].yw - irSensors[3].yw;

		// u_fw_tp = u_fw_t/norm(u_fw_t);
		double norm_ufwt = Math.sqrt(u_fw_t.x * u_fw_t.x + u_fw_t.y * u_fw_t.y);

		u_fw_tp.x = u_fw_t.x / norm_ufwt;
		u_fw_tp.y = u_fw_t.y / norm_ufwt;

		u_a_p.x = p1.x - robot.x;
		u_a_p.y = p1.y - robot.y;

		// (u_a-u_p)'*u_fw_tp
		double alp = u_a_p.x * u_fw_tp.x + u_a_p.y * u_fw_tp.y;
		u_fw_p.x = u_a_p.x - alp * u_fw_tp.x;
		u_fw_p.y = u_a_p.y - alp * u_fw_tp.y;

		// % 3. Combine u_fw_tp and u_fw_pp into u_fw;
		double norm_ufwp = Math.sqrt(u_fw_p.x * u_fw_p.x + u_fw_p.y * u_fw_p.y);
		u_fw_pp.x = u_fw_p.x / norm_ufwp;
		u_fw_pp.y = u_fw_p.y / norm_ufwp;

		// u_fw_pp = u_fw_p/norm(u_fw_p);
		// u_fw = d_fw*u_fw_tp+(u_fw_p-d_fw*u_fw_pp);

		u_fw.x = d_fw * u_fw_tp.x + (u_fw_p.x - d_fw * u_fw_pp.x);
		u_fw.y = d_fw * u_fw_tp.y + (u_fw_p.y - d_fw * u_fw_pp.y);

		double e, e_I, e_D, w, theta_fw;

		// % Compute the heading and error for the PID controller
		theta_fw = Math.atan2(u_fw.y, u_fw.x);

		// Log.i(TAG, "Follow wall:" + theta_fw + ", " + robot.theta );

		e = theta_fw - robot.theta;
		e = Math.atan2(Math.sin(e), Math.cos(e));

		double kp = Kp;
		if (Math.abs(e) > 1) {
			e_I = 0;
			// lastErrorIntegration + e * dt;
			kp = Kp / 2;
		} else {
			e_I = lastErrorIntegration + e * dt;

		}
		e_D = (e - lastError) / dt;
		w = kp * e + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		lastError = e;

		output.v = input.v; /// (1 + Math.abs(robot.w)); // 2 * Math.abs(e));
		output.w = w;

		log.info(String.format("FW: %.3f, %.3f, %.3f", output.v, e, w));

		return output;
	}

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;
	}

	@Override
	void getControllorInfo(ControllerInfo state) {

		state.uFollowWall = u_fw;
		state.uFwP = u_fw_p;
		state.p0 = p0;
		state.p1 = p1;

	}

}

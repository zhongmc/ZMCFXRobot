package com.zmc.robot.simulator;

public class AvoidObstacle extends Controller {

	private static String TAG = "AvoidObstacle";
	private Vector uAvo = new Vector();
	private Vector u_fw_t = new Vector();

	public AvoidObstacle() {
		Kp = 5;
		Ki = 0.01;
		Kd = 0.5;
		lastError = 0;
		lastErrorIntegration = 0;
	}

	Output output = new Output();

	Output execute1(AbstractRobot robot, Input input, double dt) {

		double sensor_gains[] = { 1, 1, 0.5, 1, 1 };

		IRSensor[] irSensors = robot.getIRSensors();

		double e_k, e_I, e_D, w, theta_ao;

		// go ahead
		theta_ao = robot.theta; // Math.atan2(uAvo.y, uAvo.x);

		// at obstacle
		if (irSensors[2].distance < IRSensor.maxDistance) {
			double uao_x = 0, uao_y = 0;

			if (irSensors[3].distance > irSensors[1].distance) {
				uao_x = irSensors[3].xw - robot.x;
				uao_y = irSensors[3].yw - robot.y;
			} else {
				uao_x = irSensors[1].xw - robot.x;
				uao_y = irSensors[1].yw - robot.y;

			}

			uao_x = uao_x - (irSensors[2].xw - robot.x);
			uao_y = uao_y - (irSensors[2].yw - robot.y);

			theta_ao = Math.atan2(uao_y, uao_x);

		} else {

			double uao_x = 0, uao_y = 0;
			for (int i = 1; i < 4; i++) {
				uao_x = uao_x + (irSensors[i].xw - robot.x) * sensor_gains[i];
				uao_y = uao_y + (irSensors[i].yw - robot.y) * sensor_gains[i];
			}

			theta_ao = Math.atan2(uao_y, uao_x);

		}

		e_k = theta_ao - robot.theta;

		// this.getWallVector(irSensors);
		// Log.i("AVO", uao_x + ", " + uao_y + ", " + theta_ao + ", " +
		// thetaFw);

		e_k = Math.atan2(Math.sin(e_k), Math.cos(e_k));

		e_I = lastErrorIntegration + e_k * dt;
		e_D = (e_k - lastError) / dt;
		w = Kp * e_k + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		lastError = e_k;

		output.v = input.v;
		output.w = w;
		return output;

	}

	// @Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		double sensor_gains[] = { 1, 0.5, 1, 0.5, 1 }; // { 1, 1, 1, 1, 1 };

		IRSensor[] irSensors = robot.getIRSensors();

		double uao_x = 0, uao_y = 0;
		for (int i = 0; i < 5; i++) {
			uao_x = uao_x + (irSensors[i].xw - robot.x) * sensor_gains[i];
			uao_y = uao_y + (irSensors[i].yw - robot.y) * sensor_gains[i];
		}

		uAvo.x = uao_x;
		uAvo.y = uao_y;

		double e_k, e_I, e_D, w, theta_ao;

		theta_ao = Math.atan2(uAvo.y, uAvo.x);

		double theta = Math.atan2((irSensors[2].yw - robot.y), (irSensors[2].xw - robot.x));

		boolean vToObstacle = false;
		if ((irSensors[1].distance < IRSensor.maxDistance && irSensors[3].distance < IRSensor.maxDistance)
				&& irSensors[2].distance < IRSensor.maxDistance) {
			if (Math.abs(irSensors[1].distance - irSensors[3].distance) < IRSensor.maxDistance / 10)
				vToObstacle = true;
		}

		if ((irSensors[1].distance >= IRSensor.maxDistance && irSensors[3].distance >= IRSensor.maxDistance)
				&& irSensors[2].distance < IRSensor.maxDistance)
			vToObstacle = true;

		int idx = 0;
		// ���ⴹֱ�ӽ��ϰ���ʱ��avo�����޷��ܿ��ϰ��ѡȡ�����ir
		if (vToObstacle) {

			if ((irSensors[1].distance > irSensors[3].distance)) {
				if (irSensors[0].distance >= IRSensor.maxDistance) // turn left
				{
					idx = 0;
				} else if (irSensors[0].distance > irSensors[4].distance) // turn left
				{
					idx = 0;
				} else // turn right
				{
					idx = 4;
				}
			} else {
				if (irSensors[4].distance >= IRSensor.maxDistance) // turn right
				{
					idx = 4;
				}

				else if (irSensors[4].distance > irSensors[0].distance) // turn right
				{
					idx = 4;
				} else // turn left
				{
					idx = 0;
				}

			}

			uAvo.x = (irSensors[idx].xw - robot.x) * sensor_gains[idx] + (irSensors[1].xw - robot.x) * sensor_gains[1];
			uAvo.y = (irSensors[idx].yw - robot.y) * sensor_gains[idx] + (irSensors[1].yw - robot.y) * sensor_gains[1];
			theta_ao = Math.atan2(uAvo.y, uAvo.x);

			// if (Math.abs(theta - theta_ao) < 0.2) {
			// if (theta - theta_ao < 0) {
			// theta_ao = Math.atan2((irSensors[1].yw - robot.y),
			// (irSensors[1].xw - robot.x));
			// uAvo.x = irSensors[1].xw - robot.x;
			// uAvo.y = irSensors[1].yw - robot.y;
			// } else {
			// theta_ao = Math.atan2((irSensors[3].yw - robot.y),
			// (irSensors[3].xw - robot.x));
			// uAvo.x = irSensors[3].xw - robot.x;
			// uAvo.y = irSensors[3].yw - robot.y;
			// }
			// }
		}

		e_k = theta_ao - robot.theta;

		// this.getWallVector(irSensors);
		// Log.i("AVO", uao_x + ", " + uao_y + ", " + theta_ao + ", " +
		// thetaFw);

		e_k = Math.atan2(Math.sin(e_k), Math.cos(e_k));

		e_I = lastErrorIntegration + e_k * dt;
		e_D = (e_k - lastError) / dt;
		w = Kp * e_k + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		lastError = e_k;

		output.v = input.v;
		output.w = w;
		return output;

	}

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uAoidObstacle = uAvo;
	}

}

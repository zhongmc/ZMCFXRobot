package com.zmc.robot.simulator;

import org.apache.log4j.Logger;

public class SimpleAvoidObstacle extends Controller {
	private Vector uAvo = new Vector();
	private final static String TAG = "AVO-S";

	private int m_state = -1;
	private double m_theta, m_w;
	public boolean beQuit = false;
	private double thetaToTurn;

	Logger log = Logger.getLogger(TAG);

	public SimpleAvoidObstacle() {
		Kp = 5;
		Ki = 0.01;
		Kd = 0.5;
		lastError = 0;
		lastErrorIntegration = 0;
	}

	Output output = new Output();

	// @Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		if (m_state == -1) {
			m_state = 0;
			m_theta = robot.theta;
			m_w = 2 * input.v / (2 * 0.1 + robot.wheel_base_length); // 1.7;
			IRSensor[] irSensors = robot.getIRSensors();
			if (irSensors[1].distance < (IRSensor.maxDistance - 0.1))
				m_w = -m_w;

			double atObs = robot.getSettings().atObstacle;
			if (irSensors[1].distance <= atObs || irSensors[3].distance <= atObs)
				thetaToTurn = 2 * Math.PI / 5;
			else
				thetaToTurn = Math.PI / 2;

			log.info(String.format("Start avo: %.3f, %.3f, %.3f, %.2f ", irSensors[1].distance, irSensors[2].distance,
					irSensors[3].distance, m_w));

		} else {
			double te = robot.theta - m_theta;
			te = Math.atan2(Math.sin(te), Math.cos(te));

			if (Math.abs(te) > thetaToTurn) {
				m_state++;
				thetaToTurn = Math.PI / 2;

				log.info("change state " + m_state);
				m_theta = robot.theta;
				double sig = Math.signum(m_w);
				m_w = -2 * sig * input.v / (2 * 0.2 + robot.wheel_base_length); // 1.7;
				// m_w = -0.5 * m_w;
				beQuit = (m_state > 1);
			}

			if (m_state == 1) {
				double u_x = input.x_g - robot.x;
				double u_y = input.y_g - robot.y;
				double theta_g = Math.atan2(u_y, u_x);

				if (Math.abs(robot.theta - theta_g) < 0.1)
					beQuit = true;

			}

		}

		output.v = input.v;
		output.w = m_w;
		return output;

	}

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;
		m_state = -1;
		beQuit = false;
		m_w = 2;

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uAoidObstacle = uAvo;
	}

}

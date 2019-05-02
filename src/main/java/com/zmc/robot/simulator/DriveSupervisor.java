package com.zmc.robot.simulator;

import com.sun.javafx.geom.Point2D;

import org.apache.log4j.Logger;

public class DriveSupervisor {

	private final static String TAG = "DriveSupervisor";

	Logger log = Logger.getLogger(TAG);

	DriveController m_driver = new DriveController();

	AbstractRobot robot;

	boolean mSimulateMode = true;

	Input m_input = new Input();
	Output m_output = new Output();

	public void setRobot(AbstractRobot robot) {
		this.robot = robot;
		m_driver.updateSettings(robot.getSettings());
	}

	public void setDriveGoal(double v, double theta) {
		m_driver.setGoal(v, theta);
		m_input.v = v;
		m_input.turning = theta;
	}

	public void execute(long left_ticks, long right_ticks, double dt) {

		// if (mSimulateMode)
		// robot.updateState((long) m_left_ticks, (long) m_right_ticks, dt);
		// else
		if (!mSimulateMode)
			robot.updateState(left_ticks, right_ticks, dt);

		m_output = m_driver.execute(robot, m_input, dt);
		double v = m_output.v;
		double w = m_output.w;

		Vel mVel = robot.ensure_w(v, w);

		double pwm_l = (int) robot.vel_l_to_pwm(mVel.vel_l);
		double pwm_r = (int) robot.vel_r_to_pwm(mVel.vel_r);

		// if (mSimulateMode) {
		// m_left_ticks = m_left_ticks + robot.pwm_to_ticks_l(pwm_l, dt);
		// m_right_ticks = m_right_ticks + robot.pwm_to_ticks_r(pwm_r, dt);
		// } else {
		robot.moveMotor((int) pwm_l, (int) pwm_r, dt);
		// }

	}

	void StopMotor() {

	}

	void MoveMotor(int pwm_l, int pwm_r) {

	}

	public RobotState getRobotState() {
		return robot.getState();
	}

	public void updateSettings(Settings settings) {

		m_input.v = settings.velocity;
		robot.updateSettings(settings);
		m_driver.updateSettings(settings);

	}

}

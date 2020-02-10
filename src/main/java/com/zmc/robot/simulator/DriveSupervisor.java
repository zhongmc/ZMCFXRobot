package com.zmc.robot.simulator;

import com.sun.javafx.geom.Point2D;

import org.apache.log4j.Logger;

public class DriveSupervisor {

	private final static String TAG = "DriveSupervisor";

	Logger log = Logger.getLogger(TAG);

	DriveController m_driver = new DriveController();
	DifferencialController mDiffCtrl = new DifferencialController();
	AbstractRobot robot;

	boolean mSimulateMode = true;

	Input m_input = new Input();
	Output m_output = new Output();

	public void setRobot(AbstractRobot robot) {
		this.robot = robot;
		m_driver.updateSettings(robot.getSettings());
		mDiffCtrl.updateSettings( robot.getSettings());
	}

	public void setDriveGoal(double v, double theta) {
		m_driver.setGoal(v, theta);
		m_input.v = v;
		m_input.w = theta;
	}

	public void execute(long left_ticks, long right_ticks, double dt) {

		// if (mSimulateMode)
		// robot.updateState((long) m_left_ticks, (long) m_right_ticks, dt);
		// else
		if (!mSimulateMode)
			robot.updateState(left_ticks, right_ticks, dt);

		m_output = m_driver.execute(robot, m_input, dt);

		Input in = new Input();
		in.v = m_output.v;
		in.w = m_output.w;
	  
		Output out = mDiffCtrl.execute(robot, in, dt);
		
		int pwm_l = (int)robot.vel_l_to_pwm(out.vel_l );
		int pwm_r = (int)robot.vel_r_to_pwm(out.vel_r );

		robot.moveMotor(pwm_l, pwm_r, dt);

		// PWMOut pwm = robot.getPWMOut(v, w);
		// robot.moveMotor(pwm.pwm_l, pwm.pwm_r, dt);

		// Vel mVel = robot.ensure_w(v, w);

		// int pwm_l = (int) robot.vel_l_to_pwm(mVel.vel_l);
		// int pwm_r = (int) robot.vel_r_to_pwm(mVel.vel_r);

		// int dif = pwm_l - pwm_r;

		// if (dif > 80 && pwm_l * pwm_r > 0) {
		// if (pwm_l > 0) {
		// pwm_l = pwm_r + 80;
		// } else {
		// pwm_r = pwm_l - 80;
		// }
		// } else if (dif < -80) {
		// if (pwm_l > 0) {
		// pwm_r = pwm_l + 80;
		// } else {
		// pwm_l = pwm_r - 80;
		// }
		// }

		// if (mSimulateMode) {
		// m_left_ticks = m_left_ticks + robot.pwm_to_ticks_l(pwm_l, dt);
		// m_right_ticks = m_right_ticks + robot.pwm_to_ticks_r(pwm_r, dt);
		// } else {

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
		mDiffCtrl.updateSettings(settings);

	}

	public void reset()
	{
		m_driver.reset( robot );
		mDiffCtrl.reset();
	}

}

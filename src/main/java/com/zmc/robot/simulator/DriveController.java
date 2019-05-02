package com.zmc.robot.simulator;

public class DriveController extends Controller {

	Output output = new Output();

	double mTheta; // 目标方向
	double mW; // 转弯
	double curW; // 当前的转弯状态
	boolean keepTheta; // 是否需要保存当前方向

	long thetaPrevMillis;

	boolean okToKeep;

	public DriveController() {
		mW = 0;
		curW = 0;
		mTheta = 0;
		keepTheta = false;
	}

	public void setGoal(double v, double w) {

		if (w == 0 && curW != 0) // remain the current theta; 加速过程中会有晃动；保留初始角度？
		{
			keepTheta = true;
			thetaPrevMillis = System.currentTimeMillis();
		}
		curW = w;
		mW = w;
		if (mW == 0) {
			lastErrorIntegration = 0;
			lastError = 0;
		}
	}

	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		double e, e_I, e_D, w;

		if (mW != 0) // 转弯，控制角速度？
		{
			output.v = input.v;
			output.w = mW;

			// e = mW - robot.w;

			// e_I = lastErrorIntegration + e * dt;
			// e_D = (e - lastError) / dt;
			// w = Kp * e + Kd * e_D + Ki * e_I;

			// lastErrorIntegration = e_I;
			// if (Math.abs(lastErrorIntegration) > 10)
			// lastErrorIntegration = 0;

			// output.w = w;
			return output;
		}

		if (keepTheta) {
			if (System.currentTimeMillis() - thetaPrevMillis > 50) //
			{
				keepTheta = false; // next circle to keep the theta??
				okToKeep = true;
			} else {
				e = 0;
				lastErrorIntegration = 0;
				output.v = input.v;
				output.w = 0;
				return output;
			}
		}

		if (okToKeep) {
			okToKeep = false;
			mTheta = robot.theta; // keep current direction
		}

		e = mTheta - robot.theta;
		e = Math.atan2(Math.sin(e), Math.cos(e));

		e_I = lastErrorIntegration + e * dt;
		e_D = (e - lastError) / dt;
		w = Kp * e + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		if (Math.abs(lastErrorIntegration) > 10)
			lastErrorIntegration = 0;

		output.v = input.v;
		output.w = w;
		lastError = e;

		return output;
	}

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
	}

}

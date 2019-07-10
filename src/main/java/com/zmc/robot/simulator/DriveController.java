package com.zmc.robot.simulator;

import org.apache.log4j.Logger;

public class DriveController extends Controller {

	private final static String TAG = "DRV_CTRL";

	Logger log = Logger.getLogger(TAG);

	Output output = new Output();

	double mTheta; // 目标方向
	double mW; // 转弯
	double curW; // 当前的转弯状态
	boolean keepTheta; // 是否需要保存当前方向

	long thetaPrevMillis;

	boolean okToKeep;

	private double m_v, m_w; // 拐弯时的输出

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
		lastErrorIntegration = 0;
		lastError = 0;

		if (mW != 0) // 拐弯，计算拐弯mW
		{
			m_v = 0;

			double sw = Math.abs(mW);
			if (sw > 1.5)
				sw = 1.5;

			if (v != 0) // 按照w 减速
			{
				m_v = -0.027 * sw + 0.12;
				if (m_v < 0.08)
					m_v = 0.08;
				if (m_v > Math.abs(v))
					m_v = Math.abs(v);

				if (v < 0)
					m_v = -m_v;
			}

			// D = -maxD*mw/PI + maxD
			// w = 2*V/(2*D+L)

			double maxD = 0.2;

			m_w = 2 * m_v / (2 * (-maxD * sw / Math.PI + maxD) + 0.16);
			if (mW < 0)
				m_w = -m_w;

			if (v == 0)
				m_w = 2 * mW;

		}
	}

	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		double e, e_I, e_D, w;

		if (mW != 0) // 转弯，控制角速度？
		{
			output.v = m_v; // input.v / (1 + Math.abs(robot.w) / 3);
			output.w = m_w; // 2 * mW;

			// output.v = input.v; // / (1 + Math.abs(robot.w));
			// output.w = mW; // 5 * mW;

			// e = mW - robot.w;

			// e_I = lastErrorIntegration + e * dt;
			// e_D = (e - lastError) / dt;
			// w = Kp * e + Kd * e_D + Ki * e_I;

			// lastErrorIntegration = e_I;
			// if (Math.abs(lastErrorIntegration) > 10)
			// lastErrorIntegration = 0;
			log.info(String.format("tr(tw,v,w,iv,ov,ow):%.3f,%.3f,%.3f,%.3f,%.3f,%.3f", mW, robot.velocity, robot.w,
					input.v, output.v, output.w));
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

		double p = Kp;

		if (Math.abs(e) > 2)
			p = p / 3;
		else if (Math.abs(e) > 1)
			p = p / 2;

		if (Math.abs(e) > 1) {
			e_I = 0;
		} else
			e_I = lastErrorIntegration + e * dt;

		// e_I = lastErrorIntegration + e * dt;
		e_D = (e - lastError) / dt;
		w = p * e + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		// if (Math.abs(lastErrorIntegration) > 10)
		// lastErrorIntegration = 0;

		output.v = input.v;
		output.w = w;
		lastError = e;

		// log.info(String.format("Li(v, iv,e,ow):%.3f,%.3f,%.3f,%.3f", robot.velocity,
		// output.v, e, output.w));

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

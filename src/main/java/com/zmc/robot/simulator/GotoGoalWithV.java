package com.zmc.robot.simulator;

import org.apache.log4j.Logger;

public class GotoGoalWithV extends Controller {

	// private static final String TAG = "GTG";

	private final static String TAG = "GTG";

	Logger log = Logger.getLogger(TAG);

	private double m_xg, m_yg;

	private int state = 0; // normal state
	// private boolean targetMoified = false;

	Output output = new Output();
	private Vector uGtg = new Vector();

	private double lastVE = 0;
	private double lastVEI = 0;

	private double pkp, pki, pkd;
	private double tkp, tki, tkd;

	public GotoGoalWithV() {

	}

	@Override
	public void updateSettings(Settings settings) {
		Kp = settings.kp;
		Ki = settings.ki;
		Kd = settings.kd;

		pkp = settings.pkp;
		pki = settings.pki;
		pkd = settings.pkd;

		tkp = settings.tkp;
		tki = settings.tki;
		tkd = settings.tkd;
		// Log.i("CTRL", "Update settings: kp=" + Kp + ", ki=" + Ki + ",kd=" + Kd);

	}

	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		double u_x, u_y, e, e_I, e_D, w, theta_g;

		u_x = input.x_g - robot.x;
		u_y = input.y_g - robot.y;

		theta_g = Math.atan2(u_y, u_x);

		double d = Math.sqrt(Math.pow(u_x, 2) + Math.pow(u_y, 2));

		output.v = input.v;

		double ve, vei, ved;

		if (d < 0.3 && d > 0.02) {
			ve = d;
			vei = lastVEI + ve * dt;
			ved = (ve - lastVE) / dt;
			output.v = pkp * ve + pki * vei + pkd * ved; //
			if (output.v > 0.3)
				output.v = 0.3; ///////

			log.info(String.format("D: %.3f, %.3f", d, output.v));

			lastVEI = vei;
			lastVE = ve;
		} else {
			lastVEI = 0;
			lastVE = 0;
		}

		if (d < 0.02) // at goal
		{
			if (state == 0) {
				// Log.i(TAG, "Zero V, cirle!");
				lastError = 0;
				lastErrorIntegration = 0;
				state = 1;
			}
			output.v = 0;
			theta_g = input.theta;
		} else {
			state = 0;
		}

		uGtg.x = u_x;
		uGtg.y = u_y;

		e = theta_g - robot.theta;
		e = Math.atan2(Math.sin(e), Math.cos(e));
		e_I = lastErrorIntegration + e * dt;
		e_D = (e - lastError) / dt;

		if (output.v == 0) {
			w = tkp * e + tki * e_I + tkd * e_D;
			log.info(String.format("T: %.3f, %.3f", e, w));
		} else {
			w = Kp * e + Ki * e_I + Kd * e_D;
		}
		lastErrorIntegration = e_I;
		lastError = e;
		output.w = w;

		return output;

	}

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;

		lastVE = 0;
		lastVEI = 0;

		state = 0;
		// targetMoified = false;

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uGotoGoal = uGtg;
		state.ux = this.m_xg;
		state.uy = this.m_yg;
	}

}

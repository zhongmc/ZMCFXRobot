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

		double u_x, u_y, e, e_I, e_D, w = 0, theta_g;

		u_x = input.x_g - robot.x;
		u_y = input.y_g - robot.y;

		theta_g = Math.atan2(u_y, u_x);

		double d = Math.sqrt(Math.pow(u_x, 2) + Math.pow(u_y, 2));

		output.v = input.v;

		double ve, vei, ved;

		// state 0: normal, 1: d control 2: theta control
		if (d >= 0.3) {
			if (state != 0) {
				state = 0;
				lastError = 0;
				lastErrorIntegration = 0;
				log.info("Change to normal ctrl...");
			}
		} else if (d < 0.3 && d > 0.02) {
			if (state == 2 && d > 0.03) {
				state = 1; // change to d cntrol
				log.info("Change to distance ctrl...");
			} else if (state == 0) // if d small enougth, stay theta control
			{
				log.info("Change to distance ctrl...");
				state = 1;
				lastVEI = 0;
				lastVE = 0;
			}
		}

		if (d <= 0.02) {
			if (state != 2) {
				log.info("Change to theta ctrl...");
				state = 2;
				lastErrorIntegration = 0;
				lastError = 0;
			}
		}

		if (state == 2)
			theta_g = input.targetAngle;

		e = theta_g - robot.theta;
		e = Math.atan2(Math.sin(e), Math.cos(e));

		e_D = (e - lastError) / dt;

		if (state == 0 || state == 1) { // dir
			double p = Kp;

			// if (Math.abs(e) > 2)
			// 	p = p / 3;
			// else if (Math.abs(e) > 1)
			// 	p = p / 2;

			if (Math.abs(e) > 1) {
				e_I = 0;
			} else
				e_I = lastErrorIntegration + e * dt;

			w = p * e + Ki * e_I + Kd * e_D;
			lastErrorIntegration = e_I;
			lastError = e;
			output.v = input.v; // / (1 + Math.abs(w)); // robot.w Math.abs(e));
			output.w = w;

			// if (state == 0) // && Math.abs(e) > 0.5) {
			// log.info(String.format("V: %.3f, %.3f, %.3f", e, w, output.v));

		}
		// } else

		if (state == 1) { // distance

			output.v = d*(input.v - 0.05)/0.3 + 0.05;

			// ve = d;
			// vei = lastVEI + ve * dt;
			// ved = (ve - lastVE) / dt;
			// output.v = pkp * ve + pki * vei + pkd * ved; //
			// if (output.v > input.v)
			// 	output.v = input.v; ///////
			// // output.w = 0;
			// // log.info(String.format("D: %.3f, %.3f, %.3f", d, w, output.v));
			// lastVEI = vei;
			// lastVE = ve;
		} else if (state == 2) { // theta
			double p = tkp;
			// if (Math.abs(e) > 2)
			// 	p = p / 3;
			// else if (Math.abs(e) > 1)
			// 	p = p / 2;
			if (Math.abs(e) > 1) {
				e_I = 0;
			} else
				e_I = lastErrorIntegration + e * dt;
			w = p * e + tki * e_I + tkd * e_D;
			// log.info(String.format("T: %.3f, %.3f, %.3f, %.3f, %.1f, %.1f", d, e, w,
			// robot.theta, robot.m_left_ticks,
			// robot.m_right_ticks));
			lastErrorIntegration = e_I;
			lastError = e;

			output.w = w;
			output.v = 0;
		}

		uGtg.x = u_x;
		uGtg.y = u_y;

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

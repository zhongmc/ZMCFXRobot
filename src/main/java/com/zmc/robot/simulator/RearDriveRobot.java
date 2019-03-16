package com.zmc.robot.simulator;

public class RearDriveRobot extends AbstractRobot {

	public RearDriveRobot() {

		x = 0;
		y = 0;
		theta = 0;

		prev_left_ticks = 0;
		prev_right_ticks = 0;

		settings.kp = 6; // 25;// 5;
		settings.ki = 0.01; //// 0.8; // 0.1; //0.01;
		settings.kd = 0.05; // 0.1;

		settings.atObstacle = 0.3; // 0.15; //0.12;// 0.25; 0.2
		settings.unsafe = 0.1; // 0.05; // 0.05
		settings.dfw = 0.25; // 0.20;// 0.30; //0.25

		settings.velocity = 0.5; // 0.50;
		settings.max_rpm = 150; // 240;
		settings.min_rpm = 80; // 90; // 110; // 0
		settings.angleOff = 0;
		settings.pwm_diff = 0;

		wheel_radius = 0.065 / 2;

		wheel_base_length = 0.150;

		settings.wheelRadius = wheel_radius;
		settings.wheelDistance = wheel_base_length;

		ticks_per_rev_l = 330;
		ticks_per_rev_r = 360;

		m_per_tick_l = (2 * Math.PI * wheel_radius) / ticks_per_rev_l;
		m_per_tick_r = (2 * Math.PI * wheel_radius) / ticks_per_rev_r;

		max_rpm = settings.max_rpm; // 267
		max_vel = max_rpm * 2 * Math.PI / 60;

		min_rpm = settings.min_rpm; // 113
		min_vel = min_rpm * 2 * Math.PI / 60;

		max_v = max_vel * wheel_radius;
		min_v = min_vel * wheel_radius;

		// min_w = 0; תȦʱ���߸� min_vel, max_w Ϊ����ٶȲ �� max_velʱ��
		max_w = (wheel_radius / wheel_base_length) * (2 * min_vel);
		min_w = 0; // (wheel_radius / wheel_base_length) * (min_vel);

		irSensors[0] = new IRSensor(-0.045, 0.05, Math.PI / 2);
		// irSensors[1] = new IRSensor(0.10, 0.04, Math.PI / 4); // 0.16,0.045, PI/6
		// 0.075, 0.035
		irSensors[1] = new IRSensor(0.08, 0.04, Math.PI / 4); // 0.16,0.045, PI/6 0.075, 0.035
		irSensors[2] = new IRSensor(0.162, 0.0, 0);
		irSensors[3] = new IRSensor(0.08, -0.04, -Math.PI / 4);
		irSensors[4] = new IRSensor(-0.045, -0.05, -Math.PI / 2);
	}

	public double vel_l_to_pwm(double vel) {
		double nvel = Math.abs(vel);
		if (nvel < min_vel - 0.1)
			return 0.0;

		if (nvel < this.min_vel)
			nvel = min_vel;

		if (nvel > max_vel)
			nvel = max_vel;

		double retVal = 6.257 * nvel + 46.868;
		// double retVal = 0.5729 * nvel * nvel - 5.1735 * nvel + 86.516;

		if (vel >= 0)
			return retVal;
		else
			return -retVal;
	}

	public double vel_r_to_pwm(double vel) {
		double nvel = Math.abs(vel);
		if (nvel < (min_vel - 0.1))
			return 0.0;

		if (nvel < this.min_vel)
			nvel = min_vel;

		if (nvel > max_vel)
			nvel = max_vel;

		double retVal = 6.257 * nvel + 46.868;

		// double retVal = 0.5649 * nvel * nvel - 4.3156 * nvel + 80.706;
		// 0.4747*nvel*nvel - 3.956*nvel + 80.706;
		if (vel >= 0)
			return retVal;
		else
			return -retVal;
	}

	public double pwm_to_ticks_l(double pwm, double dt) {
		double npwm = Math.abs(pwm);

		if (npwm < 60)
			return 0;

		if (npwm > 220)
			npwm = 220;

		double ticks = dt * (-0.024 * npwm * npwm + 12.097 * npwm - 426.23);// dt*(0.4975*npwm

		// y = -0.024x2 + 12.097x - 426.23
		// y = -0.0218x2 + 11.634x - 358.83

		if (pwm > 0)
			return ticks;
		else
			return -ticks;

	}

	public double pwm_to_ticks_r(double pwm, double dt) {
		double npwm = Math.abs(pwm);

		if (npwm < 60)
			return 0;

		if (npwm > 220)
			npwm = 220;

		double ticks = dt * (-0.0218 * npwm * npwm + 11.634 * npwm - 358.83);

		// y = -0.024x2 + 12.097x - 426.23
		// y = -0.0218x2 + 11.634x - 358.83

		if (pwm > 0)
			return ticks;
		else
			return -ticks;
	}

	// public void addObstacle(Obstacle obs) {
	// obstacles.add(obs);
	// }

	public Vel ensure_w(double v, double w) {
		Vel vel;

		if (Math.abs(v) > 0) {
			vel = new Vel();
			double v_lim, w_lim;
			v_lim = Math.abs(v);
			if (v_lim > this.max_v)
				v_lim = max_v;
			w_lim = Math.abs(w);
			if (w_lim > max_w)
				w_lim = max_w;

			w_lim = Math.signum(w) * w_lim;

			Vel vel_d = uni_to_diff(v_lim, w_lim);

			double vel_rl_max, vel_rl_min;
			if (vel_d.vel_l > vel_d.vel_r) {
				vel_rl_min = vel_d.vel_r;
				vel_rl_max = vel_d.vel_l;
			} else {
				vel_rl_min = vel_d.vel_l;
				vel_rl_max = vel_d.vel_r;

			}

			if (vel_rl_max > this.max_vel) {
				vel.vel_r = vel_d.vel_r - (vel_rl_max - max_vel);
				vel.vel_l = vel_d.vel_l - (vel_rl_max - max_vel);

				if ((vel_rl_min - (vel_rl_max - max_vel)) < min_vel) // ����䣿
				{
					if (vel.vel_l < vel.vel_r) {
						vel.vel_l = 0;
						vel.vel_r = (max_vel + min_vel) / 2;
					} else {
						vel.vel_r = 0;
						vel.vel_l = (max_vel + min_vel) / 2;

					}
				}

			} else if (vel_rl_min < min_vel) // ����ͣһ���ֵĹ���
			{
				vel.vel_r = vel_d.vel_r + (min_vel - vel_rl_min);
				vel.vel_l = vel_d.vel_l + (min_vel - vel_rl_min);
			} else {
				vel.vel_r = vel_d.vel_r;
				vel.vel_l = vel_d.vel_l;
			}

			if (vel.vel_l > max_vel)
				vel.vel_l = max_vel;
			if (vel.vel_r > max_vel)
				vel.vel_r = max_vel;

		} else {
			vel = uni_to_diff(0, w);
			if (Math.abs(vel.vel_l) < this.min_vel) {
				vel.vel_l = Math.signum(vel.vel_l) * min_vel;
				vel.vel_r = Math.signum(vel.vel_r) * min_vel;

			} else if (Math.abs(vel.vel_l) > (min_vel + max_vel) / 2) {
				vel.vel_l = Math.signum(vel.vel_l) * (min_vel + max_vel) / 2;
				vel.vel_r = Math.signum(vel.vel_r) * (min_vel + max_vel) / 2;

			}

		}

		return vel;
	}

}

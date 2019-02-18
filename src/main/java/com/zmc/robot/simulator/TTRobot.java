package com.zmc.robot.simulator;
//import com.zmc.robot.ui.ObstacleCrossPoint;

public class TTRobot extends AbstractRobot {
	public TTRobot() {

		x = 0;
		y = 0;
		theta = 0;

		prev_left_ticks = 0;
		prev_right_ticks = 0;

		settings.kp = 5;
		settings.ki = 0.1; // 0.01;
		settings.kd = 0.1; // 0.1;

		settings.atObstacle = 0.35; // 0.15; //0.12;// 0.25; 0.2
		settings.unsafe = 0.05; // 0.05; // 0.05
		settings.dfw = 0.25; // 0.20;// 0.30; //0.25

		settings.velocity = 0.6; // 0.50;
		settings.max_rpm = 200;
		settings.min_rpm = 40; // 80; // 0
		settings.angleOff = 0;
		settings.pwm_diff = 0;

		wheel_radius = 0.065 / 2;
		// �־� 13cm
		wheel_base_length = 0.156;
		ticks_per_rev_l = 20;
		ticks_per_rev_r = 20;
		m_per_tick_l = (2 * Math.PI * wheel_radius) / ticks_per_rev_l;
		m_per_tick_r = (2 * Math.PI * wheel_radius) / ticks_per_rev_r;

		max_rpm = settings.max_rpm;

		max_vel = max_rpm * 2 * Math.PI / 60;

		min_rpm = settings.min_rpm;

		min_vel = min_rpm * 2 * Math.PI / 60;

		max_v = max_vel * wheel_radius;
		min_v = min_vel * wheel_radius;
		max_w = (wheel_radius / wheel_base_length) * (max_vel - min_vel);

		irSensors[0] = new IRSensor(-0.11, 0.063, Math.PI / 2);
		irSensors[1] = new IRSensor(0.062, 0.045, Math.PI / 4);
		irSensors[2] = new IRSensor(0.076, 0.0, 0);
		irSensors[3] = new IRSensor(0.062, -0.045, 2 * Math.PI - Math.PI / 4);
		irSensors[4] = new IRSensor(-0.11, -0.063, 2 * Math.PI - Math.PI / 2);

		// double ir_positions[][] = {{-0.11,0.063}, {0.062, 0.045},
		// {0.076, 0.0},{0.062, -0.045},{-0.11, -0.063}};
		// double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4,
		// Math.PI/2 };

		// ocps[0] = new ObstacleCrossPoint();
		// ocps[1] = new ObstacleCrossPoint();
		// ocps[2] = new ObstacleCrossPoint();
		// ocps[3] = new ObstacleCrossPoint();
		// ocps[4] = new ObstacleCrossPoint();

		// double ir_positions[][] = {{-0.0582,0.0584}, {0.05725, 0.03555},{0.0686,
		// 0.0},{0.05725, -0.0355},{-0.0582, -0.0584}};
		// double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4,
		// Math.PI/2 };

	}

	public double vel_l_to_pwm(double vel) {
		// ax^2+bx+c��
		double nvel = Math.abs(vel);
		double retVal = 9.6893 * nvel + 10.179; // 6.393*nvel + 13.952;

		if (vel >= 0)
			return retVal;
		else
			return -retVal;
	}

	public double vel_r_to_pwm(double vel) {
		// ax^2+bx+c��
		double nvel = Math.abs(vel);
		double retVal = 9.5946 * nvel + 18.738; // 6.2798*nvel + 18.787;

		if (vel >= 0)
			return retVal;
		else
			return -retVal;

	}

	public double pwm_to_ticks_r(double pwm, double dt) {
		double npwm = Math.abs(pwm);
		if (npwm < 20)
			return 0;

		double ticks = dt * (0.3316 * npwm - 6.1946);// dt*(0.5084*npwm - 9.7666);
		if (pwm > 0)
			return ticks;
		else
			return -ticks;
	}

	public double pwm_to_ticks_l(double pwm, double dt) {
		double npwm = Math.abs(pwm);
		if (npwm < 14)
			return 0;

		double ticks = dt * (0.3248 * npwm - 2.8198);// dt*(0.4975*npwm -6.9066);
		if (pwm > 0)
			return ticks;
		else
			return -ticks;

	}

	public Vel ensure_w(double v, double w) {
		if (w > max_w)
			w = max_w;
		else if (w < 0 && Math.abs(w) > max_w)
			w = -max_w;

		Vel vel = uni_to_diff(v, w);
		double vel_l, vel_r;

		double vel_min, vel_max;
		vel_min = vel.vel_l;
		vel_max = vel.vel_r;

		if (vel_min > vel.vel_r) {
			vel_min = vel.vel_r;
			vel_max = vel.vel_l;
		}

		// stop one motor to support large angle turning
		double minVel = 0;
		if (Math.abs(w) < 0.2)
			minVel = min_vel;

		if (vel_max > max_vel) {
			vel_r = vel.vel_r - (vel_max - max_vel);
			vel_l = vel.vel_l - (vel_max - max_vel);

		} else if (vel_min < minVel) {
			vel_r = vel.vel_r + (minVel - vel_min);
			vel_l = vel.vel_l + (minVel - vel_min);

		} else {
			vel_r = vel.vel_r;
			vel_l = vel.vel_l;
		}

		if (vel_l < minVel)
			vel_l = minVel;
		else if (vel_l > max_vel)
			vel_l = max_vel;

		if (vel_r < minVel)
			vel_r = minVel;
		else if (vel_r > max_vel)
			vel_r = max_vel;

		vel.vel_l = vel_l;
		vel.vel_r = vel_r;
		return vel;
	}

}

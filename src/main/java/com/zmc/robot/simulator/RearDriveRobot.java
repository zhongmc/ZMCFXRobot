package com.zmc.robot.simulator;

public class RearDriveRobot extends AbstractRobot {

	public RearDriveRobot() {

		x = 0;
		y = 0;
		theta = 0;

		prev_left_ticks = 0;
		prev_right_ticks = 0;

		settings.kp = 4; // 25;// 5;
		settings.ki = 0.05; //// 0.8; // 0.1; //0.01;
		settings.kd = 0.0; // 0.1;

		settings.pkp = 0.4;//5;
		settings.pki = 0.6;//0.1
		settings.pkd = 0.001;

		settings.tkp = 10;
		settings.tki = 0.2;
		settings.tkd = 0.1;

		settings.atObstacle = 0.3; // 0.15; //0.12;// 0.25; 0.2
		settings.unsafe = 0.05; // 0.05; // 0.05
		settings.dfw = 0.25; // 0.25; // 0.20;// 0.30; //0.25

		settings.velocity = 0.12; // 0.50;

		//to get vel and pwm according to vel-pwm
		settings.max_rpm = 86; // 240;   
		settings.min_rpm = 19; // 90; // 110; // 0

		settings.angleOff = 0;
		settings.pwm_diff = 0;

		// 黑色轮子 自制板
		wheel_radius = 0.0317;
		wheel_base_length = 0.162; // 0.166; 长板

		settings.wheelRadius = wheel_radius;
		settings.wheelDistance = wheel_base_length;

		ticks_per_rev_l = 990;
		ticks_per_rev_r = 990;

		m_per_tick_l = (2 * Math.PI * wheel_radius) / ticks_per_rev_l;
		m_per_tick_r = (2 * Math.PI * wheel_radius) / ticks_per_rev_r;

		max_rpm = settings.max_rpm; // 267
		max_vel = max_rpm * 2 * Math.PI / 60;

		min_rpm = settings.min_rpm; // 113
		min_vel = min_rpm * 2 * Math.PI / 60;

		max_w = 1.5;  //转弯限制

		irSensors[0] = new IRSensor(-0.085, 0.067, Math.PI / 2);
		irSensors[1] = new IRSensor(0.052, 0.057, Math.PI / 4); // 0.16,0.045, PI/6 0.075, 0.035
		irSensors[2] = new IRSensor(0.063, 0.0, 0);
		irSensors[3] = new IRSensor(0.052, -0.057, -Math.PI / 4);
		irSensors[4] = new IRSensor(-0.085, -0.067, -Math.PI / 2);

	
	  
		// final double ir_positions[][] = { { -0.073, 0.066, 1 }, { 0.061, 0.05, 1 }, {
		// 0.072, 0, 1 },
		// { 0.061, -0.05, 1 }, { -0.073, -0.066, 1 } };

	}

	public double vel_l_to_pwm(double vel) {
		double nvel = Math.abs(vel);

		if (nvel < min_vel )
			return 0;

		if( nvel > max_vel )
			nvel = max_vel;

		double retVal = 13 * nvel + 24;

		if (vel >= 0)
			return retVal;
		else
			return -retVal;
	}

	public double vel_r_to_pwm(double vel) {

		double nvel = Math.abs(vel);

		if (nvel < min_vel )
			return 0;

		if( nvel > max_vel )
			nvel = max_vel;

		double retVal = 13 * nvel + 24; // 10 * nvel; //
		if (vel >= 0)
			return retVal;
		else
			return -retVal;
	}

	public double pwm_to_ticks_l(double pwm, double dt) {
		double npwm = Math.abs(pwm);

		if( npwm == 0)
			return 0;

		double ticks = dt*(-0.0585*npwm*npwm+21.217*npwm-564.14);

		if (pwm > 0)
			return ticks;
		else
			return -ticks;

	}

	public double pwm_to_ticks_r(double pwm, double dt) {
		double npwm = Math.abs(pwm);

		if( npwm == 0 )
			return 0;
		double ticks = dt * (-0.0707*npwm*npwm+23.943*npwm-709.5);
		if (pwm > 0)
			return ticks;
		else
			return -ticks;
	}

	public PWMOut getPWMOut(double v, double w) {
		Vel vel = ensure_w(v, w);

		int pwm_l = (int) vel_l_to_pwm(vel.vel_l);
		int pwm_r = (int) vel_r_to_pwm(vel.vel_r);

		PWMOut pwmOut = new PWMOut(pwm_l, pwm_r);
		return pwmOut;
	}

	public Vel ensure_w(double v, double w) {
		
		Vel vel = uni_to_diff(v, w);

		if( v == 0 ) //
		{
			if( Math.abs( vel.vel_l ) > 1.5*min_vel )
			{
				if( vel.vel_l < 0 )
				{
					vel.vel_l = -1.5*min_vel;
					vel.vel_r = 1.5*min_vel;
				}
				else
				{
					vel.vel_l = 1.5*min_vel;
					vel.vel_r = -1.5*min_vel;

				}
			}
			return vel;			
		}

		double sw = w;
		if (sw > max_w)
		  sw = max_w;  
		else if (sw < -max_w)
		  sw = -max_w;
		
	  double sv = Math.abs(v);
	  sv = (-sv+0.05)/max_w * Math.abs(sw) + sv;
	  if( sv <=0 )
		sv = 0.01;

	  if( v < 0  )
		sv = -sv;
		  
	  vel = uni_to_diff(sv, sw);
	  
	if (vel.vel_l * vel.vel_r >= 0)
		  return vel;
	  
		if (Math.abs(vel.vel_l) > Math.abs(vel.vel_r)) 
		{
		  vel.vel_r = 0;
		}
		else
		  vel.vel_l = 0;
	  
		return vel;		

	}

}

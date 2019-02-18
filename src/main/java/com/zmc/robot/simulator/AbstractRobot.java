package com.zmc.robot.simulator;

public abstract class AbstractRobot {

	private RobotState mState = new RobotState();

	protected Settings settings = new Settings();

	double x, y, theta; // λ��`
	double w;

	double velocity;

	double wheel_radius; // ���Ӱ뾶
	public double wheel_base_length; // �־�

	// ������С����ÿ����Ȧ��
	double max_rpm;
	double min_rpm;

	double max_vel, min_vel; // min_vel ������������ƶ�����С���ٶ�

	double max_v, min_v;
	double min_w, max_w;
	// double angle;

	int ticks_per_rev_l, ticks_per_rev_r; // ÿȦ��������
	double m_per_tick_l, m_per_tick_r; // ÿ������ת���ľ���
	long prev_left_ticks, prev_right_ticks;

	IRSensor irSensors[] = new IRSensor[5];
	double irDistances[] = new double[5];

	// ObstacleCrossPoint ocps[] = new ObstacleCrossPoint[5];
	// private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

	public AbstractRobot() {
	}

	public String toString() {
		return x + ", " + y + ":" + theta;
	}

	// public void addObstacle(Obstacle obs) {
	// obstacles.add(obs);
	// }

	public IRSensor[] getIRSensors() {
		return irSensors;

	}

	public void setPosition(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		this.theta = theta;
	}

	public void updateState(long left_ticks, long right_ticks, double dt) {

		// long left_ticks, right_ticks;
		if (prev_right_ticks == right_ticks && prev_left_ticks == left_ticks) {
			// vel_l = 0;
			// vel_r = 0;
			velocity = 0;
			mState.velocity = 0;
			return; // no change
		}
		double d_right, d_left, d_center;

		d_left = (left_ticks - prev_left_ticks) * m_per_tick_l;
		d_right = (right_ticks - prev_right_ticks) * m_per_tick_r;

		// vel_l = (d_left / wheel_radius) / dt;
		// vel_r = (d_right / wheel_radius) / dt;

		velocity = (d_right + d_left) / (2 * dt);

		prev_right_ticks = right_ticks;
		prev_left_ticks = left_ticks;

		d_center = (d_right + d_left) / 2;
		double phi = (d_right - d_left) / wheel_base_length;
		w = phi / dt;

		x = x + d_center * Math.cos(theta);
		y = y + d_center * Math.sin(theta);
		theta = theta + phi;
		theta = Math.atan2(Math.sin(theta), Math.cos(theta));

		mState.x = x;
		mState.y = y;
		mState.theta = theta;
		mState.velocity = velocity;

		// updateIRDistances();

	}

	public void setIRDistances(double[] irds) {
		irDistances = irds;
		for (int i = 0; i < 5; i++) {

			IRSensor irSensor = irSensors[i];
			irSensor.setDistance(irDistances[i]);
			irSensor.applyGeometry(x, y, theta);

		}
	}

	public double[] getIRDistances() {
		return irDistances;
	}

	public double getObstacleDistance() {
		double d = irDistances[1];
		if (d > irDistances[2])
			d = irDistances[2];
		if (d > irDistances[3])
			d = irDistances[3];
		return d;
	}

	// ��ȡIR ָ�����������
	public Vector getIRSensorVector(int idx, double distance) {
		Vector p = new Vector();
		IRSensor irSensor = irSensors[idx].clone();
		irSensor.setDistance(distance);
		irSensor.applyGeometry(x, y, theta);

		p.x = irSensor.xw;
		p.y = irSensor.yw;
		return p;
	}

	public void reset(long left_ticks, long right_ticks) {
		prev_left_ticks = left_ticks;
		prev_right_ticks = right_ticks;
	}

	public Vel uni_to_diff(double v, double w) {
		Vel vel = new Vel();
		vel.vel_r = (2 * v + w * wheel_base_length) / (2 * wheel_radius);
		vel.vel_l = (2 * v - w * wheel_base_length) / (2 * wheel_radius);
		return vel;
	}

	public Output diff_to_uni(double vel_l, double vel_r) {
		Output out = new Output();
		if (vel_l + vel_r == 0) {
			// Log.i(TAG, "div by o...in robot 1");
			out.v = 0.5;
			return out;
		} else
			out.v = wheel_radius / 2 * (vel_l + vel_r);

		if (vel_r - vel_l == 0) {
			// Log.i(TAG, "div by o...in robot 2");
			out.w = Math.PI / 2;
		} else
			out.w = wheel_radius / wheel_base_length * (vel_r - vel_l);

		return out;
	}

	public abstract Vel ensure_w(double v, double w);

	public abstract double vel_l_to_pwm(double vel);

	public abstract double vel_r_to_pwm(double vel);

	public abstract double pwm_to_ticks_r(double pwm, double dt);

	public abstract double pwm_to_ticks_l(double pwm, double dt);

	/**
	 * ��Բ�ͬ�ĵ����robot������ͬ�Ĳ����趨
	 * 
	 * @return
	 */
	public Settings getSettings()

	{
		return settings;
	}

	void updateSettings(Settings settings) {

		this.settings.settingsType = 4;
		this.settings.copyFrom(settings); // update
		this.settings.settingsType = 0;
		this.settings.copyFrom(settings); // update PID param

		max_rpm = settings.max_rpm; // 267
		max_vel = max_rpm * 2 * Math.PI / 60;

		min_rpm = settings.min_rpm; // 113
		min_vel = min_rpm * 2 * Math.PI / 60;

		max_v = max_vel * wheel_radius;
		min_v = min_vel * wheel_radius;
		max_w = (wheel_radius / wheel_base_length) * (max_vel - min_vel);

		// Log.i(TAG, "UpdateSettings: mrpm:" + max_rpm + ", min_rpm:" + min_rpm);
	}

	public RobotState getState() {
		return mState;
	}

}

package com.zmc.robot.simulator;

import com.sun.javafx.geom.Point2D;

import org.apache.log4j.Logger;

public class Supervisor {

	static int S_STOP = 0;
	static int S_GTG = 1;
	static int S_AVO = 2;
	static int S_FW = 3;

	private boolean ignoreObstacle = false;
	private double velocity = 0.2;

	private final static String TAG = "Supervisor";

	Logger log = Logger.getLogger(TAG);

	private long counter = 0;

	private int mMode = 0; // goto goal 0; avoid obstacle 1;

	GotoGoalWithV m_GoToGoal = new GotoGoalWithV(); // GotoGoal();
	AvoidObstacle m_AvoidObstacle = new AvoidObstacle();
	FollowWall m_FollowWall = new FollowWall();
	SlidingMode m_SlidingMode = new SlidingMode();

	// GotoGoalVelocityCtrl m_gtgv = new GotoGoalVelocityCtrl();
	// CircleAvoidObstacle m_CAVO = new CircleAvoidObstacle();

	TraceRoute m_traceRoute = new TraceRoute();

	// ZMCRobot robot; // = new ZMCRobot();

	AbstractRobot robot;

	Controller m_currentController;

	double d_fw;// =0.25; //distance to follow wall

	double d_stop = 0.02;
	double d_at_obs;// = 0.18;
	double d_unsafe;// = 0.05;
	double d_prog = 100;

	boolean progress_made;
	boolean at_goal;
	boolean at_obstacle;
	boolean unsafe;
	boolean danger;
	boolean mSimulateMode = true;

	int m_state = S_GTG;
	double m_distanceToGoal;
	double irDistance;

	Vector m_Goal = new Vector();

	double m_left_ticks, m_right_ticks;

	Input m_input = new Input();
	Output m_output = new Output();

	float[][] mRoutes = null;
	int mRouteSize = 0;

	public Supervisor() {
		m_currentController = m_GoToGoal;
		m_state = S_GTG;
	}

	public void setIgnoreObstacle(boolean value) {
		ignoreObstacle = value;
	}

	public void setMode(int mode) {
		mMode = mode;
		if (this.mMode == 0) // gtg mode
		{
			m_state = S_GTG;
			m_currentController = m_GoToGoal;
		} else {
			m_currentController = this.m_traceRoute;
		}
	}

	public void setRoute(float[][] route, int size) {
		this.mRoutes = route;
		this.mRouteSize = size;
		m_traceRoute.setRoute(route, size);

	}

	public void setGoal(double x, double y, double theta) {
		m_Goal.x = x;
		m_Goal.y = y;

		m_input.x_g = x;
		m_input.y_g = y;
		m_input.theta = theta;

		at_goal = false;

		if (this.mMode == 0) // gtg mode
		{
			m_state = S_GTG;
			m_currentController = m_GoToGoal;
			m_GoToGoal.reset();
			d_prog = 100;
			counter = 0;
		}

	}

	public RobotState getRobotState() {
		return robot.getState();
	}

	public void setIrDistances(double distances[]) {
		robot.setIRDistances(distances);
	}

	public void setRobot(AbstractRobot robot) {
		this.robot = robot;
		this.updateSettings(robot.getSettings());
	}

	public void execute(long left_ticks, long right_ticks, double dt) {
		// m_currentController = null;
		counter++;
		if (mSimulateMode)
			robot.updateState((long) m_left_ticks, (long) m_right_ticks, dt);
		else
			robot.updateState(left_ticks, right_ticks, dt);

		check_states();

		Output output = null;
		if (mMode == 0) {
			output = executeGoToGoal(dt);
		} else if (mMode == 2) // trace route
		{
			output = executeTraceRoute(dt);
			// if( this.m_currentController == this.m_CAVO )
			// at_goal = true;
		}

		if (output == null)
			return;

		// double dis[] = robot.getIRDistances();
		// double obsDis = Math.min(dis[1], dis[2]);
		// obsDis = Math.min(obsDis, dis[3]);
		//
		// double v1 = output.v;
		// double v2 = output.v;

		// ����Ŀ��������ϰ�����롢ת��Ƕȣ��Զ�����
		// if( Math.abs(m_output.w) > 1.5 )
		// v2 = m_output.v /( Math.log10(Math.abs(m_output.w) + 1) + 1 );
		// if( Math.abs(output.w) <5 )
		// v2 = output.v/(Math.abs(output.w/5) + 1); //m_output.w/5
		// else
		// v2 = output.v/(Math.abs(output.w) + 1);

		// if( obsDis < 0.2 )
		// {
		// v1 = output.v * Math.log10(10*obsDis +1);
		// }

		double v = output.v;

		// Math.min(v1, v2);

		if (v != 0 && v < robot.min_v)
			v = 1.01 * robot.min_v;

		double w = output.w; // Math.max( Math.min(output.w, robot.max_w), -robot.max_w);

		Vel vel;
		vel = robot.ensure_w(v, w);

		double pwm_l = robot.vel_l_to_pwm(vel.vel_l);
		double pwm_r = robot.vel_r_to_pwm(vel.vel_r);

		if (mSimulateMode) {
			m_left_ticks = m_left_ticks + robot.pwm_to_ticks_l(pwm_l, dt);
			m_right_ticks = m_right_ticks + robot.pwm_to_ticks_r(pwm_r, dt);

		} else {
			MoveMotor((int) pwm_l, (int) pwm_r);
		}

	}

	public Output executeGoToGoal(double dt) {
		if (at_goal) {
			if (m_state != S_STOP)
				log.info("At Goal! " + counter);
			log.info("At Goal! " + counter);

			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		} else if (danger) {
			if (m_state != S_STOP)
				log.info("Danger! " + counter + "; ird=" + irDistance);
			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if (m_currentController != m_GoToGoal && m_currentController != m_FollowWall)
			m_currentController = m_GoToGoal;

		if (m_state == S_STOP && !unsafe) // recover from stop
		{
			m_state = S_GTG; // gotoGoal;
			m_currentController = m_GoToGoal;
			m_GoToGoal.reset();
		}

		if (m_currentController == m_GoToGoal) {
			if (at_obstacle) {

				this.m_SlidingMode.execute(robot, m_input, dt);
				if (m_SlidingMode.slidingLeft())
					m_FollowWall.dir = 0;
				else if (m_SlidingMode.slidingRight())
					m_FollowWall.dir = 1;

				else
					m_FollowWall.dir = 0;

				log.info("Change to fallow wall ..." + m_FollowWall.dir);
				m_FollowWall.reset();
				m_currentController = m_FollowWall;

				set_progress_point();

				xf = robot.x;
				yf = robot.y;
				theta = robot.theta;

			}

		} else // follow wall
		{
			this.m_SlidingMode.execute(robot, m_input, dt);

			if (progress_made) {

				if (m_FollowWall.dir == 0 && m_SlidingMode.quitSlidingLeft())// !m_SlidingMode.slidingLeft())
				{
					m_state = S_GTG; // gotoGoal;
					m_currentController = m_GoToGoal;
					m_GoToGoal.reset();
					log.info("Change to go to goal state(FW L PM) " + counter + ", IDS=" + irDistance);

				} else if (m_FollowWall.dir == 1 && m_SlidingMode.quitSlidingRight())// !m_SlidingMode.slidingRight())
				{
					m_state = S_GTG; // gotoGoal;
					m_currentController = m_GoToGoal;
					m_GoToGoal.reset();
					log.info("Change to go to goal state (FW R PM) " + counter + ", IDS=" + irDistance);
				}
				// if( shouldGotoGoal())
				// {
				// m_state = S_GTG; // gotoGoal;
				// m_currentController = m_GoToGoal;
				// m_GoToGoal.reset();
				// log.info( "Change to go to goal 2");
				//
				// }
				else {

					Point2D p = getGoalCrossPoint();

					if (p != null) {
						m_state = S_GTG; // gotoGoal;
						log.info("Change to goto goal 1 ...");
						m_GoToGoal.reset();
						m_currentController = m_GoToGoal;

					}

				}
			}

		}

		return m_currentController.execute(robot, m_input, dt);

	}

	private boolean shouldGotoGoal() {
		double sensor_gains[] = { 1, 1, 1, 1, 1 }; // { 1, 1, 1, 1, 1 };

		IRSensor[] irSensors = robot.getIRSensors();

		double uao_x = 0, uao_y = 0;
		for (int i = 0; i < 5; i++) {
			uao_x = uao_x + (irSensors[i].xw - robot.x) * sensor_gains[i];
			uao_y = uao_y + (irSensors[i].yw - robot.y) * sensor_gains[i];
		}

		double gtg_x, gtg_y;

		gtg_x = m_input.x_g - robot.x;
		gtg_y = m_input.y_g - robot.y;

		double px = uao_x * gtg_x + uao_y * gtg_y;
		if (px > 0)
			return true;

		return false;

	}

	private Point2D getGoalCrossPoint() {
		Utils util = new Utils();
		Point2D p0 = new Point2D();
		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		p0.x = (float) robot.x;
		p0.y = (float) robot.y;

		p1.x = (float) xf;
		p1.y = (float) yf;

		p2.x = (float) m_input.x_g;
		p2.y = (float) m_input.y_g;

		Point2D p = util.getCrossPoint(p0, robot.theta, 0.1f, p1, p2);
		return p;

	}

	int wallDir;
	double xf, yf, theta; // enter follow wall point

	/**
	 * 01/30/2019 ��Ϊfollow wall ��ʽ����
	 * 
	 * @param dt
	 * @return
	 */
	public Output executeTraceRoute(double dt) {
		if (danger) {
			if (m_state != S_STOP)
				log.info("Danger! " + counter + "; ird=" + irDistance);
			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if (m_currentController != m_traceRoute && m_currentController != m_FollowWall)
			m_currentController = m_traceRoute;

		if (m_currentController == m_traceRoute) {
			if (at_obstacle) {
				wallDir = getWallDir();

				log.info("Change to fallow wall ..." + wallDir);
				m_FollowWall.reset();
				m_currentController = m_FollowWall;
				m_FollowWall.dir = wallDir - 1;
				xf = robot.x;
				yf = robot.y;
				theta = robot.theta;

			}
		} else {
			// if( !at_obstacle )
			// {
			// int idx = m_traceRoute.recoverGoalFromWall(robot, wallDir);
			// if( idx != -1 )
			// {
			//
			// log.info( "recover to route trace 1..." + idx );
			// m_traceRoute.setCurrentRouteIdx( idx );
			// m_currentController = this.m_traceRoute;
			// }
			// }
			// else

			if (isAwayFromObsEnter()) {
				int idx = m_traceRoute.recoverGoalFromWall(robot, wallDir);
				if (idx != -1) {

					log.info("recover to route trace 2 ..." + idx);
					m_traceRoute.setCurrentRouteIdx(idx);
					m_currentController = this.m_traceRoute;
				}

			}

		}

		return m_currentController.execute(robot, m_input, dt);

	}

	private boolean isAwayFromObsEnter() {
		double d = Math.sqrt(Math.pow((robot.x - xf), 2) + Math.pow((robot.y - yf), 2));
		if (d > 0.3)
			return true;
		return false;
	}

	public Output executeTraceRoute1(double dt) {
		if (danger) {
			if (m_state != S_STOP)
				log.info("Danger! " + counter + "; ird=" + irDistance);
			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if (at_obstacle) {
			if (m_currentController != this.m_AvoidObstacle) {
				log.info("Change to avo obstacle...");
				m_AvoidObstacle.reset();
				m_currentController = m_AvoidObstacle;
			}

			m_currentController = m_AvoidObstacle;
		} else {
			if (m_currentController == this.m_AvoidObstacle) {
				log.info("Recover from avo obstacle...");
				m_traceRoute.recoverGoal(robot);
				// m_traceRoute.reset();
				m_currentController = this.m_traceRoute;
			}

			m_currentController = this.m_traceRoute;
		}

		return m_currentController.execute(robot, m_input, dt);

		// return null;

	}

	private boolean isOneSideWall() {
		IRSensor[] irSensors = robot.getIRSensors();

		int l = 0;
		for (int i = 0; i < 3; i++) {
			if (irSensors[i].distance < IRSensor.maxDistance) {
				l++;
				break;
			}
		}

		if (l == 0)
			return true;

		l = 0;
		for (int i = 2; i < 5; i++) {
			if (irSensors[i].distance < IRSensor.maxDistance) {
				l++;
				break;
			}
		}

		if (l == 0)
			return true;

		return false;
	}

	// 0, 1, 2; none, left, right
	private int getWallDir() {

		Vector p = m_traceRoute.getGoalPoint();
		Input input = new Input();
		input.x_g = p.x;
		input.y_g = p.y;

		this.m_SlidingMode.execute(robot, input, 0.02);
		if (m_SlidingMode.slidingLeft())
			return 1;
		else if (m_SlidingMode.slidingRight())
			return 2;

		return 1;

	}

	public Vector getRecoverPoint() {
		m_traceRoute.reset();
		m_traceRoute.recoverGoal(robot);
		return m_traceRoute.getRecoverPoint();
	}

	public Output executeGoToGoal2(double dt) {

		if (at_goal) {
			if (m_state != S_STOP)
				log.info("At Goal! " + counter);

			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		} else if (danger) {
			if (m_state != S_STOP)
				log.info("Danger! " + counter + "; ird=" + irDistance);
			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if (m_state == S_STOP && !unsafe) // recover from stop
		{
			m_state = S_GTG; // gotoGoal;
			m_currentController = m_GoToGoal;
			m_GoToGoal.reset();
		}

		if (unsafe) // 1 avoid obstacle
		{
			if (m_state != S_AVO) {
				log.info("unsafe , change to avoid Obstacle " + counter + ", IDS=" + irDistance);
				m_AvoidObstacle.reset();
			}
			m_state = S_AVO; // avoidObstacle;
			m_currentController = m_AvoidObstacle;
		} else {
			// Serial.println("exec sliding");

			m_SlidingMode.execute(robot, m_input, dt);

			if (m_state == S_GTG) {
				if (at_obstacle && m_SlidingMode.slidingLeft()) {
					m_FollowWall.dir = 0; // left
					m_currentController = m_FollowWall;
					m_state = S_FW; // followWall;

					log.info("Change to follow wall left state " + counter + ", IDS=" + irDistance);
					m_FollowWall.reset();
					set_progress_point();
				} else if (at_obstacle && m_SlidingMode.slidingRight()) {
					m_FollowWall.dir = 1; // right
					m_currentController = m_FollowWall;
					m_state = S_FW; // followWall;
					log.info("Change to follow wall right state " + counter + ", IDS=" + irDistance);
					m_FollowWall.reset();
					set_progress_point();
				} else if (at_obstacle) {
					m_currentController = m_AvoidObstacle;
					m_state = S_AVO; // avoidObstacle;
					m_AvoidObstacle.reset();
					log.info("Change to Avoid obstacle (from GTG )" + counter);
				}
				// else
				// {
				// m_currentController = m_GoToGoal;
				// // log.info( "Set to goto goal! ");
				// }
			} else if (m_state == S_FW) // followWall )
			{

				if (progress_made) {
					if (m_FollowWall.dir == 0 && m_SlidingMode.quitSlidingLeft())// !m_SlidingMode.slidingLeft())
					{
						m_state = S_GTG; // gotoGoal;
						m_currentController = m_GoToGoal;
						m_GoToGoal.reset();
						log.info("Change to go to goal state(FW L PM) " + counter + ", IDS=" + irDistance);

					} else if (m_FollowWall.dir == 1 && m_SlidingMode.quitSlidingRight())// !m_SlidingMode.slidingRight())
					{
						m_state = S_GTG; // gotoGoal;
						m_currentController = m_GoToGoal;
						m_GoToGoal.reset();
						log.info("Change to go to goal state (FW R PM) " + counter + ", IDS=" + irDistance);
					}
				}
			} else if (m_state == S_AVO) // avoidObstacle)
			{
				if (!at_obstacle) {
					if (m_SlidingMode.slidingLeft()) {
						m_FollowWall.dir = 0; // left
						m_currentController = m_FollowWall;
						m_state = S_FW; // followWall;
						m_FollowWall.reset();
						set_progress_point();
						log.info("Change to follow wall left! " + counter + ", IDS=" + irDistance);

					} else if (m_SlidingMode.slidingRight()) {
						m_FollowWall.dir = 1; // right
						m_currentController = m_FollowWall;
						m_state = S_FW; // followWall;
						m_FollowWall.reset();
						set_progress_point();

						log.info("Change to follow wall right! " + counter + ", IDS=" + irDistance);

					} else {
						m_state = S_GTG; // gotoGoal;
						m_currentController = m_GoToGoal;
						m_GoToGoal.reset();
						log.info("Change to go to goal state (from AVO) " + counter + ", IDS=" + irDistance);

					}
				}
			}

		}

		if (m_currentController != null) {
			return m_currentController.execute(robot, m_input, dt);
		}

		return null;
	}

	void StopMotor() {

	}

	void MoveMotor(int pwm_l, int pwm_r) {

	}

	void set_progress_point() {
		double d = Math.sqrt(Math.pow((robot.x - m_Goal.x), 2) + Math.pow((robot.y - m_Goal.y), 2));
		d_prog = d;
		log.info("mark prog point:" + robot.x + ", " + robot.y + "; d:" + d_prog);

		// if( d < d_prog )
		// {
		// d_prog = d;
		// log.info( "Do set, d:" + d_prog );
		// }
	}

	private void check_states() {
		double d = Math.sqrt(Math.pow((robot.x - m_Goal.x), 2) + Math.pow((robot.y - m_Goal.y), 2));
		m_distanceToGoal = d;

		if ((d < d_prog - 0.1)) {

			progress_made = true;
			// log.info( "Prog maded: " + d + ":" + d_prog);
		} else
			progress_made = false;

		at_goal = false;
		if (d < d_stop) {
			if (Math.abs(robot.theta - this.m_input.theta) < 0.05) // 0.05
			{
				at_goal = true;
				log.info("Dis at goal and: " + robot.theta + ":" + m_input.theta);
			}
		}
		if (mMode == 1)
			at_goal = false;

		IRSensor[] irSensors = robot.getIRSensors();

		// boolean ofObstacle = true;
		// if( at_obstacle )
		// {
		// irDistance = 100;
		// double offDis = 3*IRSensor.maxDistance/5;
		// for( int i=1; i<4; i++)
		// {
		// if( irSensors[i].distance < offDis ) //
		// ofObstacle = false;
		// if( irDistance > irSensors[i].distance )
		// irDistance = irSensors[i].distance;
		// }
		//
		// at_obstacle = !ofObstacle;
		// }
		// else
		{
			at_obstacle = false;
			// return;
			irDistance = 100;
			for (int i = 1; i < 4; i++) {
				if (irSensors[i].distance < d_at_obs)
					at_obstacle = true;
				if (irDistance > irSensors[i].distance)
					irDistance = irSensors[i].distance;
			}
			// if( irSensors[2].distance < d_at_obs )
			// at_obstacle = true;
		}
		unsafe = false;
		if (irSensors[1].distance < d_unsafe || irSensors[2].distance < d_unsafe || irSensors[3].distance < d_unsafe)
			unsafe = true;

		if (irSensors[1].distance < d_unsafe && irSensors[2].distance < d_unsafe && irSensors[3].distance < d_unsafe)
			danger = true;
		else
			danger = false;

		if (ignoreObstacle)
			at_obstacle = false;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
		this.m_input.v = velocity;
	}

	public double getVelocity() {
		return velocity;
	}

	public boolean atGoal() {
		return at_goal;
	}

	public void updateSettings(Settings settings) {
		d_at_obs = settings.atObstacle;
		d_unsafe = settings.unsafe;
		d_fw = settings.dfw;
		m_input.v = settings.velocity;
		velocity = settings.velocity;

		robot.updateSettings(settings);

		// m_gtgv.updateSettings(settings);
		// robot.max_rpm = settings.max_rpm;
		// robot.min_rpm = settings.min_rpm;

		m_GoToGoal.updateSettings(settings);
		m_AvoidObstacle.updateSettings(settings);

		m_FollowWall.updateSettings(settings);
		m_FollowWall.d_fw = settings.dfw;

		m_traceRoute.updateSettings(settings);

		// m_CAVO.updateSettings(settings );

		// m_SlidingMode;

	}

	private ControllerInfo mCtrlInfo = new ControllerInfo();

	public ControllerInfo getControllerInfo() {
		mCtrlInfo.reset();

		this.m_SlidingMode.getControllorInfo(mCtrlInfo);
		if (m_currentController != null)
			this.m_currentController.getControllorInfo(mCtrlInfo);
		mCtrlInfo.uGotoGoal = null;
		return mCtrlInfo;
	}

	public void reset() {
		m_state = S_GTG;
		m_currentController = m_GoToGoal;
		m_GoToGoal.reset();
		m_FollowWall.reset();
		m_AvoidObstacle.reset();
		m_traceRoute.reset();
		// m_CAVO.reset();
		// m_gtgv.reset();

		d_prog = 100;
		counter = 0;
		at_goal = false;
	}

}

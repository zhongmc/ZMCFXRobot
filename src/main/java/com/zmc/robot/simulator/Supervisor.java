package com.zmc.robot.simulator;

import javax.lang.model.util.ElementScanner6;

import com.sun.javafx.geom.Point2D;

import org.apache.log4j.Logger;

public class Supervisor {

	static int S_STOP = 0;
	static int S_GTG = 1;
	static int S_AVO = 2;
	static int S_FLW = 3;

	private boolean ignoreObstacle = false;
	private double velocity = 0.2;

	private final static String TAG = "Supervisor";

	Logger log = Logger.getLogger(TAG);

	private long counter = 0;
	private int mMode = 0; // goto goal 0; avoid obstacle 1;

	GotoGoalWithV m_GoToGoal = new GotoGoalWithV(); // GotoGoal();
	// AvoidObstacle m_AvoidObstacle = new AvoidObstacle();
	SimpleAvoidObstacle m_AvoidObstacle = new SimpleAvoidObstacle();
	FollowWall m_FollowWall = new FollowWall();
	SlidingMode m_SlidingMode = new SlidingMode();

	DifferencialController m_DiffCtrl = new DifferencialController();
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
	// boolean danger;
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
		m_input.targetAngle = theta;

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

	public void setGoal(double x, double y, double theta, double v) {
		m_Goal.x = x;
		m_Goal.y = y;

		m_input.x_g = x;
		m_input.y_g = y;
		m_input.targetAngle = theta;
		m_input.v = v;

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
		// counter++;
		// if (mSimulateMode)
		// robot.updateState((long) m_left_ticks, (long) m_right_ticks, dt);
		// else

		if (!mSimulateMode)
			robot.updateState(left_ticks, right_ticks, dt);

		check_states();

		Output output = null;
		if (mMode == 0) {
			output = executeFollowWallGoToGoal(dt);
			
			//output = executeAvoidAndFollowWallGoToGoal(dt);
		} else if (mMode == 2) // trace route
		{
			output = executeTraceRoute(dt);
			// if( this.m_currentController == this.m_CAVO )
			// at_goal = true;
		}

		if (output == null)
			return;

		if (m_currentController != null)
			m_currentController.getControllorInfo(mCtrlInfo);

			Input in = new Input();
			in.v = output.v;
			in.w = output.w;
		  
			Output out = m_DiffCtrl.execute(robot, in, dt);
			
			int pwm_l = (int)robot.vel_l_to_pwm(out.vel_l );
			int pwm_r = (int)robot.vel_r_to_pwm(out.vel_r );
	
			robot.moveMotor(pwm_l, pwm_r, dt);
	
		// double v = output.v;
		// double w = output.w; // Math.max( Math.min(output.w, robot.max_w), -robot.max_w);
		// PWMOut pwm = robot.getPWMOut(v, w);
		// robot.moveMotor(pwm.pwm_l, pwm.pwm_r, dt);

	}



	//直接follow Wall
	public Output executeFollowWallGoToGoal(double dt)
	{
		if (at_goal) {
			if (m_state != S_STOP)
				log.info("At Goal! " + counter);

			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		} else if (unsafe) {
			if (m_state != S_STOP)
				log.info("Danger! " + counter + "; ird=" + irDistance);
			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if(m_currentController != m_GoToGoal && m_currentController != m_FollowWall)
			m_currentController = m_GoToGoal;
		
		
		if (m_state == S_STOP && !unsafe) // recover from stop
		{
			m_state = S_GTG; // gotoGoal;
			m_currentController = m_GoToGoal;
			m_GoToGoal.reset();
		}

		if( m_currentController == m_GoToGoal )
		{
			  if( at_obstacle )
			  {

					this.m_SlidingMode.execute(robot, m_input, dt);
					if( m_SlidingMode.slidingLeft() )
						m_FollowWall.dir = 0;
					else if( m_SlidingMode.slidingRight() )
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
		
			
		}
		else //follow wall
		{
			this.m_SlidingMode.execute(robot, m_input, dt);
			
			if (progress_made) {
				
				if (m_FollowWall.dir == 0
						&& m_SlidingMode.quitSlidingLeft())// !m_SlidingMode.slidingLeft())
				{
					m_state = S_GTG; // gotoGoal;
					m_currentController = m_GoToGoal;
					m_GoToGoal.reset();
					log.info("Change to go to goal state(FW L PM) "
							+ counter + ", IDS=" + irDistance);

				} else if (m_FollowWall.dir == 1
						&& m_SlidingMode.quitSlidingRight())// !m_SlidingMode.slidingRight())
				{
					m_state = S_GTG; // gotoGoal;
					m_currentController = m_GoToGoal;
					m_GoToGoal.reset();
					log.info( "Change to go to goal state (FW R PM) "
							+ counter + ", IDS=" + irDistance);
				}
//				if( shouldGotoGoal())
//				{
//					m_state = S_GTG; // gotoGoal;
//					m_currentController = m_GoToGoal;
//					m_GoToGoal.reset();
//					Log.i(TAG, "Change to go to goal 2");
//					
//				}
				else
				{
					
					  Point2D p = getGoalCrossPoint();
					  
					  if( p != null )
					  {
							m_state = S_GTG; // gotoGoal;
							log.info("Change to goto goal while meet cross point..." );
							m_GoToGoal.reset();
							m_currentController = m_GoToGoal;
						  
					  }
					
					
				}
			}

		}
		
		return m_currentController.execute(robot, m_input, dt); 
		
		
	}


	public Output executeAvoidAndFollowWallGoToGoal(double dt) {

		mCtrlInfo.reset();
		if (at_goal) {
			if (m_state != S_STOP)
				log.info("At Goal! " + counter);
			log.info("At Goal! " + counter);

			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if (unsafe) {
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

		if (m_state == S_GTG) // GTG state
		{
			if (at_obstacle) {

				int dir = changeToFollowWall();
				if (dir != -1) {
					m_state = S_FLW;
					m_FollowWall.dir = dir;
					log.info("Change to fallow wall from gtg ..." + m_FollowWall.dir);
					m_FollowWall.reset();
					m_currentController = m_FollowWall;
					set_progress_point();
					xf = robot.x;
					yf = robot.y;
					theta = robot.theta;

				} else {
					m_state = S_AVO;
					m_currentController = m_AvoidObstacle;
					m_AvoidObstacle.reset();
					log.info("Change to AVO from gtg...");
				}
			}
		} else if (m_state == S_AVO) {

			// else {
			// m_state = S_GTG; // gotoGoal;
			// m_currentController = m_GoToGoal;
			// log.info("Change to GTG from avo ...");
			// m_GoToGoal.reset();

			// }

			if (m_AvoidObstacle.beQuit) {
				m_state = S_GTG; // gotoGoal;
				m_currentController = m_GoToGoal;
				log.info("Change to GTG from avo ...");
				m_GoToGoal.reset();

			} else if (at_obstacle) {

				int dir = changeToFollowWall();
				if (dir != -1) {
					m_state = S_FLW;
					m_FollowWall.dir = dir;
					log.info("Change to fallow wall from avo ..." + m_FollowWall.dir);
					m_FollowWall.reset();
					m_currentController = m_FollowWall;
					set_progress_point();
					xf = robot.x;
					yf = robot.y;
					theta = robot.theta;
				}
			}

		} else { // follow wall

			this.m_SlidingMode.execute(robot, m_input, dt);
			m_SlidingMode.getControllorInfo(mCtrlInfo);

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
				} else {

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

	// 0: left; 1: right; -1 no;
	private int changeToFollowWall() {
		this.m_SlidingMode.execute(robot, m_input, 0);
		m_SlidingMode.getControllorInfo(mCtrlInfo);

		if (m_SlidingMode.slidingLeft()) {
			return 0;
		} else if (m_SlidingMode.slidingRight()) {
			return 1;
		}

		return -1;

	}

	public Output executeGoToGoal1(double dt) {

		mCtrlInfo.reset();
		if (at_goal) {
			if (m_state != S_STOP)
				log.info("At Goal! " + counter);
			log.info("At Goal! " + counter);

			m_state = S_STOP; // s_stop;
			StopMotor();
			return null;
		}

		if (unsafe) {
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

		if (m_state == S_GTG) // GTG state
		{
			if (at_obstacle) {

				int dir = changeToFollowWall();
				if (dir != -1) {
					m_state = S_FLW;
					m_FollowWall.dir = dir;
					log.info("Change to fallow wall from gtg ..." + m_FollowWall.dir);
					m_FollowWall.reset();
					m_currentController = m_FollowWall;
					set_progress_point();
					xf = robot.x;
					yf = robot.y;
					theta = robot.theta;

				} else {
					m_state = S_AVO;
					m_currentController = m_AvoidObstacle;
					m_AvoidObstacle.reset();
					log.info("Change to AVO from gtg...");
				}
			}
		} else if (m_state == S_AVO) {

			if (at_obstacle) {

				int dir = changeToFollowWall();
				if (dir != -1) {
					m_state = S_FLW;
					m_FollowWall.dir = dir;
					log.info("Change to fallow wall from avo ..." + m_FollowWall.dir);
					m_FollowWall.reset();
					m_currentController = m_FollowWall;
					set_progress_point();
					xf = robot.x;
					yf = robot.y;
					theta = robot.theta;
				}
			}
			// else {
			// m_state = S_GTG; // gotoGoal;
			// m_currentController = m_GoToGoal;
			// log.info("Change to GTG from avo ...");
			// m_GoToGoal.reset();

			// }

			if (m_AvoidObstacle.beQuit) {
				m_state = S_GTG; // gotoGoal;
				m_currentController = m_GoToGoal;
				log.info("Change to GTG from avo ...");
				m_GoToGoal.reset();

			}

		} else { // follow wall

			this.m_SlidingMode.execute(robot, m_input, dt);
			m_SlidingMode.getControllorInfo(mCtrlInfo);

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
				} else {

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
		if (unsafe) {
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
		if (unsafe) {
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

		return getObstacleDir();
	}

	// 0 none 1 left 2 right
	private int getObstacleDir() {

		IRSensor[] irSensors = robot.getIRSensors();

		int l = 0;
		for (int i = 0; i < 3; i++) {
			if (irSensors[i].distance < IRSensor.maxDistance - 0.05) {
				l++;
			}
		}

		int r = 0;
		for (int i = 2; i < 5; i++) {
			if (irSensors[i].distance < IRSensor.maxDistance - 0.05) {
				r++;
			}
		}

		if (l == 0 && r == 0)
			return 0;

		if (l >= r)
			return 1;
		else
			return 2;
	}

	public Vector getRecoverPoint() {
		m_traceRoute.reset();
		m_traceRoute.recoverGoal(robot);
		return m_traceRoute.getRecoverPoint();
	}

	void StopMotor() {

	}

	void MoveMotor(int pwm_l, int pwm_r) {

	}

	void set_progress_point() {
		double d = Math.sqrt(Math.pow((robot.x - m_Goal.x), 2) + Math.pow((robot.y - m_Goal.y), 2));
		d_prog = d;
		log.info(String.format("mark prog point:%.3f,%.3f;d:%.3f", robot.x, robot.y, d_prog));

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
		if (d < d_stop + 0.005) {
			if (Math.abs(robot.theta - this.m_input.targetAngle) < 0.05) // 0.05
			{
				at_goal = true;
				log.info(String.format("Dis at goal and:%.3f:%.3f", robot.theta, m_input.targetAngle));
			}
		}
		if (mMode == 1)
			at_goal = false;

		IRSensor[] irSensors = robot.getIRSensors();

		boolean ofObstacle = true;
		if (at_obstacle) {
			irDistance = 100;
			double offDis = 4 * IRSensor.maxDistance / 5;
			for (int i = 1; i < 4; i++) {
				if (irSensors[i].distance < offDis) //
					ofObstacle = false;
				if (irDistance > irSensors[i].distance)
					irDistance = irSensors[i].distance;
			}

			at_obstacle = !ofObstacle;
		} else {
			at_obstacle = false;
			irDistance = 100;
			for (int i = 1; i < 4; i++) {
				if (irSensors[i].distance < d_at_obs)
					at_obstacle = true;
				if (irDistance > irSensors[i].distance)
					irDistance = irSensors[i].distance;
			}
		}
		unsafe = false;
		if (irSensors[1].distance < d_unsafe || irSensors[2].distance < d_unsafe || irSensors[3].distance < d_unsafe)
			unsafe = true;

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
		m_SlidingMode.d_fw = settings.dfw;

		m_traceRoute.updateSettings(settings);

		m_DiffCtrl.updateSettings(settings);
		// m_CAVO.updateSettings(settings );

		// m_SlidingMode;

	}

	private ControllerInfo mCtrlInfo = new ControllerInfo();

	public ControllerInfo getControllerInfo() {
		return mCtrlInfo;
	}

	public void reset() {
		m_state = S_GTG;
		m_currentController = m_GoToGoal;
		m_GoToGoal.reset();
		m_FollowWall.reset();
		m_AvoidObstacle.reset();
		m_traceRoute.reset();
		m_DiffCtrl.reset();

		// m_CAVO.reset();
		// m_gtgv.reset();

		d_prog = 100;
		counter = 0;
		at_goal = false;
	}

}

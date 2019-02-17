package com.zmc.robot.simulator;

import com.sun.javafx.geom.Point2D;

import org.apache.log4j.Logger;

public class TraceRoute extends Controller {

	private static final String TAG = "TRC";
	private Logger log = Logger.getLogger(TAG);

	float[][] mRoutes = null;
	int mRouteSize = 0;
	int mCurRouteIdx = 0;

	Output output = new Output();
	private Vector uGtg = new Vector();

	// �Ӷ���ϰ���ظ��ĵ�
	private Vector recoverPoint = new Vector();

	private boolean recoverMode = false;

	// �뿪·��ʱ���ϰ���
	private Vector obstaclePoint;

	public void setObstaclePoint(Vector p) {
		obstaclePoint = p;
	}

	//
	public Vector getRecoverPoint() {
		return recoverPoint;
	}

	public void setRoute(float[][] route, int size) {
		this.mRoutes = route;
		this.mRouteSize = size;

		this.mCurRouteIdx = 0;

	}

	public void setCurrentRouteIdx(int idx) {
		mCurRouteIdx = idx;
	}

	Vector getGoalPoint() {
		Vector p = new Vector();
		p.x = mRoutes[mCurRouteIdx + 1][0];

		p.y = mRoutes[mCurRouteIdx + 1][1];
		return p;

	}

	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		getNextGoal(robot);
		if (mCurRouteIdx >= mRouteSize - 1) // circle ??
			mCurRouteIdx = 0;
		// return null;

		output.v = input.v;

		double u_x, u_y, e, e_I, e_D, w, theta_g;

		u_x = mRoutes[mCurRouteIdx + 1][0] - robot.x; // mRoutes[mCurRouteIdx][0];
		u_y = mRoutes[mCurRouteIdx + 1][1] - robot.y; // mRoutes[mCurRouteIdx][1];
		// double d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
		theta_g = Math.atan2(u_y, u_x);

		uGtg.x = u_x;
		uGtg.y = u_y;

		e = theta_g - robot.theta;
		e = Math.atan2(Math.sin(e), Math.cos(e));
		e_I = lastErrorIntegration + e * dt;
		e_D = (e - lastError) / dt;
		w = Kp * e + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		lastError = e;

		output.w = w;

		return output;

	}

	/**
	 * �ж��Ƿ�follow wall ����ʱ���ص��˹켣
	 * 
	 * @param robot
	 * @return
	 */
	public int recoverGoalFromWall(AbstractRobot robot, int wallDir) {
		float x, y, theta;
		x = (float) robot.x;
		y = (float) robot.y;
		theta = (float) robot.theta;
		CrossInfo crossInfo = this.getCrossInfo(x, y, 0.3f, theta); // the idx

		if (crossInfo != null) {

			return crossInfo.idx;
		}

		theta = theta + (float) Math.PI / 6;
		crossInfo = this.getCrossInfo(x, y, 0.2f, theta);
		if (crossInfo != null) {

			return crossInfo.idx;
		}

		theta = (float) robot.theta - (float) Math.PI / 6;
		crossInfo = this.getCrossInfo(x, y, 0.2f, theta);
		if (crossInfo != null) {

			return crossInfo.idx;
		}

		if (wallDir == 1) // left
			theta = (float) (robot.theta - Math.PI / 2);
		else
			theta = (float) (robot.theta + Math.PI / 2);

		crossInfo = this.getCrossInfo(x, y, 0.2f, theta);
		if (crossInfo != null) {

			return crossInfo.idx;
		}

		return -1;

	}

	/**
	 * �ж��Ƿ���follow wall ����ʱ���ص��˹켣
	 * 
	 * @param robot
	 * @return
	 */
	public int recoverGoalFromWall1(AbstractRobot robot) {

		float x0, y0, r = 0.8f, d1, d2, d;

		x0 = (float) robot.x;
		y0 = (float) robot.y;

		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		float x, y;
		int idx = mCurRouteIdx;
		for (int i = 0; i < mRouteSize; i++) {
			p1.x = mRoutes[idx][0];
			p1.y = mRoutes[idx][1];
			if (idx == mRouteSize - 1) {
				p2.x = mRoutes[0][0];
				p2.y = mRoutes[0][1];
			} else {
				p2.x = mRoutes[idx + 1][0];
				p2.y = mRoutes[idx + 1][1];
			}

			x = p1.x - x0;
			y = p1.y - y0;

			d1 = (float) Math.sqrt(x * x + y * y);

			x = p2.x - x0;
			y = p2.y - y0;
			d2 = (float) Math.sqrt(x * x + y * y);

			x = p1.x - p2.x;
			y = p1.y - p2.y;
			d = (float) Math.sqrt(x * x + y * y);

			if (Math.abs(d - d1 - d2) < 0.01) {
				// log.info( "found it! " + idx );
				return idx;
			}

			idx++;

		}

		return -1;

	}

	public int recoverFromAvoGoal(AbstractRobot robot) {

		float x0, y0, r = 0.8f, d1, d2;

		x0 = (float) robot.x;
		y0 = (float) robot.y;

		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		recoverPoint.x = 0;
		recoverPoint.y = 0;

		float x, y;
		int idx = mCurRouteIdx;
		for (int i = 0; i < mRouteSize; i++) {

			p1.x = mRoutes[idx][0];
			p1.y = mRoutes[idx][1];
			if (idx == mRouteSize - 1) {
				p2.x = mRoutes[0][0];
				p2.y = mRoutes[0][1];
			} else {
				p2.x = mRoutes[idx + 1][0];
				p2.y = mRoutes[idx + 1][1];
			}

			x = p1.x - x0;
			y = p1.y - y0;

			d1 = (float) Math.sqrt(x * x + y * y);

			x = p2.x - x0;
			y = p2.y - y0;
			d2 = (float) Math.sqrt(x * x + y * y);

			if ((d1 >= r && d2 <= r) || (d1 <= r && d2 >= r)) {
				// log.info( "found it! " + idx );
				recoverPoint.x = p1.x;
				recoverPoint.y = p1.y;
				return idx;
				// break;
			}

			idx++;

		}

		return -1;
		// mCurRouteIdx = idx;

	}

	/**
	 * �жϲ���λ����һ��Ŀ���
	 * 
	 * @param robot
	 */
	private void getNextGoal(AbstractRobot robot) {

		int idx = mCurRouteIdx;
		double x0, y0, xe, ye, d0, de, d;

		x0 = mRoutes[idx][0];
		y0 = mRoutes[idx][1];

		xe = mRoutes[idx + 1][0];
		ye = mRoutes[idx + 1][1];

		// �ж��Ƿ��ѵ����յ�
		de = Math.sqrt(Math.pow((robot.x - xe), 2) + Math.pow((robot.y - ye), 2));
		if (de < 0.05) {
			if (recoverMode)
				recoverMode = false;

			// log.info( "Change target 0 from " + mCurRouteIdx + " to " + (idx+1));
			mCurRouteIdx = idx + 1;
			return;
		}

		if (recoverMode)
			return;

		// �Ƿ���Խ����ǰ·��
		d0 = Math.sqrt(Math.pow((robot.x - x0), 2) + Math.pow((robot.y - y0), 2));
		d = Math.sqrt(Math.pow((xe - x0), 2) + Math.pow((ye - y0), 2));

		if (Math.abs((d - d0 - de) / d) < 0.05)
			return;

		for (int i = 0; i < mRouteSize; i++) {
			x0 = mRoutes[idx][0];
			y0 = mRoutes[idx][1];

			if (idx == mRouteSize - 1) {
				if (x0 > mRoutes[0][0]) {
					xe = x0;
					x0 = mRoutes[0][0];
				} else
					xe = mRoutes[0][0];

				if (y0 > mRoutes[0][1]) {
					ye = y0;
					y0 = mRoutes[0][1];
				} else
					ye = mRoutes[0][1];
			} else {
				if (x0 > mRoutes[idx + 1][0]) {
					xe = x0;
					x0 = mRoutes[idx + 1][0];
				} else
					xe = mRoutes[idx + 1][0];

				if (y0 > mRoutes[idx + 1][1]) {
					ye = y0;
					y0 = mRoutes[idx + 1][1];
				} else
					ye = mRoutes[idx + 1][1];
			}

			if ((robot.x >= x0 && robot.x <= xe) && (robot.y >= y0 && robot.y <= ye)) {
				if (mCurRouteIdx != idx)
					// log.info( "Change target2 from " + mCurRouteIdx + " to " + idx);
					mCurRouteIdx = idx;
				return;

				// double d = Math.sqrt(Math.pow((robot.x - xe ),2) + Math.pow((robot.y - ye),
				// 2));
				// if( d < 0.1 )
				// {
				// log.info( "Change target from " + si + " to " + (mCurRouteIdx+1));
				//
				// lastErrorIntegration = 0;
				// lastError = 0;
				//
				// mCurRouteIdx++;
				// return;
				// }
			}

			d = Math.sqrt(Math.pow((robot.x - x0), 2) + Math.pow((robot.y - y0), 2));
			if (d < 0.05) {
				if (mCurRouteIdx != idx)
					// log.info( "Change target 2 from " + mCurRouteIdx + " to " + idx);
					mCurRouteIdx = idx;
				return;

			}

			d = Math.sqrt(Math.pow((robot.x - xe), 2) + Math.pow((robot.y - ye), 2));
			if (d < 0.05) {
				// log.info( "Change target 3 from " + mCurRouteIdx + " to " + idx+1);
				mCurRouteIdx = idx + 1;
				return;

			}

			idx++;
			if (idx == mRouteSize)
				idx = 0;
		}

		// out of road, recover???

		recoverGoal(robot);

	}

	/**
	 * �Ի�����Ϊ���Ļ�Բ��Ѱ�Һ��ʵĽ����
	 * 
	 * @param robot
	 */
	public void recoverGoal(AbstractRobot robot) {

		log.info("recover route, current idx: " + mCurRouteIdx);

		recoverMode = true;

		float x0, y0, r = 0.8f, d1, d2;

		x0 = (float) robot.x;
		y0 = (float) robot.y;

		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		recoverPoint.x = 0;
		recoverPoint.y = 0;

		float x, y;
		int idx = mCurRouteIdx;
		for (int i = 0; i < mRouteSize; i++) {

			p1.x = mRoutes[idx][0];
			p1.y = mRoutes[idx][1];
			if (idx == mRouteSize - 1) {
				p2.x = mRoutes[0][0];
				p2.y = mRoutes[0][1];
			} else {
				p2.x = mRoutes[idx + 1][0];
				p2.y = mRoutes[idx + 1][1];
			}

			x = p1.x - x0;
			y = p1.y - y0;

			d1 = (float) Math.sqrt(x * x + y * y);

			x = p2.x - x0;
			y = p2.y - y0;
			d2 = (float) Math.sqrt(x * x + y * y);

			if ((d1 >= r && d2 <= r) || (d1 <= r && d2 >= r)) {
				log.info("found it! " + idx);
				recoverPoint.x = p1.x;
				recoverPoint.y = p1.y;

				break;
			}

			idx++;

		}

		mCurRouteIdx = idx;

	}

	/**
	 * ����ϰ�������¶�λ,ʹ�û����˵ĵ��ϰ��������������ԭ��ǰ�������������Ѱ��
	 * 
	 * @param robot
	 */

	public void recoverGoal2(AbstractRobot robot) {
		log.info("recover route, current idx: " + mCurRouteIdx);

		float x0, y0;

		x0 = (float) robot.x;
		y0 = (float) robot.y;

		Vector po = new Vector();

		double theta;

		po.x = obstaclePoint.x - robot.x;
		po.y = obstaclePoint.y - robot.y;

		theta = Math.atan2(po.y, po.x);

		CrossInfo crossInfo = this.getCrossInfo(x0, y0, 2.0f, (float) theta); // the idx

		if (crossInfo == null) {
			log.info("cant found the cross point,according tho the obstacle point!");
			mCurRouteIdx = mCurRouteIdx + 6; ////

			if (mCurRouteIdx > mRouteSize - 1)
				mCurRouteIdx = mCurRouteIdx - mRouteSize;
			recoverPoint.x = mRoutes[mCurRouteIdx][0];
			recoverPoint.y = mRoutes[mCurRouteIdx][1];
			return;
		}

		int idx = crossInfo.idx;
		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		p1.x = mRoutes[idx][0];
		p1.y = mRoutes[idx][1];
		if (idx == mRouteSize - 1) {
			p2.x = mRoutes[0][0];
			p2.y = mRoutes[0][1];
		} else {
			p2.x = mRoutes[idx + 1][0];
			p2.y = mRoutes[idx + 1][1];
		}

		Vector pt = new Vector();

		pt.x = p2.x - p1.x;
		pt.y = p2.y - p1.y;

		double norm = Math.sqrt(pt.x * pt.x + pt.y * pt.y);

		pt.x = 0.4 * pt.x / norm;
		pt.y = 0.4 * pt.y / norm;

		pt.x = pt.x + po.x;
		pt.y = pt.y + po.y;

		theta = Math.atan2(pt.y, pt.x);
		crossInfo = this.getCrossInfo(x0, y0, 2.0f, (float) theta);

		if (crossInfo == null) {
			log.info("cant found the cross point,according tho the obstacle point and robot position!");
			mCurRouteIdx = idx + 5; ////

			if (mCurRouteIdx > mRouteSize - 1)
				mCurRouteIdx = mCurRouteIdx - mRouteSize;

			recoverPoint.x = mRoutes[mCurRouteIdx][0];
			recoverPoint.y = mRoutes[mCurRouteIdx][1];

		} else {
			mCurRouteIdx = idx;
			recoverPoint.x = crossInfo.x;
			recoverPoint.y = crossInfo.y;

		}

	}

	/**
	 * ����ϰ�������¶�λ,ʹ�û����˵Ĳ�ͬ�����ٷ���
	 * 
	 * @param robot
	 */
	public void recoverGoal1(AbstractRobot robot) {
		double dd = 0;
		log.info("Change rout: " + mCurRouteIdx);

		// ��ǰ������·���Ľ���㣿��

		Point2D p0 = new Point2D((float) robot.x, (float) robot.y);

		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		recoverPoint.x = 0;
		recoverPoint.y = 0;

		int idx = mCurRouteIdx;
		Point2D p = null;

		double theta0, theta1, theta3, theta4;

		double theta = Math.PI / 2 + robot.theta;
		theta0 = Math.atan2(Math.sin(theta), Math.cos(theta));

		theta = Math.PI / 4 + robot.theta;
		theta1 = Math.atan2(Math.sin(theta), Math.cos(theta));

		theta = -Math.PI / 4 + robot.theta;
		theta3 = Math.atan2(Math.sin(theta), Math.cos(theta));

		theta = -Math.PI / 2 + robot.theta;
		theta4 = Math.atan2(Math.sin(theta), Math.cos(theta));

		for (int i = 0; i < mRouteSize; i++) {

			p1.x = mRoutes[idx][0];
			p1.y = mRoutes[idx][1];
			if (idx == mRouteSize - 1) {
				p2.x = mRoutes[0][0];
				p2.y = mRoutes[0][1];
			} else {
				p2.x = mRoutes[idx + 1][0];
				p2.y = mRoutes[idx + 1][1];
			}

			p = this.getCrossPoint(p0, theta0, p1, p2);

			if (p != null)
				break;

			p = this.getCrossPoint(p0, theta1, p1, p2);

			if (p != null)
				break;

			p = this.getCrossPoint(p0, robot.theta, p1, p2);
			if (p != null)
				break;

			p = this.getCrossPoint(p0, theta3, p1, p2);
			if (p != null)
				break;

			p = this.getCrossPoint(p0, theta4, p1, p2);
			if (p != null)
				break;

			idx++;
			if (idx > mRouteSize - 1)
				idx = 0;

		}

		if (p != null) {
			recoverPoint.x = p.x;
			recoverPoint.y = p.y;

			mCurRouteIdx = idx;
			log.info("To: " + mCurRouteIdx);

		} else {
			mCurRouteIdx = mCurRouteIdx + 5; // ������

			if (mCurRouteIdx >= mRouteSize - 1) // circle ??
				mCurRouteIdx = 0;

			log.info("lost, rand set To: " + mCurRouteIdx);

		}
		//
		//
		// while(mCurRouteIdx < this.mRouteSize-1)
		// {
		// double d = Math.sqrt(Math.pow((mRoutes[mCurRouteIdx+1][0] -
		// mRoutes[mCurRouteIdx][0]),2) + Math.pow((mRoutes[mCurRouteIdx+1][1] -
		// mRoutes[mCurRouteIdx][1]), 2));
		// dd = dd + d;
		// mCurRouteIdx++;
		//
		// if(dd > IRSensor.maxDistance/2 )
		// break;
		// }

	}

	private class CrossInfo {

		float x, y, theta;
		int idx;

		public CrossInfo() {

		}

		public CrossInfo(float x, float y, float theta, int idx) {
			this.x = x;
			this.y = y;
			this.theta = theta;
			this.idx = idx;
		}

	}

	/**
	 * ��x,y���ĵ�������Ƕ�Ϊtheta ������������Ϊr, ��·���Ľ������
	 * 
	 * @param robot
	 * @param theta
	 * @return
	 */
	private CrossInfo getCrossInfo(float x, float y, float r, float theta) {

		Point2D p0 = new Point2D(x, y);

		Point2D p1 = new Point2D();
		Point2D p2 = new Point2D();

		Point2D p3 = new Point2D();
		p3.x = p0.x + r * (float) Math.cos(theta); // 1�׷�Χ�ڣ�
		p3.y = p0.y + r * (float) Math.sin(theta);

		int idx = mCurRouteIdx;
		Point2D p = null;

		double tahTheta = Math.tan(theta);
		double a1 = tahTheta;
		double b1 = p0.y - a1 * p0.x;

		for (int i = 0; i < mRouteSize; i++) {

			p1.x = mRoutes[idx][0];
			p1.y = mRoutes[idx][1];
			if (idx == mRouteSize - 1) {
				p2.x = mRoutes[0][0];
				p2.y = mRoutes[0][1];
			} else {
				p2.x = mRoutes[idx + 1][0];
				p2.y = mRoutes[idx + 1][1];
			}

			idx++;

			if (!doesVectorCross(p0, p3, p1, p2))
				continue;

			if (p1.y == p2.y) {
				p3.x = (float) ((p1.y - b1) / a1);
				p3.y = p1.y;

				p = p3;
				break;
			}

			if (p1.x == p2.x) {
				p3.x = p1.x;
				p3.y = (float) (a1 * p1.x + b1);

				p = p3;
				break;
			}

			double a = (p2.y - p1.y) / (p2.x - p1.x);
			double b = p2.y - a * p2.x;

			double x1 = (b1 - b) / (a - a1);
			double y1 = a * x1 + b;

			p3.x = (float) x1;
			p3.y = (float) y1;

			p = p3;
			break;
		}
		if (p == null)
			return null;
		return new CrossInfo(p.x, p.y, theta, idx);
	}

	private boolean doesVectorCross(Point2D P0, Point2D P1, Point2D Q0, Point2D Q1) {

		// log.info( "getCross:" + P0 + ";" + P1 + ";" + Q0 + ";" + Q1 );

		// �����ཻ�ж�
		boolean val = (Math.max(P0.x, P1.x) >= Math.min(Q0.x, Q1.x)) && (Math.max(Q0.x, Q1.x) >= Math.min(P0.x, P1.x))
				&& (Math.max(P0.y, P1.y) >= Math.min(Q0.y, Q1.y)) && (Math.max(Q0.y, Q1.y) >= Math.min(P0.y, P1.y));

		if (!val)
			return false;

		// (p0-q0)*(q1-q0)
		double p1xq = CrossProduct(P0.x - Q0.x, P0.y - Q0.y, Q1.x - Q0.x, Q1.y - Q0.y);

		// (p1-q0)*(q1-q0)
		double p2xq = CrossProduct(P1.x - Q0.x, P1.y - Q0.y, Q1.x - Q0.x, Q1.y - Q0.y);
		// (q0-p0)*(p1-p0)
		double q1xp = CrossProduct(Q0.x - P0.x, Q0.y - P0.y, P1.x - P0.x, P1.y - P0.y);

		// (q1-p0)*(p1-p0)
		double q2xp = CrossProduct(Q1.x - P0.x, Q1.y - P0.y, P1.x - P0.x, P1.y - P0.y);

		return (p1xq * p2xq <= 0) && (q1xp * q2xp <= 0);
	}

	// ʸ�����
	private double CrossProduct(double x1, double y1, double x2, double y2) {
		return x1 * y2 - x2 * y1;
	}

	// CrossProduct(x1,y1,x2,y2) =

	private Point2D getCrossPoint(Point2D p0, double theta, Point2D p1, Point2D p2) {

		Point2D p3 = new Point2D();

		p3.x = p0.x + 1 * (float) Math.cos(theta); // 1�׷�Χ�ڣ�
		p3.y = p0.y + 1 * (float) Math.sin(theta);

		if (!doesVectorCross(p0, p3, p1, p2))
			return null;

		double tahTheta = Math.tan(theta);

		double a1 = tahTheta;
		double b1 = p0.y - a1 * p0.x;

		if (p1.y == p2.y) {
			p3.x = (float) ((p1.y - b1) / a1);
			p3.y = p1.y;
			return p3;

			// return new PointF((float)((p1.y-b1)/a1), p1.y);
		}

		if (p1.x == p2.x) {
			p3.x = p1.x;
			p3.y = (float) (a1 * p1.x + b1);
			return p3;
			// return new PointF(p1.x, (float)(a1*p1.x + b1 ));
		}

		double a = (p2.y - p1.y) / (p2.x - p1.x);
		double b = p2.y - a * p2.x;

		double x1 = (b1 - b) / (a - a1);
		double y1 = a * x1 + b;

		p3.x = (float) x1;
		p3.y = (float) y1;
		return p3;
		// return new PointF((float)x1, (float)y1);
	}

	// private void getRoutePosition(double cx, double cy)
	// {
	// if( mCurRouteIdx >= mRouteSize-1 )
	// return;
	//
	// log.info( "rp: " + cx + ", " + cy);
	//
	// double x=0, y=0, xe=0, ye=0, xr, yr;
	// while( mCurRouteIdx < mRouteSize-1 )
	// {
	// x = mRoutes[mCurRouteIdx][0];
	// y = mRoutes[mCurRouteIdx][1];
	//
	// xe = mRoutes[mCurRouteIdx+1][0];
	// ye = mRoutes[mCurRouteIdx+1][1];
	//
	// xr = (cx - x) * (cx - xe);
	// yr = (cy - y ) * (cy - ye);
	//
	// if( xr <=0 && yr <= 0 )
	// {
	// log.info( "got it idx: " + mCurRouteIdx + " : " + x + ", " + y + "; " + xe +
	// ", " + ye + "; " );
	// break;
	// }
	// log.info( "idx: " + mCurRouteIdx + " : " + x + ", " + y + "; " + xe + ", " +
	// ye + "; " );
	//
	// mCurRouteIdx++;
	//
	// }
	//
	//
	// }

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;
		mCurRouteIdx = 0;
		recoverMode = false;

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uGotoGoal = uGtg;
	}

}

package com.zmc.robot.simulator;

public class ControllerInfo {

	public Vector uGotoGoal;
	public Vector uAoidObstacle;
	public Vector uFollowWall;

	public Vector uFwP;

	// follow wall ʱ��ѡ��wall������ p0 -> p1
	public Vector p0, p1;

	// GotoGoal Ϊ�ӽ�Ŀ����趨�ĵڶ�Ŀ��㣬�Ա��ڵ�������
	public double ux, uy;

	public void reset() {
		uGotoGoal = null;
		uAoidObstacle = null;
		uFollowWall = null;
		uFwP = null;

		p0 = null;
		ux = 0;
		uy = 0;
	}
}

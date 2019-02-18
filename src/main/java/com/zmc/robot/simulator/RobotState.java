package com.zmc.robot.simulator;

public class RobotState {

	public int sType; // state type 1 normal state with all data; 2: balance state,
						// angle1,angle2,angle3, voltage;

	public double[] obstacles;
	public double x, y, theta;
	public double velocity;

	public double voltage;

	// private long rightTicks;
	// private long leftTicks;

	// static RobotState robotState = new RobotState();

	public RobotState() {
		obstacles = new double[5];
	}

	// x,y,theta,d0,d1,d2,d3,d4,voltage;

	public void decode(byte[] data) {

		sType = (int) data[0];

		if (sType == 1) {

			if (data.length < 19)
				return;

			int iVal;

			iVal = byteToInt(data, 1); // ((data[1] & 0xff)<<8) | (data[0] & 0xff);
			x = (float) ((float) iVal / 1000.0);

			iVal = byteToInt(data, 3); // ((data[3] & 0xff)<<8) | (data[2] & 0xff);
			y = (float) ((float) iVal / 1000.0);

			iVal = byteToInt(data, 5); // ((data[5] & 0xff)<<8) | (data[4] & 0xff);
			theta = (float) ((float) iVal / 1000.0);

			for (int i = 0; i < 5; i++) {
				iVal = byteToInt(data, 7 + i * 2);
				obstacles[i] = (float) iVal / 100.0;
			}

			iVal = ((data[18] & 0xff) << 8) | (data[17] & 0xff);
			voltage = (float) ((float) iVal / 100.0);
		} else {
			int iVal;

			iVal = byteToInt(data, 1); // ((data[1] & 0xff)<<8) | (data[0] & 0xff);
			x = (float) ((float) iVal / 1000.0);

			iVal = byteToInt(data, 3); // ((data[3] & 0xff)<<8) | (data[2] & 0xff);
			y = (float) ((float) iVal / 1000.0);

			iVal = byteToInt(data, 5); // ((data[5] & 0xff)<<8) | (data[4] & 0xff);
			theta = (float) ((float) iVal / 1000.0);

			for (int i = 0; i < 5; i++) {
				iVal = byteToInt(data, 7 + i * 2);
				obstacles[i] = (float) iVal / 100.0;
			}

			iVal = ((data[18] & 0xff) << 8) | (data[17] & 0xff);
			voltage = (float) ((float) iVal / 100.0);

		}

		return;

	}

	int byteToInt(byte[] data, int offset) {
		int value = ((data[offset + 1] & 0x7f) << 8) | (data[offset] & 0xff);

		if ((data[offset + 1] & 0x80) != 0) {
			value = -value;
		}
		return value;
	}

}

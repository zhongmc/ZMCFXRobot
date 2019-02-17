package com.zmc.robot.simulator;

public class Settings {

	// 1: pid for 3 wheel; KP,KI,KD
	// 2: pid for balance; KP,KI,KD
	// 3: pid for speed;
	// 4: settings for 3 wheel robot;
	// atObstacle��unsafe��dfw��velocity��max_rpm��min_rpm, wheelRadius,
	// wheelDistance
	// 5: settings for balance robot; atObstacle��unsafe��max_pwm��pwm_zero,
	// angleOff, wheelSyncKP
	public int settingsType;

	public double kp, ki, kd;

	public double atObstacle, unsafe;
	public double dfw;
	public double velocity;
	public int max_rpm, min_rpm;

	public double wheelDistance, wheelRadius;

	public int pwm_diff;

	public int max_pwm;

	public int pwm_zero;
	public double angleOff;

	public double wheelSyncKP;

	private byte[] encodedData = new byte[18];

	private static String TAG = "Settings";

	public void decodeWithSettingsType(byte[] data) {
		settingsType = data[0];
		if (settingsType == 1 || settingsType == 2 || settingsType == 3) {
			int idx = 1;
			kp = byteFloatValue(data, idx, 100);
			idx = idx + 2;
			ki = byteFloatValue(data, idx, 1000);
			idx = idx + 2;
			kd = byteFloatValue(data, idx, 1000);
		} else if (settingsType == 4) {
			int idx = 1;
			atObstacle = byteFloatValue(data, idx, 100);
			idx = idx + 2;
			unsafe = byteFloatValue(data, idx, 100);

			idx = idx + 2;
			dfw = byteFloatValue(data, idx, 100);

			idx = idx + 2;
			velocity = byteFloatValue(data, idx, 100);

			idx = idx + 2;
			max_rpm = byteToInt(data, idx);
			idx = idx + 2;
			min_rpm = byteToInt(data, idx);

			idx = idx + 2;
			wheelRadius = byteFloatValue(data, idx, 1000);
			idx = idx + 2;
			wheelDistance = byteFloatValue(data, idx, 1000);

			//
			// pwm_diff = data[idx]; //byteToInt(data, idx);
			// idx = idx + 1;
			// pwm_zero = data[idx];
			//
			// idx=idx+1;
			// angleOff = byteFloatValue(data, idx, 100);

		} else if (settingsType == 5) // atObstacle��unsafe��max_pwm��pwm_zero, angleOff, wheelSyncKP
		{

			int idx = 1;
			atObstacle = byteFloatValue(data, idx, 100);

			idx = idx + 2;
			unsafe = byteFloatValue(data, idx, 100);

			idx = idx + 2;
			max_pwm = byteToInt(data, idx);
			idx = idx + 2;

			pwm_zero = data[idx];
			idx = idx + 1;
			pwm_diff = data[idx];
			idx++;

			angleOff = byteFloatValue(data, idx, 100);

			idx = idx + 2;
			wheelSyncKP = byteFloatValue(data, idx, 100);

		}
	}

	public void decode(byte[] data) {
		int idx = 0;
		kp = byteFloatValue(data, idx, 100);
		idx = idx + 2;
		ki = byteFloatValue(data, idx, 1000);
		idx = idx + 2;
		kd = byteFloatValue(data, idx, 1000);

		idx = idx + 2;
		atObstacle = byteFloatValue(data, idx, 100);
		idx = idx + 2;
		unsafe = byteFloatValue(data, idx, 100);

		idx = idx + 2;
		dfw = byteFloatValue(data, idx, 100);

		idx = idx + 2;
		velocity = byteFloatValue(data, idx, 100);

		idx = idx + 2;
		max_rpm = byteToInt(data, idx);
		idx = idx + 2;
		min_rpm = byteToInt(data, idx);

	}

	byte[] encodeWithSettingsType() {
		if (settingsType == 1 || settingsType == 2 || settingsType == 3) {
			byte[] dataBuf = new byte[8];
			dataBuf[0] = (byte) settingsType;
			int idx = 1;
			doubleToByte(kp, dataBuf, idx, 100);
			idx = idx + 2;
			doubleToByte(ki, dataBuf, idx, 1000);
			idx = idx + 2;
			doubleToByte(kd, dataBuf, idx, 1000);
			// Log.i(TAG, "encode with type:" + settingsType + "," + kp + "," + ki + "," +
			// kd);
			return dataBuf;

		} else if (settingsType == 4) {

			byte[] dataBuf = new byte[18];

			dataBuf[0] = (byte) settingsType;
			int idx = 1;

			doubleToByte(atObstacle, dataBuf, idx, 100);
			idx = idx + 2;
			doubleToByte(unsafe, dataBuf, idx, 100);
			idx = idx + 2;
			doubleToByte(dfw, dataBuf, idx, 100);
			idx = idx + 2;
			doubleToByte(velocity, dataBuf, idx, 100);
			idx = idx + 2;
			intToByte(max_rpm, dataBuf, idx);
			idx = idx + 2;
			intToByte(min_rpm, dataBuf, idx);

			idx = idx + 2;
			doubleToByte(wheelRadius, dataBuf, idx, 1000);
			idx = idx + 2;
			doubleToByte(wheelDistance, dataBuf, idx, 1000);

			// dataBuf[idx] = (byte)pwm_diff;
			// idx++;
			// dataBuf[idx] = (byte)pwm_zero;
			// idx++;
			// doubleToByte(angleOff, dataBuf, idx, 100);

			return dataBuf;

		} else if (settingsType == 5) {
			byte[] dataBuf = new byte[13];

			dataBuf[0] = (byte) settingsType;
			int idx = 1;
			doubleToByte(atObstacle, dataBuf, idx, 100);
			idx = idx + 2;
			doubleToByte(unsafe, dataBuf, idx, 100);
			idx = idx + 2;
			intToByte(max_pwm, dataBuf, idx);
			idx = idx + 2;

			dataBuf[idx] = (byte) pwm_zero;
			idx++;
			dataBuf[idx] = (byte) pwm_diff;
			idx++;

			doubleToByte(angleOff, dataBuf, idx, 100);
			idx = idx + 2;
			doubleToByte(wheelSyncKP, dataBuf, idx, 100);

			return dataBuf;
		} else
			return null;
	}

	byte[] encode() {
		int idx = 0;
		doubleToByte(kp, encodedData, idx, 100);
		idx = idx + 2;
		doubleToByte(ki, encodedData, idx, 1000);
		idx = idx + 2;
		doubleToByte(kd, encodedData, idx, 1000);

		idx = idx + 2;
		doubleToByte(atObstacle, encodedData, idx, 100);
		idx = idx + 2;
		doubleToByte(unsafe, encodedData, idx, 100);

		idx = idx + 2;
		doubleToByte(dfw, encodedData, idx, 100);

		idx = idx + 2;
		doubleToByte(velocity, encodedData, idx, 100);

		idx = idx + 2;
		intToByte(max_rpm, encodedData, idx);

		idx = idx + 2;
		intToByte(min_rpm, encodedData, idx);

		return encodedData;
	}

	int byteToInt(byte[] data, int offset) {
		int value = ((data[offset + 1] & 0x7f) << 8) | (data[offset] & 0xff);

		if ((data[offset + 1] & 0x80) != 0) {
			value = -value;
		}
		return value;
	}

	void intToByte(int value, byte[] data, int offset) {
		int intVal = value;
		if (value < 0)
			intVal = -value;

		data[offset] = (byte) (intVal & 0xff);
		if (value >= 0) {
			data[offset + 1] = (byte) ((intVal >> 8) & 0x7f);
		} else {
			data[offset + 1] = (byte) (((intVal >> 8) & 0x7f) | 0x80);

		}
	}

	void doubleToByte(double value, byte[] data, int offset, int scale) {
		double val = value * scale;
		int intVal = (int) val;

		if (val < 0)
			intVal = -intVal;

		data[offset] = (byte) (intVal & 0xff);

		if (val >= 0)
			data[offset + 1] = (byte) ((intVal >> 8) & 0xff);
		else
			data[offset + 1] = (byte) (((intVal >> 8) & 0x7f) | 0x80);

	}

	double byteFloatValue(byte[] data, int offset, int scale) {
		int intVal = byteToInt(data, offset);
		double value = (double) intVal / (double) scale;
		return value;
	}

	public void copyFrom(Settings s) {

		if (settingsType < 4) {
			kp = s.kp;
			ki = s.ki;
			kd = s.kd;
		} else {
			atObstacle = s.atObstacle;
			unsafe = s.unsafe;
			dfw = s.dfw;
			velocity = s.velocity;
			max_rpm = s.max_rpm;
			min_rpm = s.min_rpm;
			pwm_diff = s.pwm_diff;
			pwm_zero = s.pwm_zero;
			angleOff = s.angleOff;
			max_pwm = s.max_pwm;
			wheelSyncKP = s.wheelSyncKP;

			wheelRadius = s.wheelRadius;
			wheelDistance = s.wheelDistance;

		}
	}

	public String toString() {
		return "st: type=" + this.settingsType + ", KP:" + kp + ",ki:" + ki + ",kd:" + kd + "; v:" + this.velocity
				+ ", max-rpm：" + this.max_rpm + ",min-rpm:" + this.min_rpm;

	}
}

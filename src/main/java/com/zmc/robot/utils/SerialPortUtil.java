package com.zmc.robot.utils;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

public class SerialPortUtil implements SerialPortEventListener {

	private int timeout = 2000;// open �˿�ʱ�ĵȴ�ʱ��
	private int threadTime = 0;

	private CommPortIdentifier commPort;
	private SerialPort serialPort = null;
	private InputStream inputStream;
	private OutputStream outputStream;

	private SerialPortEventListener serialListener = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public static List<String> listAllPorts() {
		ArrayList<String> ports = new ArrayList<String>();

		CommPortIdentifier cpid;// ��ǰ���ڶ���
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		// System.out.print("�г����ж˿ڣ�");
		while (en.hasMoreElements()) {
			cpid = (CommPortIdentifier) en.nextElement();
			// ���˿������Ƿ�Ϊ����
			if (cpid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				ports.add(cpid.getName() + ", " + cpid.getCurrentOwner());
				System.out.println(cpid.getName() + ", " + cpid.getCurrentOwner());
			}
		}

		return ports;
	}

	public void setSerialPortEventListener(SerialPortEventListener listener) {
		serialListener = listener;
	}

	private void open(String portName) throws Exception {

		this.commPort = null;
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();

		while (en.hasMoreElements()) {
			cpid = (CommPortIdentifier) en.nextElement();
			if (cpid.getPortType() == CommPortIdentifier.PORT_SERIAL && cpid.getName().equals(portName)) {
				this.commPort = cpid;
				break;
			}
		}

		if (commPort == null)
			throw new PortNotFoundException("Port " + portName + " not found!");

		if (serialPort != null)
			serialPort.close();
		serialPort = null;

		serialPort = (SerialPort) commPort.open("ZMCRobot", timeout);

		serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);

		this.inputStream = serialPort.getInputStream();
		this.outputStream = serialPort.getOutputStream();

		serialPort.addEventListener(this);
		// ���ÿɼ���
		serialPort.notifyOnDataAvailable(true);
	}

	public void close() {
		if (serialPort != null)
			serialPort.close();

		inputStream = null;
		outputStream = null;
		serialPort = null;
		commPort = null;

	}

	public void write(byte[] buffer) throws IOException {
		if (outputStream == null)
			throw new IOException("com port not opened!");

		outputStream.write(buffer);
	}

	public void write(String msg) throws IOException {
		write(msg.getBytes());
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public int read(byte[] buffer) throws IOException {
		if (inputStream == null)
			throw new IOException("com port not opened!");

		int count = 0;
		int readLen = 0;
		int len = buffer.length;

		while (inputStream.available() > 0) {
			readLen = inputStream.read(buffer, count, len);
			count += readLen;
			len -= readLen;
			if (readLen == 0)
				break;
		}

		return count;
	}

	public int readLine(byte[] buffer) throws IOException {
		if (inputStream == null)
			throw new IOException("com port not opened!");

		int count = 0;

		while (true) {
			int readByte = inputStream.read();
			if (readByte == -1) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {

				}
				continue;
			}
			if (readByte == '\r' || readByte == '\n')
				break;
			buffer[count++] = (byte) readByte;
			if (count >= buffer.length) {
				System.out.println("Out of bound!");
				break;
			}
		}
		return count;
	}

	private int baudRate = 115200;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;

	// 115200, SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE)
	public void setSerialPortParams(int baudRate, int dataBits, int stopBits, int parity)
			throws UnsupportedCommOperationException {
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;

		if (serialPort != null) {
			serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
		}
	}

	public static SerialPortUtil openPort(String portName) throws Exception {
		SerialPortUtil serialUtil = new SerialPortUtil();
		serialUtil.open(portName);
		return serialUtil;
	}

	public void serialEvent(SerialPortEvent event) {
		if (serialListener != null)
			serialListener.serialEvent(event);

		switch (event.getEventType()) {
		case SerialPortEvent.BI:/* Break interrupt,ͨѶ�ж� */
		case SerialPortEvent.OE:/* Overrun error����λ���� */
		case SerialPortEvent.FE:/* Framing error����֡���� */
		case SerialPortEvent.PE:/* Parity error��У����� */
		case SerialPortEvent.CD:/* Carrier detect���ز���� */
		case SerialPortEvent.CTS:/* Clear to send��������� */
		case SerialPortEvent.DSR:/* Data set ready�������豸���� */
		case SerialPortEvent.RI:/* Ring indicator������ָʾ */
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:/* Output buffer is empty�������������� */
			break;

		case SerialPortEvent.DATA_AVAILABLE:/* Data available at the serial port���˿��п������ݡ������������飬������ն� */
			break;
		}

	}

}

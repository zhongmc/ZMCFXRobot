package com.zmc.robot.fxrobotui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.sun.javafx.geom.Point2D;

import java.text.DecimalFormat;

public class CurveView extends Canvas {

	private double width, height;
	private GraphicsContext gc;

	private int xStep = 2;
	private int dataCount = 0, maxData;

	private double lastPoints[] = new double[100];

	private float maxValue = 10; // 0;

	private DecimalFormat fmt = new DecimalFormat("#0.00");

	public CurveView(double width, double height) {
		super(width, height);

		this.width = width;
		this.height = height;

		dataCount = 0;
		maxData = (int) width / xStep;

		for (int i = 0; i < lastPoints.length; i++) {
			lastPoints[i] = height / 2;
		}

		gc = getGraphicsContext2D();
		draw(gc);
	}

	final Color colors[] = { Color.BLUE, Color.RED, Color.GREEN, Color.CYAN, Color.BLACK, Color.YELLOW, Color.DARKRED,
			Color.DEEPPINK };

	public void addData(double[] data, int count) {

		gc.setLineWidth(1);
		double xs, ys, xe, ye;

		for (int i = 0; i < count; i++) {

			xs = xStep * dataCount;
			ys = lastPoints[i];
			xe = xs + xStep;
			ye = height / 2 - data[i] * height / (2 * maxValue);

			gc.setStroke(colors[i % 8]);
			gc.strokeLine(xs, ys, xe, ye);
			lastPoints[i] = ye;
		}
		dataCount++;
		if (dataCount >= maxData) {
			dataCount = 0;
			invalidate();
		}

	}

	public void invalidate() {

		dataCount = 0;
		gc.clearRect(0, 0, width, height);
		draw(gc);
		for (int i = 0; i < lastPoints.length; i++) {
			lastPoints[i] = height / 2;
		}

	}

	public void draw(GraphicsContext gc) {

		double curPos = 0;
		double step = 20;
		int idx = 0;
		gc.setLineWidth(1);

		while (true) {
			if (idx % 5 == 0) {
				gc.setStroke(Color.DARKGRAY);
			} else {
				gc.setStroke(Color.LIGHTGRAY);
			}
			gc.strokeLine((float) (width / 2 + curPos), 0, (float) (width / 2 + curPos), (float) (height));
			gc.strokeLine((float) (width / 2 - curPos), 0, (float) (width / 2 - curPos), (float) (height));

			idx++;
			curPos = curPos + step;
			if (curPos > width / 2)
				break;

		}

		double mScale = ((height - 20) / 2) / maxValue;

		double labelValue = 0;

		// gc.setTextSize(30);
		// gc.setColor(Color.DKGRAY);

		String label = fmt.format(labelValue);

		// canvas.drawText(label, 5, 35, paint);
		// canvas.drawText(label, 5, height - 5, paint);

		curPos = 0;
		step = 20;
		idx = 0;
		while (true) {
			if (idx % 5 == 0) {

				gc.setStroke(Color.DARKGRAY);
				label = fmt.format(labelValue);
				if (idx == 0) {
					// canvas.drawText(label, 5, (float) (height / 2 + curPos - 5), paint);
					gc.strokeText(label, 5, height / 2 + curPos - 5);
				} else {
					gc.strokeText("-" + label, 5, (float) (height / 2 + curPos - 5));
					gc.strokeText(label, 5, (float) (height / 2 - curPos - 5));

					// canvas.drawText("-" + label, 5, (float) (height / 2 + curPos - 5), paint);
					// canvas.drawText(label, 5, (float) (height / 2 - curPos - 5), paint);

				}
				gc.setStroke(Color.DARKGRAY);
			} else {
				gc.setStroke(Color.LIGHTGRAY);
			}

			gc.strokeLine(0, (float) (height / 2 + curPos), (float) (width), (float) (height / 2 + curPos));
			gc.strokeLine(0, (float) (height / 2 - curPos), (float) (width), (float) (height / 2 - curPos));

			curPos = curPos + step;
			if (curPos > height / 2)
				break;
			labelValue = (float) (labelValue + step / mScale);
			idx++;
		}

		gc.setStroke(Color.DARKGRAY);
		gc.strokeLine(0, 1, width, 1);
		gc.strokeLine(0, height - 2, width, height - 2);
		// canvas.drawLine(0, height/2, width, height/2, paint);

	}
}

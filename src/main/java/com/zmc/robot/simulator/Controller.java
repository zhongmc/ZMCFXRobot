package com.zmc.robot.simulator;

public abstract class Controller {

    protected double lastError = 0;
    protected double lastErrorIntegration = 0;

    protected double Kp, Ki, Kd;

    Controller() {

    }

    abstract Output execute(AbstractRobot robot, Input input, double dt);

    abstract void getControllorInfo(ControllerInfo state);

    public void updateSettings(Settings settings) {
        Kp = settings.kp;
        Ki = settings.ki;
        Kd = settings.kd;

        // Log.i("CTRL", "Update settings: kp=" + Kp + ", ki=" + Ki + ",kd=" + Kd);

    }

    void reset() {
        lastError = 0;
        lastErrorIntegration = 0;
    }

    public String toString() {
        return "Controller: " + this.getClass().getSimpleName() + " with: KP=" + Kp + ", KI=" + Ki + "Kd=" + Kd
                + "; Current: e=" + lastError + ",ie=" + lastErrorIntegration;
    }
}

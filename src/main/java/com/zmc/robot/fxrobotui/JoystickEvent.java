package com.zmc.robot.fxrobotui;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.event.EventTarget;
import javafx.beans.NamedArg;

/**
 * An {@link Event} representing some type of action. This event type is widely
 * used to represent a variety of things, such as when a
 * {@link javafx.scene.control.Button} has been fired, when a
 * {@link javafx.animation.KeyFrame} has finished, and other such usages.
 * 
 * @since JavaFX 2.0
 */
public class JoystickEvent extends Event {

    private double angle = 0;
    private double throttle = 0;

    private static final long serialVersionUID = 20121107L;
    /**
     * The only valid EventType for the ActionEvent.
     */
    public static final EventType<JoystickEvent> JOYSTICK = new EventType<JoystickEvent>(Event.ANY, "JOYSTICK");

    /**
     * Common supertype for all action event types.
     * 
     * @since JavaFX 8.0
     */
    public static final EventType<JoystickEvent> ANY = JOYSTICK;

    /**
     * Creates a new {@code ActionEvent} with an event type of {@code ACTION}. The
     * source and target of the event is set to {@code NULL_SOURCE_TARGET}.
     */
    public JoystickEvent() {
        super(JOYSTICK);
    }

    /**
     * @param angle
     * @param throttle
     */
    public JoystickEvent(@NamedArg("angle") double angle, @NamedArg("throttle") double throttle) {
        super(JOYSTICK);
        this.angle = angle;
        this.throttle = throttle;
    }

    public double getThrottle() {
        return throttle;
    }

    public void setThrottle(double throttle) {
        this.throttle = throttle;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Construct a new {@code ActionEvent} with the specified event source and
     * target. If the source or target is set to {@code null}, it is replaced by the
     * {@code NULL_SOURCE_TARGET} value. All ActionEvents have their type set to
     * {@code ACTION}.
     *
     * @param source
     *                   the event source which sent the event
     * @param target
     *                   the event target to associate with the event
     */
    public JoystickEvent(Object source, EventTarget target) {
        super(source, target, JOYSTICK);
    }

    @Override
    public JoystickEvent copyFor(Object newSource, EventTarget newTarget) {
        return (JoystickEvent) super.copyFor(newSource, newTarget);
    }

    @Override
    public EventType<? extends JoystickEvent> getEventType() {
        return (EventType<? extends JoystickEvent>) super.getEventType();
    }

}

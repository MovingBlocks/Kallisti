package org.terasology.kallisti.base.component;

public class ComponentTickEvent extends ComponentEvent {
    private final double tickTime;

    public ComponentTickEvent(double tickTime) {
        this.tickTime = tickTime;
    }

    public double getTickTime() {
        return tickTime;
    }
}

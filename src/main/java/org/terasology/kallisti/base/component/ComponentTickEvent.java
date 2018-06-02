package org.terasology.kallisti.base.component;

/**
 * A component event emitted once every machine tick.
 */
public class ComponentTickEvent extends ComponentEvent {
    private final double tickTime;

    public ComponentTickEvent(double tickTime) {
        this.tickTime = tickTime;
    }

    /**
     * @return The duration of the tick in question, in seconds.
     */
    public double getTickTime() {
        return tickTime;
    }
}

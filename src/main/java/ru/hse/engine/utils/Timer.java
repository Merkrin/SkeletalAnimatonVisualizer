package ru.hse.engine.utils;

/**
 * Timer class.
 */
public class Timer {
    protected double lastLoopTime;

    /**
     * Initialization method.
     */
    public void init() {
        lastLoopTime = getTime();
    }

    /**
     * Current time getter.
     *
     * @return current time in seconds.
     */
    public double getTime() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    /**
     * Elapsed time getter.
     *
     * @return elapsed time
     */
    public float getElapsedTime() {
        double time = getTime();
        float elapsedTime = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return elapsedTime;
    }

    /**
     * Last check time getter.
     *
     * @return last check time
     */
    public double getLastLoopTime() {
        return lastLoopTime;
    }
}

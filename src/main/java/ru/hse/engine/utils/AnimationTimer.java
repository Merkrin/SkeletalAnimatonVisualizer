package ru.hse.engine.utils;

/**
 * Timer for animation frames.
 */
public class AnimationTimer extends Timer {
    /**
     * Overridden elapsed time getter.
     *
     * @return time elapsed since last check
     */
    @Override
    public float getElapsedTime() {
        double time = getTime();

        return (float) (time - lastLoopTime);
    }

    /**
     * Update last check time.
     */
    public void updateLastLoopTime() {
        lastLoopTime = getTime();
    }
}

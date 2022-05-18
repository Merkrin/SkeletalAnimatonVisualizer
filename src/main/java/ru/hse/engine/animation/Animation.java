package ru.hse.engine.animation;

import java.util.List;

/**
 * Animation storage class.
 */
public class Animation {
    private int currentFrame;

    private final List<AnimatedFrame> frames;

    private final String name;

    private final double duration;

    /**
     * The class' constructor.
     *
     * @param name     animation name
     * @param frames   animation frames
     * @param duration animation duration
     */
    public Animation(String name, List<AnimatedFrame> frames, double duration) {
        this.name = name;
        this.frames = frames;
        this.duration = duration;

        currentFrame = 0;
    }

    /**
     * Current frame getter.
     *
     * @return current frame
     */
    public AnimatedFrame getCurrentFrame() {
        return this.frames.get(currentFrame);
    }

    /**
     * Name getter.
     *
     * @return animation name
     */
    public String getName() {
        return name;
    }

    /**
     * Set next animation frame.
     */
    public void nextFrame() {
        int nextFrame = currentFrame + 1;

        if (nextFrame > frames.size() - 1)
            currentFrame = 0;
        else
            currentFrame = nextFrame;
    }
}

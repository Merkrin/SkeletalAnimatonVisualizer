package ru.hse.engine.utils;

public class AnimationTimer extends Timer{
    @Override
    public float getElapsedTime(){
        double time = getTime();

        return (float) (time - lastLoopTime);
    }

    public void updateLastLoopTime(){
        lastLoopTime = getTime();
    }
}

package ru.hse.engine;

import ru.hse.engine.utils.MouseInput;
import ru.hse.engine.utils.Timer;
import ru.hse.engine.utils.Window;

public class Engine implements Runnable {
    public static final int TARGET_FPS = 75;

    public static final int TARGET_UPS = 30;

    private final Window window;

    private final Timer timer;

    private final Logic logic;

    private final MouseInput mouseInput;

    public Engine(String windowTitle, boolean vSync, Window.WindowOptions opts, Logic logic) throws Exception {
        this(windowTitle, 0, 0, vSync, opts, logic);
    }

    public Engine(String windowTitle, int width, int height, boolean vSync, Window.WindowOptions opts, Logic logic) {
        window = new Window(windowTitle, width, height, vSync, opts);
        mouseInput = new MouseInput();

        this.logic = logic;

        timer = new Timer();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        window.init();
        timer.init();
        mouseInput.init(window);
        logic.init(window);
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        while (!window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            if (!window.isvSync())
                sync();
        }
    }

    protected void cleanup() {
        logic.cleanup();
    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;

        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }

    protected void input() {
        mouseInput.input();
        logic.input(window, mouseInput);
    }

    protected void update(float interval) {
        logic.update(interval, mouseInput);
    }

    protected void render() {
        logic.render(window);
        window.update();
    }
}

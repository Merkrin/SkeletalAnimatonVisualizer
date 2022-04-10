package ru.hse.engine;

import ru.hse.engine.utils.MouseInput;
import ru.hse.engine.utils.Window;

public interface Logic {
    void init(Window window) throws Exception;

    void input(Window window, MouseInput mouseInput);

    void update(float interval, MouseInput mouseInput);

    void render(Window window);

    void cleanup();
}

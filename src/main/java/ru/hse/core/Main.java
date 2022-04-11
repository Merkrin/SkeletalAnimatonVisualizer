package ru.hse.core;

import ru.hse.engine.AnimatorLogic;
import ru.hse.engine.Engine;
import ru.hse.engine.Logic;
import ru.hse.engine.utils.Window;

public class Main {
    public static void main(String[] args) {
        try {
            boolean vSync = true;

            Logic animatorLogic = new AnimatorLogic();

            Window.WindowOptions opts = new Window.WindowOptions();

            Engine gameEng = new Engine("GAME", vSync, opts, animatorLogic);
            gameEng.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}

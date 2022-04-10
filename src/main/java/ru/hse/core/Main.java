package ru.hse.core;

import ru.hse.engine.GameEngine;
import ru.hse.engine.Logic;
import ru.hse.engine.utils.Window;

public class Main {
    public static void main(String[] args) {
        try {
            boolean vSync = true;
            Logic gameLogic = new AnimatorLogic();
            Window.WindowOptions opts = new Window.WindowOptions();
            GameEngine gameEng = new GameEngine("GAME", vSync, opts, gameLogic);
            gameEng.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}

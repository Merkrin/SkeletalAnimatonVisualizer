package ru.hse.core;

import ru.hse.engine.GameEngine;
import ru.hse.engine.IGameLogic;
import ru.hse.engine.utils.Window;

public class Main {
    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGameLogic gameLogic = new DummyGame();
            Window.WindowOptions opts = new Window.WindowOptions();
            GameEngine gameEng = new GameEngine("GAME", vSync, opts, gameLogic);
            gameEng.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}

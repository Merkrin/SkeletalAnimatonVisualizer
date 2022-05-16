package ru.hse.core;

import ru.hse.core.utils.Constants;
import ru.hse.core.utils.Settings;
import ru.hse.engine.AnimatorLogic;
import ru.hse.engine.Engine;
import ru.hse.engine.Logic;
import ru.hse.engine.utils.Window;

public class Main {
    private static final Settings SETTINGS = Settings.getInstance();

    public static void main(String[] args) {
        try {
            Logic animatorLogic = new AnimatorLogic();

            Window.WindowOptions windowOptions = new Window.WindowOptions();

            Engine engine = new Engine(Constants.WINDOW_TITLE, SETTINGS.isVSyncEnabled(),
                    windowOptions, animatorLogic);

            engine.run();
        } catch (Exception e) {
            System.out.println("An error occurred.");

            e.printStackTrace();
        }
    }
}

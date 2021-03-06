package ru.hse.core;

import ru.hse.core.utils.ArgumentsParser;
import ru.hse.core.utils.Constants;
import ru.hse.core.utils.Settings;
import ru.hse.engine.AnimatorLogic;
import ru.hse.engine.Engine;
import ru.hse.engine.Logic;
import ru.hse.engine.utils.CmdSaver;
import ru.hse.engine.utils.Window;

/**
 * Main class of the application.
 */
public class Main {
    private static final Settings SETTINGS = Settings.getInstance();

    /**
     * Main method of the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            ArgumentsParser.readArguments(args);

            Logic animatorLogic = new AnimatorLogic();

            Window.WindowOptions windowOptions = new Window.WindowOptions();

            Engine engine = new Engine(Constants.WINDOW_TITLE, SETTINGS.isVSyncEnabled(),
                    windowOptions, animatorLogic);

            engine.run();
        } catch (Exception e) {
            System.out.println("An error occurred.");

            e.printStackTrace();
        } finally {
            System.out.println("Saving settings...");

            CmdSaver.saveCmd(ArgumentsParser.createCommandLine());

            System.out.println("Terminating process...");
        }
    }
}

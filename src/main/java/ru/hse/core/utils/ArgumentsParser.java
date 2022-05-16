package ru.hse.core.utils;

import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hse.core.utils.exceptions.CommandLineArgumentsException;
import ru.hse.core.utils.exceptions.InvalidSettingException;
import ru.hse.core.utils.exceptions.SettingsFileException;
import ru.hse.graphics.lighting.PointLight;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class ArgumentsParser {
    private static final Settings SETTINGS = Settings.getInstance();

    private static final String[] flags = {"-al", "-sl", "-ld", "-lc", "-li", "-cla", "-afps", "-cp", "-cr", "-sp",
            "-vsync", "-st", "-anim", "-ptm", "-ptt", "-scale", "-skscale", "-sc", "-pts", "-plc", "-plp"};

    private static final int MAXIMAL_ARGUMENTS_AMOUNT = flags.length;

    private static final HashMap<String, String> settings = new HashMap<>();

    public static void readArguments(String[] args) {
        boolean isMac = System.getProperty("os.name").startsWith("Mac");

        String[] arguments = null;

        if (isMac) {
            if (args.length != 1) {
                arguments = new String[args.length - 1];

                if (args.length - 1 >= 0) System.arraycopy(args, 1,
                        arguments, 0, args.length - 1);
            }
        } else {
            if (args.length != 0)
                arguments = args;
        }

        if (arguments != null) {
            try {
                parseArguments(arguments);
            } catch (CommandLineArgumentsException | InvalidSettingException | NullPointerException e) {
                System.out.println("An error in command line format found: " +
                        e.getMessage());
                System.out.println("Starting with standard settings...");
            } catch (IOException | SettingsFileException e) {
                System.out.println("An error while reading file occurred: " +
                        e.getMessage());
                System.out.println("Starting with standard settings...");
            } catch (Exception e) {
                System.out.println("An unhandled exception occurred: " + e.getMessage());
                System.out.println("Exiting...");

                System.exit(0);
            }
        }
    }

    private static void parseArguments(String[] args) throws SettingsFileException, IOException, CommandLineArgumentsException, InvalidSettingException {
        if (args[0].equals("-FF"))
            args = readArgsFromFile(args[1]);

        double argsAmount = args.length / 2.0;

        checkLength(argsAmount);

        for (int i = 0; i < argsAmount * 2; i += 2) {
            if (isValidFlag(args[i]))
                settings.put(args[i], args[i + 1]);
            else
                throw new CommandLineArgumentsException("Invalid " +
                        "argument found.");
        }

        if (settings.containsKey("-al"))
            SETTINGS.setAmbientLight(createVector3f(settings.get("-al")));
        if (settings.containsKey("-sl"))
            SETTINGS.setSkyboxLight(createVector3f(settings.get("-sl")));
        if (settings.containsKey("-ld"))
            SETTINGS.setLightDirection(createVector3f(settings.get("-ld")));
        if (settings.containsKey("-lc"))
            SETTINGS.setLightColor(createVector3f(settings.get("-lc")));
        if (settings.containsKey("-li"))
            SETTINGS.setLightIntensity(Float.parseFloat(settings.get("-li")));
        if (settings.containsKey("-cla"))
            SETTINGS.setCurrentLightAngle(Float.parseFloat(settings.get("-cla")));
        if (settings.containsKey("-afps"))
            SETTINGS.setAnimationFramesPerSecond(Integer.parseInt(settings.get("-afps")));
        if (settings.containsKey("-cp"))
            SETTINGS.setCameraPosition(createVector3f(settings.get("-cp")));
        if (settings.containsKey("-cr"))
            SETTINGS.setCameraRotation(createVector3f(settings.get("-cr")));
        if (settings.containsKey("-sp"))
            SETTINGS.setSpecularPower(Float.parseFloat(settings.get("-sp")));
        if (settings.containsKey("-vsync"))
            SETTINGS.setVSyncEnabled(createBoolean(settings.get("-vsync")));
        if (settings.containsKey("-st"))
            SETTINGS.setScreenshotType(Integer.parseInt(settings.get("-st")));
        if (!settings.containsKey("-anim"))
            throw new CommandLineArgumentsException("Required flag \"-anim\" not found.");
        else
            SETTINGS.setModelAnimated(createBoolean(settings.get("-anim")));
        if (!settings.containsKey("-ptm"))
            throw new CommandLineArgumentsException("Required flag \"-ptm\" not found.");
        else
            SETTINGS.setPathToModel((settings.get("-ptm")));
        if (!settings.containsKey("-ptt"))
            throw new CommandLineArgumentsException("Required flag \"-ptt\" not found.");
        else
            SETTINGS.setPathToTexture((settings.get("-ptt")));
        if (settings.containsKey("-scale"))
            SETTINGS.setScale(Float.parseFloat(settings.get("-scale")));
        if (settings.containsKey("-skscale"))
            SETTINGS.setSkyboxScale(Float.parseFloat(settings.get("-skscale")));
        if (settings.containsKey("-sc"))
            SETTINGS.setSkyboxColor(createVector4f(settings.get("-sc")));
        if (!settings.containsKey("-pts"))
            throw new CommandLineArgumentsException("Required flag \"-pts\" not found.");
        else
            SETTINGS.setSkyboxPath((settings.get("-pts")));
        if (settings.containsKey("-plp")) {
            if (settings.containsKey("-plc"))
                SETTINGS.setPointLights(createPointLights(settings.get("-plp"), settings.get("-plc")));
            else
                throw new CommandLineArgumentsException("No light colors set for point lights.");
        }
    }

    private static String[] readArgsFromFile(String filePath) throws SettingsFileException, IOException {
        if (!(filePath).endsWith(".sav"))
            throw new SettingsFileException("Not a *.sav-file given for settings reading.");

        Path path = Paths.get(filePath);

        return Files.readAllLines(path).get(0).split(" ");
    }

    public static String createCommandLine() {
        String commandLine = "";

        commandLine += "-al " + SETTINGS.getAmbientLightAsString();
        commandLine += "-sl " + SETTINGS.getSkyboxLightAsString();
        commandLine += "-ld " + SETTINGS.getLightDirectionAsString();
        commandLine += "-lc " + SETTINGS.getLightColorAsString();
        commandLine += "-li " + SETTINGS.getLightIntensity();
        commandLine += "-cla " + SETTINGS.getCurrentLightAngle();
        commandLine += "-afps " + SETTINGS.getAnimationFramesPerSecond();
        commandLine += "-cp " + SETTINGS.getCameraPositionAsString();
        commandLine += "-cr " + SETTINGS.getCameraRotationAsString();
        commandLine += "-sp " + SETTINGS.getSpecularPower();
        commandLine += "-vsync " + SETTINGS.isVSyncEnabled();
        commandLine += "-st " + SETTINGS.getScreenshotType();
        commandLine += "-anim " + SETTINGS.isModelAnimated();
        commandLine += "-ptm " + SETTINGS.getPathToModel();
        commandLine += "-ptt " + SETTINGS.getPathToTexture();
        commandLine += "-scale " + SETTINGS.getScale();
        commandLine += "-skscale " + SETTINGS.getSkyboxScale();
        commandLine += "-sc " + SETTINGS.getSkyboxColor();
        commandLine += "-pts " + SETTINGS.getSkyboxPath();
        if (SETTINGS.getPointLights().length != 0) {
            commandLine += "-plp " + SETTINGS.getPointLightPositions();
            commandLine += "-plc " + SETTINGS.getPointLightColors();
        }

        return commandLine;
    }

    private static void checkLength(double argsAmount) throws CommandLineArgumentsException {
        if (argsAmount > MAXIMAL_ARGUMENTS_AMOUNT || argsAmount != (int) argsAmount)
            throw new CommandLineArgumentsException("Invalid arguments amount.");
    }

    private static boolean isValidFlag(String flag) {
        for (String validFlag : flags)
            if (validFlag.equals(flag))
                return true;

        return false;
    }

    private static Vector3f createVector3f(String cmd) throws InvalidSettingException {
        String[] positionStrings = cmd.split(",");

        if (positionStrings.length != 3)
            throw new InvalidSettingException("Invalid Vector3f argument.");

        return new Vector3f(Float.parseFloat(positionStrings[0]),
                Float.parseFloat(positionStrings[1]),
                Float.parseFloat(positionStrings[2]));
    }

    private static Vector4f createVector4f(String cmd) throws InvalidSettingException {
        String[] positionStrings = cmd.split(",");

        if (positionStrings.length != 4)
            throw new InvalidSettingException("Invalid Vector4f argument.");

        return new Vector4f(Float.parseFloat(positionStrings[0]),
                Float.parseFloat(positionStrings[1]),
                Float.parseFloat(positionStrings[2]),
                Float.parseFloat(positionStrings[3]));
    }

    private static boolean createBoolean(String cmd) throws InvalidSettingException {
        if (cmd.equalsIgnoreCase("true"))
            return true;
        if (cmd.equalsIgnoreCase("false"))
            return false;

        throw new InvalidSettingException("Invalid boolean value.");
    }

    private static PointLight[] createPointLights(String cmdPositions, String cmdColors) throws CommandLineArgumentsException {
        String[] positionStrings = cmdPositions.split(",");
        String[] colorStrings = cmdColors.split(",");

        if (positionStrings.length != colorStrings.length)
            throw new CommandLineArgumentsException("Point light positions cannot be mapped onto colors.");

        PointLight[] pointLights = null;

        if (positionStrings.length % 3 == 0) {
            pointLights = new PointLight[positionStrings.length / 3];

            int index = 0;

            for (int i = 0; i <= positionStrings.length - 3; i += 3) {
                pointLights[index] = new PointLight(new Vector3f(Float.parseFloat(colorStrings[i]),
                        Float.parseFloat(colorStrings[i + 1]),
                        Float.parseFloat(colorStrings[i + 2])),
                        new Vector3f(Float.parseFloat(positionStrings[i]),
                                Float.parseFloat(positionStrings[i + 1]),
                                Float.parseFloat(positionStrings[i + 2])),
                        10);

                index++;
            }
        }

        return pointLights;
    }
}

package ru.hse.engine.wrappers;

import ru.hse.engine.GameItem;
import ru.hse.graphics.skybox.Skybox;

public class Scene {
    private GameItem[] gameItems;

    private Skybox skybox;

    private SceneLight sceneLight;

    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void setGameItems(GameItem[] gameItems) {
        this.gameItems = gameItems;
    }

    public Skybox getSkyBox() {
        return skybox;
    }

    public void setSkyBox(Skybox skyBox) {
        this.skybox = skyBox;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}

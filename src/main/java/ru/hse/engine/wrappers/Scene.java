package ru.hse.engine.wrappers;

import ru.hse.engine.MeshedItem;
import ru.hse.graphics.model.InstancedMesh;
import ru.hse.graphics.model.Mesh;
import ru.hse.graphics.skybox.Skybox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {
    private final Map<Mesh, List<MeshedItem>> meshMap;

    private final Map<InstancedMesh, List<MeshedItem>> instancedMeshMap;

    private Skybox skyBox;

    private SceneLight sceneLight;

    private boolean renderShadows;

    public Scene() {
        meshMap = new HashMap();
        instancedMeshMap = new HashMap();
        renderShadows = true;
    }

    public Map<Mesh, List<MeshedItem>> getGameMeshes() {
        return meshMap;
    }

    public Map<InstancedMesh, List<MeshedItem>> getGameInstancedMeshes() {
        return instancedMeshMap;
    }

    public boolean isRenderShadows() {
        return renderShadows;
    }

    public void setMeshItems(MeshedItem[] meshedItems) {
        int meshedItemsAmount = meshedItems != null ? meshedItems.length : 0;

        for (int i = 0; i < meshedItemsAmount; i++) {
            MeshedItem meshedItem = meshedItems[i];

            Mesh[] meshes = meshedItem.getMeshes();

            for (Mesh mesh : meshes) {
                boolean instancedMesh = mesh instanceof InstancedMesh;

                List<MeshedItem> list = instancedMesh ? instancedMeshMap.get(mesh) : meshMap.get(mesh);

                if (list == null) {
                    list = new ArrayList<>();

                    if (instancedMesh)
                        instancedMeshMap.put((InstancedMesh) mesh, list);
                    else
                        meshMap.put(mesh, list);
                }

                list.add(meshedItem);
            }
        }
    }

    public void cleanup() {
        for (Mesh mesh : meshMap.keySet())
            mesh.cleanUp();

        for (Mesh mesh : instancedMeshMap.keySet())
            mesh.cleanUp();
    }

    public Skybox getSkyBox() {
        return skyBox;
    }

    public void setRenderShadows(boolean renderShadows) {
        this.renderShadows = renderShadows;
    }

    public void setSkyBox(Skybox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}

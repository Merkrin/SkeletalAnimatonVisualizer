package ru.hse.engine.animation;

import ru.hse.engine.MeshedItem;
import ru.hse.graphics.model.Mesh;

import java.util.Map;
import java.util.Optional;

public class AnimGameItem extends MeshedItem {
    private final Map<String, Animation> animations;

    private Animation currentAnimation;

    public AnimGameItem(Mesh[] meshes, Map<String, Animation> animations) {
        super(meshes);
        this.animations = animations;
        Optional<Map.Entry<String, Animation>> entry = animations.entrySet().stream().findFirst();
        currentAnimation = entry.map(Map.Entry::getValue).orElse(null);
    }

    public Animation getAnimation(String name) {
        return animations.get(name);
    }

    public Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
    }
}

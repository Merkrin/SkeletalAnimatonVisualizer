package ru.hse.engine.animation;

import ru.hse.engine.MeshedItem;
import ru.hse.graphics.model.Mesh;

import java.util.Map;
import java.util.Optional;

/**
 * Animated scene item representation class.
 */
public class AnimatedItem extends MeshedItem {
    private final Map<String, Animation> animations;

    private Animation currentAnimation;

    /**
     * The class' constructor.
     *
     * @param meshes     meshes of the item
     * @param animations animations of the item
     */
    public AnimatedItem(Mesh[] meshes, Map<String, Animation> animations) {
        super(meshes);

        this.animations = animations;

        Optional<Map.Entry<String, Animation>> entry = animations.entrySet().stream().findFirst();

        currentAnimation = entry.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Current animation getter.
     *
     * @return current animation
     */
    public Animation getCurrentAnimation() {
        return currentAnimation;
    }
}

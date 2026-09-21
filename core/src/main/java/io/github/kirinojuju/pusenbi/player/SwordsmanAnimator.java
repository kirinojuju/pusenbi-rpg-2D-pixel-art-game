package io.github.kirinojuju.pusenbi.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.EnumMap;
import java.util.Map;

/** Kaito's existing body frames, loaded explicitly for both IDE and packaged runs. */
public class SwordsmanAnimator implements Disposable {
    private static final String BODY_PATH = "characters/kaito/body/";
    private static final float IDLE_FRAME_DURATION = 0.18f;
    private static final float WALK_FRAME_DURATION = 0.12f;
    private static final int WALK_FRAME_COUNT = 4;
    private static final int DASH_FRAME_COUNT = 5;

    private final Array<Texture> textures = new Array<>();
    private final Map<FacingDirection, Animation<TextureRegion>> idle = new EnumMap<>(FacingDirection.class);
    private final Map<FacingDirection, Animation<TextureRegion>> walk = new EnumMap<>(FacingDirection.class);
    private final Map<FacingDirection, Animation<TextureRegion>> dash = new EnumMap<>(FacingDirection.class);

    public SwordsmanAnimator(float dashDuration) {
        try {
            loadSide(FacingDirection.LEFT, "l", dashDuration);
            loadSide(FacingDirection.RIGHT, "r", dashDuration);
        } catch (RuntimeException failure) {
            dispose();
            throw failure;
        }
    }

    private void loadSide(FacingDirection side, String suffix, float dashDuration) {
        // Each side has one idle pose, four walk frames, and five dash frames.
        Array<TextureRegion> idleFrames = new Array<>();
        idleFrames.add(loadFrame("idle/_kaito_idle_" + suffix + ".png"));
        idle.put(side, new Animation<>(IDLE_FRAME_DURATION, idleFrames));
        walk.put(side, loadSequence("walk", suffix, WALK_FRAME_COUNT, WALK_FRAME_DURATION));
        dash.put(side, loadSequence("dash", suffix, DASH_FRAME_COUNT, dashDuration / DASH_FRAME_COUNT));
    }

    private Animation<TextureRegion> loadSequence(String state, String side, int count, float duration) {
        Array<TextureRegion> frames = new Array<>();
        for (int index = 0; index < count; index++) {
            frames.add(loadFrame(state + "/_kaito_" + state + "_" + side + "0" + index + ".png"));
        }
        return new Animation<>(duration, frames);
    }

    private TextureRegion loadFrame(String path) {
        // Individual resources work inside a JAR; listing internal directories does not.
        // Missing required frames raise an error instead of silently disabling animation.
        Texture texture = new Texture(Gdx.files.internal(BODY_PATH + path));
        textures.add(texture);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return new TextureRegion(texture);
    }

    public TextureRegion getFrame(AnimationState state, FacingDirection side, float stateTime) {
        FacingDirection availableSide = side == FacingDirection.LEFT ? FacingDirection.LEFT : FacingDirection.RIGHT;
        Animation<TextureRegion> animation;
        switch (state) {
            case WALK:
                animation = walk.get(availableSide);
                break;
            case DASH:
                animation = dash.get(availableSide);
                break;
            default:
                animation = idle.get(availableSide);
        }
        return animation.getKeyFrame(stateTime, state != AnimationState.DASH);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) texture.dispose();
        textures.clear();
    }
}

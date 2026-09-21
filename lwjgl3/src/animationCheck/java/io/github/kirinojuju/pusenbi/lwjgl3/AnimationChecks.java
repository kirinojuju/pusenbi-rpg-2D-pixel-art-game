package io.github.kirinojuju.pusenbi.lwjgl3;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxNativesLoader;
import io.github.kirinojuju.pusenbi.player.AnimationState;
import io.github.kirinojuju.pusenbi.player.FacingDirection;
import io.github.kirinojuju.pusenbi.player.Player;
import io.github.kirinojuju.pusenbi.player.SwordsmanAnimator;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/** Exercises real asset decoding and game updates without opening a graphics window. */
public final class AnimationChecks {
    public static void main(String[] args) throws Exception {
        GdxNativesLoader.load();
        Gdx.files = new Lwjgl3Files();
        Gdx.app = stub(Application.class);
        Gdx.graphics = stub(Graphics.class);
        Gdx.input = stub(Input.class);
        Gdx.gl = Gdx.gl20 = stub(GL20.class);
        System.out.println("Asset directory visible on disk: "
                + Gdx.files.internal("characters/kaito/body/walk").isDirectory());
        SwordsmanAnimator animator = new SwordsmanAnimator(0.14f);
        try {
            for (FacingDirection side : new FacingDirection[]{FacingDirection.LEFT, FacingDirection.RIGHT}) {
                Set<TextureRegion> frames = new HashSet<>();
                for (int i = 0; i < 100; i++) {
                    frames.add(animator.getFrame(AnimationState.WALK, side, i * 0.01f));
                }
                require(frames.size() == 4, side + " walk needs 4 frames; got " + frames.size());
                frames.clear();
                for (int i = 0; i < 9; i++) {
                    frames.add(animator.getFrame(AnimationState.DASH, side, i / 60f));
                }
                require(frames.size() == 5, side + " dash must show all five frames within 0.14 seconds");
                require(animator.getFrame(AnimationState.WALK, side, 0f)
                        == animator.getFrame(AnimationState.WALK, side, 0.49f), "Walk must loop");
                require(animator.getFrame(AnimationState.DASH, side, 0.13f)
                        == animator.getFrame(AnimationState.DASH, side, 1f), "Dash must hold its last frame");
            }
            for (AnimationState state : AnimationState.values()) {
                require(animator.getFrame(state, FacingDirection.LEFT, 0f).getTexture()
                        != animator.getFrame(state, FacingDirection.RIGHT, 0f).getTexture(),
                        state + " must use different left/right textures");
            }
        } finally {
            animator.dispose();
        }
        System.out.println("PASS: classpath frame loading, walk cycling, left/right selection");

        Player player = new Player(320f, 240f);
        try {
            player.keyDown(Input.Keys.A);
            for (int i = 0; i < 30; i++) player.update(1f / 60f);
            player.keyUp(Input.Keys.A);
            require(Math.abs(position(player).x - 230f) < 0.01f, "Walking must move 90 pixels left");
            require(player.getFacing() == FacingDirection.LEFT, "Left input must set facing");
            require(field(player, "state") == AnimationState.WALK, "Moving must select WALK");
            require((Float) field(player, "stateTime") > 0.45f, "Walk timer must accumulate");
            player.update(1f / 60f);
            require(field(player, "state") == AnimationState.IDLE, "Release must select IDLE");
            float beforeDash = position(player).x;
            player.keyDown(Input.Keys.SPACE);
            for (int i = 0; i < 30; i++) player.update(1f / 60f);
            player.keyUp(Input.Keys.SPACE);
            float distance = beforeDash - position(player).x;
            require(distance > 30f && distance < 55f, "Short dash must travel left 30-55 pixels; got " + distance);
            require(field(player, "state") == AnimationState.IDLE, "Dash must finish promptly");
            player.keyDown(Input.Keys.W);
            player.update(1f / 60f);
            player.keyUp(Input.Keys.W);
            require(field(player, "spriteFacing") == FacingDirection.LEFT,
                    "Vertical movement must preserve the last left/right pose");
            player.keyDown(Input.Keys.D);
            player.update(1f / 60f);
            player.keyUp(Input.Keys.D);
            require(field(player, "spriteFacing") == FacingDirection.RIGHT, "Right input must change sprite side");
            beforeDash = position(player).x;
            player.keyDown(Input.Keys.SPACE);
            player.update(0.3f);
            require(Math.abs(position(player).x - beforeDash - 42f) < 0.01f,
                    "A slow frame must not make the dash longer");
            System.out.println("PASS: movement, state timer, idle, dash distance = " + distance);
        } finally {
            player.dispose();
        }
    }

    private static Vector2 position(Player player) throws Exception {
        return (Vector2) field(player, "position");
    }

    private static Object field(Player player, String name) throws Exception {
        Field field = Player.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(player);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    // Only the graphics/input boundary is stubbed; textures decode the real PNG files.
    private static <T> T stub(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("equals")) return proxy == arguments[0];
                    Class<?> result = method.getReturnType();
                    if (result == boolean.class) return false;
                    if (result == int.class) return 0;
                    if (result == float.class) return 0f;
                    if (result == long.class) return 0L;
                    return null;
                }));
    }
}

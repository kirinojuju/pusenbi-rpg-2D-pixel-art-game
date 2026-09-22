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
import io.github.kirinojuju.pusenbi.input.KeyboardPlayerInput;
import io.github.kirinojuju.pusenbi.input.PlayerInput;
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

        KeyboardPlayerInput input = new KeyboardPlayerInput(Gdx.input);
        Player player = new Player(320f, 240f, input);
        try {
            input.keyDown(Input.Keys.A);
            for (int i = 0; i < 30; i++) player.update(1f / 60f);
            input.keyUp(Input.Keys.A);
            require(Math.abs(position(player).x - 235f) < 0.01f, "Walking must move 85 pixels left at 170 pixels/second");
            require(player.getFacing() == FacingDirection.LEFT, "Left input must set facing");
            require(field(player, "state") == AnimationState.WALK, "Moving must select WALK");
            require((Float) field(player, "stateTime") > 0.45f, "Walk timer must accumulate");
            player.update(1f / 60f);
            require(field(player, "state") == AnimationState.IDLE, "Release must select IDLE");
            float beforeDash = position(player).x;
            input.keyDown(Input.Keys.SHIFT_LEFT);
            for (int i = 0; i < 30; i++) player.update(1f / 60f);
            input.keyUp(Input.Keys.SHIFT_LEFT);
            float distance = beforeDash - position(player).x;
            require(Math.abs(distance - 60f) < 0.01f, "Dash must travel left 60 pixels once while held; got " + distance);
            require(field(player, "state") == AnimationState.IDLE, "Dash must finish promptly");
            input.keyDown(Input.Keys.W);
            player.update(1f / 60f);
            input.keyUp(Input.Keys.W);
            require(field(player, "spriteFacing") == FacingDirection.LEFT,
                    "Vertical movement must preserve the last left/right pose");
            input.keyDown(Input.Keys.D);
            player.update(1f / 60f);
            input.keyUp(Input.Keys.D);
            require(field(player, "spriteFacing") == FacingDirection.RIGHT, "Right input must change sprite side");
            beforeDash = position(player).x;
            input.keyDown(Input.Keys.SHIFT_LEFT);
            player.update(0.3f);
            require(Math.abs(position(player).x - beforeDash - 60f) < 0.01f,
                    "A slow frame must not make the dash longer");
            System.out.println("PASS: movement, state timer, idle, dash distance = " + distance);
        } finally {
            player.dispose();
        }
        checkKeyboardInput();
        checkIndependentInput();
        CombatChecks.run();
    }

    private static void checkKeyboardInput() {
        Set<Integer> held = new HashSet<>();
        Set<Integer> buttons = new HashSet<>();
        Set<Integer> justPressedButtons = new HashSet<>();
        Input keyboard = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(),
                new Class<?>[]{Input.class}, (proxy, method, arguments) -> {
                    if (method.getName().equals("isKeyPressed")) return held.contains((Integer) arguments[0]);
                    if (method.getName().equals("isButtonPressed")) return buttons.contains((Integer) arguments[0]);
                    if (method.getName().equals("isButtonJustPressed")) return justPressedButtons.contains((Integer) arguments[0]);
                    throw new AssertionError("Unexpected keyboard call: " + method.getName());
                });
        KeyboardPlayerInput input = new KeyboardPlayerInput(keyboard);
        int[] keys = {Input.Keys.W, Input.Keys.UP, Input.Keys.S, Input.Keys.DOWN,
                Input.Keys.A, Input.Keys.LEFT, Input.Keys.D, Input.Keys.RIGHT};
        float[] x = {0, 0, 0, 0, -1, -1, 1, 1};
        float[] y = {1, 1, -1, -1, 0, 0, 0, 0};
        for (int i = 0; i < keys.length; i++) {
            held.add(keys[i]);
            require(input.getMoveX() == x[i] && input.getMoveY() == y[i], "Polling mapping: " + keys[i]);
            held.clear();
            require(!input.keyDown(keys[i]), "Key events must remain unconsumed");
            require(input.getMoveX() == x[i] && input.getMoveY() == y[i], "Event mapping: " + keys[i]);
            require(!input.keyUp(keys[i]), "Key release must remain unconsumed");
            require(input.getMoveX() == 0f && input.getMoveY() == 0f, "Release must clear movement");
        }
        held.add(Input.Keys.A);
        held.add(Input.Keys.LEFT);
        require(input.getMoveX() == -1f, "Aliases must not double movement");
        input.keyDown(Input.Keys.A);
        input.keyUp(Input.Keys.A);
        held.remove(Input.Keys.A);
        require(input.getMoveX() == -1f, "Polling must retain a held alias after another is released");
        held.add(Input.Keys.D);
        held.add(Input.Keys.W);
        held.add(Input.Keys.S);
        require(input.getMoveX() == 0f && input.getMoveY() == 0f, "Opposing keys must cancel");
        held.clear();
        held.add(Input.Keys.SHIFT_LEFT);
        require(input.isDashPressed() && input.isDashPressed(), "Held dash reads must not consume input");
        held.clear();
        require(!input.isDashPressed(), "Left Shift release must clear dash");
        for (int unboundKey : new int[]{Input.Keys.SPACE, Input.Keys.SHIFT_RIGHT}) {
            held.add(unboundKey);
            input.keyDown(unboundKey);
            require(!input.isDashPressed(), "Only Left Shift should trigger dash");
            input.keyUp(unboundKey);
            held.clear();
        }
        require(!input.isAttackPressed(), "Attack starts released");
        buttons.add(Input.Buttons.LEFT);
        require(input.isAttackPressed() && input.isAttackPressed(), "Mouse polling must report held attack");
        buttons.clear();
        require(!input.isAttackPressed(), "Mouse release must clear attack");
        input.touchDown(0, 0, 0, Input.Buttons.LEFT);
        require(input.isAttackPressed(), "Left click events must report attack");
        input.touchUp(0, 0, 0, Input.Buttons.LEFT);
        require(!input.isAttackPressed(), "Left button up clears attack");
        buttons.add(Input.Buttons.RIGHT);
        input.touchDown(0, 0, 0, Input.Buttons.RIGHT);
        input.keyDown(Input.Keys.J);
        require(!input.isAttackPressed(), "Right click and old J binding must not attack");
        buttons.clear();
        input.touchUp(0, 0, 0, Input.Buttons.RIGHT);
        input.keyUp(Input.Keys.J);
        justPressedButtons.add(Input.Buttons.LEFT);
        require(input.isAttackPressed(), "A quick click between frames must still register");
        System.out.println("PASS: WASD/arrows, event and polling paths, aliases, opposing keys, Left Shift");
    }

    private static void checkIndependentInput() throws Exception {
        MutablePlayerInput input = new MutablePlayerInput();
        Player player = new Player(0f, 0f, input);
        Input previousKeyboard = Gdx.input;
        // Any accidental device access by gameplay will fail this check.
        Gdx.input = null;
        try {
            input.x = 1f;
            input.y = 1f;
            player.update(0.5f);
            require(Math.abs(position(player).len() - 85f) < 0.01f, "Diagonal walking must stay normalized");
            input.x = 0f;
            input.y = 0f;
            Vector2 before = position(player).cpy();
            input.dash = true;
            player.update(0.1f);
            require(Math.abs(position(player).dst(before) - 30f) < 0.01f, "Dash speed must remain 300");
            require(field(player, "state") == AnimationState.DASH, "Dash must select DASH");
            input.x = -1f;
            player.update(0.1f);
            require(Math.abs(position(player).dst(before) - 60f) < 0.01f, "Dash direction must stay locked");
            input.x = 0f;
            player.update(0.3f);
            require(Math.abs(position(player).dst(before) - 60f) < 0.01f, "Holding dash must not repeat it");
            input.dash = false;
            player.update(0f);
            input.dash = true;
            input.y = -1f;
            before.set(position(player));
            player.update(0.3f);
            require(Math.abs(position(player).y - before.y + 60f) < 0.01f,
                    "Release/repress must dash in the current input direction, capped at 0.2 seconds");
            input.dash = false;
            input.y = 0f;
            player.update(0f);
            require(field(player, "state") == AnimationState.IDLE, "Dash must return to idle");
            System.out.println("PASS: device-independent input, diagonal speed, dash timing/direction/press edges");
        } finally {
            Gdx.input = previousKeyboard;
            player.dispose();
        }
    }

    private static final class MutablePlayerInput implements PlayerInput {
        float x;
        float y;
        boolean dash;

        @Override public float getMoveX() { return x; }
        @Override public float getMoveY() { return y; }
        @Override public boolean isAttackPressed() { return false; }
        @Override public boolean isDashPressed() { return dash; }
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

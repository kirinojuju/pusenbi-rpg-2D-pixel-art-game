package io.github.kirinojuju.pusenbi.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import io.github.kirinojuju.pusenbi.PusenbiGame;
import io.github.kirinojuju.pusenbi.enemy.DummyEnemy;
import io.github.kirinojuju.pusenbi.player.AnimationState;
import io.github.kirinojuju.pusenbi.player.Player;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/** Optional real-OpenGL smoke check. Captures local frames without OS input automation. */
public final class DesktopCombatChecks extends PusenbiGame {
    private int frame;
    private Player checkedPlayer;
    private DummyEnemy checkedDummy;
    private Vector2 position;
    private float movingAttackStartX;
    private Graphics fixedStepGraphics;
    private static Throwable failure;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PUSENBI combat validation");
        config.setWindowedMode(800, 600);
        config.setInitialVisible(false);
        config.disableAudio(true);
        config.useVsync(false);
        config.setForegroundFPS(120);
        new Lwjgl3Application(new DesktopCombatChecks(), config);
        if (failure != null) throw new AssertionError("Desktop combat validation failed", failure);
    }

    @Override public void create() {
        super.create();
        try {
            checkedPlayer = (Player) field(PusenbiGame.class, this, "player");
            checkedDummy = (DummyEnemy) field(PusenbiGame.class, this, "dummy");
            position = (Vector2) field(Player.class, checkedPlayer, "position");
            Graphics actual = Gdx.graphics;
            fixedStepGraphics = (Graphics) Proxy.newProxyInstance(Graphics.class.getClassLoader(),
                    new Class<?>[]{Graphics.class}, (proxy, method, arguments) ->
                            method.getName().equals("getDeltaTime") ? 0.01f : method.invoke(actual, arguments));
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }

    @Override public void render() {
        Graphics actual = Gdx.graphics;
        try {
            switch (frame) {
                case 1: mouseDown(); break;
                case 2: mouseUp(); break;
                case 61: down(Input.Keys.D); break;
                case 81: up(Input.Keys.D); break;
                case 82: down(Input.Keys.A); break;
                case 102: up(Input.Keys.A); break;
                case 103: down(Input.Keys.SHIFT_LEFT); break;
                case 104: up(Input.Keys.SHIFT_LEFT); break;
                case 131:
                    position.set(320f, 240f);
                    checkedDummy.getBounds().setPosition(350f, 246f);
                    down(Input.Keys.D); break;
                case 132: movingAttackStartX = position.x; mouseDown(); break;
                case 133: mouseUp(); break;
                case 144: up(Input.Keys.D); break;
                case 191:
                    position.set(320f, 240f);
                    checkedDummy.getBounds().setPosition(270f, 246f);
                    down(Input.Keys.A); break;
                case 192: up(Input.Keys.A); mouseDown(); break;
                case 193: mouseUp(); break;
                case 251:
                    position.set(320f, 240f);
                    checkedDummy.getBounds().setPosition(308f, 280f);
                    down(Input.Keys.W); break;
                case 252: up(Input.Keys.W); mouseDown(); break;
                case 253: mouseUp(); break;
                default: break;
            }
            Gdx.graphics = fixedStepGraphics;
            super.render();
            Gdx.graphics = actual;
            switch (frame) {
                case 0: capture("idle"); break;
                case 5:
                    require(checkedDummy.getHealth() == 100, "No wind-up damage");
                    require(checkedPlayer.getStamina() == 90f, "Mouse attack costs 10 stamina");
                    capture("wind-up"); break;
                case 12: impact("down", 75); break;
                case 22:
                    require(!checkedPlayer.getWeapon().isHitboxActive(), "No recovery hitbox");
                    capture("recovery"); break;
                case 60:
                    require(checkedPlayer.getState() == AnimationState.IDLE, "Attack returns to idle");
                    require(position.epsilonEquals(320f, 240f, 0.01f), "Attack keeps player anchor fixed");
                    break;
                case 81: require(Math.abs(position.x - 354f) < 0.01f, "Walking keeps speed 170"); break;
                case 102: require(Math.abs(position.x - 320f) < 0.01f, "Left walking unchanged"); break;
                case 110: capture("dash"); break;
                case 125: require(Math.abs(position.x - 260f) < 0.01f, "Dash still travels 60"); break;
                case 143:
                    require(Math.abs(position.x - movingAttackStartX - 10.2f) < 0.01f,
                            "Walk during attack is half speed");
                    require(Math.abs(checkedPlayer.getWeapon().getAttackBounds().x - position.x - 10f) < 0.01f,
                            "Sword hitbox follows moving player");
                    impact("right", 50); break;
                case 203: impact("left", 25); break;
                case 263: impact("up", 0); break;
                case 310:
                    require(checkedDummy.getHitCount() == 4, "Exactly one hit per swing");
                    require(checkedPlayer.getState() == AnimationState.IDLE, "Final recovery returns to idle");
                    System.out.println("PASS: real Desktop rendering, scripted mouse/Left Shift/WASD, four directional hits");
                    Gdx.app.exit(); break;
                default: break;
            }
            frame++;
        } catch (Throwable error) {
            failure = error;
            Gdx.app.exit();
        } finally {
            Gdx.graphics = actual;
        }
    }

    private void impact(String name, int health) {
        require(checkedPlayer.getState() == AnimationState.ATTACK, "Impact is in ATTACK");
        require(checkedPlayer.getWeapon().isHitboxActive(), "Impact hitbox is active");
        require(checkedDummy.getHealth() == health, "Directional hit decreases health by 25");
        capture("impact-" + name);
    }

    private void capture(String name) {
        Pixmap pixels = Pixmap.createFromFrameBuffer(0, 0, 800, 600);
        try {
            // OpenGL's framebuffer starts at the bottom; flip rows in the saved PNG.
            PixmapIO.writePNG(Gdx.files.local("build/combat-validation/" + name + ".png"), pixels, -1, true);
        } finally {
            pixels.dispose();
        }
    }

    private static void down(int key) { Gdx.input.getInputProcessor().keyDown(key); }
    private static void up(int key) { Gdx.input.getInputProcessor().keyUp(key); }
    private static void mouseDown() { Gdx.input.getInputProcessor().touchDown(400, 300, 0, Input.Buttons.LEFT); }
    private static void mouseUp() { Gdx.input.getInputProcessor().touchUp(400, 300, 0, Input.Buttons.LEFT); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static Object field(Class<?> type, Object object, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }
}

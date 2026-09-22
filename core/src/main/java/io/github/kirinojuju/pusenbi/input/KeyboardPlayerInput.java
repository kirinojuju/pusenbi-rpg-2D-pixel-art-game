package io.github.kirinojuju.pusenbi.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/** Desktop keyboard movement/dash and left mouse attack, with polling fallbacks. */
public class KeyboardPlayerInput extends InputAdapter implements PlayerInput {
    private final Input keyboard;
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean dashPressed;
    private boolean attackPressed;

    public KeyboardPlayerInput(Input keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public float getMoveX() {
        float x = 0f;
        if (leftPressed || keyboard.isKeyPressed(Input.Keys.A) || keyboard.isKeyPressed(Input.Keys.LEFT)) x -= 1f;
        if (rightPressed || keyboard.isKeyPressed(Input.Keys.D) || keyboard.isKeyPressed(Input.Keys.RIGHT)) x += 1f;
        return x;
    }

    @Override
    public float getMoveY() {
        float y = 0f;
        if (upPressed || keyboard.isKeyPressed(Input.Keys.W) || keyboard.isKeyPressed(Input.Keys.UP)) y += 1f;
        if (downPressed || keyboard.isKeyPressed(Input.Keys.S) || keyboard.isKeyPressed(Input.Keys.DOWN)) y -= 1f;
        return y;
    }

    @Override
    public boolean isAttackPressed() {
        return attackPressed || keyboard.isButtonPressed(Input.Buttons.LEFT)
                || keyboard.isButtonJustPressed(Input.Buttons.LEFT);
    }

    @Override
    public boolean isDashPressed() {
        return dashPressed || keyboard.isKeyPressed(Input.Keys.SHIFT_LEFT);
    }

    @Override
    public boolean keyDown(int keycode) {
        setKey(keycode, true);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        setKey(keycode, false);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) attackPressed = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) attackPressed = false;
        return false;
    }

    private void setKey(int keycode, boolean pressed) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) upPressed = pressed;
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) downPressed = pressed;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) leftPressed = pressed;
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) rightPressed = pressed;
        if (keycode == Input.Keys.SHIFT_LEFT) dashPressed = pressed;
    }
}

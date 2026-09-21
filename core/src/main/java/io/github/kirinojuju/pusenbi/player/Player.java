package io.github.kirinojuju.pusenbi.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/** Owns player input, position, and the visual state used by the prototype. */
public class Player extends InputAdapter {
    private static final float MOVE_SPEED = 180f;
    private static final float DASH_SPEED = 300f;
    private static final float DASH_DURATION = 0.14f;
    private static final float DRAW_WIDTH = 64f;
    private static final float DRAW_HEIGHT = 64f;

    private final Vector2 position;
    private final Vector2 dashDirection = new Vector2(0, -1);
    private final SwordsmanAnimator animator;
    private FacingDirection facing = FacingDirection.DOWN;
    // Only left/right artwork exists; retain the last side when moving vertically.
    private FacingDirection spriteFacing = FacingDirection.RIGHT;
    private AnimationState state = AnimationState.IDLE;
    private float stateTime;
    private float dashTimeRemaining;
    private boolean dashWasPressed;
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean spacePressed;

    public Player(float x, float y) {
        position = new Vector2(x, y);
        animator = new SwordsmanAnimator(DASH_DURATION);
    }

    public void update(float delta) {
        boolean dashPressed = spacePressed || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (dashPressed && !dashWasPressed && dashTimeRemaining <= 0f) {
            startDash();
        }
        dashWasPressed = dashPressed;

        if (dashTimeRemaining > 0f) {
            float dashStep = Math.min(delta, dashTimeRemaining);
            position.mulAdd(dashDirection, DASH_SPEED * dashStep);
            dashTimeRemaining = Math.max(0f, dashTimeRemaining - dashStep);
            changeState(AnimationState.DASH);
        } else {
            updateMovement(delta);
        }
        stateTime += delta;
    }

    private void updateMovement(float delta) {
        Vector2 movement = new Vector2();
        if (upPressed || Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y += 1f;
        if (downPressed || Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y -= 1f;
        if (leftPressed || Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x -= 1f;
        if (rightPressed || Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x += 1f;

        if (movement.isZero()) {
            changeState(AnimationState.IDLE);
            return;
        }
        movement.nor();
        position.mulAdd(movement, MOVE_SPEED * delta);
        dashDirection.set(movement);
        updateFacing(movement);
        changeState(AnimationState.WALK);
    }

    private void startDash() {
        Vector2 direction = new Vector2();
        if (upPressed || Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) direction.y += 1f;
        if (downPressed || Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) direction.y -= 1f;
        if (leftPressed || Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) direction.x -= 1f;
        if (rightPressed || Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direction.x += 1f;
        if (!direction.isZero()) {
            dashDirection.set(direction).nor();
            updateFacing(dashDirection);
        }
        dashTimeRemaining = DASH_DURATION;
        changeState(AnimationState.DASH);
    }

    private void updateFacing(Vector2 direction) {
        if (direction.x != 0f) {
            spriteFacing = direction.x < 0f ? FacingDirection.LEFT : FacingDirection.RIGHT;
        }
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            facing = direction.x < 0f ? FacingDirection.LEFT : FacingDirection.RIGHT;
        } else {
            facing = direction.y < 0f ? FacingDirection.DOWN : FacingDirection.UP;
        }
    }

    private void changeState(AnimationState nextState) {
        if (state != nextState) {
            state = nextState;
            stateTime = 0f;
        }
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

    private void setKey(int keycode, boolean pressed) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) upPressed = pressed;
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) downPressed = pressed;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) leftPressed = pressed;
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) rightPressed = pressed;
        if (keycode == Input.Keys.SPACE) spacePressed = pressed;
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame = animator.getFrame(state, spriteFacing, stateTime);
        // Position is the feet anchor: every frame is drawn upward from the same point.
        batch.draw(frame, position.x - DRAW_WIDTH / 2f, position.y, DRAW_WIDTH, DRAW_HEIGHT);
    }

    public void dispose() {
        animator.dispose();
    }

    public FacingDirection getFacing() {
        return facing;
    }
}

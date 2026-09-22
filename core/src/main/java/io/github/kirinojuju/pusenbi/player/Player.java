package io.github.kirinojuju.pusenbi.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.kirinojuju.pusenbi.input.PlayerInput;
import io.github.kirinojuju.pusenbi.combat.Damageable;
import io.github.kirinojuju.pusenbi.combat.Sword;
import io.github.kirinojuju.pusenbi.combat.Weapon;
import java.util.Collections;

/** Owns player movement, position, and visual state, using device-independent input. */
public class Player {
    private static final float MOVE_SPEED = 170f;
    private static final float ATTACK_MOVE_SPEED = MOVE_SPEED * 0.5f;
    private static final float DASH_SPEED = 300f;
    private static final float DASH_DURATION = 0.2f;
    private static final float DRAW_WIDTH = 64f;
    private static final float DRAW_HEIGHT = 64f;

    private final Vector2 position;
    private final PlayerInput input;
    private final Vector2 dashDirection = new Vector2(0, -1);
    private final SwordsmanAnimator animator;
    private final Weapon weapon;
    private final Stamina stamina = new Stamina();
    private FacingDirection facing = FacingDirection.DOWN;
    // Only left/right artwork exists; retain the last side when moving vertically.
    private FacingDirection spriteFacing = FacingDirection.RIGHT;
    private AnimationState state = AnimationState.IDLE;
    private float stateTime;
    private float dashTimeRemaining;
    private boolean dashWasPressed;
    private boolean attackWasPressed;

    public Player(float x, float y, PlayerInput input) {
        this.input = input;
        position = new Vector2(x, y);
        animator = new SwordsmanAnimator(DASH_DURATION);
        weapon = new Sword();
    }

    public void update(float delta) {
        update(delta, Collections.<Damageable>emptyList());
    }

    public void update(float delta, Iterable<? extends Damageable> targets) {
        stamina.update(delta, !weapon.isAttacking());
        boolean dashPressed = input.isDashPressed();
        boolean attackPressed = input.isAttackPressed();
        if (dashPressed && !dashWasPressed && dashTimeRemaining <= 0f) {
            weapon.cancel();
            startDash();
        } else if (attackPressed && !attackWasPressed && dashTimeRemaining <= 0f
                && !weapon.isAttacking() && stamina.trySpend(weapon.getStaminaCost())) {
            weapon.start(position, facing);
            changeState(AnimationState.ATTACK);
        }
        dashWasPressed = dashPressed;
        attackWasPressed = attackPressed;

        if (dashTimeRemaining > 0f) {
            float dashStep = Math.min(delta, dashTimeRemaining);
            position.mulAdd(dashDirection, DASH_SPEED * dashStep);
            dashTimeRemaining = Math.max(0f, dashTimeRemaining - dashStep);
            changeState(AnimationState.DASH);
        } else if (weapon.isAttacking()) {
            updateAttack(delta, targets);
            stateTime = weapon.getElapsed();
            // Resume the appropriate state at recovery end.
            if (!weapon.isAttacking()) updateMovement(0f);
            return;
        } else {
            updateMovement(delta);
        }
        stateTime += delta;
    }

    private void updateAttack(float delta, Iterable<? extends Damageable> targets) {
        Vector2 movement = new Vector2(input.getMoveX(), input.getMoveY());
        if (!movement.isZero()) {
            movement.nor();
            dashDirection.set(movement);
        }
        float remaining = delta;
        // Split at phase boundaries: recovery movement must not move an earlier impact.
        while (remaining > 0f && weapon.isAttacking()) {
            float step = Math.min(remaining, weapon.timeUntilNextPhase());
            position.mulAdd(movement, ATTACK_MOVE_SPEED * step);
            weapon.follow(position);
            weapon.update(step, targets);
            remaining -= step;
        }
    }

    private void updateMovement(float delta) {
        Vector2 movement = new Vector2(input.getMoveX(), input.getMoveY());

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
        Vector2 direction = new Vector2(input.getMoveX(), input.getMoveY());
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

    public void render(SpriteBatch batch) {
        // Upward swings pass behind the body; other directions render in front.
        if (facing == FacingDirection.UP) weapon.render(batch);
        TextureRegion frame = animator.getFrame(state, spriteFacing, stateTime);
        // Position is the feet anchor: every frame is drawn upward from the same point.
        batch.draw(frame, position.x - DRAW_WIDTH / 2f, position.y, DRAW_WIDTH, DRAW_HEIGHT);
        if (facing != FacingDirection.UP) weapon.render(batch);
    }

    public void dispose() {
        animator.dispose();
        weapon.dispose();
    }

    public FacingDirection getFacing() {
        return facing;
    }

    public AnimationState getState() { return state; }
    public Weapon getWeapon() { return weapon; }
    public float getStamina() { return stamina.getCurrent(); }
}

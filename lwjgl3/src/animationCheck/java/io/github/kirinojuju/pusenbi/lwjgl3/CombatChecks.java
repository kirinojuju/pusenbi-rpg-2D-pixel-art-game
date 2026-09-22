package io.github.kirinojuju.pusenbi.lwjgl3;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.kirinojuju.pusenbi.combat.Sword;
import io.github.kirinojuju.pusenbi.enemy.DummyEnemy;
import io.github.kirinojuju.pusenbi.input.PlayerInput;
import io.github.kirinojuju.pusenbi.player.AnimationState;
import io.github.kirinojuju.pusenbi.player.FacingDirection;
import io.github.kirinojuju.pusenbi.player.Player;
import io.github.kirinojuju.pusenbi.player.Stamina;

/** Runs inside AnimationChecks' existing graphics boundary, using packaged sword artwork. */
public final class CombatChecks {
    public static void run() {
        checkTimingAndTargets();
        checkDirections();
        checkPlayerTransitions();
        checkStamina();
        checkMovingAttack();
        System.out.println("PASS: sword timing, all directions, single-hit damage, recovery, dash priority/cancel");
    }

    private static void checkTimingAndTargets() {
        Sword sword = new Sword();
        DummyEnemy dummy = new DummyEnemy(40f, 6f);
        DummyEnemy second = new DummyEnemy(42f, 6f);
        Array<DummyEnemy> targets = Array.with(dummy, second, dummy);
        try {
            sword.start(new Vector2(), FacingDirection.RIGHT);
            sword.update(0.08f, targets);
            require(dummy.getHealth() == 100 && !sword.isHitboxActive(), "Wind-up cannot damage");
            sword.update(0.02f, targets);
            require(sword.isHitboxActive(), "Impact must activate hitbox");
            require(dummy.getHealth() == 75 && second.getHealth() == 75, "Every overlapping target takes 25");
            for (int i = 0; i < 4; i++) sword.update(0.01f, targets);
            require(dummy.getHitCount() == 1, "Repeated impact frames/duplicate target references hit only once");
            sword.update(0.02f, targets);
            DummyEnemy lateTarget = new DummyEnemy(40f, 6f);
            targets.add(lateTarget);
            require(!sword.isHitboxActive() && sword.isAttacking(), "Recovery has no hitbox");
            sword.update(0.24f, targets);
            require(lateTarget.getHealth() == 100 && !sword.isAttacking(), "Recovery cannot damage a new target");
            for (int i = 0; i < 3; i++) {
                sword.start(new Vector2(), FacingDirection.RIGHT);
                sword.update(0.75f, targets);
            }
            require(dummy.getHealth() == 0 && dummy.getHitCount() == 4, "Four separate swings defeat dummy");
            sword.start(new Vector2(), FacingDirection.RIGHT);
            sword.update(0.75f, targets);
            require(dummy.getHitCount() == 4, "Defeated targets cannot take more hits");
            sword.start(new Vector2(), FacingDirection.RIGHT);
            sword.cancel();
            DummyEnemy afterCancel = new DummyEnemy(40f, 6f);
            sword.update(0.5f, Array.with(afterCancel));
            require(afterCancel.getHealth() == 100 && !sword.isHitboxActive(), "Cancel disables damage");
        } finally {
            sword.dispose();
        }
    }

    private static void checkDirections() {
        Sword sword = new Sword();
        Vector2 position = new Vector2(100f, 100f);
        try {
            for (FacingDirection direction : FacingDirection.values()) {
                DummyEnemy right = new DummyEnemy(144f, 106f);
                DummyEnemy left = new DummyEnemy(56f, 106f);
                DummyEnemy up = new DummyEnemy(100f, 140f);
                DummyEnemy down = new DummyEnemy(100f, 50f);
                Array<DummyEnemy> targets = Array.with(right, left, up, down);
                sword.start(position, direction);
                Rectangle box = sword.getAttackBounds();
                boolean horizontal = direction == FacingDirection.LEFT || direction == FacingDirection.RIGHT;
                require(box.width == (horizontal ? 46f : 32f)
                        && box.height == (horizontal ? 32f : 46f), "Directional hitbox dimensions");
                sword.update(0.2f, targets);
                DummyEnemy expected = direction == FacingDirection.RIGHT ? right
                        : direction == FacingDirection.LEFT ? left : direction == FacingDirection.UP ? up : down;
                for (DummyEnemy target : targets) {
                    require(target.getHealth() == (target == expected ? 75 : 100), "Only facing-side dummy is hit");
                }
                sword.cancel();
            }
        } finally {
            sword.dispose();
        }
    }

    private static void checkPlayerTransitions() {
        Controls input = new Controls();
        Player player = new Player(0f, 0f, input);
        DummyEnemy dummy = new DummyEnemy(40f, 6f);
        Array<DummyEnemy> targets = Array.with(dummy);
        try {
            input.x = 1f;
            player.update(0f, targets); // Face right without changing position.
            input.x = 0f;
            input.attack = true;
            player.update(0.05f, targets);
            require(player.getState() == AnimationState.ATTACK, "Click starts ATTACK");
            Rectangle lockedBounds = new Rectangle(player.getWeapon().getAttackBounds());
            input.x = -1f;
            player.update(0.15f, targets);
            require(player.getFacing() == FacingDirection.RIGHT, "Attack locks current facing");
            require(Math.abs(player.getWeapon().getAttackBounds().x - lockedBounds.x + 12.75f) < 0.01f,
                    "Attack movement is 85 px/s and hitbox follows player");
            require(dummy.getHealth() == 75, "Player attack damages dummy");
            input.attack = false;
            player.update(0.01f, targets);
            input.attack = true;
            player.update(0.01f, targets);
            require(player.getWeapon().getElapsed() > 0.2f, "Repress cannot restart an unfinished attack");
            player.update(0.3f, targets);
            require(player.getState() == AnimationState.WALK, "Recovery returns to WALK when movement is held");
            input.x = 0f;
            player.update(0.1f, targets);
            require(player.getState() == AnimationState.IDLE && dummy.getHitCount() == 1,
                    "Held mouse cannot repeat or queue another swing");
            input.attack = false;
            player.update(0f, targets);
            input.attack = true;
            player.update(0.01f, targets);
            input.dash = true;
            player.update(0.01f, targets);
            require(player.getState() == AnimationState.DASH && !player.getWeapon().isAttacking(),
                    "Fresh dash cancels attack");
            input.attack = false;
            player.update(0.01f, targets);
            input.attack = true;
            player.update(0.01f, targets);
            require(player.getState() == AnimationState.DASH, "Attack cannot interrupt dash");
            player.update(0.3f, targets);
            player.update(0.01f, targets);
            require(player.getState() == AnimationState.IDLE, "Attack during dash is not queued");
            input.dash = false;
            input.attack = false;
            player.update(0f, targets);
            input.dash = true;
            input.attack = true;
            player.update(0.01f, targets);
            require(player.getState() == AnimationState.DASH, "Simultaneous presses prioritize dash");
            player.update(0.3f, targets);
            input.dash = false;
            input.attack = false;
            player.update(0f, targets);
            input.attack = true;
            player.update(0.6f, targets);
            require(player.getState() == AnimationState.IDLE, "Completed stationary attack returns to IDLE");
        } finally {
            player.dispose();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void checkStamina() {
        Stamina resource = new Stamina();
        require(resource.trySpend(10f) && resource.getCurrent() == 90f, "Spending stamina");
        resource.update(0.5f, true);
        require(resource.getCurrent() == 90f, "Regeneration waits one second");
        resource.update(0.75f, true);
        require(Math.abs(resource.getCurrent() - 95f) < 0.001f, "Only time beyond delay regenerates at 20/s");
        resource.update(10f, true);
        require(resource.getCurrent() == 100f, "Stamina cannot exceed 100");

        Controls input = new Controls();
        Player player = new Player(0f, 0f, input);
        try {
            for (int i = 0; i < 10; i++) {
                input.attack = false;
                player.update(0f);
                input.attack = true;
                player.update(0.01f);
                require(player.getState() == AnimationState.ATTACK, "Affordable attack must start");
                require(player.getStamina() == 90f - i * 10f, "Charge exactly 10 SP per accepted attack");
                input.attack = false;
                player.update(0.01f);
                input.attack = true;
                player.update(0.01f);
                require(player.getStamina() == 90f - i * 10f, "Rejected recovery presses do not spend SP");
                player.update(0.28f);
            }
            input.attack = false;
            player.update(0f);
            input.attack = true;
            player.update(0f);
            require(player.getState() == AnimationState.IDLE && player.getStamina() == 0f,
                    "Empty stamina prevents an attack without going negative");
            input.dash = true;
            player.update(0.01f);
            require(player.getState() == AnimationState.DASH, "Empty attack stamina must not block dash");
            input.dash = false;
            input.attack = false;
            player.update(0.3f);
            player.update(2f);
            require(player.getStamina() >= 10f, "Stamina regenerates after combat");
            input.attack = true;
            player.update(0f);
            require(player.getState() == AnimationState.ATTACK, "Recovered stamina allows another click");
            player.update(0.31f);
            player.update(10f);
            require(player.getStamina() == 100f, "Player regeneration caps at max");
        } finally {
            player.dispose();
        }
        System.out.println("PASS: stamina cost, exhaustion, recovery, cap, and unchanged dash availability");
    }

    private static void checkMovingAttack() {
        Controls input = new Controls();
        Player player = new Player(0f, 0f, input);
        try {
            input.attack = true;
            player.update(0f);
            Rectangle before = new Rectangle(player.getWeapon().getAttackBounds());
            input.x = 1f;
            input.y = 1f;
            player.update(0.12f);
            Rectangle after = player.getWeapon().getAttackBounds();
            require(Math.abs(new Vector2(after.x - before.x, after.y - before.y).len() - 10.2f) < 0.01f,
                    "Diagonal movement during a swing stays normalized at half speed");
            require(player.getFacing() == FacingDirection.DOWN && player.getWeapon().isHitboxActive(),
                    "Moving attack retains facing and impact state");
            player.update(0.2f);
            require(player.getState() == AnimationState.WALK, "Moving recovery returns to WALK");
        } finally {
            player.dispose();
        }
    }

    private static final class Controls implements PlayerInput {
        float x;
        float y;
        boolean attack;
        boolean dash;
        @Override public float getMoveX() { return x; }
        @Override public float getMoveY() { return y; }
        @Override public boolean isAttackPressed() { return attack; }
        @Override public boolean isDashPressed() { return dash; }
    }
}

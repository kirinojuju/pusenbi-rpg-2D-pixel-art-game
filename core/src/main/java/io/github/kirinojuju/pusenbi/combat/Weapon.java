package io.github.kirinojuju.pusenbi.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.kirinojuju.pusenbi.player.FacingDirection;

/** A single timed melee swing, with identity-based per-target hit tracking. */
public abstract class Weapon implements Disposable {
    private final int damage;
    private final int staminaCost;
    private final float windUp;
    private final float activeDuration;
    private final float recovery;
    private final Array<Damageable> hitTargets = new Array<>();
    protected final Rectangle attackBounds = new Rectangle();
    protected final Vector2 origin = new Vector2();
    protected FacingDirection facing = FacingDirection.DOWN;
    private float elapsed;
    private boolean attacking;

    protected Weapon(int damage, int staminaCost, float windUp, float activeDuration, float recovery) {
        this.damage = damage;
        this.staminaCost = staminaCost;
        this.windUp = windUp;
        this.activeDuration = activeDuration;
        this.recovery = recovery;
    }

    public void start(Vector2 position, FacingDirection direction) {
        if (attacking) return;
        origin.set(position);
        facing = direction;
        elapsed = 0f;
        attacking = true;
        hitTargets.clear();
        updateBounds();
    }

    public void update(float delta, Iterable<? extends Damageable> targets) {
        if (!attacking) return;
        float next = Math.min(elapsed + delta, getDuration());
        // Intersect the elapsed interval with impact, so a slow frame cannot skip a hit.
        if (next > elapsed && elapsed < windUp + activeDuration && next >= windUp) {
            for (Damageable target : targets) {
                if (target.isAlive() && !hitTargets.contains(target, true)
                        && attackBounds.overlaps(target.getBounds())) {
                    hitTargets.add(target);
                    target.takeDamage(damage);
                }
            }
        }
        elapsed = next;
        if (elapsed >= getDuration()) attacking = false;
    }

    public void cancel() {
        attacking = false;
        hitTargets.clear();
    }

    /** Follow the wielder without changing the direction chosen at swing start. */
    public void follow(Vector2 position) {
        origin.set(position);
        updateBounds();
    }

    /** Lets moving attacks process impact before any recovery movement in a slow frame. */
    public float timeUntilNextPhase() {
        if (elapsed < windUp) return windUp - elapsed;
        if (elapsed < windUp + activeDuration) return windUp + activeDuration - elapsed;
        return getDuration() - elapsed;
    }

    public boolean isAttacking() { return attacking; }
    public boolean isHitboxActive() {
        return attacking && elapsed >= windUp && elapsed < windUp + activeDuration;
    }
    /** Read-only view for collision/debug rendering; valid damage only during impact. */
    public Rectangle getAttackBounds() { return attackBounds; }
    public float getElapsed() { return elapsed; }
    public float getDuration() { return windUp + activeDuration + recovery; }
    public float getWindUp() { return windUp; }
    public float getActiveDuration() { return activeDuration; }
    public int getDamage() { return damage; }
    public int getStaminaCost() { return staminaCost; }

    protected abstract void updateBounds();
    public abstract void render(SpriteBatch batch);
}

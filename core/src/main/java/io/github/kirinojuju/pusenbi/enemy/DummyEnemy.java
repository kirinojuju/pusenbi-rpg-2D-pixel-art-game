package io.github.kirinojuju.pusenbi.enemy;

import com.badlogic.gdx.math.Rectangle;
import io.github.kirinojuju.pusenbi.combat.Damageable;

/** Stationary combat target. Rendering and diagnostics belong to the prototype scene. */
public final class DummyEnemy implements Damageable {
    public static final int MAX_HEALTH = 100;
    private final Rectangle bounds;
    private int health = MAX_HEALTH;
    private int hitCount;

    /** x is the horizontal center; y is the feet anchor. */
    public DummyEnemy(float x, float y) {
        bounds = new Rectangle(x - 12f, y, 24f, 40f);
    }

    @Override public Rectangle getBounds() { return bounds; }
    @Override public boolean isAlive() { return health > 0; }
    @Override public void takeDamage(int damage) {
        if (damage <= 0 || !isAlive()) return;
        health = Math.max(0, health - damage);
        hitCount++;
    }

    public int getHealth() { return health; }
    public int getHitCount() { return hitCount; }
}

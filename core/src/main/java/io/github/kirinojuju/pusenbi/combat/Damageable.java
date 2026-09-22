package io.github.kirinojuju.pusenbi.combat;

import com.badlogic.gdx.math.Rectangle;

/** The small target contract needed by a melee weapon. */
public interface Damageable {
    Rectangle getBounds();
    boolean isAlive();
    void takeDamage(int damage);
}

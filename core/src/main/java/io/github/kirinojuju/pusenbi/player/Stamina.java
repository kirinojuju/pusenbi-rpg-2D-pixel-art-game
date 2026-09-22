package io.github.kirinojuju.pusenbi.player;

/** Attack resource with a short delay before regeneration. */
public final class Stamina {
    public static final float MAX = 100f;
    public static final float REGEN_PER_SECOND = 20f;
    public static final float REGEN_DELAY = 1f;
    private float current = MAX;
    private float delayRemaining;

    public boolean trySpend(float amount) {
        if (amount <= 0f || current < amount) return false;
        current -= amount;
        delayRemaining = REGEN_DELAY;
        return true;
    }

    public void update(float delta, boolean canRegenerate) {
        float regenerationTime = Math.max(0f, delta - delayRemaining);
        delayRemaining = Math.max(0f, delayRemaining - delta);
        if (canRegenerate) current = Math.min(MAX, current + regenerationTime * REGEN_PER_SECOND);
    }

    public float getCurrent() { return current; }
}

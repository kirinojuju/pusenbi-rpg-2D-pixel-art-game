package io.github.kirinojuju.pusenbi.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** Existing sword artwork, rotated around its grip as a separate layer. */
public final class Sword extends Weapon {
    public static final float WIND_UP = 0.09f;
    public static final float ACTIVE = 0.06f;
    public static final float RECOVERY = 0.15f;
    public static final int STAMINA_COST = 10;
    public static final int DAMAGE = 25;
    private final Texture texture;
    private final TextureRegion region;

    public Sword() {
        super(DAMAGE, STAMINA_COST, WIND_UP, ACTIVE, RECOVERY);
        texture = new Texture(Gdx.files.internal("characters/kaito/weapons/normal_sword.png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        region = new TextureRegion(texture);
    }

    @Override
    protected void updateBounds() {
        switch (facing) {
            case LEFT: attackBounds.set(origin.x - 56f, origin.y + 6f, 46f, 32f); break;
            case RIGHT: attackBounds.set(origin.x + 10f, origin.y + 6f, 46f, 32f); break;
            case UP: attackBounds.set(origin.x - 16f, origin.y + 28f, 32f, 46f); break;
            default: attackBounds.set(origin.x - 16f, origin.y - 28f, 32f, 46f); break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isAttacking()) return;
        float x = origin.x;
        float y = origin.y;
        float direction;
        switch (facing) {
            case LEFT: x -= 10f; y += 22f; direction = 180f; break;
            case RIGHT: x += 10f; y += 22f; direction = 0f; break;
            case UP: y += 30f; direction = 90f; break;
            default: y += 18f; direction = -90f; break;
        }
        float time = getElapsed();
        float angle;
        if (time < WIND_UP) {
            angle = MathUtils.lerp(-70f, -55f, time / WIND_UP);
        } else if (time < WIND_UP + ACTIVE) {
            angle = MathUtils.lerp(-55f, 55f, (time - WIND_UP) / ACTIVE);
        } else {
            angle = MathUtils.lerp(55f, 70f, (time - WIND_UP - ACTIVE) / RECOVERY);
        }
        // Source is 64x12, drawn at 75%; source grip (6,6) becomes (4.5,4.5).
        batch.draw(region, x - 4.5f, y - 4.5f, 4.5f, 4.5f,
                48f, 9f, 1f, 1f, direction + angle);
    }

    @Override public void dispose() { texture.dispose(); }
}

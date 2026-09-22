package io.github.kirinojuju.pusenbi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.kirinojuju.pusenbi.enemy.DummyEnemy;
import io.github.kirinojuju.pusenbi.input.KeyboardPlayerInput;
import io.github.kirinojuju.pusenbi.player.Player;
import io.github.kirinojuju.pusenbi.player.Stamina;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PusenbiGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Player player;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private final Array<DummyEnemy> targets = new Array<>();
    private DummyEnemy dummy;
    private int lastHitCount;

    @Override
    public void create() {
        batch = new SpriteBatch();
        KeyboardPlayerInput input = new KeyboardPlayerInput(Gdx.input);
        player = new Player(320f, 240f, input);
        Gdx.input.setInputProcessor(input);
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        // Starts in range of Kaito's initial downward-facing attack.
        dummy = new DummyEnemy(320f, 188f);
        targets.add(dummy);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        player.update(Gdx.graphics.getDeltaTime(), targets);
        if (dummy.getHitCount() != lastHitCount) {
            lastHitCount = dummy.getHitCount();
            Gdx.app.log("Combat", "Sword hit " + lastHitCount + ": dummy HP "
                    + dummy.getHealth() + "/" + DummyEnemy.MAX_HEALTH);
        }
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Rectangle bounds = dummy.getBounds();
        if (dummy.isAlive()) shapes.setColor(0.65f, 0.4f, 0.2f, 1f);
        else shapes.setColor(0.25f, 0.25f, 0.25f, 1f);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapes.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapes.rect(bounds.x - 8f, bounds.y + bounds.height + 8f, 40f, 4f);
        shapes.setColor(0.25f, 0.9f, 0.35f, 1f);
        shapes.rect(bounds.x - 8f, bounds.y + bounds.height + 8f,
                40f * dummy.getHealth() / DummyEnemy.MAX_HEALTH, 4f);
        shapes.setColor(0.15f, 0.2f, 0.2f, 1f);
        shapes.rect(16f, 476f, 200f, 8f);
        shapes.setColor(0.2f, 0.8f, 0.85f, 1f);
        shapes.rect(16f, 476f, 200f * player.getStamina() / Stamina.MAX, 8f);
        shapes.end();
        batch.begin();
        player.render(batch);
        font.draw(batch, "WASD / arrows: move   Left Shift: dash   Left click: sword", 16f, 580f);
        font.draw(batch, "Dummy HP: " + dummy.getHealth() + "/" + DummyEnemy.MAX_HEALTH
                + "   Hits: " + dummy.getHitCount(), 16f, 558f);
        font.draw(batch, "State: " + player.getState()
                + "   Impact: " + player.getWeapon().isHitboxActive(), 16f, 536f);
        font.draw(batch, "Stamina: " + (int) player.getStamina() + "/100   Attack: "
                + player.getWeapon().getStaminaCost() + " SP", 16f, 510f);
        if (player.getStamina() < player.getWeapon().getStaminaCost()) {
            font.draw(batch, "Not enough stamina", 230f, 486f);
        }
        if (!dummy.isAlive()) font.draw(batch, "Dummy defeated - restart to reset", 16f, 454f);
        batch.end();
        if (player.getWeapon().isHitboxActive()) {
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(1f, 0.85f, 0.2f, 1f);
            Rectangle hitbox = player.getWeapon().getAttackBounds();
            shapes.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
            shapes.end();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        shapes.dispose();
        font.dispose();
    }
}

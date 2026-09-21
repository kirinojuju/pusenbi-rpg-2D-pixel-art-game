package io.github.kirinojuju.pusenbi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.kirinojuju.pusenbi.player.Player;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PusenbiGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Player player;

    @Override
    public void create() {
        batch = new SpriteBatch();
        player = new Player(320f, 240f);
        Gdx.input.setInputProcessor(player);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        player.update(Gdx.graphics.getDeltaTime());
        batch.begin();
        player.render(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
    }
}

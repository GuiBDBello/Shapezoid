package com.mygdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.game.MainMenu;
import com.mygdx.game.Shapezoid;

/**
 * @author GuiDB
 */
public class ScreenMainMenu implements Screen {

    final MainMenu game;
    OrthographicCamera camera;

    public ScreenMainMenu(final MainMenu game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
    }

    @Override
    public void render(float delta) {
        // Limpa a tela;
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Projeta a nova cena na tela/câmera a cada frame;
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // Define elementos em posições da tela;
        game.batch.begin();
        game.font.draw(game.batch, "Hello World! ", 100, 150);
        game.font.draw(game.batch, "Tap anywhere to begin!", 100, 100);
        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new Shapezoid(game));
            dispose();
        }
    }
    
    // ######################### UNUSED #########################;

    @Override
    public void show() {
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}

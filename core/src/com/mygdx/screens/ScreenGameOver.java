/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Shapezoid;

/**
 *
 * @author guilh
 */
public class ScreenGameOver implements Screen {

    final Shapezoid game;

    OrthographicCamera camera;
    SpriteBatch spriteBatch;
    BitmapFont bitmapFont;
    float score, highscore;

    public ScreenGameOver(final Shapezoid game, float score, float highscore) {
        this.game = game;
        this.score = score;
        this.highscore = highscore;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 720, 720);
        
        spriteBatch = new SpriteBatch();
        bitmapFont = new BitmapFont();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float f) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.spriteBatch.setProjectionMatrix(camera.combined);

        

        if (Gdx.input.isTouched()) {
            game.setScreen(new ScreenGame(game));
            dispose();
        }
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
        spriteBatch.dispose();
        bitmapFont.dispose();
    }
}

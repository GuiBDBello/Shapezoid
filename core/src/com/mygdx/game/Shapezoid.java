package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.screens.ScreenGame;

/**
 * @author GuiDB
 */
public class Shapezoid extends Game {

    public SpriteBatch spriteBatch;
    public BitmapFont bitmapFont;

    public void create() {
        spriteBatch = new SpriteBatch();
        //Use LibGDX's default Arial font.
        bitmapFont = new BitmapFont();
        this.setScreen(new ScreenGame(this));
    }

    public void render() {
        super.render(); //important!
    }

    public void dispose() {
        spriteBatch.dispose();
        bitmapFont.dispose();
    }
}

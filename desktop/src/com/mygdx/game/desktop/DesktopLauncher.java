package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MainMenu;
import com.mygdx.game.Shapezoid;
import com.mygdx.game.BulletTest;
import com.mygdx.game.BulletTest2;

public class DesktopLauncher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Shapezoid";
        config.width = 1280;
        config.height = 720;
        new LwjglApplication(new Shapezoid(), config);
    }
}
package com.mygdx.tetris.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.tetris.Tetris;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 800;
        config.height = 800;
        config.resizable = false;
        config.addIcon("icon/icon.png", Files.FileType.Local);
		new LwjglApplication(new Tetris(), config);
	}
}

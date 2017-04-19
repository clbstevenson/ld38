package com.exovum.ludumdare;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by exovu_000 on 04/10/2017
 *
 * LDGame - Setup Game class for Ludum Dare events
 * Includes basic setup for: screens, assets, sprites, etc
 */

public class LDGame extends Game {

    SpriteBatch batch;
    AssetManager am;

    //public static ScreenDispatcherWarmup screenManager;
    public static ScreenManager screenManager;

    @Override
    public void create() {
        // Load the assets before hand, and setup AssetManager to access later
        am = Assets.load();
        batch = new SpriteBatch();
        screenManager = new ScreenManager();
        // loading screen to make sure assets are loaded
        Screen splashScreen = new SplashScreen(batch, screenManager);
        Screen sampleGameScreen = new SampleGameScreen(this, batch, screenManager);
//        Screen gameScreen = new GameScreenWarmup(this, batch, screenManager);
//        Screen schoolScreen = new SchoolGameScreen(this, batch, screenManager);
        Screen gameoverScreen = new GameOverScreen(this, batch, screenManager);
        screenManager.AddScreen(splashScreen);
        screenManager.AddScreen(sampleGameScreen);
        // Old GameScreen
        // screenManager.AddScreen(gameScreen);
//        screenManager.AddScreen(schoolScreen);
        screenManager.AddScreen(gameoverScreen);
        setScreen(splashScreen);
    }

    @Override
    public void render () {
        float r = 0/255f;
        float g = 24f/255f;
        float b = 72f/255f;
        Gdx.gl.glClearColor(r, g, b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Screen nextScreen = screenManager.getNextScreen();
        if(nextScreen != getScreen()){
            setScreen(nextScreen);
            Gdx.app.log("LDGame", "Switching screens");
        }

        super.render();
    }

    @Override
    public void dispose() {
        Gdx.app.log("LDGame", "Disposing of assets");
        am.dispose(); // am.clear();
    }

}

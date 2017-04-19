package com.exovum.ludumdare;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.exovum.ludumdare.systems.AnimationSystem;
import com.exovum.ludumdare.systems.CollisionSystem;
import com.exovum.ludumdare.systems.FontSystem;
import com.exovum.ludumdare.systems.PhysicsDebugSystem;
import com.exovum.ludumdare.systems.PhysicsSystem;
import com.exovum.ludumdare.systems.RenderingSystem;

/**
 * Created by exovu_000 on 12/9/2016.
 */

public class MenuScreen extends ScreenAdapter {

    Game game; // May need to change this to LD37Game type. Maybe not though

    private boolean initialized;
    private boolean paused;
    private float elapsedTime = 0f;

    OrthographicCamera camera;

    private SpriteBatch batch;
    private IScreenManager manager; // TODO change this simple screen switcher or don't use it

    private InputMultiplexer multiplexer;

    // font related items. glyphlayout makes it easier to draw and center fonts and other stuff probably
    // Font rendering should be processed as FontComponent so the ECS RenderingSystem can grab it

    /**
     * Game-Specific data
     */
    private World box2dWorld; // Box2D World
    private GameWorld gameWorld;
    private PooledEngine engine; // Ashley ECS engine

    public MenuScreen(Game game, SpriteBatch batch, IScreenManager manager) {
        this.game = game;
        this.batch = batch;
        this.manager = manager;
    }

    private void init() {
        Gdx.app.log("MenuScreen", "Initializing Menu Screen");

        // new Box2D box2dWorld with no gravity
        box2dWorld = new World(new Vector2(0f, 0f), true);
        engine = new PooledEngine();
        // DONE: change this game box2dWorld - changed to School World
        gameWorld = new GameWorld(engine, box2dWorld);

        // create ECS system to process rendering
        RenderingSystem renderingSystem = new RenderingSystem(batch);
        engine.addSystem(renderingSystem);
        engine.addSystem(new FontSystem(batch, engine));//batch, engine));
        engine.addSystem(new AnimationSystem());//batch, engine));
        // add ECS system to process physics in the Box2D box2dWorld
        engine.addSystem(new PhysicsSystem(box2dWorld));

        CollisionSystem collisionSystem = new CollisionSystem(gameWorld, box2dWorld);
        engine.addSystem(collisionSystem);
        box2dWorld.setContactListener(collisionSystem);

        // TODO: eventually remove these systems. they are just used for testing
        engine.addSystem(new PhysicsDebugSystem(box2dWorld, renderingSystem.getCamera()));
        //engine.addSystem(new UselessStateSwapSystem());

        // DONE: changed to school box2dWorld
        //gameWorld.create();

        camera = engine.getSystem(RenderingSystem.class).getCamera();

        // load fonts
        //mediumFont = Assets.getMediumFont();

        // Create all input devices and add to the multiplexer
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new GameInput(camera, gameWorld));
        // Set Gdx.input to track all the created input handlers
        Gdx.input.setInputProcessor(multiplexer);


        // set initialized if everything else was a success
        initialized = true;
    }

    private void update(float delta) {
        engine.update(delta);
        gameWorld.update(delta);

        elapsedTime += delta;

//        if(gameWorld.state == SampleSchoolWorld.State.GAMEOVER) {
//            manager.endCurrentScreen();
//            // Game is over, so reset the data to be used later.
//            gameWorld.state = SampleSchoolWorld.State.RUNNING;
//            gameWorld.reset();
//        }
        // Use a FontComponent to render text. It will be processed by RenderingSystem
        //glyphLayout.setText(Assets.getMediumFont(), "Time: " + elapsedTime);
        //mediumFont.draw(batch, glyphLayout, )
        //Assets.getMediumFont().draw(batch, glyphLayout, camera.position.x - glyphLayout.width / 2,
        //        camera.viewportHeight / 2);
        // can trigger stuff based on elapsedTime [children etc]
    }

    @Override
    public void render(float delta) {
        // If the Screen is not paused, then continue updating everything [engine, box2dWorld, etc]
        //if(!paused) {
            // if everything is ready, then update. otherwise need to setup game box2dWorld
            if (initialized) {
                update(delta);
            } else {
                init();
            }
        //}
    }

    private void setEngineOn(boolean turnOn) {


        if(engine != null) {
            Gdx.app.log("Menu Screen", "Setting engine to " + turnOn);
            gameWorld.pause(!turnOn);
            // If turning on the engine, set processing to true. otherwise turn them false
            if (engine.getSystem(PhysicsSystem.class) != null)
                engine.getSystem(PhysicsSystem.class).setProcessing(turnOn);
            if (engine.getSystem(PhysicsDebugSystem.class) != null)
                engine.getSystem(PhysicsDebugSystem.class).setProcessing(turnOn);
            if (engine.getSystem(CollisionSystem.class) != null)
                engine.getSystem(CollisionSystem.class).setProcessing(turnOn);
            if (engine.getSystem(AnimationSystem.class) != null)
                engine.getSystem(AnimationSystem.class).setProcessing(turnOn);
        }
    }

    @Override
    public void hide() {
        paused = true;

        setEngineOn(false);
    }

    @Override
    public void show() {
        Gdx.app.log("Menu Screen", "Switched to MenuScreen");
        paused = false;
        setEngineOn(true);
        // Set Gdx.input to track all the created input handlers
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void pause() {
        paused = true;
        setEngineOn(false);
    }

    @Override
    public void resume() {
        paused = false;
        setEngineOn(true);
    }

}

package com.exovum.ludumdare;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

public class SampleGameScreen extends ScreenAdapter {

    Game game; // May need to change this to LD37Game type. Maybe not though

    private boolean initialized;
    private boolean paused;
    private float elapsedTime = 0f;
    private World world; // Box2D World

    OrthographicCamera camera;
    private SampleSchoolWorld gameWorld; // DONE: change this game world - changed to SampleSchoolWorld

    private PooledEngine engine; // Ashley ECS engine

    private SpriteBatch batch;
    private IScreenManager manager; // TODO change this simple screen switcher or don't use it

    private InputMultiplexer multiplexer;

    // font related items. glyphlayout makes it easier to draw and center fonts and other stuff probably
    // Font rendering should be processed as FontComponent so the ECS RenderingSystem can grab it
    private BitmapFont mediumFont;
    private GlyphLayout glyphLayout;

    // TODO add InputListener

    public SampleGameScreen(Game game, SpriteBatch batch, IScreenManager manager) {
        this.game = game;
        this.batch = batch;
        this.manager = manager;
    }

    private void init() {
        Gdx.app.log("School Game Screen", "Initializing School Game Screen");

        // new Box2D world with no gravity
        world = new World(new Vector2(0f, 0f), true);
        engine = new PooledEngine();
        // DONE: change this game world - changed to School World
        gameWorld = new SampleSchoolWorld(engine, world);

        // create ECS system to process rendering
        RenderingSystem renderingSystem = new RenderingSystem(batch);
        engine.addSystem(renderingSystem);
        engine.addSystem(new FontSystem(batch, engine));
        engine.addSystem(new AnimationSystem());
        // add ECS system to process physics in the Box2D world
        engine.addSystem(new PhysicsSystem(world));

        CollisionSystem collisionSystem = new CollisionSystem(gameWorld, world);
        engine.addSystem(collisionSystem);
        world.setContactListener(collisionSystem);

        // TODO: eventually remove these systems. they are just used for testing
        //engine.addSystem(new PhysicsDebugSystem(world, renderingSystem.getCamera()));
        //engine.addSystem(new UselessStateSwapSystem());

        // DONE: changed to school world
        gameWorld.create();

        camera = engine.getSystem(RenderingSystem.class).getCamera();

        // load fonts
        //mediumFont = Assets.getMediumFont();
        glyphLayout = new GlyphLayout();

        // Create all input devices and add to the multiplexer
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new SampleSchoolInput(camera, gameWorld));
        // Set Gdx.input to track all the created input handlers
        Gdx.input.setInputProcessor(multiplexer);


        // set initialized if everything else was a success
        initialized = true;
    }

    private void update(float delta) {
        engine.update(delta);
        gameWorld.update(delta);

        elapsedTime += delta;

        if(gameWorld.state == SampleSchoolWorld.State.GAMEOVER) {
            manager.endCurrentScreen();
            // Game is over, so reset the data to be used later.
            gameWorld.state = SampleSchoolWorld.State.RUNNING;
            gameWorld.reset();
        }
        // Use a FontComponent to render text. It will be processed by RenderingSystem
        //glyphLayout.setText(Assets.getMediumFont(), "Time: " + elapsedTime);
        //mediumFont.draw(batch, glyphLayout, )
        //Assets.getMediumFont().draw(batch, glyphLayout, camera.position.x - glyphLayout.width / 2,
        //        camera.viewportHeight / 2);
        // can trigger stuff based on elapsedTime [children etc]
    }

    @Override
    public void render(float delta) {
        // If the Screen is not paused, then continue updating everything [engine, world, etc]
        //if(!paused) {
            // if everything is ready, then update. otherwise need to setup game world
            if (initialized) {
                update(delta);
            } else {
                init();
            }
        //}
    }

    private void setEngineOn(boolean turnOn) {


        if(engine != null) {
            Gdx.app.log("School Game Screen", "Setting engine to " + turnOn);
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
        Gdx.app.log("School Game Screen", "Switched to SchoolGameScreen");
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

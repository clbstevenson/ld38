package com.exovum.ludumdare.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.exovum.ludumdare.GameWorld;
import com.exovum.ludumdare.components.FontComponent;
import com.exovum.ludumdare.components.TransformComponent;

import java.util.Comparator;

public class FontSystem extends SortedIteratingSystem {

    static final float PPM = 16.0f;
    static final float FRUSTUM_WIDTH = Gdx.graphics.getWidth(); //
        // GameWorld.WORLD_WIDTH; //40f;//400f;//Gdx.graphics.getWidth()/PPM;//37.5f;
    static final float FRUSTUM_HEIGHT = Gdx.graphics.getHeight();
        // GameWorld.WORLD_HEIGHT; // 30f;//300f;//Gdx.graphics.getHeight()/PPM;//.0f;


    private final float RATIO_WIDTH = Gdx.graphics.getWidth() / GameWorld.WORLD_WIDTH;
    private final float RATIO_HEIGHT = Gdx.graphics.getHeight() / GameWorld.WORLD_HEIGHT;
    public static final float PIXELS_TO_METRES = 1.0f / PPM;

    private static Vector2 meterDimensions = new Vector2();
    private static Vector2 pixelDimensions = new Vector2();

    private Array<Entity> removeEntities;
    // TODO set engine by a parameter to constructor
    private Engine engine;

    public static Vector2 getScreenSizeInMeters(){
        meterDimensions.set(Gdx.graphics.getWidth()*PIXELS_TO_METRES,
                            Gdx.graphics.getHeight()*PIXELS_TO_METRES);
        return meterDimensions;
    }

    public static Vector2 getScreenSizeInPixesl(){
        pixelDimensions.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return pixelDimensions;
    }

    public static float PixelsToMeters(float pixelValue){
        return pixelValue * PIXELS_TO_METRES;
    }

    private SpriteBatch batch;
    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;
    private OrthographicCamera cam;

    private ComponentMapper<TransformComponent> transformMap;
    private ComponentMapper<FontComponent> fontMap;

    public FontSystem(SpriteBatch batch, Engine engine) {
        super(Family.all(TransformComponent.class, FontComponent.class).get(), new ZComparator());

        transformMap = ComponentMapper.getFor(TransformComponent.class);
        fontMap = ComponentMapper.getFor(FontComponent.class);

        renderQueue = new Array<>();

        this.engine = engine;

        removeEntities = new Array<>();

        // CONFIRMED: Add a comparator so the queue.sort doesn't crash *crosses fingers*

        comparator = new Comparator<Entity>() {
            @Override
            public int compare(Entity entityA, Entity entityB) {
                return (int)Math.signum(transformMap.get(entityB).position.z -
                        transformMap.get(entityA).position.z);
            }
        };

        this.batch = batch;

        cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
        cam.position.set(FRUSTUM_WIDTH / 2f, FRUSTUM_HEIGHT / 2f, 0);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        renderQueue.sort(comparator);

        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.enableBlending();
        batch.begin();

        for(Entity entity: renderQueue) {
            // Every font will have FontCP and TransformCP
            // FontComponent has a BitmapFont and GlyphLayout
            FontComponent font = fontMap.get(entity);
            TransformComponent t = transformMap.get(entity);
            if(font == null) {
                continue;
            }
            GlyphLayout glyph = font.glyph;
            if(font.font == null || glyph == null || t.isHidden)
                continue;

            //Gdx.app.log("FontSystem", "Starting fontQueue rendering");

            // RATIO_WIDTH and RATIO_HEIGHT scale the FontSystem to match the
            //      world rendering. FontSystem uses the Graphics width/height and
            //      RenderingSystem uses the World width/height which is approx 40x30.
            font.font.draw(batch, font.glyph, (RATIO_WIDTH * t.position.x - glyph.width / 2),
                    (RATIO_HEIGHT* t.position.y - glyph.height / 2));

            // Update the font timer
            /*
            if(font.type != FontComponent.TYPE.PERM) {
                if(font.displayTime < 0 ) {
                    font.displayTime -= deltaTime;
                    removeEntities.add(entity);
                }
            }
            */

        }

        batch.end();

        /*
        // Remove everything from the entity and then kill the entity too
        for(Entity e: removeEntities) {
            FontComponent deadFont = fontMap.get(e);
            if(deadFont != null) {
                deadFont.font.dispose();
                engine.removeEntity(e);
            }
        }
        removeEntities.clear();
        */

        renderQueue.clear();

    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        renderQueue.add(entity);
        // remember to add entity to font queue so they can actually render
    }


    /*
    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
    }
    */

    public OrthographicCamera getCamera() {
        return cam;
    }
}

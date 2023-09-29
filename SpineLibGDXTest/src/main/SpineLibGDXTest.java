/*
 * File:    SpineLibGDXTest.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

public class SpineLibGDXTest extends ApplicationAdapter {
    
    //Fields
    
    public Viewport viewport;
    
    public Camera camera;
    
    public TwoColorPolygonBatch batch;
    
    public SkeletonRenderer renderer;
    
    public TextureAtlas atlas;
    
    public Skeleton skeleton;
    
    public AnimationState state;
    
    
    //Main Method
    
    static public void main(String[] args) throws Exception {
        final SpineLibGDXTest adapter = new SpineLibGDXTest();
        
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.disableAudio(true);
        config.setResizable(true);
        config.setWindowedMode(1080, 540);
        
        new Lwjgl3Application(adapter, config);
    }
    
    
    //Methods
    
    public void create() {
        load("data/mons_9340");
        
        camera = new OrthographicCamera();
        viewport = new FitViewport(1080, 540, camera);
        
        resize((int) skeleton.getData().getWidth(), (int) skeleton.getData().getHeight());
        
        batch = new TwoColorPolygonBatch(3100);
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true);
    }
    
    public void load(String path) {
        FileHandle atlasFile = new FileHandle(new File(path + ".atlas").getAbsoluteFile());
        FileHandle jsonFile = new FileHandle(new File(path + ".json").getAbsoluteFile());
        FileHandle pngFile = new FileHandle(new File(path.toUpperCase() + ".PNG").getAbsoluteFile());
        
        atlas = new TextureAtlas(atlasFile);
        
        SkeletonJson json = new SkeletonJson(atlas);
        SkeletonData skeletonData = json.readSkeletonData(jsonFile);
        
        skeleton = new Skeleton(skeletonData);
        
        AnimationStateData stateData = new AnimationStateData(skeletonData);
        state = new AnimationState(stateData);
        
        state.setAnimation(0, "animation_01", true);
    }
    
    public void render() {
        state.update(Gdx.graphics.getDeltaTime());
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        state.apply(skeleton);
        skeleton.updateWorldTransform();
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        renderer.draw(batch, skeleton);
        batch.end();
    }
    
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        
        float scale = Math.min((viewport.getWorldWidth() / skeleton.getData().getWidth()), (viewport.getWorldHeight() / skeleton.getData().getHeight()));
        skeleton.setScale(scale, scale);
        skeleton.setPosition(viewport.getWorldWidth() / 2.0f, (viewport.getWorldHeight() - skeleton.getData().getHeight()) / 4.0f);
        skeleton.updateWorldTransform();
    }
    
    public void dispose() {
        atlas.dispose();
    }
    
}

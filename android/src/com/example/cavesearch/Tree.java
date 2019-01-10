package com.example.cavesearch;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

public class Tree {
	public static Texture treeTexture;
	public static TextureRegion treeTextureRegion;
	public Sprite treeSprite;
	
	public static boolean loadResource(MainActivity mAct){
		TextureRegionFactory.setAssetBasePath("gfx/");
		treeTexture = new Texture(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		treeTextureRegion = TextureRegionFactory.createFromAsset(treeTexture, mAct.getApplicationContext(), "tree.png",0,0);
		mAct.getEngine().getTextureManager().loadTexture(treeTexture);
		return true;
	}
	public void attachToScene(float x,float y,Scene scene){
		treeSprite = new Sprite(x,y,Tree.treeTextureRegion);
		
		
		scene.getLastChild().attachChild(treeSprite);
		
	}
	

}

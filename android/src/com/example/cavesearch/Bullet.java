package com.example.cavesearch;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import android.util.Log;

public class Bullet {
	
	static private String filepath;
	static private Texture redbulletTexture;
	static private Texture bluebulletTexture;
	static private TextureRegion redbulletTextureRegion;
	static private TextureRegion bluebulletTextureRegion;
	static MainActivity boss;
	static TextureRegionFactory trf;
	private Sprite bulletSprite;
	private PhysicsHandler phyHandler;
	private Scene scene;
	Bullet(){	
		
	}
	static boolean loadResource(MainActivity mAct){
		TextureRegionFactory.setAssetBasePath("gfx/");
		redbulletTexture = new Texture(16,16,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		bluebulletTexture = new Texture(16,16,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		bluebulletTextureRegion = TextureRegionFactory.createFromAsset(bluebulletTexture, mAct.getApplicationContext(), "blue_bullet.png",0,0);
		redbulletTextureRegion = TextureRegionFactory.createFromAsset(redbulletTexture, mAct.getApplicationContext(), "red_bullet.png",0,0);
		
		return true;
	}

	public static  Texture getredTexture(){
		return redbulletTexture;
		
	}
	public static  Texture getblueTexture(){
		return bluebulletTexture;
		
	}
	public void attachToScene(Scene exscene,float facex,float facey,float x,float y,String color){
		if(color.equals("red"))
			bulletSprite = new Sprite(facex,facey,Bullet.redbulletTextureRegion);
		else
			bulletSprite = new Sprite(facex,facey,Bullet.bluebulletTextureRegion);
		phyHandler = new PhysicsHandler(bulletSprite);
		bulletSprite.registerUpdateHandler(phyHandler);
		exscene.getLastChild().attachChild(bulletSprite);
		//bulletSprite.registerUpdateHandler(this);
		float xval = x-facex;
		float yval = y-facey;
		float dist = (float) Math.sqrt(xval*xval+yval*yval);
		float xvec = xval/dist;
		float yvec = yval/dist;
		phyHandler.setVelocity(xvec*250,yvec*250);
		this.scene = exscene;
		
	}
	public boolean isOutOfScene(){
		
		if(bulletSprite.getX()<-200 || bulletSprite.getX()+ bulletSprite.getWidth() >boss.CAMERA_WIDTH+200  )
			return true;
		if(bulletSprite.getY()<-200 || bulletSprite.getY()+ bulletSprite.getHeight() >boss.CAMERA_HEIGHT+200)
			return true;
		
		return false;
	}
	public Sprite getSprite(){
		return this.bulletSprite;
	}
//	@Override
//	public void reset(){}
//	
//	@Override
//	public void onUpdate(final float pSecondsElapsed){
////		if(boss.face.collidesWith(bulletSprite)){
////			Log.d("miku","gothca");
////		}
//		if (this.isOutOfScene());
//			//scene.detachChild(bulletSprite);
//			//Log.d("miku","this bullet is out of scene");
//	}
	

}

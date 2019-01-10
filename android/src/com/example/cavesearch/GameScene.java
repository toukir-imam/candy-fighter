package com.example.cavesearch;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.samsung.chord.*;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.provider.Settings;
import android.provider.MediaStore.Audio.AudioColumns;

import android.util.Log;

public class GameScene implements IOnSceneTouchListener {


	private Texture mOnScreenControlTexture;
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;

	private Font youFont;
	private Font opponentFont;
	private Texture mFontTexture;
	MainActivity boss;
	public Music music;
	public Music ah;

	//commcord
	CommChord mChord;

	//OverLord
	float x0=0,xFinal=0;
	float y0=0,yFinal= 0;
	LinkedList<Bullet> liveFaceBullets;
	LinkedList<Bullet> liveEnemyBullets;
	LinkedList<Tree> maze;
	Scene scene;
	CaveMan face;
	CaveMan enemy;
	int candyThrown;
	int candyHit;
	float oldOpnX;
	float oldOpnY;
	//time
	long dtMii ;

	private Texture backgroundtexture;
	private TextureRegion backgroundtextureregion;

	//empty constructor
	public GameScene(MainActivity mAct){
		this.boss = mAct;

	}
	public void loadGameScene(){
		//set movement bound
		x0=0;
		xFinal=boss.CAMERA_WIDTH;
		y0=0;
		yFinal=boss.CAMERA_HEIGHT;

		TextureRegionFactory.setAssetBasePath("gfx/");

		maze = new LinkedList<Tree>();

		this.backgroundtexture = new Texture(1024,512,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.backgroundtextureregion = TextureRegionFactory.createFromAsset(this.backgroundtexture, this.boss, "game_screen_2.png",0,0);

		this.mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this.boss, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this.boss, "onscreen_control_knob.png", 128, 0);

		//load bullet resource

		Bullet.loadResource(this.boss);

		//load Tree
		Tree.loadResource(this.boss);
		//load enemy
		enemy = new CaveMan();
		enemy.loadResource(this.boss,"enemy22.png");
		face = new CaveMan();
		face.loadResource(this.boss,"face22.png");
		//Log.d("miku","load resource");
		this.boss.getEngine().getTextureManager().loadTextures( this.mOnScreenControlTexture,Bullet.getredTexture(),Bullet.getblueTexture(),enemy.getTexture(),face.getTexture(),this.backgroundtexture,Tree.treeTexture);


		//load font
		this.mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR);
		Texture opponentHealttxtTexture = new Texture(256, 256, TextureOptions.BILINEAR);
		this.youFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 14, true, Color.BLUE);
		this.opponentFont = new Font(opponentHealttxtTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 14, true, Color.RED);
		boss.getEngine().getTextureManager().loadTexture(this.mFontTexture);
		boss.getEngine().getTextureManager().loadTexture(opponentHealttxtTexture);
		boss.getEngine().getFontManager().loadFont(this.youFont);
		boss.getEngine().getFontManager().loadFont(this.opponentFont);
		Log.d("miku","resource loaded");	

	}
	public Scene createScene(){

		Log.d("miku","2");
		dtMii =0;
		boss.getEngine().registerUpdateHandler(new FPSLogger());
		scene = new Scene(1);
		liveFaceBullets=new LinkedList<Bullet>();
		liveEnemyBullets=new LinkedList<Bullet>();
		Sprite backgroundImage = new Sprite(0,0, this.backgroundtextureregion);
		scene.setBackground(new SpriteBackground(backgroundImage));


		scene.setOnSceneTouchListener(this);
		final int centerX = (boss.CAMERA_WIDTH - face.getWidth()) / 2;
		final int centerY = (boss.CAMERA_HEIGHT - face.getHeight()) / 2;

		String facetxt,enemytxt;
		Font faceFont,enemyFont;
		String facecolor,enemycolor;
		if(CommChord.getInstance().caveManStatus.equals("face")){
			facetxt="Your Health : 100";
			enemytxt="Enemy Health: 100";
			faceFont=youFont;
			enemyFont=opponentFont;
			facecolor="blue";
			enemycolor="red";
		}
		else{
			facetxt="Enemy Health : 100";
			enemytxt="Your Health: 100";
			faceFont=opponentFont;
			enemyFont=youFont;
			facecolor="red";
			enemycolor="blue";
		}
		enemy.attachToScene(scene, centerX, centerY*2/5,enemycolor,boss.CAMERA_WIDTH-150,50);
		face.attachToScene(scene, centerX, centerY*2-centerY/5,facecolor,boss.CAMERA_WIDTH-150,boss.CAMERA_HEIGHT-50);

		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(50, boss.CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight()-50, boss.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, 200, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {

				if(CommChord.getInstance().caveManStatus.equals("face")){
					face.setVelocity(pValueX, pValueY);
				}
				else if(CommChord.getInstance().caveManStatus.equals("enemy")){
					enemy.setVelocity(pValueX, pValueY);

				}
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {

			}
		});
		analogOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		analogOnScreenControl.getControlBase().setScale(1.25f);
		analogOnScreenControl.getControlKnob().setScale(1.25f);
		analogOnScreenControl.refreshControlKnobPosition();



		scene.setChildScene(analogOnScreenControl);

		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() { }

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				//Log.d("miku","elapsed time"+pSecondsElapsed);
				if(CommChord.getInstance().caveManStatus.equals("face")){
					//mazeHitAndAdjust(face);
					if(face.getSprite().getX()!=oldOpnX || face.getSprite().getY()!=oldOpnY)
						CommChord.getInstance().sendData(face.getSprite().getX(),face.getSprite().getY());
					oldOpnX = face.getSprite().getX();
					oldOpnY= face.getSprite().getY();
				}
				else{
					//mazeHitAndAdjust(enemy);
					if(enemy.getSprite().getX()!=oldOpnX || enemy.getSprite().getY()!=oldOpnY)
						CommChord.getInstance().sendData(enemy.getSprite().getX(),enemy.getSprite().getY());
					oldOpnX = enemy.getSprite().getX();
					oldOpnY= enemy.getSprite().getY();
				}
				face.adjust(x0, xFinal, y0, yFinal);
				enemy.adjust(x0, xFinal, y0, yFinal);

				for(int i =0;i<liveFaceBullets.size();i++){
					if(liveFaceBullets.get(i).getSprite().collidesWith(enemy.getSprite())){
						playAh();
						//Log.d("miku","die you sucker");
						scene.getLastChild().detachChild(liveFaceBullets.get(i).getSprite());
						if(CommChord.getInstance().caveManStatus.equals("enemy")){
							enemy.reduceHealth();
							CommChord.getInstance().sendReduceHealth();
						}
						liveFaceBullets.remove(i);
					}
				}
				for(int i =0;i<liveFaceBullets.size();i++){
					if(liveFaceBullets.get(i).isOutOfScene()){
						scene.getLastChild().detachChild(liveFaceBullets.get(i).getSprite());
						liveFaceBullets.remove(i);
						//Log.d("miku","bullet out of bound remianing "+liveFaceBullets.size());

					}
				}


				for(int i =0;i<liveEnemyBullets.size();i++){
					if(liveEnemyBullets.get(i).getSprite().collidesWith(face.getSprite())){
						playAh();
						//Log.d("miku","die you sucker");
						scene.getLastChild().detachChild(liveEnemyBullets.get(i).getSprite());



						if(CommChord.getInstance().caveManStatus.equals("face")){
							face.reduceHealth();
							CommChord.getInstance().sendReduceHealth();

						}
						liveEnemyBullets.remove(i);
					}
				}
				for(int i =0;i<liveEnemyBullets.size();i++){
					if(liveEnemyBullets.get(i).isOutOfScene()){
						scene.getLastChild().detachChild(liveEnemyBullets.get(i).getSprite());
						liveEnemyBullets.remove(i);
						//Log.d("miku","bullet out of bound remianing "+liveFaceBullets.size());
					}
				}

				if(enemy.getHealth()<=0){
					CommChord.getInstance().status=CommChord.Status.network_connected;
					if(CommChord.getInstance().caveManStatus.equals("face"))
						rejoice("ldfk");
					else 
						mourn("dl");				
				}
				if(face.getHealth()<=0){
					CommChord.getInstance().status=CommChord.Status.network_connected;
					if(CommChord.getInstance().caveManStatus.equals("face"))

						mourn("ldfk");
					else 
						rejoice("dl");
				}
			}
		});
		Log.d("miku","sldkfjd");


		//attach tree
		//createMaze();
		//play music
		AudioManager am = (AudioManager) boss.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(new AudioFocusListener(), AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			Log.d("miku","audio focus granted");
			try
			{
				music = MusicFactory.createMusicFromAsset(boss.getMusicManager(), boss,"gfx/POL-final-act-short.wav");
				ah = MusicFactory.createMusicFromAsset(boss.getMusicManager(), boss,"gfx/ah.mp3");
				music.play();
				music.setLooping(true);


			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else{
			Log.d("miku","audio focus rejected");
		}
		new Handler().postDelayed(new Runnable(){
			public void run(){
				if(CommChord.getInstance().caveManStatus.equals("face"))
					face.getSprite().registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.75f, 1, 2f), new ScaleModifier(0.75f, 2.0f, 1)));
				else
					enemy.getSprite().registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.75f, 1, 2f), new ScaleModifier(0.75f, 2.0f, 1)));
			}
		}, 1000);
		candyThrown =0;
		candyHit =0;
		return scene;

	}

	public boolean mazeHit(Sprite sp){
		for(int i =0;i<maze.size();i++){
			if(maze.get(i).treeSprite.collidesWith(sp)){
				return true;
			}
		}
		return false;
	}
	public boolean mazeHitAndAdjust(CaveMan cv){
		for(int i =0;i<maze.size();i++){
			if(maze.get(i).treeSprite.collidesWith(cv.getSprite())){

				if(cv.getSprite().getX()>maze.get(i).treeSprite.getX() && cv.getPhyHandler().getVelocityX()<0){
					cv.getSprite().setPosition(maze.get(i).treeSprite.getX()+maze.get(i).treeSprite.getWidth(),cv.getSprite().getY());
				}
				else if(cv.getSprite().getX()<maze.get(i).treeSprite.getX() && cv.getPhyHandler().getVelocityX()>0){
					cv.getSprite().setPosition(maze.get(i).treeSprite.getX()-maze.get(i).treeSprite.getWidth(),cv.getSprite().getY());
				}
				if(cv.getSprite().getY()>maze.get(i).treeSprite.getY() && cv.getPhyHandler().getVelocityY()<0){
					cv.getSprite().setPosition(cv.getSprite().getX(),maze.get(i).treeSprite.getY()-maze.get(i).treeSprite.getHeight());
				}
				else if(cv.getSprite().getY()<maze.get(i).treeSprite.getY() && cv.getPhyHandler().getVelocityY()>0){
					cv.getSprite().setPosition(cv.getSprite().getX(),maze.get(i).treeSprite.getY()+maze.get(i).treeSprite.getHeight());
				}
				if(cv.getPhyHandler().getVelocityY()!=0)
					Log.d("miku","maze final cv val X= "+cv.getPhyHandler().getVelocityX()+"   vval Y = "+cv.getPhyHandler().getVelocityY());

			}
			else{
				cv.oldposx=cv.getSprite().getX();
				cv.oldposy=cv.getSprite().getY();
			}
		}
		return false;

	}
	public void reduceOpponentHealth(){
		if(CommChord.getInstance().caveManStatus.equals("face")){
			enemy.reduceHealth();
		}
		else{ 
			face.reduceHealth();

		}
		candyHit++;
	}
	public void moveOpponent(float x,float y){
		if(CommChord.getInstance().caveManStatus.equals("face")){
			enemy.getSprite().setPosition(x,y);

		}
		else if(CommChord.getInstance().caveManStatus.equals("enemy")){
			face.getSprite().setPosition(x,y);

		}
	}


	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.isActionUp()){
			if(dtMii!=0){
				if(System.currentTimeMillis()-dtMii <150)
					return true;
				//Log.d("miku","dtmii diff "+ (System.currentTimeMillis()-dtMii));
			}
			dtMii = System.currentTimeMillis();

			if(CommChord.getInstance().caveManStatus.equals("face"))
				createFaceBullet(pSceneTouchEvent.getX(),pSceneTouchEvent.getY(),"blue");
			else if(CommChord.getInstance().caveManStatus.equals("enemy")){
				createEnemyBullet(pSceneTouchEvent.getX(),pSceneTouchEvent.getY(),"bule");
			}
			candyThrown++;
			CommChord.getInstance().sendBullet(pSceneTouchEvent.getX(),pSceneTouchEvent.getY());
		}
		return true;
	}
	public void createOpponentBullet(float x ,float y){
		//enemyHealthtxt.setText(""+x+" "+y);
		if(CommChord.getInstance().caveManStatus.equals("face"))
			createEnemyBullet(x,y,"red");
		else if(CommChord.getInstance().caveManStatus.equals("enemy")){
			createFaceBullet(x,y,"red");
		}
	}
	private void createFaceBullet(float x, float y,String color){
		Bullet b = new Bullet();
		float centerx = face.getSprite().getX()+face.getSprite().getWidth()/2;
		float centery= face.getSprite().getY()+face.getSprite().getHeight()/2;
		b.attachToScene(scene,centerx,centery, x,y,color);
		liveFaceBullets.add(b);
	}
	private void createEnemyBullet(float x, float y,String color){
		Bullet b = new Bullet();
		float centerx = enemy.getSprite().getX()+enemy.getWidth()/2;
		float centery= enemy.getSprite().getY()+enemy.getHealth()/2;
		b.attachToScene(scene,centerx,centery, x,y,color);
		liveEnemyBullets.add(b);
	}
	public void rejoice(String winner){
		int yourHealth,enemyHealth;
		if(CommChord.getInstance().caveManStatus.equals("face")){
			yourHealth = face.getHealth();
			enemyHealth = enemy.getHealth();

		}
		else{
			yourHealth = enemy.getHealth();
			enemyHealth = face.getHealth();
		}
		finish();
		SceneManager.getManager().setResultScene("won" ,yourHealth,enemyHealth,candyThrown,candyHit);

	}
	public void mourn(String mourner){
		int yourHealth,enemyHealth;
		if(CommChord.getInstance().caveManStatus.equals("face")){
			yourHealth = face.getHealth();
			enemyHealth = enemy.getHealth();

		}
		else{
			yourHealth = enemy.getHealth();
			enemyHealth = face.getHealth();
		}
		finish();
		SceneManager.getManager().setResultScene("lost" ,yourHealth,enemyHealth,candyThrown,candyHit);

	}
	public void finish(){
		Log.d("miku","stop called");
		music.setLooping(false);
		music.pause();
		music.stop();

		//show fail scene
		int yourHealth,enemyHealth;
		if(CommChord.getInstance().caveManStatus.equals("face")){
			yourHealth = face.getHealth();
			enemyHealth = enemy.getHealth();

		}
		else{
			yourHealth = enemy.getHealth();
			enemyHealth = face.getHealth();
		}
		SceneManager.getManager().dismissEnemyPause();
		SceneManager.getManager().dismissOwnPause();
		SceneManager.getManager().setResultScene("Aborted" ,yourHealth,enemyHealth,candyThrown,candyHit);

	}
	private synchronized void  playAh(){    
		ah.play(); 
	}
	private void createMaze(){
		Tree tree = new Tree();
		tree.attachToScene(300, 200, this.scene);
		maze.add(tree);
		tree = new Tree();
		tree.attachToScene(300,254, scene);
		maze.add(tree);
		tree = new Tree();
		tree.attachToScene(300,254+54, scene);
		maze.add(tree);
		/*tree = new Tree();
		tree.attachToScene(300,264+54+54, scene);
		maze.add(tree);*/
	}
	public void pauseMusic(){
		music.pause();
	}
}

class AudioFocusListener implements OnAudioFocusChangeListener{

	@Override
	public void onAudioFocusChange(int focusArg) {
		if(focusArg == AudioManager.AUDIOFOCUS_LOSS){
			Log.d("miku","focus lost");

		}
		else if(focusArg == AudioManager.AUDIOFOCUS_GAIN){
			Log.d("miku","Focus gain");
			SceneManager.getManager().getGameSceneObj().music.play();
		}
		else if(focusArg == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
			SceneManager.getManager().getGameSceneObj().music.pause();
			Log.d("miku","transient loss");
		}

		else if (focusArg == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
			Log.d("miku","can duck");
			SceneManager.getManager().getGameSceneObj().music.pause();
		}
		else{
			Log.d("miku","something else");
		}

	}


}


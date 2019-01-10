package com.example.cavesearch;


import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.os.PowerManager;
import android.util.Log;



public class MainActivity extends BaseGameActivity  {

	public static final int CAMERA_WIDTH = 880;
	public static final int CAMERA_HEIGHT = 495;

	public Camera mCamera;
	SceneManager sm;
	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final Engine engine =new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera).setNeedsMusic(true));
		if(MultiTouch.isSupported(this)){
			try{
				engine.setTouchController(new MultiTouchController());
			}catch(final MultiTouchException e){
				Log.d("miku","Multitouch exception raised");
			}
		}
		return engine; 
	}

	@Override
	public void onLoadResources() {

		SceneManager.init(this);

		sm = SceneManager.getManager();
		sm.loadResultScene();
		sm.loadGameSceneResource();
		sm.loadFlashScene();
		sm.loadHelpScene();

		CommChord.getInstance().status=CommChord.Status.network_disconnected;
		//Log.d("miku","on resource load completed");

	}

	@Override
	public Scene onLoadScene() {

		return sm.getFlashScene();
	}


	@Override
	public void onLoadComplete() {

	}
	@Override
	public void onResume(){
		super.onResume();
		Log.d("miku", "on resume");
	}
	@Override
	public void onStop(){
		super.onStop();
		Log.d("miku","onStop");
		SceneManager.dismissConf();
		Log.d("miku","screen is off");
		if(SceneManager.waitingDialog.isShowing()){
			Log.d("miku","screen off and waiting on");
			CommChord.getInstance().sendConfirm(CommChord.getInstance().opponentId, false);
			SceneManager.dissmissWaiting();
		}
		else if(SceneManager.getManager().curScene==SceneManager.SceneNames.game){
			SceneManager.getManager().getGameSceneObj().pauseMusic();
			if(!SceneManager.getManager().ownPauseDialog.isShowing() && !SceneManager.getManager().enemyPauseDialog.isShowing()){
				Log.d("miku","in something");
				CommChord.getInstance().sendPauseRequest();
				SceneManager.getManager().showOwnPauseDialog();
			}

		}
		if(this.isFinishing()){
			Log.d("miku","finishing");
			if(CommChord.getInstance()!=null){
				CommChord.getInstance().stopChord();
			}
		}		
	}
	@Override
	public void onPause(){
		Log.d("miku","on pause");
		super.onPause();
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();

		if (!isScreenOn || SceneManager.getManager().curScene==SceneManager.SceneNames.game) {
			SceneManager.dismissConf();
			Log.d("miku","screen is off");
			if(SceneManager.waitingDialog.isShowing()){
				Log.d("miku","screen off and waiting on");
				CommChord.getInstance().sendConfirm(CommChord.getInstance().opponentId, false);
				SceneManager.dissmissWaiting();
			}
			else if(SceneManager.getManager().curScene==SceneManager.SceneNames.game){
				SceneManager.getManager().getGameSceneObj().pauseMusic();
				if(!SceneManager.getManager().ownPauseDialog.isShowing() ){
					Log.d("miku","in something");
					SceneManager.getManager().getGameSceneObj().pauseMusic();
					CommChord.getInstance().sendPauseRequest();
					SceneManager.getManager().showOwnPauseDialog();
				}

			}


		}

	}
	@Override
	public void onBackPressed()
	{
		Log.d("miku","back is pressed"+SceneManager.getManager().curScene.toString());

		if(SceneManager.getManager().curScene==SceneManager.SceneNames.game ){
			Log.d("miku","exit confirm");
			if(CommChord.getInstance().sendPauseRequest()){
				SceneManager.getManager().showOwnPauseDialog();

			}
		}
		else if(SceneManager.getManager().curScene==SceneManager.SceneNames.home ){

			finish();
		}

		else if(SceneManager.getManager().curScene==SceneManager.SceneNames.result){

			SceneManager.getManager().setHomeScene();
		}
		else if(SceneManager.getManager().curScene==SceneManager.SceneNames.help){

			SceneManager.getManager().setHomeScene();
		}

	}
}

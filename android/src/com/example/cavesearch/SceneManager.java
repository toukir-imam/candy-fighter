package com.example.cavesearch;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;


public class SceneManager {
	private static MainActivity boss; // your main activity
	private static SceneManager sm;

	private GameScene gScene;

	//Flash card
	private Scene fScene;
	private TextureRegion bkgFlashReg;
	private TextureRegion bkgHelpReg;
	private TextureRegion gamestatusreg;
	public enum SceneNames{home,game,result,fail,pause,help};
	public SceneNames curScene;
	private Font homeFont;
	//Result Scene
	private TextureRegion homeReg;
	private TextureRegion helpReg;
	private TextureRegion backReg;
	private TextureRegion restartReg;
	private TextureRegion crownReg;
	private TextureRegion haloReg;
	private Scene resScene;
	private Font resFont;

	private Scene hScene;

	//Fail Scene
	private Scene failScene;
	private TextureRegion bkgFailReg;

	public static AlertDialog nodeListDialog;
	public static AlertDialog.Builder builder;
	public static AlertDialog waitingDialog;
	public static AlertDialog confDialog;
	public static AlertDialog declineDialog;
	public static AlertDialog enemyPauseDialog;
	public static AlertDialog ownPauseDialog;
	private SceneManager(){
		AlertDialog.Builder ab = new AlertDialog.Builder(boss.getApplicationContext());
		nodeListDialog = ab.create();
		waitingDialog = ab.create();
		confDialog = ab.create();
		enemyPauseDialog = ab.create();
		ownPauseDialog = ab.create();
		declineDialog =ab.create();

	}

	public static void init(MainActivity mAct){
		SceneManager.boss = mAct;
	}

	public static SceneManager getManager(){
		if(boss == null) throw new IllegalStateException("You must first initialize scenemanager class");
		if(sm == null) return sm = new SceneManager();

		return sm;
	}
	public void setGameScene(){
		curScene = SceneManager.SceneNames.game;  
		gScene.createScene();
		boss.getEngine().setScene(gScene.scene);
	}
	public void loadFlashScene(){
		TextureRegionFactory.setAssetBasePath("gfx/");
		Texture bkgTex = new Texture(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		bkgFlashReg = TextureRegionFactory.createFromAsset(bkgTex, boss, "xxx_3.jpeg", 0, 0);
		Texture mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR);
		homeFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 24, true, Color.WHITE);

		//help icon
		Texture helpTex = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		helpReg = TextureRegionFactory.createFromAsset(helpTex, boss, "help_button.png", 0, 0);

		boss.getEngine().getTextureManager().loadTexture(mFontTexture);
		boss.getEngine().getFontManager().loadFont(homeFont);
		boss.getEngine().getTextureManager().loadTextures(bkgTex);
		boss.getEngine().getTextureManager().loadTextures(helpTex);

	}
	public void setHomeScene(){

		CommChord.getInstance().stopChord();
		curScene=SceneManager.SceneNames.home;
		Log.d("miku","setting home");
		boss.getEngine().setScene(fScene);
		this.curScene=SceneManager.SceneNames.home;
	}
	public Scene getFlashScene(){
		curScene = SceneManager.SceneNames.home;
		fScene = new Scene(1);
		Sprite sp = new Sprite(0,0,bkgFlashReg);
		fScene.setBackground(new SpriteBackground(sp));
		Text helpfullInformation = new Text(boss.CAMERA_WIDTH/2-150, boss.CAMERA_HEIGHT-100,homeFont,"Tap anywhere to start game");
		fScene.getLastChild().attachChild(helpfullInformation);

		Sprite help = new Sprite(boss.CAMERA_WIDTH-200,boss.CAMERA_HEIGHT-150,helpReg){

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY){

				if(pSceneTouchEvent.isActionUp()){

					setHelpScene();
					Log.d("+++++++++++++++","help button is pressed");
				}
				return true;
			}

		};
		Text helpiconText = new Text(boss.CAMERA_WIDTH-90, boss.CAMERA_HEIGHT-95,homeFont,"Help"){

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY){

				if(pSceneTouchEvent.isActionUp()){

					setHelpScene();
					Log.d("+++++++++++++++","help button is pressed");
				}
				return true;
			}

		};

		fScene.getLastChild().attachChild(helpiconText);
		fScene.getLastChild().attachChild(help);
		fScene.registerTouchArea(help);
		fScene.setOnSceneTouchListener(new IOnSceneTouchListener(){

			@Override
			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {

				if(pSceneTouchEvent.isActionUp()){
					//Start Chord
					CommChord mChord = CommChord.getInstance();

					if(mChord.status!=CommChord.Status.network_disconnected || mChord.ChordInitiate(boss)){

						builder = new AlertDialog.Builder(boss);

						builder.setTitle("Select Oponent");
						final CharSequence[] cs =CommChord.getInstance().getNodeList();
						builder.setItems(cs, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//	   if(CommChord.getInstance().availble.tryAcquire()){
								Log.d("miku","semaphore acquired at setFlashScene");
								CommChord.getInstance().opponentId=cs[which].toString();
								CommChord.getInstance().sendJoinRequest((String)cs[which]);
								Log.d("miku",cs[which].toString());
								showWaitingforConfirm();
								//	   }
							}
						});
						builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {				        	   
								if(nodeListDialog!=null){
									nodeListDialog.cancel();
								}

							}
						});
						builder.setOnCancelListener(new DialogInterface.OnCancelListener() {         
							@Override
							public void onCancel(DialogInterface dialog) {
							}
						});

						nodeListDialog = builder.show();

					}
					else
						boss.startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),0);
					Log.d("miku","lsdk");



				}
				return true;
			}		   

		});
		return fScene;	   
	}

	//Help Scene
	public void loadHelpScene()
	{
		TextureRegionFactory.setAssetBasePath("gfx/");
		Texture bkgTex = new Texture(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		bkgHelpReg = TextureRegionFactory.createFromAsset(bkgTex, boss, "help_content.png", 0, 0);

		//back icon
		Texture backTex = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		backReg = TextureRegionFactory.createFromAsset(backTex, boss, "back_button.png", 0, 0);

		boss.getEngine().getTextureManager().loadTextures(bkgTex);
		boss.getEngine().getTextureManager().loadTextures(backTex);
	}
	public Scene setHelpScene(){
		curScene = SceneManager.SceneNames.home;
		if(hScene==null)
			hScene = new Scene(1);
		Sprite sp = new Sprite(0,0,bkgHelpReg);
		hScene.setBackground(new SpriteBackground(sp));

		Sprite back = new Sprite(boss.CAMERA_WIDTH/2+300,boss.CAMERA_HEIGHT-110,backReg){

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY){

				if(pSceneTouchEvent.isActionUp()){
					setHomeScene();
					Log.d("+++++++++++++++","help button is pressed");
				}
				return true;
			}

		};
		hScene.getLastChild().attachChild(back);
		hScene.registerTouchArea(back);

		boss.getEngine().setScene(hScene);
		this.curScene=SceneManager.SceneNames.help;
		return hScene;	   
	}

	public void updateNodeListDialog(){
		if(SceneManager.builder != null && SceneManager.nodeListDialog.isShowing()){
			final CharSequence cs[]=CommChord.getInstance().getNodeList();
			SceneManager.builder.setItems(cs, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {   
					//	   if(CommChord.getInstance().availble.tryAcquire()){
					Log.d("miku","semaphore acquired at update NodeList");
					CommChord.getInstance().opponentId=cs[which].toString();
					CommChord.getInstance().sendJoinRequest((String)cs[which]);
					Log.d("miku",cs[which].toString());
					showWaitingforConfirm();
					//  }


				}
			});
			SceneManager.nodeListDialog.dismiss();
			SceneManager.nodeListDialog = SceneManager.builder.show();
		}
	}
	public void showDecline(){
		if(waitingDialog!=null){
			dissmissWaiting();
		}
		AlertDialog.Builder decBuilder = new AlertDialog.Builder(boss);
		decBuilder.setTitle("Declined");	   
		decBuilder.setMessage("Request declined");
		decBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				updateNodeListDialog();

			}
		});
		declineDialog  = decBuilder.show();



	}
	public void showWaitingforConfirm(){
		AlertDialog.Builder waitBuilder = new AlertDialog.Builder(boss);
		waitBuilder.setTitle("Waiting...");
		waitBuilder.setMessage("Waiting for confirmation...");

		waitBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {         
			@Override
			public void onCancel(DialogInterface dialog) {
				CommChord.getInstance().sendConfirm(CommChord.getInstance().opponentId,false);
				// curScene=prvScene;
				dismissConf();
			}
		});


		waitingDialog = waitBuilder.show();
		new Handler().postDelayed(new Runnable(){
			public void run(){
				if(waitingDialog.isShowing()){
					dissmissWaiting();
					CommChord.getInstance().sendConfirm(CommChord.getInstance().opponentId, false);
				}
			}
		}, 5000);
	}
	public void showConfirmationDialog(final String enemyid){
		Log.d("miku","showing confirm dialog");
		//dismiss decline dialog
		dismissDecline();
		AlertDialog.Builder confBuilder = new AlertDialog.Builder(boss);
		confBuilder.setTitle("Joining Request");
		confBuilder.setMessage("Request To join from "+enemyid);
		confBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				CommChord.getInstance().status=CommChord.Status.opponent_connected;
				CommChord.getInstance().opponentId=enemyid;
				CommChord.getInstance().caveManStatus="enemy";
				CommChord.getInstance().sendConfirm(enemyid,true);
				SceneManager.getManager().setGameScene();

				if(nodeListDialog!=null){
					nodeListDialog.dismiss();
				}

			}
		});
		confBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				CommChord.getInstance().sendConfirm(enemyid,false);
			}
		});
		confBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {         
			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});

		confDialog = confBuilder.show();

	}
	public void setScene(Scene scene){
		boss.getEngine().setScene(scene);
	}


	public void loadGameSceneResource(){
		gScene= new GameScene(boss);
		gScene.loadGameScene();	   
	}
	public Scene getGameScene(){
		return gScene.scene;  
	}
	public GameScene getGameSceneObj(){
		return gScene;
	}

	public void loadResultScene(){
		// background
		TextureRegionFactory.setAssetBasePath("gfx/");

		Texture gamestatus = new Texture(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gamestatusreg = TextureRegionFactory.createFromAsset(gamestatus, boss, "epic_fail.png", 0, 0);
		boss.getEngine().getTextureManager().loadTexture(gamestatus);

		//Fonts
		Texture mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR);
		resFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 24, true, Color.BLACK);
		boss.getEngine().getTextureManager().loadTexture(mFontTexture);
		boss.getEngine().getFontManager().loadFont(resFont);
		//home icon
		Texture homeTex = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		homeReg = TextureRegionFactory.createFromAsset(homeTex, boss, "home_button.png", 0, 0);

		//restart icon

		Texture restartTex = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		restartReg = TextureRegionFactory.createFromAsset(restartTex, boss, "restart_2.png", 0, 0);
		boss.getEngine().getTextureManager().loadTextures(homeTex,restartTex);

		//crown
		Texture crownTex = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		crownReg = TextureRegionFactory.createFromAsset(crownTex, boss, "crwon.png", 0, 0);
		//halo
		Texture haloTex = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		haloReg = TextureRegionFactory.createFromAsset(haloTex, boss, "halo.png", 0, 0);

		boss.getEngine().getTextureManager().loadTextures(homeTex,restartTex,crownTex,haloTex);


	}
	public void setResultScene(String msg,int yourHealth,int enemyHealth,int candyThrown,int candyHit){
		resScene = new Scene(1);
		Log.d("miku","result "+ msg);
		Sprite sp = new Sprite(0,0,gamestatusreg);
		resScene.setBackground(new SpriteBackground(sp));
		if(msg.equals("won")){   
			Sprite crown = new Sprite(boss.CAMERA_WIDTH/2-170,210,crownReg);
			resScene.getLastChild().attachChild(crown);
			Text winmsg = new Text(boss.CAMERA_WIDTH/2-62,260 ,resFont,"Congratulations!!! You Won");
			resScene.getLastChild().attachChild(winmsg);
		}
		else if (msg.equals("lost")){
			Sprite halo = new Sprite(boss.CAMERA_WIDTH/2-170,210,haloReg);
			resScene.getLastChild().attachChild(halo);
			Text lostmsg = new Text(boss.CAMERA_WIDTH/2-62,260 ,resFont,"Sorry , You Lost");
			resScene.getLastChild().attachChild(lostmsg);
		}
		else{
			Text hiatusmsg = new Text(boss.CAMERA_WIDTH/2-62,260 ,resFont,"Fight on hiatus");
			resScene.getLastChild().attachChild(hiatusmsg);
		}

		Text youHealthtxt = new Text(boss.CAMERA_WIDTH/2-150, 85,resFont,"Your Health         :  "+yourHealth);
		Text opponentHealthtxt = new Text(boss.CAMERA_WIDTH/2-150, 115,resFont,"Enemy Health     :  "+enemyHealth);
		Text candyThrowntxt = new Text(boss.CAMERA_WIDTH/2-150, 145,resFont,"Candies Thrown :  "+candyThrown);
		Text candyHittxt = new Text(boss.CAMERA_WIDTH/2-150, 175,resFont,"Candies Hit          :  "+candyHit);
		resScene.getLastChild().attachChild(youHealthtxt);
		resScene.getLastChild().attachChild(opponentHealthtxt);
		resScene.getLastChild().attachChild(candyThrowntxt);
		resScene.getLastChild().attachChild(candyHittxt);


		Sprite home = new Sprite(boss.CAMERA_WIDTH/2-170,boss.CAMERA_HEIGHT-160,homeReg){

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY){

				if(pSceneTouchEvent.isActionUp()){

					setHomeScene();
				}
				return true;
			}

		};
		Text hometxt = new Text(boss.CAMERA_WIDTH/2-140, boss.CAMERA_HEIGHT-52,resFont,"Home");
		resScene.getLastChild().attachChild(hometxt);
		resScene.registerTouchArea(home);
		resScene.getLastChild().attachChild(home);

		Sprite restart = new Sprite(boss.CAMERA_WIDTH/2+20,boss.CAMERA_HEIGHT-160,restartReg){

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY){
				Log.d("miku","restart "+CommChord.getInstance().status.toString());
				if(pSceneTouchEvent.isActionUp()){
					if(CommChord.getInstance().status==CommChord.Status.network_connected && CommChord.getInstance().isOpponentAlive()){
						if(CommChord.getInstance().isOpponentAlive()){
							CommChord.getInstance().sendJoinRequest(CommChord.getInstance().opponentId);
							showWaitingforConfirm();
						}
					}
					else {
						setHomeScene();
					}
				}
				return true;
			}

		};
		Text restxt = new Text(boss.CAMERA_WIDTH/2+50, boss.CAMERA_HEIGHT-52,resFont,"Restart");
		resScene.getLastChild().attachChild(restxt);

		resScene.registerTouchArea(restart);
		resScene.getLastChild().attachChild(restart);
		SceneManager.getManager().curScene=SceneManager.SceneNames.result;
		boss.getEngine().setScene(resScene);
	}


	public void showEnemyPauseDialog(){
		AlertDialog.Builder confExitBuilder = new AlertDialog.Builder(boss);
		confExitBuilder.setTitle("Game Paused");
		confExitBuilder.setMessage("Opponent Paused");
		confExitBuilder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				SceneManager.getManager().getGameSceneObj().finish();
				CommChord.getInstance().sendDisconnectRequest();
			}
		});

		confExitBuilder.setCancelable(false);
		enemyPauseDialog = confExitBuilder.show();
	}
	public void showOwnPauseDialog(){
		AlertDialog.Builder confExitBuilder = new AlertDialog.Builder(boss);
		confExitBuilder.setTitle("Confirm quit");
		confExitBuilder.setMessage("Are you sure you want to Quit?");
		confExitBuilder.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) { 
				CommChord.getInstance().sendResumeRequest();
				if(!SceneManager.getManager().getGameSceneObj().music.isPlaying()){
					SceneManager.getManager().getGameSceneObj().music.play();
				}
			}
		});
		confExitBuilder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				SceneManager.getManager().getGameSceneObj().finish();
				CommChord.getInstance().sendDisconnectRequest();
			}
		});

		confExitBuilder.setCancelable(false);
		ownPauseDialog = confExitBuilder.show();

	}
	public void dismissEnemyPause(){
		if(enemyPauseDialog != null && enemyPauseDialog.isShowing()){
			if(!SceneManager.getManager().getGameSceneObj().music.isPlaying())
				SceneManager.getManager().getGameSceneObj().music.play();
			enemyPauseDialog.dismiss();
		}

	}
	public void dismissOwnPause(){
		if(ownPauseDialog !=null && ownPauseDialog.isShowing()){
			if(SceneManager.getManager().curScene==SceneManager.SceneNames.game){
				if(!SceneManager.getManager().getGameSceneObj().music.isPlaying()){
					SceneManager.getManager().getGameSceneObj().music.play();
				}
			}
			ownPauseDialog.dismiss();
		}
	}
	public static void dissmissWaiting(){
		if(waitingDialog!=null && waitingDialog.isShowing()){
			waitingDialog.dismiss();
		}
		dismissConf();

	}
	public static void dismissConf(){
		if(confDialog!=null && confDialog.isShowing()){

			Log.d("miku","semaphore released at dismissconf");
			confDialog.dismiss();
		}
	}
	public static void dismissDecline(){
		if(declineDialog.isShowing()){
			declineDialog.dismiss();
		}
	}
}
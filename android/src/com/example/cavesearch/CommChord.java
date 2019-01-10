package com.example.cavesearch;

import java.util.List;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Environment;
import android.util.Log;
import com.samsung.chord.*;
import com.samsung.chord.ChordManager.INetworkListener;



public class CommChord implements IChordManagerListener {
	private ChordManager chordM;
	public static final String chordFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Chord";
	private ChannelListener channelListener;
	private IChordChannel channel;
	private boolean isChordActive;
	public static MainActivity boss;

	private WifiP2pManager wifiManager;
	private List peers;
	public enum Status{network_disconnected,opponent_connected,network_connected};
	public Status status;
	public String opponentId;
	public String caveManStatus="none";
	private Channel mChannel;
	private static final CommChord commChord = new CommChord();
	//public final Semaphore availble = new Semaphore(1,true);
	private CommChord(){
		status=Status.network_disconnected;
	}

	public static CommChord getInstance(){
		return commChord;
	}
	public boolean ChordInitiate(MainActivity mAct){
		boss = mAct;

		chordM = ChordManager.getInstance(boss);
		chordM.stop();
		Log.d("miku","line xx");
		chordM.setNetworkListener(new INetworkListener() {

			@Override
			public void onConnected(int interfaceType) {
			}
			@Override
			public void onDisconnected(int interfaceType) {

			}

		});
		chordM.setTempDirectory(chordFilePath);

		chordM.setHandleEventLooper(boss.getMainLooper());
		Log.d("miku","line yy");
		List<Integer> ifaceList = chordM.getAvailableInterfaceTypes();
		for(int i =0;i<ifaceList.size();i++){
			if(ifaceList.get(i) == chordM.INTERFACE_TYPE_WIFI){
				Log.d("miku","wifi interface available");
			}
			if(ifaceList.get(i) == chordM.INTERFACE_TYPE_WIFIAP){
				Log.d("miku","wifi AP interface available");

			}
			if(ifaceList.get(i) == chordM.INTERFACE_TYPE_WIFIP2P){
				Log.d("miku","wifi P2P interface available");

			}
		}
		Log.d("miku","line 2");
		if(ifaceList.size()==0){
			Log.d("miku","No available interface");
			return false;
		}
		else{
			Log.d("miku","nu interface "+ifaceList.size());
			chordM.setTempDirectory(chordFilePath);
			int x =chordM.start(ChordManager.INTERFACE_TYPE_WIFIP2P,this);
			if(x == chordM.ERROR_INVALID_INTERFACE){
				Log.d("miku","error failed");
				return false;
			}
			Log.d("miku","Chord started");

		}		
		return true;
	}

	@Override
	public void onError(int arg0) {

		Log.d("miku","chord start failed");

	}
	@Override
	public void onNetworkDisconnected() {


	}
	@Override
	public void onStarted(String arg0, int arg1) {
		//join channel.
		Log.d("miku","on started called");
		channelListener = new ChannelListener();
		channel = chordM.joinChannel("org.cavesearch",channelListener);
		isChordActive=true;
		status=CommChord.Status.network_connected;
		SceneManager.getManager().updateNodeListDialog();

	}
	public boolean isAnyEnimy(){
		if(this.status==CommChord.Status.network_disconnected)
			return false;
		List<String> nodeList = channel.getJoinedNodeList();
		if(nodeList.size()>0)
			return true;
		else
			return false;
	}
	public CharSequence[] getNodeList(){
		Log.d("miku","getting nodelist");
		CharSequence cs[]={};
		if(isAnyEnimy()){
			List<String> nodeList = channel.getJoinedNodeList();
			cs = nodeList.toArray(new CharSequence[nodeList.size()]);
			return cs;	
		}	
		return cs;

	}
	public void sendDisconnectRequest(){
		if(opponentId!=null && !opponentId.equals("")){
			byte payload[][] =new byte[1][];
			payload[0]="Stomach Ach".getBytes();
			channel.sendData(opponentId, "disconnect", payload);
			Log.d("miku","sent disconnect request");
			status=Status.network_connected;
		}
	}
	public boolean sendPauseRequest(){
		if(opponentId!=null && !opponentId.equals("")){
			byte payload[][] =new byte[1][];
			payload[0]="Stomach Ach".getBytes();
			channel.sendData(opponentId, "pause", payload);
			Log.d("miku","sent pause request");

			return true;
		}
		else return false;
	}
	public boolean sendResumeRequest(){
		if(opponentId!=null && !opponentId.equals("")){
			byte payload[][] =new byte[1][];
			payload[0]="healthy again".getBytes();
			channel.sendData(opponentId, "resume", payload);
			Log.d("miku","sent resume request");

			return true;
		}
		else return false;
	}
	public void sendJoinRequest(String id){
		Log.d("miku","sent request"+SceneManager.getManager().curScene.toString()+ " "+ status.toString());
		if(SceneManager.confDialog!=null && !SceneManager.confDialog.isShowing() ){
			byte payload[][] =new byte[1][];
			payload[0]=this.chordM.getName().getBytes();
			channel.sendData(id, "join_request", payload);

			opponentId=id;
			Log.d("miku","sent request"+SceneManager.getManager().curScene.toString()+ " "+ status.toString());
		}
	}
	public void sendConfirm(String enemyid,boolean stat){
		//availble.release();
		Log.d("miku","semaphore released and sendConfirm");
		byte payload[][] =new byte[1][];
		if(stat)
			payload[0]="true".getBytes();
		else 
			payload[0]="false".getBytes();
		channel.sendData(enemyid, "join_request_confirm", payload);
		Log.d("miku","sent request");


	}
	public void sendBullet(float x,float y){
		Float xI = new Float(x);
		Float yI = new Float(y);
		byte payload[][] =new byte[2][];

		payload[0]= xI.toString().getBytes();
		payload[1]=yI.toString().getBytes();

		channel.sendData(opponentId,"bullet_fired",payload );
		//}

	}
	public void sendReduceHealth(){
		byte payload[][] =new byte[1][];
		payload[0]=this.chordM.getName().getBytes();
		channel.sendData(opponentId, "reduce_health", payload);

	}
	public void sendData(float x,float y){
		//Log.d("miku","first line of send data");
		//Log.d("miku","original data ,"+x+" "+y);
		Float xI = new Float(x);
		Float yI = new Float(y);
		byte payload[][] =new byte[2][];

		payload[0]= xI.toString().getBytes();
		payload[1]=yI.toString().getBytes();
		//Log.d("miku","Transformed "+xS+" "+yS);



		//Log.d("miku","berfore size check");

		if(status==Status.opponent_connected)
			channel.sendData(opponentId,"position",payload );


	}
	public boolean isOpponentAlive(){
		CharSequence cs[] = getNodeList();
		for(int i =0;i<cs.length;i++){
			if(cs[i].equals(opponentId)){
				return true;
			}
		}
		return false;
	}
	public void stopChord(){		
		if(chordM!=null  ){		
			chordM.stop();
		}
		status=Status.network_disconnected;
	}


}
class ChannelListener implements IChordChannelListener{

	@Override
	public void onDataReceived(String arg0, String arg1, String arg2,
			byte[][] arg3) {

		Log.d("miku","arg2 = "+ arg2);
		if(arg1.equals("org.cavesearch")){
			if(arg2.equalsIgnoreCase("join_request")){

				//if(CommChord.getInstance().availble.tryAcquire()){
				//Log.d("miku","semaphore acquired at join request");

				Log.d("miku","join_request"+SceneManager.getManager().curScene.toString()+ " " +CommChord.getInstance().status.toString());
				if(CommChord.getInstance().status==CommChord.Status.network_connected){

					CommChord.getInstance().opponentId=arg0;
					SceneManager.getManager().showConfirmationDialog(arg0);
				}
				//}
		}
			else if(arg2.equalsIgnoreCase("pause")){
				Log.d("miku","received pause request");
				SceneManager.getManager().showEnemyPauseDialog();
			}
			else if(arg2.equals("resume")){
				SceneManager.getManager().dismissEnemyPause();
			}
			else if(arg2.equalsIgnoreCase("join_request_confirm")){
				if(CommChord.getInstance().status==CommChord.Status.network_connected){
					if(new String(arg3[0]).equalsIgnoreCase("true")){
						if(SceneManager.waitingDialog.isShowing()){
							SceneManager.dissmissWaiting();
							Log.d("miku","request accepete");
							CommChord.getInstance().status=CommChord.Status.opponent_connected;
							CommChord.getInstance().caveManStatus="face";
							CommChord.getInstance().opponentId=arg0;
							SceneManager.getManager().setGameScene();
						}
					}
					else{
						CommChord.getInstance().status=CommChord.Status.network_connected;
						//SceneManager.getManager().setHomeScene();
						if(SceneManager.confDialog.isShowing()){
							if(SceneManager.confDialog!=null)
								SceneManager.confDialog.cancel();
						}
						else if(SceneManager.waitingDialog.isShowing())
							SceneManager.dissmissWaiting();
						SceneManager.getManager().showDecline();
						Log.d("miku","request declined");
					}
				}
			}

			else if(arg2.equals("disconnect")){
				Log.d("miku","disconnect request"+CommChord.getInstance().status.toString()+SceneManager.getManager().curScene.toString());
				if(CommChord.getInstance().status==CommChord.Status.opponent_connected){
					CommChord.getInstance().status=CommChord.Status.network_connected;
					if(SceneManager.getManager().curScene==SceneManager.SceneNames.game || SceneManager.waitingDialog.isShowing()){
						if(SceneManager.getManager().curScene==SceneManager.SceneNames.game){
							SceneManager.getManager().getGameSceneObj().finish();
						}
						SceneManager.dissmissWaiting();

					}

				}

			}
			if(SceneManager.getManager().curScene==SceneManager.SceneNames.game && CommChord.getInstance().status==CommChord.Status.opponent_connected && SceneManager.getManager().getGameSceneObj()!=null){
				if(arg2.equals("reduce_health")){
					SceneManager.getManager().getGameSceneObj().reduceOpponentHealth();
				}
				else if(arg2.equals("position") && arg0.equals(CommChord.getInstance().opponentId)){

					SceneManager.getManager().getGameSceneObj().moveOpponent(Float.parseFloat(new String(arg3[0])), Float.parseFloat(new String(arg3[1])));
				}
				else if(arg2.equals("bullet_fired") && arg0.equals(CommChord.getInstance().opponentId)){
					Log.d("miku","bullet fired received");
					SceneManager.getManager().getGameSceneObj().createOpponentBullet(Float.parseFloat(new String(arg3[0])), Float.parseFloat(new String(arg3[1])));
				}
			}

		}
	}

	@Override
	public void onFileChunkReceived(String arg0, String arg1, String arg2,
			String arg3, String arg4, String arg5, long arg6, long arg7) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileChunkSent(String arg0, String arg1, String arg2,
			String arg3, String arg4, String arg5, long arg6, long arg7,
			long arg8) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileFailed(String arg0, String arg1, String arg2,
			String arg3, String arg4, int arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileReceived(String arg0, String arg1, String arg2,
			String arg3, String arg4, String arg5, long arg6, String arg7) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileSent(String arg0, String arg1, String arg2, String arg3,
			String arg4, String arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFileWillReceive(String arg0, String arg1, String arg2,
			String arg3, String arg4, String arg5, long arg6) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNodeJoined(String arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("miku","on node joined is called " +arg0);


		SceneManager.getManager().updateNodeListDialog();

	}

	@Override
	public void onNodeLeft(String arg0, String arg1) {
		if(CommChord.getInstance().status==CommChord.Status.network_connected ){

			SceneManager.getManager().updateNodeListDialog();
		}

		if(arg0.equals(CommChord.getInstance().opponentId)){
			if(CommChord.getInstance().status==CommChord.Status.opponent_connected){
				CommChord.getInstance().status=CommChord.Status.network_connected;
				//CommChord.getInstance().sendDisconnectRequest();
			}
			if(SceneManager.getManager().curScene==SceneManager.SceneNames.game || SceneManager.waitingDialog.isShowing()){
				if(SceneManager.getManager().curScene==SceneManager.SceneNames.game){
					SceneManager.getManager().getGameSceneObj().finish();

				}
				else {
					SceneManager.dissmissWaiting();
					SceneManager.dissmissWaiting();
				}
			}
		}

	}



}


package com.example.nilserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;


@SuppressLint("SdCardPath") public class NilServer extends Activity implements OnClickListener{
	/*static
	{
		System.loadLibrary("nilserver");
	}
	
	public native void start();

	public native void stop();
*/
	public static final String NIL_SERVER_TAG = "hello-ndk" ;
	public static ArrayList<String> CMD = new ArrayList<String>();
	public static ArrayList<String> CHANGE_MODE = new ArrayList<String>();
	static{
		CMD.add("rm -rf /sdcard/nilserver/");	
		CMD.add("rm -rf /sdcard/MobileCEM/");
		CMD.add("rm /data/data/com.hipipal.qpyplus/files/bin/qpython.sh");
		/*cmd.add("mkdir -p /data/local/tmp/nilserver/");
		cmd.add("chmod 777 /data/local/tmp/nilserver/");
		cmd.add("mkdir -p /data/local/tmp/nilserver/lib/");
		cmd.add("chmod 777 /data/local/tmp/nilserver/lib/");*/
		CMD.add("mkdir -p /sdcard/MobileCEM/");
		CMD.add("mkdir -p /sdcard/MobileCEM/IncomingDir/");
		CMD.add("mkdir -p /sdcard/MobileCEM/ProcessingDir/");
		CMD.add("mkdir -p /sdcard/MobileCEM/ProcessedDir/");
		CMD.add("mkdir -p /sdcard/MobileCEM/Log/");
		CMD.add("mkdir -p /sdcard/MobileCEM/OutputDir/");
		CMD.add("mkdir -p /sdcard/MobileCEM/Python/");
		CMD.add("mkdir -p /sdcard/MobileCEM/LocationInfo/");
	}
	
	static{
		CHANGE_MODE.add("chmod 777 /data/");
		CHANGE_MODE.add("chmod 777 /data/local/");
		/*CHANGE_MODE.add("chmod 777 /data/local/tmp/");*/
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/IncomingDir/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/ProcessingDir/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/ProcessedDir/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/Log/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/OutputDir/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/Python/");
		CHANGE_MODE.add("chmod 777 /sdcard/MobileCEM/LocationInfo/");
	}
	private Button start;
	private Button stop;
	//protected Handler progHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nil__server);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initializeView();
		setStrictModeProperty();
		
		//makeDirectory();
		/*//progHandler = new Handler();
		DialogProgressBar.showDialogForInput(this);
		progHandler.post(new Runnable() {
			@Override
			public void run() {
				ProgressCircle.showProgress(NilServer.this);
				}
			});
			*/
		SharedPreferences pref_device = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref_device.getBoolean("NIL_SERVER_INITIALISE", true)){
			Log.d(NIL_SERVER_TAG, "first time called");
			makeDirectory();
			changePermission();
			copyNilServerFile("/data/local/", "config_nilserver.txt");
			copyNilServerFile("/data/local/", "NilServer_tablet");
			pushFilesInMobile();
			Editor editor_device = pref_device.edit(); 
			editor_device.putBoolean("NIL_SERVER_INITIALISE", false);
			editor_device.commit();
		}else{
			Log.d(NIL_SERVER_TAG, "second time called");
			changePermission();
		}
		//DialogProgressBar.dismissDialog();
		//ProgressCircle.destroyProgress();
	}

	private void initializeView() {
		try{
			findViewById(R.id.linear_container).setOnTouchListener(new OnTouchListener() {
				@SuppressLint("ClickableViewAccessibility") @Override
				public boolean onTouch(View v, MotionEvent event) {
					try{
						InputMethodManager input_method_manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
						input_method_manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
						return true;
					}catch (Exception e) {
						return true;
					}
				}
			});
		}catch (NullPointerException e) {
			Log.d(NIL_SERVER_TAG, "initializeView NullPointerException" + e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			Log.d(NIL_SERVER_TAG, "initializeView Exception" + e.getMessage());
			e.printStackTrace();
		}
		start = (Button) findViewById(R.id.button_start);
		start.setOnClickListener(this);
		stop = (Button) findViewById(R.id.button_stop);
		stop.setOnClickListener(this);
		stop.setEnabled(false);
	}

	@SuppressLint("SdCardPath") private void pushFilesInMobile() {
		CopyAssets("/sdcard/MobileCEM/Python/", "python");
		//CopyAssets("/data/local/tmp/nilserver/lib/", "lib");
		copyNilServerFile("/data/local/", "NilServer_tablet");
		copyNilServerFile("/data/local/tmp/", "config_nilserver.txt");
		copyNilServerFile("/data/data/com.hipipal.qpyplus/files/bin/", "qpython-android5.sh");
	}
    
	
	public void makeDirectory(){
		Log.d(NIL_SERVER_TAG, "makeDirectory called");
		for(int i=0; i < CMD.size(); i++){
			runRootCommand(CMD.get(i));
		}
		Log.d(NIL_SERVER_TAG, "makeDirectory stop");
	}
	
	public void changePermission(){
		Log.d(NIL_SERVER_TAG, "changePermission called");
		for(int i=0; i < CHANGE_MODE.size(); i++){
			runRootCommand(CHANGE_MODE.get(i));
		}
		Log.d(NIL_SERVER_TAG, "changePermission stop");
	}
	@Override
	public void onClick(View view){
		switch (view.getId()){
			case R.id.button_start:
				try{
					start.setEnabled(false);
					stop.setEnabled(true);
					Log.d(NIL_SERVER_TAG, "start called");
					killNilServer();
					//start();
					//runRootCommand("export LD_LIBRARY_PATH=/data/local/tmp/nilserver/lib/");
					//runRootCommand("echo $LD_LIBRARY_PATH >> /sdcard/nilLog.txt");
					//runRootCommand("sh /data/local/tmp/nilserver/NilServer_tablet");
					final String cmd =  "./data/local/NilServer_tablet";
					 Thread executeThread = new Thread() {
						   @Override
						   public void run() {
						    try {
						     Log.d(NIL_SERVER_TAG, "startNilServerApp++" + cmd);
						     runRootCommand(cmd);
						    } catch (Exception e) {
						     e.printStackTrace();
						    }
						   }};
						   executeThread.start();
					Log.d(NIL_SERVER_TAG, "start processed");
				}catch (Exception e) {
					Log.d(NIL_SERVER_TAG, "start" + e.getMessage());
				}
				break;
			case R.id.button_stop:
				try{
					stop.setEnabled(false);
					start.setEnabled(true);
					Log.d(NIL_SERVER_TAG, "stop called");
					//stop();
					//int Pid = android.os.Process.getUidForName("./data/local/NilServer_tablet");
					//Log.d(NIL_SERVER_TAG, "Pid value is : "+String.valueOf(Pid));
					//android.os.Process.killProcess(Pid);
					killNilServer();
					Log.d(NIL_SERVER_TAG, "stop processed");
				}catch (Exception e) {
					Log.d(NIL_SERVER_TAG, "stop" + e.getMessage());
				}
				break;
		}
	}

	public void killNilServer()
	{
		Log.d(NIL_SERVER_TAG, "killNilServer++");
		int pid = getNilServerPid();
		try {
			Log.d(NIL_SERVER_TAG, "killing++");
			//DMHandler.isDMInitialzed = false;
			String killCommand = "kill -s 2 " + pid + " \n"; // -s 2
			if (pid != 0) {
				runRootCommand(killCommand);
				pid = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	//	runRootCommand("killall NilServer_tablet");
	}
	
	public int getNilServerPid() {
		int pid = 0;
		Process process2;
		try {
			process2 = Runtime.getRuntime().exec("ps NilServer_tab");
			// read the output of ps
			DataInputStream in = new DataInputStream(
					process2.getInputStream());
			BufferedReader d = new BufferedReader(
					new InputStreamReader(in));
			String temp = d.readLine();
			temp = d.readLine();
			// We apply a regexp to the second line of the ps output
			// to get the
			// pid
			if (temp != null) {
				Log.d(NIL_SERVER_TAG, temp);
				temp = temp.replaceAll("^root *([0-9]*).*", "$1");
				pid = Integer.parseInt(temp);
			}
			Log.d(NIL_SERVER_TAG,"pid = " + pid);
			/*TcpDump temp2 = new TcpDump();
			temp2.PrintLog("TCPDUMP PID =" + pid);*/
			// the ps process is no more needed
			process2.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pid;
	}
	
	
	
	
	public static boolean runRootCommand(String cmd){
		boolean retval = false;
		Process suProcess;
		try{
			suProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
			if (null != os && null != osRes){
				os.writeBytes("id\n");
				os.flush();
				@SuppressWarnings("deprecation")
				String currUid = osRes.readLine();
				boolean exitSu = false;
				if (null == currUid){
					retval = false;
					exitSu = false;
					Log.d(NIL_SERVER_TAG, "Can't get root access or denied by user");
				}else if (true == currUid.contains("uid=0")) {
					retval = true;
					exitSu = true;
					Log.d(NIL_SERVER_TAG, "Root access granted. UID :" + currUid + "Executing  " + cmd);
					os.writeBytes(cmd + "\n");
					os.flush();
				}else {
					retval = false;
					exitSu = true;
					Log.d(NIL_SERVER_TAG, "Root access rejected: " + currUid);
				}if (exitSu){
					os.writeBytes("exit\n");
					os.flush();
				}
			}
		}catch (Exception e){
			retval = false;
			Log.d(NIL_SERVER_TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
		}try {
			Thread.sleep(1000);
		}catch (InterruptedException e)	{
			e.printStackTrace();
			Log.d(NIL_SERVER_TAG, e.getMessage());
		}
		return retval;
	}

	@SuppressLint("SdCardPath") @SuppressWarnings("resource")
	private void CopyAssets(String path, String folder_name) {
	        AssetManager assetManager = getAssets();
	        String[] files = null;
	        try {
	            files = assetManager.list(folder_name);
	        } catch (IOException e) {
	        	Log.d(NIL_SERVER_TAG, e.getMessage());
	        }

	        for(String filename : files) {
	        	Log.d(NIL_SERVER_TAG,"File name => "+filename);
	            InputStream in = null;
	            OutputStream out = null;
	            try {
	              in = assetManager.open(folder_name + "/" + filename);   // if files resides inside the "Files" directory itself
	              Log.d(NIL_SERVER_TAG, path + filename);
	              out = new FileOutputStream(path+ filename);
	              runRootCommand("chmod 777 " + path + filename);
	              out = new FileOutputStream("/sdcard/" + filename);
	              runRootCommand("chmod 777 " + "/sdcard/" + filename);
	              copyFile(in, out);
	              in.close();
	              in = null;
	              out.flush();
	              out.close();
	              out = null;
	              runRootCommand("cp /sdcard/"+ filename +" "+ path + filename);
	              runRootCommand("chmod 777 " + path + filename);
	      		  runRootCommand("rm /sdcard/"+ filename);
	            } catch(Exception e) {
	            	Log.d(NIL_SERVER_TAG, e.getMessage());
	            }
	        }
	}
	 
	@SuppressLint("SdCardPath") private void copyNilServerFile(String path, String filename){
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		try{
			in = assetManager.open(filename);
			/*File outFile = new File(path + filename);
			runRootCommand("chmod 777 " + path + filename);*/
			File outFile = new File("/sdcard/" + filename);
			runRootCommand("chmod 777 " + "/sdcard/" + filename);
			out = new FileOutputStream(outFile);
			copyFile(in, out);
			
			runRootCommand("cp /sdcard/"+ filename +" "+ path + filename);
			runRootCommand("chmod 777 " + path + filename);
    		runRootCommand("rm /sdcard/"+ filename);
		}catch (IOException e){
			Log.d(NIL_SERVER_TAG, "Failed to copy asset file: " + filename, e);
		}finally{
			if (in != null)	{
				try	{
					in.close();
				}catch (IOException e){
					// NOOP
					Log.d(NIL_SERVER_TAG, e.getMessage());
				}
			}if (out != null){
				try	{
					out.close();
				}catch (IOException e){
					// NOOP
					Log.d(NIL_SERVER_TAG, e.getMessage());
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
	
	public void onBackPressed(){
		new AlertDialog.Builder(this).setTitle("Exit").setMessage("Are you sure you want to exit?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener()	{
				@Override
				public void onClick(DialogInterface dialog, int which)	{
					finish();
				}
			}).setNegativeButton("No", null).show();
	}
	
	public void setStrictModeProperty() {
		StrictMode.ThreadPolicy policy = new
		StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}
}

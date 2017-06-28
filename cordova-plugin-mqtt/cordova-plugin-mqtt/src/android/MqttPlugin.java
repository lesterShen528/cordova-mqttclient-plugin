package com.MqttPlugin;

import android.os.Environment;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.eclipse.moquette.server.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class MqttPlugin extends CordovaPlugin {

	private Server server;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		makeFile("moquette.conf");
		makeFile("password_file.conf");
	}

	@Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		try {
            if ("open".equals(action)) {
				final Server newServer = getServer();
				try {
					newServer.startServer();
				} catch (IOException e) {
					Log.w("moquette","Open server failed, Please check your configuration");
				}
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						newServer.stopServer();
					}
				});
				callbackContext.success("open success");
				return true;
            }
            if("close".equals(action)) {
				if(server != null){
					try{
						server.stopServer();
					} catch (Exception e) {
						Log.w("moquette","There are some problems that cause the server to fail,but the server is still shut down");
					}
				}
				callbackContext.success("close success");
				return true;
			}
			if("setConfig".equals(action)){
				if(setConfig(args.getString(0),args.getString(1),args.getString(2),args.getString(3))) {
					callbackContext.success("Set config success");
					return true;
				}
				callbackContext.success("Set config fial, Please check your configuration");
				return true;
			}
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            Log.w("Exception: " , e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
	}

	private Server getServer(){
		if(server == null) {
			server = new Server();
		}
		return server;
	}

	private void makeFile(String filename) {
		try{
			InputStream is = webView.getContext().getAssets().open("config/" + filename);
			String path = Environment.getExternalStorageDirectory() + File.separator + filename;
			File f = new File(path);
			if (!f.exists()) {
				FileOutputStream fos = new FileOutputStream(path);
				byte[] b = new byte[1024];
				while ((is.read(b)) != -1) {
					fos.write(b);
				}
				is.close();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean setConfig(String port, String host, String username, String password) throws IOException {
		if(port != null && host != null && username != null && password != null && !"".equals(port) &&	!"".equals(host) &&	!"".equals(username) &&	!"".equals(password) ) {
			String path = Environment.getExternalStorageDirectory() + File.separator + "moquette.conf";
			File f = new File(path);
			if(f.exists()) {
				f.delete();
			}
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(("port " + port + "\n").getBytes());
			fos.write(("host " + host + "\n").getBytes());
			fos.write(("password_file /storage/emulated/0/password_file.conf").getBytes());
			fos.flush();
			fos.close();
			String Ppath = Environment.getExternalStorageDirectory() + File.separator + "password_file.conf";
			File pf = new File(Ppath);
			if(pf.exists()) {
				pf.delete();
			}
			FileOutputStream pfos = new FileOutputStream(Ppath);
			pfos.write((username + ":" + password).getBytes());
			pfos.flush();
			pfos.close();
			return true;
		}
		return false;
	}
}
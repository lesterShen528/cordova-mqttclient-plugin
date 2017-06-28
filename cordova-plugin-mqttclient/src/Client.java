package org.apache.cordova.mqttclient;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shenlele on 2017/4/18.
 */

public class Client extends CordovaPlugin {
    private Map<String, MqttConnect> clientMap = new HashMap<String, MqttConnect>();
    private CallbackContext callback;
    private CordovaWebView webView;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.webView = webView;
    }

    @Override
    public boolean execute(String action, final JSONArray jsonArray, final CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;

        final JSONObject json = jsonArray.getJSONObject(0);
        final String type = json.getString("type");

        if(clientMap.get(type) == null) {
            clientMap.put(type, new MqttConnect());
        }

        if("connect".equals(action)) {
            final Map<String,String> map = new HashMap<String, String>();

            map.put("type", type);
            if (!"local".equals(type)) {
                map.put("host", "ssl://" + json.getString("host") + ":" + json.getString("port"));
                map.put("key", json.getString("key"));
                map.put("cert", json.getString("cert"));
                map.put("ca", json.getString("ca"));
            } else {
                map.put("host", "tcp://" + json.getString("host") + ":" + json.getString("port"));
            }

            map.put("cid", json.getString("cid"));
            map.put("ka", String.valueOf(json.getInt("ka")));
            map.put("cleanSess", String.valueOf(json.getBoolean("cleanSess")));
            map.put("connTimeOut", String.valueOf(json.getInt("connTimeOut")));
            map.put("username", json.getString("username"));
            map.put("password", json.getString("password"));
            map.put("topic", json.getString("topic"));
            map.put("willQos", String.valueOf(json.getInt("qos")));

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    MqttConnect client = clientMap.get(type);

                    client.getconnection(callback, webView, map);
                }
            });
            return true;
        }

        if("publish".equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String topic = json.getString("topic");
                        String payload = json.getString("payLoad");
                        int qos = json.getInt("qos");
                        boolean retained = json.getBoolean("retained");

                        MqttConnect mqttClient = clientMap.get(type);
                        mqttClient.publish(callback,type,topic,payload,qos,retained);
                    }catch(JSONException e){
                        e.printStackTrace();
                        callback.error(e.toString());
                    }
                }
            });
            return true;
        }

        if("disconnect".equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    MqttConnect client = clientMap.get(type);

                    client.disconnect(callback,type);
                }
            });
            return true;
        }

        if("receive".equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    MqttConnect client = clientMap.get(type);

                    client.receive(callback, type);
                }
            });
            return true;
        }
        callback.error("没有对应action");
        return false;
    }
}

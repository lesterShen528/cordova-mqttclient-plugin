package org.apache.cordova.mqttclient;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Mqtt的连接类
 * Created by shenlele on 2017/4/20.
 */

public class MqttConnect {
    /**
     * MqttClient实例
     */
    private MqttClient client;
    /**
     * 连接参数
     */
    private MqttConnectOptions options;
    /**
     * 用来存放消息
     */
    private BlockingQueue queue = new ArrayBlockingQueue(10);

    /**
     * mqtt连接的方法
     * @param webView
     * @param map 连接参数以键值对放在map里面了
     */
    public void getconnection(final CallbackContext callback, CordovaWebView webView, Map<String, String> map){
        String type = map.get("type");
        try {
            if(client == null) {
                client = new MqttClient(map.get("host"), map.get("cid"), new MemoryPersistence());

                options = new MqttConnectOptions();

                if (!"local".equals(type)) {
                    options.setSocketFactory(SslUtil.getSocketFactory(
                            webView, map.get("ca"), map.get("cert"), map.get("key"), "66")
                    );
                }

                options.setCleanSession(Boolean.parseBoolean(map.get("cleanSess")));

                options.setUserName(map.get("username"));

                options.setPassword(map.get("password").toCharArray());

                options.setConnectionTimeout(Integer.parseInt(map.get("connTimeOut")));

                options.setKeepAliveInterval(Integer.parseInt(map.get("ka")));

                client.setCallback(new MqttCallback() {

                    @Override
                    public void connectionLost(Throwable cause) {
                        try {
                            client.connect(options);
                        } catch (MqttException e) {
                            e.printStackTrace();
                            callback.error(e.toString());
                        }
                    }

                    @Override
                    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                        try {
                            queue.put(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            callback.error(e.toString());
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                client.connect(options);

                int[] qos = {Integer.parseInt(map.get("willQos"))};

                String[] s_topic = {map.get("topic")};

                client.subscribe(s_topic, qos);

                callback.success(type + "连接成功");
            }else{
                callback.success(type + "不能重复连接");
            }
        }catch (Exception e){
            e.printStackTrace();
            callback.error(e.toString());
        }
    }

    /**
     * 发布消息方法
     * @param callback  回调函数
     * @param type      连接类型
     * @param topic     发布消息的目标主题
     * @param payload   消息内容
     * @param qos
     * @param retained
     */
    public void publish(CallbackContext callback,String type,String topic, String payload, int qos, boolean retained){
        try {
            if (client != null) {
                client.publish(topic, payload.getBytes(), qos, retained);
                callback.success(type + "发布成功");
            }else{
                callback.success(type + "没有连接服务器");
            }
        }catch (MqttException e){
            e.printStackTrace();
            callback.error(e.toString());
        }
    }

    /**
     * 断开连接的方法
     * @param callback   回调函数
     * @param type       连接类型
     */
    public void disconnect(CallbackContext callback,String type){
        try {
            if (client != null) {
                client.disconnect();
                client = null;
                callback.success(type + "断开连接");
            }else{
                callback.success(type + "连接已经是关闭的");
            }
        }catch (MqttException e){
            e.printStackTrace();
            callback.error(e.toString());
        }
    }

    /**
     * 接收消息
     * @param callback  回调函数
     * @param type      连接类型
     */
    public void receive(CallbackContext callback,String type){
        try {
            if (client != null) {
                String message = queue.take().toString();
                callback.success(message);
            } else {
                callback.error(type + "没有连接服务器");
            }
        }catch (InterruptedException e){
            e.printStackTrace();
            callback.error(e.toString());
        }
    }
}

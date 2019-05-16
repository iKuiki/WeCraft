package cn.kuiki.mc.wgclient;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.logging.Logger;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.Thread;
import cn.kuiki.mc.WeCraftInf;

public final class WGClient extends Thread implements MqttCallback {

    private Logger logger;
    private WGConfig config;
    private LinkedBlockingQueue<String> sendMessageQueue;
    private WeCraftInf wecraft;

    public WGClient(WGConfig config, Logger logger, WeCraftInf wecraft) {
        this.config = config;
        this.logger = logger;
        this.wecraft = wecraft;
        this.sendMessageQueue = new LinkedBlockingQueue<String>();
    }

    public void run() {
        String clientId = "WeCraft";
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqttClient;
        try {
            mqttClient = new MqttClient(this.config.hostURL, clientId, persistence);
            this.createConn(mqttClient);
            mqttClient.setCallback(this);
            while (true) {
                String msg = this.sendMessageQueue.take();
                this.logger.info("sending text " + msg);
                mqttClient.publish("MCGate/HD_Say", msg.getBytes(), 0, false);
            }
        } catch (MqttException me) {
            this.logger.info("reason " + me.getReasonCode());
            this.logger.info("msg " + me.getMessage());
            this.logger.info("loc " + me.getLocalizedMessage());
            this.logger.info("cause " + me.getCause());
            this.logger.info("excep " + me);
            me.printStackTrace();
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    private void createConn(MqttClient mqttClient) throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setAutomaticReconnect(true);
        this.logger.info("Connecting to broker: " + this.config.hostURL);
        mqttClient.connect(connOpts);
        this.logger.info("Connected");
    }

    public void sendTextMessageToChatroom(String user, String content) {
        try {
            this.sendMessageQueue.put("{\"user\":\"" + user + "\",\"content\":\"" + content + "\"}");
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void connectionLost(Throwable cause) {
        System.out.println("connection to WeCraftManager lost: " + cause);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        switch (topic) {
        case "WeCraft/Say":
            this.wecraft.broadcast(message.toString());
            break;
        default:
            this.logger.info("recive unknown topic: " + topic);
            break;
        }
    }
}

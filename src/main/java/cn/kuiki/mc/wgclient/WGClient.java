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
    private LinkedBlockingQueue<WGItem> sendMessageQueue;
    private WeCraftInf wecraft;

    public WGClient(WGConfig config, Logger logger, WeCraftInf wecraft) {
        this.config = config;
        this.logger = logger;
        this.wecraft = wecraft;
        this.sendMessageQueue = new LinkedBlockingQueue<WGItem>();
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
                WGItem msg = this.sendMessageQueue.take();
                this.logger.info("sending text " + msg);
                mqttClient.publish("MCGate/" + msg.getTopic(), msg.getMessage().getBytes(), 0, false);
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

    @Deprecated
    public void sendTextMessageToChatroom(String user, String content) {
        try {
            this.sendMessageQueue
                    .put(new WGItem("HD_Say", "{\"user\":\"" + user + "\",\"content\":\"" + content + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendRegisterMessageToChatroom() {
        try {
            this.sendMessageQueue
                    .put(new WGItem("HD_Register", "{\"clientName\":\"" + this.config.getServerName() + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendPlayerJoinMessageToChatroom(String playerName) {
        try {
            this.sendMessageQueue.put(new WGItem("HD_PlayerJoin", "{\"playerName\":\"" + playerName + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendPlayerLeaveMessageToChatroom(String playerName) {
        try {
            this.sendMessageQueue.put(new WGItem("HD_PlayerLeave", "{\"playerName\":\"" + playerName + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendPlayerDeathMessageToChatroom(String playerName, String deathMessage) {
        try {
            this.sendMessageQueue.put(new WGItem("HD_PlayerDeath",
                    "{\"playerName\":\"" + playerName + "\",\"deathMessage\":\"" + deathMessage + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendPlayerChatMessageToChatroom(String playerName, String chatMessage) {
        try {
            this.sendMessageQueue.put(new WGItem("HD_PlayerChat",
                    "{\"playerName\":\"" + playerName + "\",\"chatMessage\":\"" + chatMessage + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendPlayerAdvancementDoneMessageToChatroom(String playerName, String advancementKey) {
        try {
            this.sendMessageQueue.put(new WGItem("HD_PlayerAdvancementDone",
                    "{\"playerName\":\"" + playerName + "\",\"advancementKey\":\"" + advancementKey + "\"}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void sendPingMessageToChatroom(String playerName, String advancementKey) {
        try {
            this.sendMessageQueue.put(new WGItem("HD_Ping", "{}"));
        } catch (InterruptedException e) {
            this.logger.info("msg queue.take InterruptedException" + e);
        }
    }

    public void connectionLost(Throwable cause) {
        this.logger.info("connection to WeCraftManager lost: " + cause);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        switch (topic) {
        case "WeCraft/Say":
            this.wecraft.broadcast(message.toString());
            break;
        case "WeCraft/NeedLogin":
            this.sendRegisterMessageToChatroom();
            break;
        default:
            this.logger.info("recive unknown topic: " + topic);
            break;
        }
    }
}

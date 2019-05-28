package cn.kuiki.mc.wgclient;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.Thread;
import cn.kuiki.mc.WeCraftInf;

public final class WGClient extends Thread implements MqttCallback {

    private Logger logger;
    private WGConfig config;
    private ConcurrentLinkedQueue<WGItem> sendMessageQueue;
    private WeCraftInf wecraft;
    private MqttClient mqttClient;

    public WGClient(WGConfig config, Logger logger, WeCraftInf wecraft) throws MqttException {
        this.config = config;
        this.logger = logger;
        this.wecraft = wecraft;
        this.sendMessageQueue = new ConcurrentLinkedQueue<WGItem>();
        String clientId = "WeCraft";
        MemoryPersistence persistence = new MemoryPersistence();
        this.mqttClient = new MqttClient(this.config.hostURL, clientId, persistence);
        this.mqttClient.setCallback(this);
    }

    public Runnable keepConn = new Runnable() {
        @Override
        public void run() {
            if (!mqttClient.isConnected()) {
                // 如果未连接，则连接
                MqttConnectOptions connOpts = new MqttConnectOptions();
                // connOpts.setAutomaticReconnect(true); // 不设置自动连接了，断线则手动检查并重连
                logger.info("Connecting to broker: " + config.hostURL);
                try {
                    mqttClient.connect(connOpts);
                    logger.info("Connected");
                    sendRegisterMessageToChatroom();
                } catch (MqttException e) {
                    logger.info("connect to broker exception: " + e.getMessage());
                    if (mqttClient.isConnected()) {
                        try {
                            mqttClient.disconnect();
                        } catch (MqttException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    public Runnable sendLoop = new Runnable() {
        @Override
        public void run() {
            if (mqttClient.isConnected()) {
                WGItem msg = sendMessageQueue.remove();
                while (msg != null) {
                    logger.info("sending text " + msg);
                    try {
                        mqttClient.publish("MCGate/" + msg.getTopic(), msg.getMessage().getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    msg = sendMessageQueue.remove();
                }
            }
        }
    };

    @Deprecated
    public void sendTextMessageToChatroom(String user, String content) {
        sendMessageQueue.add(new WGItem("HD_Say", "{\"user\":\"" + user + "\",\"content\":\"" + content + "\"}"));
    }

    public void sendRegisterMessageToChatroom() throws MqttPersistenceException, MqttException {
        this.logger.info("register to server ");
        String payload = "{\"clientName\":\"" + this.config.getServerName() + "\"}";
        this.mqttClient.publish("MCGate/HD_Register", payload.getBytes(), 0, false);
    }

    public void sendPlayerJoinMessageToChatroom(String playerName) {
        this.sendMessageQueue.add(new WGItem("HD_PlayerJoin", "{\"playerName\":\"" + playerName + "\"}"));
    }

    public void sendPlayerLeaveMessageToChatroom(String playerName) {
        this.sendMessageQueue.add(new WGItem("HD_PlayerLeave", "{\"playerName\":\"" + playerName + "\"}"));
    }

    public void sendPlayerDeathMessageToChatroom(String playerName, String deathMessage) {
        this.sendMessageQueue.add(new WGItem("HD_PlayerDeath",
                "{\"playerName\":\"" + playerName + "\",\"deathMessage\":\"" + deathMessage + "\"}"));
    }

    public void sendPlayerChatMessageToChatroom(String playerName, String chatMessage) {
        this.sendMessageQueue.add(new WGItem("HD_PlayerChat",
                "{\"playerName\":\"" + playerName + "\",\"chatMessage\":\"" + chatMessage + "\"}"));
    }

    public void sendPlayerAdvancementDoneMessageToChatroom(String playerName, String advancementKey) {
        this.sendMessageQueue.add(new WGItem("HD_PlayerAdvancementDone",
                "{\"playerName\":\"" + playerName + "\",\"advancementKey\":\"" + advancementKey + "\"}"));
    }

    public void sendPingMessageToChatroom(String playerName, String advancementKey) {
        this.sendMessageQueue.add(new WGItem("HD_Ping", "{}"));
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

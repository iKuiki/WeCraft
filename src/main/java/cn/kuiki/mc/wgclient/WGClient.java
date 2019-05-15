package cn.kuiki.mc.wgclient;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.logging.Logger;

public final class WGClient {

    private Logger logger;

    public WGClient(Logger logger) {
        this.logger = logger;
    }

    public void Test() {
        String topic = "MQTT Examples";
        String content = "Message from MqttPublishSample";
        int qos = 2;
        String broker = "tcp://iot.eclipse.org:1883";
        String clientId = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            this.logger.info("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            this.logger.info("Connected");
            this.logger.info("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            this.logger.info("Message published");
            sampleClient.disconnect();
            this.logger.info("Disconnected");
        } catch (MqttException me) {
            this.logger.info("reason " + me.getReasonCode());
            this.logger.info("msg " + me.getMessage());
            this.logger.info("loc " + me.getLocalizedMessage());
            this.logger.info("cause " + me.getCause());
            this.logger.info("excep " + me);
            me.printStackTrace();
        }
    }
}

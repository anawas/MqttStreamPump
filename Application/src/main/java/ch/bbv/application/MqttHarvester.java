/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bbv.application;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 *
 * @author andreaswassmer
 */
public class MqttHarvester implements MqttCallback, Runnable {

    String brokerUrl = "tcp://m2m.eclipse.org:1883";
    String clientId = UUID.randomUUID().toString();
    MqttClient client;
    int qos = 1;
    
    public void connectionLost(Throwable thrwbl) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Connection to {0} lost! {1}", new Object[]{this.brokerUrl, thrwbl.getLocalizedMessage()});
        System.exit(-1);
    }

    public void messageArrived(String topic, MqttMessage mm) throws Exception {
        String time = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println("Time:\t" +time +
            "  Topic:\t" + topic + 
            "  Message:\t" + new String(mm.getPayload()) +
            "  QoS:\t" + mm.getQos());    
    }

    public void deliveryComplete(IMqttDeliveryToken imdt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Should run on a thread
     */
    public void run() {
            String tmpDir = System.getProperty("java.io.tmpdir");
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
            
            MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(true);
            
            try {
            client = new MqttClient(this.brokerUrl, clientId, dataStore);
            client.setCallback(this);
            client.connect(conOpt);
            client.subscribe("#", this.qos);
        } catch (MqttException ex) {
            Logger.getLogger(MqttHarvester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

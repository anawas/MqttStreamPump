package ch.bbv.application;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            MqttClient client;
            
            client = new MqttClient("tcp://m2m.eclipse.org:1883", "FF22264E2");
            client.connect();
            MqttMessage message = new MqttMessage();
            message.setPayload("drums".getBytes());
            client.publish("fiddlerOnTheRoof/instrument", message);
            client.disconnect();
            System.out.println( "Hello World!" );
        } catch (MqttException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

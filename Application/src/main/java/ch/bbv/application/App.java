package ch.bbv.application;

/**
 * Main class.
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        MqttHarvester harvester = new MqttHarvester();
        new Thread(harvester).start();
   }
}

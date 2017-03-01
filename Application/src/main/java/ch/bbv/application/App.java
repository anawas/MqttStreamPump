package ch.bbv.application;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        MqttHarvester harvester = new MqttHarvester();
        Thread harvesterThread = new Thread(harvester);
        harvesterThread.run();
        
    }
}

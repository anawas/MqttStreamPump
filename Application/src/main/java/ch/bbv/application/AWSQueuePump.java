package ch.bbv.application;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * This class pumps the MQTT stream from the MqttHarvester class to the AWS
 * Kinesis Stream.
 *
 * @author andreaswassmer
 */
public class AWSQueuePump implements Runnable {

    static String streamName = "MqttDataStream";
    
    // Because region Frankfurt (eu-central-1) does not have the Firehose
    // service we use the one in Ireland.
    static Region streamRegion = Region.getRegion(Regions.EU_WEST_1);

    ArrayList<String> messageBuffer = new ArrayList();

    public void setMessageBuffer(ArrayList<String> messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    public static void main(String[] main) {
        AWSCredentials credentials = null;

        credentials = new ProfileCredentialsProvider().getCredentials();

        AmazonKinesisClient akc = new AmazonKinesisClient(credentials);
        akc.setRegion(streamRegion);
        PutRecordsRequest putRequest = new PutRecordsRequest();
        putRequest.setStreamName(streamName);
        ArrayList<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList();

        for (int i = 0; i < 100; i++) {
            PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
            putRecordsRequestEntry.setData(ByteBuffer.wrap(String.valueOf(i).getBytes()));
            putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", i));
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }

        putRequest.setRecords(putRecordsRequestEntryList);
        PutRecordsResult putRecordsResult = akc.putRecords(putRequest);
        System.out.println("Put Result" + putRecordsResult);
    }

    public void run() {
        AWSCredentials credentials = null;

        credentials = new ProfileCredentialsProvider().getCredentials();

        AmazonKinesisClient akc = new AmazonKinesisClient(credentials);
        akc.setRegion(streamRegion);
        PutRecordsRequest putRequest = new PutRecordsRequest();
        putRequest.setStreamName(streamName);
        ArrayList<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList();

        int i = 0;
        for (String payload: this.messageBuffer) {
            if (i % 10 > 0) System.out.println("payload: " + payload);
            PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
            putRecordsRequestEntry.setData(ByteBuffer.wrap(payload.getBytes()));
            putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", i++));
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }

        putRequest.setRecords(putRecordsRequestEntryList);
        //PutRecordsResult putRecordsResult = akc.putRecords(putRequest);
        //System.out.println("Put Result" + putRecordsResult);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bbv.application.lambda;

import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 *
 * @author andreaswassmer
 */
public class KinesisToFirehose {
    // Kinesis Firehose is not available in Frankfurt but only in Ireland (eu-west-1)
    private final String firehoseEndpointURL = "https://firehose.eu-west-1.amazonaws.com";
    private final String deliveryStreamName = "blogfirehose";
    private final String deliveryStreamRoleARN = "arn:aws:iam::<AWS Acct Id>:role/firehose_blog_role";
    private final String targetBucketARN = "arn:aws:s3:::dgraeberaws-blogs";
    private final String targetPrefix = "blogoutput/";
    private final int intervalInSec = 60;
    private final int buffSizeInMB = 2;

    private AmazonKinesisFirehoseClient firehoseClient = new AmazonKinesisFirehoseClient();
    private LambdaLogger logger;

    public void kinesisHandler(KinesisEvent event, Context context){
        logger = context.getLogger();
        setup();
        for(KinesisEvent.KinesisEventRecord rec : event.getRecords()) {
            logger.log("Got message ");
            String msg = new String(rec.getKinesis().getData().array())+"\n";
            Record deliveryStreamRecord = new Record().withData(ByteBuffer.wrap(msg.getBytes()));

            PutRecordRequest putRecordRequest = new PutRecordRequest()
                    .withDeliveryStreamName(deliveryStreamName)
                    .withRecord(deliveryStreamRecord);

            logger.log("Putting message");
            firehoseClient.putRecord(putRecordRequest);
            logger.log("Successful Put");
        }
    }

    private void setup(){
        firehoseClient = new AmazonKinesisFirehoseClient();
        firehoseClient.setEndpoint(firehoseEndpointURL);
        checkHoseStatus();
    }

    private void checkHoseStatus(){
        DescribeDeliveryStreamRequest describeHoseRequest = new DescribeDeliveryStreamRequest()
                .withDeliveryStreamName(deliveryStreamName);
        DescribeDeliveryStreamResult  describeHoseResult = null;
        String status = "UNDEFINED";
        try {
            describeHoseResult = firehoseClient.describeDeliveryStream(describeHoseRequest);
            status = describeHoseResult.getDeliveryStreamDescription().getDeliveryStreamStatus();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            logIt("Firehose Not Existent...will create");
            createFirehose();
            checkHoseStatus();
        }
        if(status.equalsIgnoreCase("ACTIVE")){
            logIt("Firehose ACTIVE");
            //return;
        }
        else if(status.equalsIgnoreCase("CREATING")){
            logIt("Firehose CREATING");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkHoseStatus();
        }
        else {
            logIt("Status = " + status);
        }
    }

    private void createFirehose(){
        BufferingHints buffHints = new BufferingHints()
                .withIntervalInSeconds(intervalInSec)
                .withSizeInMBs(buffSizeInMB);

        S3DestinationConfiguration s3DestConf = new S3DestinationConfiguration()
                .withBucketARN(targetBucketARN)
                .withCompressionFormat(CompressionFormat.UNCOMPRESSED)
                .withPrefix(targetPrefix)
                .withBufferingHints(buffHints)
                .withRoleARN(deliveryStreamRoleARN);

        CreateDeliveryStreamRequest createHoseRequest = new  CreateDeliveryStreamRequest()
                .withDeliveryStreamName(deliveryStreamName)
                .withS3DestinationConfiguration(s3DestConf);

        logIt("Sending create firehose request");
        firehoseClient.createDeliveryStream(createHoseRequest);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void logIt(String message){
        if(logger!=null)
            logger.log(message);
        else
            System.out.println(message);
    }

    private void listFirehose(){
        ListDeliveryStreamsRequest listHosesRequest = new ListDeliveryStreamsRequest();
        ListDeliveryStreamsResult lhr = firehoseClient.listDeliveryStreams(listHosesRequest);

        for(String name:lhr.getDeliveryStreamNames()){
            logIt(name);
        }
    }


    private void deleteFirehose(){
        deleteFirehose(deliveryStreamName);
    }

    private void deleteFirehose(String delivStreamName){
        DeleteDeliveryStreamRequest deleteHoseRequest= new DeleteDeliveryStreamRequest();
        deleteHoseRequest.setDeliveryStreamName(delivStreamName);
        firehoseClient.deleteDeliveryStream(deleteHoseRequest);
    }


    private void putSampleMessages(){
        setup();
        for(int i = 0; i<20000; i++) {
            String message = "{\"timestamp\":\"" + new Date().getTime() + "\"}";
            Record record = new Record()
                    .withData(ByteBuffer.wrap(message.getBytes()));
            PutRecordRequest putRecordInHoseRequest = new PutRecordRequest()
                    .withDeliveryStreamName(deliveryStreamName)
                    .withRecord(record);

            PutRecordResult res = firehoseClient.putRecord(putRecordInHoseRequest);
            logIt(res.toString());
        }
    }


    //FOR TESTING LOCALLY ONLY
//    public static void main(String[] args){
        //KinesisToFirehose kinesisToFirehose = new KinesisToFirehose();
        //kinesisToFirehose.checkHoseStatus();
        //kinesisToFirehose.listFirehose();
        //kinesisToFirehose.setup();
        //kinesisToFirehose.putSampleMessages();
//    }
}

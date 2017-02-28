# MqttStreamPump

Gets data from an MQTT broker and sends it to an Amazon SQS for further processing. This app is used for demonstration purposes only. 

### Details
This code builds an app that subscribes to the public MQTT broker on test.mosquitto.org. This broker is used for debugging and most of the topics are useless. But there is a topic delivering subtitles from BBC television series. It has a huge number of english words. Altogether these words form a nice huge text which I want to use for some Big Data analysis. Now the first step is to get these words into AWS. I planned to store the stream into AWS S3 buckets for further processing.

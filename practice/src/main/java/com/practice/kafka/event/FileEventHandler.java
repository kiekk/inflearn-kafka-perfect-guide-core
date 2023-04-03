package com.practice.kafka.event;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class FileEventHandler implements EventHandler {
    public static final Logger logger = LoggerFactory.getLogger(FileEventHandler.class.getName());

    private KafkaProducer<String, String> kafkaProducer;
    private String topicName;
    private boolean sync;

    public FileEventHandler(KafkaProducer<String, String> kafkaProducer, String topicName, boolean sync) {
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
        this.sync = sync;
    }

    @Override
    public void onMessage(MessageEvent messageEvent) throws InterruptedException, ExecutionException {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(this.topicName, messageEvent.getKey(), messageEvent.getValue());

        if (this.sync) {
            RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
            logger.info("\n ##### record metadata received ##### \n");
            logger.info("partition : {}", recordMetadata.partition());
            logger.info("offset : {}", recordMetadata.offset());
            logger.info("timestamp : {}", recordMetadata.timestamp());
        } else {
            this.kafkaProducer.send(producerRecord, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("\n ##### record metadata received ##### \n");
                    logger.info("partition : {}", metadata.partition());
                    logger.info("offset : {}", metadata.offset());
                    logger.info("timestamp : {}", metadata.timestamp());
                } else {
                    logger.error("exception error from broker {}", exception.getMessage());
                }
            });
        }
    }
}

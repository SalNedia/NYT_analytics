package kafka;

import org.apache.kafka.clients.producer.Producer;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Simulator {
    public static final long converterFactor=1000;
    public static void main(String[] args) throws IOException, InterruptedException {
        //Read dataset
        File file = new File("data/"+"CommentsApril2017.csv");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line,next="";
        Producer<String, String> producer = ProducerKafka.setConfig();
        while((line=reader.readLine())!=null){
            //Produce on Kafka
            ProducerKafka.produce(producer,line);
            if((next=reader.readLine())!=null){
                long firstApproveDate = Long.parseLong(line.split(",", -1)[0]);
                long nextApproveDate = Long.parseLong(line.split(",", -1)[0]);
                long time=(nextApproveDate-firstApproveDate); //Difference time between first time and the next
                TimeUnit.MILLISECONDS.sleep(time);
                ProducerKafka.produce(producer,next);
            }

        }
        producer.close();
    }




}
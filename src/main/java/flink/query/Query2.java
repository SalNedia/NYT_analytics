package flink.query;
import flink.utils.flink.query2.*;
import flink.utils.other.MonthlyWindowTum;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple15;
import org.apache.flink.api.java.tuple.Tuple16;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Instant;


public class Query2 {

    public static void process(DataStream<Tuple15<Long, String, Long, Long, String, Long, Integer, String, Long, String, Long, String, String, Long, String>> stream) {

        //Compute count each 2 hours
        DataStream<Tuple3<String, Integer, Long>> countHours = stream
                .filter(x -> x.f0 != -1)
                .map(x -> Query2Parser.parse(x))
                .returns(Types.TUPLE(Types.STRING, Types.STRING, Types.INT))
                .filter(x -> x.f1.equals("comment"))
                .map(x -> Query2Parser.removeCommentType(x))
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(0)
                .window(TumblingEventTimeWindows.of(Time.hours(2)))
                .aggregate(new CountAggregateComments(), new CountProcessComments())
                .setParallelism(3);


        //Week statistics
        DataStream<Tuple3<String, Integer, Long>> countWeekly = countHours
                .keyBy(0)
                .window(TumblingEventTimeWindows.of(Time.days(7), Time.days(-3)))
                .aggregate(new CountAggregateIntermediateComments(), new CountProcessComments())
                .setParallelism(3);

        //Monthly statistics
        DataStream<Tuple3<String, Integer, Long>> countMonthly = countHours
                .keyBy(0)
                .window(new MonthlyWindowTum())
                .aggregate(new CountAggregateIntermediateComments(), new CountProcessComments())
                .setParallelism(3);


        //Getting result
        countHours
                .windowAll(TumblingEventTimeWindows.of(Time.days(1)))
                .apply(new Query2Result())
                .writeAsText("commentdaily", FileSystem.WriteMode.OVERWRITE);

        countWeekly
                .timeWindowAll(Time.milliseconds(1))
                .apply(new Query2Result())
                .writeAsText("commentweekly",FileSystem.WriteMode.OVERWRITE);

        countMonthly
                .timeWindowAll(Time.milliseconds(1))
                .apply(new Query2Result())
                .writeAsText("commentmonthly",FileSystem.WriteMode.OVERWRITE);
    }

    public static void processMetrics(DataStream<Tuple16<Long, String, Long, Long, String, Long, Integer, String, Long, String, Long, String, String, Long, String, Long>> stream, String kafkaAddress) {
        //Compute count each 2 hours
        DataStream<Tuple3<String, Integer, Long>> countHours = stream
                .filter(x -> x.f0 != -1)
                .map(Query2Parser::parseMetrics)
                .returns(Types.TUPLE(Types.STRING, Types.STRING, Types.INT, Types.LONG))
                .filter(x -> x.f1.equals("comment"))
                .map(x->Query2Parser.removeCommentTypeMetrics(x,kafkaAddress))
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(0)
                .window(TumblingEventTimeWindows.of(Time.hours(2)))
                .aggregate(new CountAggregateComments(), new CountProcessComments())
                .setParallelism(3);


        //Week statistics
        DataStream<Tuple3<String, Integer, Long>> countWeekly = countHours
                .keyBy(0)
                .window(TumblingEventTimeWindows.of(Time.days(7), Time.days(-3)))
                .aggregate(new CountAggregateIntermediateComments(), new CountProcessComments())
                .setParallelism(3);

        //Monthly statistics

        DataStream<Tuple3<String, Integer, Long>> countMonthly = countHours
                .keyBy(0)
                .window(new MonthlyWindowTum())
                .aggregate(new CountAggregateIntermediateComments(), new CountProcessComments())
                .setParallelism(3);

        //Getting result
        countHours
                .windowAll(TumblingEventTimeWindows.of(Time.days(1)))
                .apply(new Query2Result())
                .writeAsText("commentdaily",FileSystem.WriteMode.OVERWRITE);

        countWeekly
                .timeWindowAll(Time.milliseconds(1))
                .apply(new Query2Result())
                .writeAsText("commentweekly",FileSystem.WriteMode.OVERWRITE);

        countMonthly
                .timeWindowAll(Time.milliseconds(1))
                .apply(new Query2Result())
                .writeAsText("commentmonthly",FileSystem.WriteMode.OVERWRITE);
    }
}

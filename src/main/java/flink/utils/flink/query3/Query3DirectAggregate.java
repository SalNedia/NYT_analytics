package flink.utils.flink.query3;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.tuple.Tuple6;


public class Query3DirectAggregate implements AggregateFunction<Tuple6<Long, String,String,Long,Long,Integer>, Tuple2<Long, Float>, Tuple2<Long, Float>> {

    float value=0;
    @Override
    public Tuple2<Long, Float> createAccumulator() {
        return new Tuple2<Long,Float>(0L,0f);
    }

    @Override
    public Tuple2<Long, Float> add(Tuple6<Long, String, String, Long,Long,Integer> tuple6, Tuple2<Long, Float> agg) {

        if(tuple6.f1.equals("comment")) {
                value = tuple6.f3;
                if (tuple6.f2.equals("True"))
                    value = tuple6.f3+tuple6.f3 * 0.1f;
            }
            else
                value=0;
            return new Tuple2<>(tuple6.f0,agg.f1+value);

    }

    @Override
    public Tuple2<Long, Float> getResult(Tuple2<Long, Float> tuple2) {

        return new Tuple2<>(tuple2.f0,tuple2.f1);

    }

    @Override
    public Tuple2<Long, Float> merge(Tuple2<Long, Float> agg1, Tuple2<Long, Float> agg2) {
        return new Tuple2<>(agg1.f0,agg1.f1+agg2.f1);
    }
}

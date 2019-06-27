package flink.utils.flink.query3;

import org.apache.flink.api.java.tuple.Tuple15;
import org.apache.flink.api.java.tuple.Tuple5;
import redis.clients.jedis.Jedis;

public class Query3Parser {
    public  synchronized static Tuple5<Long, String,String,Long,Long>  parse(Tuple15<Long, String, Long, Long, String, Long, Integer, String, Long, String, Long, String, String, Long, String> x) {

        Jedis jedis=new Jedis("localhost");
        //Delete a tuple after two weeks
        jedis.setex(String.valueOf(x.f3),1200,String.valueOf(x.f13));
        jedis.close();

        return new Tuple5<Long,String, String, Long,Long>(x.f13,x.f4,x.f7,x.f10,x.f8);
    }

    public synchronized static Tuple5<Long, String,String,Long,Long> changeKey(Tuple5<Long, String, String, Long, Long> x) {

        if(x.f1.equals("comment"))
            return new Tuple5<>(x.f0,x.f1,x.f2,x.f3,x.f4);
        else {
            Jedis jedis=new Jedis("localhost");
            //Case when the comment id doesn't exist
            if(jedis.srandmember(String.valueOf(x.f4))==null)
                return new Tuple5<Long, String, String, Long, Long>(null, x.f1, x.f2, x.f3, x.f0);
            long userId = Long.parseLong(jedis.srandmember(String.valueOf(x.f4)));
            return new Tuple5<Long, String, String, Long, Long>(userId, x.f1, x.f2, x.f3, x.f0);
        }

    }
}

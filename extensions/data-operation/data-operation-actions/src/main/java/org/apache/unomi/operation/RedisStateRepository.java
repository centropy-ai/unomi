package org.apache.unomi.operation;
//
//import org.apache.camel.impl.MemoryStateRepository;
//import redis.clients.jedis.HostAndPort;
//import redis.clients.jedis.JedisCluster;
//
//import java.io.IOException;
//
//public class RedisStateRepository extends MemoryStateRepository {
//    JedisCluster jedisCluster ;
//
//    public RedisStateRepository(String redisCluster) {
//        try (JedisCluster jedisCluster = new JedisCluster(new HostAndPort("redis-cluster", 7000))) {
//            this.jedisCluster = jedisCluster;
//        } catch (IOException e) {}
//    }
//
//    @Override
//    public void setState(String key, String value) {
//        this.jedisCluster.set(key, value);
//    }
//
//    @Override
//    public String getState(String key) {
//        return this.jedisCluster.get(key);
//    }
//
//    @Override
//    public void stop() throws Exception {
//        super.stop();
//        this.jedisCluster.close();
//    }
//}

package org.apache.unomi.operation;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.apache.camel.impl.MemoryStateRepository;

public class RedisStateRepository extends MemoryStateRepository {
    RedisAdvancedClusterCommands<String, String> redis;
    StatefulRedisClusterConnection<String, String> connection;

    public RedisStateRepository(String redisCluster) {
        RedisClusterClient redisClient = RedisClusterClient.create(redisCluster);

        connection = redisClient.connect();
        redis = connection.sync();
    }

    @Override
    public void setState(String key, String value) {
        this.redis.set(key, value);
    }

    @Override
    public String getState(String key) {
        return this.redis.get(key);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        connection.close();
        redis.shutdown(true);
    }
}

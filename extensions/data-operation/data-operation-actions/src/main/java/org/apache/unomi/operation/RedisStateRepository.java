package org.apache.unomi.operation;

import org.apache.camel.impl.MemoryStateRepository;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisStateRepository extends MemoryStateRepository {
    RedissonClient redis;

    public RedisStateRepository(String redisCluster) {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress(redisCluster);
//                .addNodeAddress("redis://127.0.0.1:7181");
        this.redis = Redisson.create(config);
    }
}

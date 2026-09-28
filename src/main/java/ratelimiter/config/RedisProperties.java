package ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    private String host = "localhost";
    private int port = 6379;
    private int timeout = 2000;


    //java client librray for Redis
//    Lets java application communicate with Redis Server
    public JedisPool getJedisPool() {

        //JedisPoolConfig is used to configure the connection pool for Jedis
//        JedisPool keeps mulitilpe conoections ready to reuse
        //resource management, performance improvement, and connection reuse
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);// Maximum number of connections in the pool
        poolConfig.setMaxIdle(10);// Maximum number of idle connections in the pool
        poolConfig.setMinIdle(5); // Minimum number of idle connections in the pool
        poolConfig.setTestOnBorrow(true);// Test the connection before borrowing it from the pool
        poolConfig.setTestOnReturn(true);// Test the connection before returning it to the pool
        return new JedisPool(poolConfig, host, port, timeout);
    }
}

package com.tr.springboot.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
//  @Profile("jedisPool") // 需要读取 spring.profiles.include 配置
    @ConditionalOnProperty(value = "spring.redis.jedis.pool.type", havingValue = "jedisPool")
    public JedisPool jedisPoolFactory() {
        if (password == null || "".equals(password.trim())) password = null;
        return new JedisPool(getJedisPoolConfig(), host, Integer.parseInt(port), timeout, null);
    }

    @Bean
//  @Profile("jedisSentinelPool")
    @ConditionalOnProperty(value = "spring.redis.jedis.pool.type", havingValue = "jedisSentinelPool")
    public JedisSentinelPool jedisSentinelPoolFactory() {
        if (password == null || "".equals(password.trim())) password = null;
        // 哨兵组
        Set<String> sentinels = new HashSet<>();
        String[] hosts = this.host.split(",");
        String[] ports = this.port.split(",");
        for (int i = 0; i < hosts.length; i++) {
            sentinels.add(hosts[i] + ":" + ports[i]);
        }
        return new JedisSentinelPool(null, sentinels, getJedisPoolConfig(), password);
    }

    @Bean
//  @Profile("shardedJedisPool")
    @ConditionalOnProperty(value = "spring.redis.jedis.pool.type", havingValue = "shardedJedisPool")
    public ShardedJedisPool shardedJedisPoolFactory() {
        List<JedisShardInfo> shards = new ArrayList<>();
        String[] hosts = this.host.split(",");
        String[] ports = this.port.split(",");
        String[] passwords = this.password.split(",");
        for (int i = 0; i < hosts.length; i++) {
            JedisShardInfo jedisShardInfo = new JedisShardInfo(hosts[i], ports[i]);
            jedisShardInfo.setPassword("".equals(passwords[i].trim()) ? null : passwords[i]);
            shards.add(jedisShardInfo);
        }
        return new ShardedJedisPool(getJedisPoolConfig(), shards);
    }

    /**
     * 构造JedisPool配置
     */
    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMinIdle(minIdle);
        // 是否启用pool的jmx管理功能, 默认true
        jedisPoolConfig.setJmxEnabled(true);
        return jedisPoolConfig;
    }
}
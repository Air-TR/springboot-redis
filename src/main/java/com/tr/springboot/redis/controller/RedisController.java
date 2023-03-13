package com.tr.springboot.redis.controller;

import com.tr.springboot.redis.service.RedisPoolService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author taorun
 * @date 2023/1/30 11:25
 */
@RestController
public class RedisController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisPoolService redisPoolService;

    @GetMapping("/set/{database}")
    public void set(@PathVariable Integer database) {
        redisPoolService.setex(database, "Time:" + System.currentTimeMillis(), "Hello", 600);
    }

    @GetMapping("/get/{database}")
    public List<String> get(@PathVariable Integer database) {
        Set<String> keys = redisPoolService.keys(database, "Time:*");
        return redisPoolService.mget(database, keys.toArray(new String[keys.size()]));
    }

    @GetMapping("/set")
    public void set() {
        stringRedisTemplate.opsForValue().set("Time:" + System.currentTimeMillis(), "Hello", 600, TimeUnit.SECONDS);
    }

    @GetMapping("/get")
    public List<String> get() {
        Set<String> keys = stringRedisTemplate.keys("Time:*");
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

}

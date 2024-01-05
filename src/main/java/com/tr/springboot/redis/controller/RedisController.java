package com.tr.springboot.redis.controller;

import com.google.common.collect.Lists;
import com.tr.springboot.redis.controller.dto.UserAddDto;
import com.tr.springboot.redis.controller.dto.UserUpdateDto;
import com.tr.springboot.redis.entity.User;
import com.tr.springboot.redis.jpa.UserJpa;
import com.tr.springboot.redis.service.RedisPoolService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
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

    @GetMapping("/redis/set/{database}")
    public void set(@PathVariable Integer database) {
        redisPoolService.setex(database, "Time:" + System.currentTimeMillis(), "Hello", 600);
    }

    @GetMapping("/redis/get/{database}")
    public List<String> get(@PathVariable Integer database) {
        Set<String> keys = redisPoolService.keys(database, "Time:*");
        return redisPoolService.mget(database, keys.toArray(new String[keys.size()]));
    }

    @GetMapping("/redis/set")
    public void set() {
        stringRedisTemplate.opsForValue().set("Time:" + System.currentTimeMillis(), "Hello", 600, TimeUnit.SECONDS);
    }

    @GetMapping("/redis/get")
    public List<String> get() {
        Set<String> keys = stringRedisTemplate.keys("Time:*");
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    // -------------------- 以下测试 @Cacheable、@CachePut、@CacheEvict --------------------

    @Resource
    private UserJpa userJpa;

    @Cacheable(value = "user") // 存进 redis 的 key 会自动拼上参数 id，如：user::1
    @GetMapping("/user/{id}")
    public User findById(@PathVariable Integer id) {
        return userJpa.findById(id).orElse(null);
    }

    @CachePut(value = "name", key = "#addDto.name") // key 必须要 #，否则报错。会自动将新增的数据放入 redis，如 name::james
    @PostMapping("/user")
    public User add(@RequestBody @Valid UserAddDto addDto) {
        return userJpa.save(new User(null, addDto.getName(), addDto.getAge()));
    }

    @CachePut(value = "user", key = "#updateDto.id") // key 必须要 #，否则报错。会同步更新 redis 数据，如 key 为 user::1 的数据，如果缓存中没有该数据则加入缓存
    @PutMapping("/user")
    public User update(@RequestBody UserUpdateDto updateDto) {
        return userJpa.save(new User(updateDto.getId(), updateDto.getName(), updateDto.getAge()));
    }

    @CacheEvict(value = "user") // 会同步删除 redis 数据，如 key 为 user::1 的数据
    @DeleteMapping("/user/{id}")
    public void deleteById(@PathVariable Integer id) {
        userJpa.deleteById(id);
    }

    @CacheEvict(value = "user", allEntries = true) // 删除 redis 以 user:: 开头数据
    @DeleteMapping("/redis/clear/user")
    public void clearUser() {
    }

    @CacheEvict(value = "*", allEntries = true) // 删除 redis 所有数据
    @DeleteMapping("/redis/clear/all")
    public void clearAll() {
    }

    /**
     * 不要在这个方法上面用 @Cacheable，数据会一直拿第一次的缓存数据，除非手动干预，不会变了，即使数据库数据已经被删除或修改，还是返回之前缓存的数据
     */
    @Cacheable(value = "list") // 调用 delete、update 后，查询的数据还是之前缓存的，即便数据已经不存在或被修改
    @GetMapping("/user/list")
    public List<User> findList(User user) {
        return userJpa.findAll(getSpecification(user));
    }

    private Specification<User> getSpecification(User user) {
        return (root, query, builder) -> {
            List<Predicate> predicates = Lists.newArrayList();
            if (Objects.nonNull(user.getName())) {
                predicates.add(builder.like(root.get("name"), "%" + user.getName() + "%"));
            }
            if (Objects.nonNull(user.getAge())) {
                predicates.add(builder.equal(root.get("age"), user.getAge()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

}

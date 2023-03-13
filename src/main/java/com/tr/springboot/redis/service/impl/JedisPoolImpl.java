package com.tr.springboot.redis.service.impl;

import com.tr.springboot.redis.service.Function;
import com.tr.springboot.redis.service.RedisPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 适用单例模式、主从模式、以及哨兵模式
 */
@Service("jedisPool")
public class JedisPoolImpl implements RedisPoolService {

    private final Logger logger = LoggerFactory.getLogger(JedisPoolImpl.class);

    @Autowired
    private JedisPool jedisPool;

    /**
     * 返还到连接池
     *
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * JedisPool通用执行方法
     *
     * @param fun     需要执行的方法
     * @param indexdb 数据库索引，默认0-15
     * @param <T>     返回结果类型
     * @return 返回结果
     */
    private <T> T execute(int indexdb, Function<Jedis, T> fun) {
        Jedis jedis = null;
        try {
            // 从连接池中获取到jedis对象
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            return fun.callback(jedis);
        } catch (Exception e) {
            logger.error("redis error", e);
        } finally {
            returnResource(jedis);
        }
        return null;
    }

    /**
     * 通过key获取储存在redis中的value
     *
     * @param key
     * @param indexdb 选择redis库 0-15
     * @return 成功返回value 失败返回null
     */
    @Override
    public String get(int indexdb, String key) {
        return execute(indexdb, j -> j.get(key));
    }

    /**
     * 通过key获取储存在redis中的value
     *
     * @param key
     * @param indexdb 选择redis库 0-15
     * @return 成功返回value 失败返回null
     */
    @Override
    public byte[] get(int indexdb, byte[] key) {
        return execute(indexdb, j -> j.get(key));
    }

    /**
     * 向redis存入key和value,并释放连接资源
     * 如果key已经存在 则覆盖
     *
     * @param key
     * @param value
     * @param indexdb 选择redis库 0-15
     * @return 成功 返回OK 失败返回 0
     */
    @Override
    public String set(int indexdb, String key, String value) {
        return execute(indexdb, j -> j.set(key, value));
    }

    /**
     * 向redis存入key和value,并释放连接资源
     * 如果key已经存在 则覆盖
     *
     * @param key
     * @param value
     * @param indexdb 选择redis库 0-15
     * @return 成功 返回OK 失败返回 0
     */
    @Override
    public String set(int indexdb, byte[] key, byte[] value) {
        return execute(indexdb, j -> j.set(key, value));
    }

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param keys 一个key 也可以使 string 数组
     * @return 返回删除成功的个数
     */
    @Override
    public Long del(int indexdb, String... keys) {
        return execute(indexdb, j -> j.del(keys));
    }

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param indexdb 选择redis库 0-15
     * @param keys    一个key 也可以使 string 数组
     * @return 返回删除成功的个数
     */
    @Override
    public Long del(int indexdb, byte[]... keys) {
        return execute(indexdb, j -> j.del(keys));
    }

    /**
     * 通过key向指定的value值追加值
     *
     * @param key
     * @param str
     * @return 成功返回 添加后value的长度 失败 返回 添加的 value 的长度 异常返回0L
     */
    @Override
    public Long append(int indexdb, String key, String str) {
        return execute(indexdb, j -> j.append(key, str));
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return true OR false
     */
    @Override
    public Boolean exists(int indexdb, String key) {
        return execute(indexdb, j -> j.exists(key));
    }

    /**
     * 清空当前数据库中的所有 key,此命令从不失败。
     *
     * @return 总是返回 OK
     */
    @Override
    public String flushDB(int indexdb) {
        return execute(indexdb, BinaryJedis::flushDB);
    }

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     *
     * @param key
     * @param value 过期时间，单位：秒
     * @return 成功返回1 如果存在 和 发生异常 返回 0
     */
    @Override
    public Long expire(int indexdb, String key, int value) {
        return execute(indexdb, j -> j.expire(key, value));
    }

    /**
     * 以秒为单位，返回给定 key 的剩余生存时间
     *
     * @param key
     * @return 当 key 不存在时，返回 -2 。当 key 存在但没有设置剩余生存时间时，返回 -1 。否则，以秒为单位，返回 key
     * *         的剩余生存时间。 发生异常 返回 0
     */
    @Override
    public Long ttl(int indexdb, String key) {
        return execute(indexdb, j -> j.ttl(key));
    }

    /**
     * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』(一个不带生存时间、永不过期的 key )
     *
     * @param key
     * @return 当生存时间移除成功时，返回 1 .如果 key 不存在或 key 没有设置生存时间，返回 0 ， 发生异常 返回 -1
     */
    @Override
    public Long persist(int indexdb, String key) {
        return execute(indexdb, j -> j.persist(key));
    }

    /**
     * 设置key value,如果key已经存在则返回0,nx==> not exist
     *
     * @param key
     * @param value
     * @return 成功返回1 如果存在 和 发生异常 返回 0
     */
    @Override
    public Long setnx(int indexdb, String key, String value) {
        return execute(indexdb, j -> j.setnx(key, value));
    }

    /**
     * 设置 key 和 value 并制定这个键值的有效期
     *
     * @param key
     * @param value
     * @param seconds 单位:秒
     * @return 成功返回OK 失败和异常返回null
     */
    @Override
    public String setex(int indexdb, String key, String value, int seconds) {
        return execute(indexdb, j -> j.setex(key, seconds, value));
    }

    /**
     * 通过key 和offset 从指定的位置开始将原先value替换
     * 下标从0开始,offset表示从offset下标开始替换
     * 如果替换的字符串长度过小则会这样
     * example: value : bigsea@zto.cn str : abc
     * 从下标7开始替换 则结果为：bigsea.abc.cn
     *
     * @param key
     * @param str
     * @param offset 下标位置
     * @return 返回替换后 value 的长度
     */
    @Override
    public Long setrange(int indexdb, String key, String str, int offset) {
        return execute(indexdb, j -> j.setrange(key, offset, str));
    }

    /**
     * 通过批量的key获取批量的value
     *
     * @param keys string数组 也可以是一个key
     * @return 成功返回value的集合, 失败返回null的集合 ,异常返回空
     */
    @Override
    public List<String> mget(int indexdb, String... keys) {
        return execute(indexdb, j -> j.mget(keys));
    }

    /**
     * 批量的设置key:value,可以一个
     * example: obj.mset(new String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回OK 失败 异常 返回 null
     */
    @Override
    public String mset(int indexdb, String... keysvalues) {
        return execute(indexdb, j -> j.mset(keysvalues));
    }

    /**
     * 批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚
     * example: obj.msetnx(new String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回1 失败返回0
     */
    @Override
    public Long msetnx(int indexdb, String... keysvalues) {
        return execute(indexdb, j -> j.msetnx(keysvalues));
    }

    /**
     * 设置key的值,并返回一个旧值
     *
     * @param key
     * @param value
     * @return 旧值 如果key不存在 则返回null
     */
    @Override
    public String getset(int indexdb, String key, String value) {
        return execute(indexdb, j -> j.getSet(key, value));
    }

    /**
     * 通过下标 和key 获取指定下标位置的 value
     *
     * @param key
     * @param startOffset 开始位置 从0 开始 负数表示从右边开始截取
     * @param endOffset
     * @return 如果没有返回null
     */
    @Override
    public String getrange(int indexdb, String key, int startOffset, int endOffset) {
        return execute(indexdb, j -> j.getrange(key, startOffset, endOffset));
    }

    /**
     * 通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1
     *
     * @param key
     * @return 加值后的结果
     */
    @Override
    public Long incr(int indexdb, String key) {
        return execute(indexdb, j -> j.incr(key));
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     *
     * @param key
     * @param integer
     * @return
     */
    @Override
    public Long incrBy(int indexdb, String key, Long integer) {
        return execute(indexdb, j -> j.incrBy(key, integer));
    }

    /**
     * 对key的值做减减操作,如果key不存在,则设置key为-1
     *
     * @param key
     * @return
     */
    @Override
    public Long decr(int indexdb, String key) {
        return execute(indexdb, j -> j.decr(key));
    }

    /**
     * 减去指定的值
     *
     * @param key
     * @param integer
     * @return
     */
    @Override
    public Long decrBy(int indexdb, String key, Long integer) {
        return execute(indexdb, j -> j.decrBy(key, integer));
    }

    /**
     * 通过key获取value值的长度
     *
     * @param key
     * @return 失败返回null
     */
    @Override
    public Long serlen(int indexdb, String key) {
        return execute(indexdb, j -> j.strlen(key));
    }

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     *
     * @param key
     * @param field 字段
     * @param value
     * @return 如果存在返回0 异常返回null
     */
    @Override
    public Long hset(int indexdb, String key, String field, String value) {
        return execute(indexdb, j -> j.hset(key, field, value));
    }

    /**
     * 通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    @Override
    public Long hsetnx(int indexdb, String key, String field, String value) {
        return execute(indexdb, j -> j.hsetnx(key, field, value));
    }

    /**
     * 通过key同时设置 hash的多个field
     *
     * @param key
     * @param hash
     * @return 返回OK 异常返回null
     */
    @Override
    public String hmset(int indexdb, String key, Map<String, String> hash) {
        return execute(indexdb, j -> j.hmset(key, hash));
    }

    /**
     * 通过key 和 field 获取指定的 value
     *
     * @param key
     * @param field
     * @return 没有返回null
     */
    @Override
    public String hget(int indexdb, String key, String field) {
        return execute(indexdb, j -> j.hget(key, field));
    }

    /**
     * 通过key 和 fields 获取指定的value 如果没有对应的value则返回null
     *
     * @param key
     * @param fields 可以使 一个String 也可以是 String数组
     * @return
     */
    @Override
    public List<String> hmget(int indexdb, String key, String... fields) {
        return execute(indexdb, j -> j.hmget(key, fields));
    }

    /**
     * 通过key给指定的field的value加上给定的值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    @Override
    public Long hincrby(int indexdb, String key, String field, Long value) {
        return execute(indexdb, j -> j.hincrBy(key, field, value));
    }

    /**
     * 通过key和field判断是否有指定的value存在
     *
     * @param key
     * @param field
     * @return
     */
    @Override
    public Boolean hexists(int indexdb, String key, String field) {
        return execute(indexdb, j -> j.hexists(key, field));
    }

    /**
     * <p>
     * 通过key返回field的数量
     * </p>
     *
     * @param key
     * @return
     */
    @Override
    public Long hlen(int indexdb, String key) {
        return execute(indexdb, j -> j.hlen(key));
    }

    /**
     * 通过key 删除指定的 field
     *
     * @param key
     * @param fields 可以是 一个 field 也可以是 一个数组
     * @return
     */
    @Override
    public Long hdel(int indexdb, String key, String... fields) {
        return execute(indexdb, j -> j.hdel(key, fields));
    }

    /**
     * 通过key返回所有的field
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> hkeys(int indexdb, String key) {
        return execute(indexdb, j -> j.hkeys(key));
    }

    /**
     * 通过key返回所有和key有关的value
     *
     * @param key
     * @return
     */
    @Override
    public List<String> hvals(int indexdb, String key) {
        return execute(indexdb, j -> j.hvals(key));
    }

    /**
     * 通过key获取所有的field和value
     *
     * @param key
     * @return
     */
    @Override
    public Map<String, String> hgetall(int indexdb, String key) {
        return execute(indexdb, j -> j.hgetAll(key));
    }

    /**
     * 通过key向list头部添加字符串
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    @Override
    public Long lpush(int indexdb, String key, String... strs) {
        return execute(indexdb, j -> j.lpush(key, strs));
    }

    /**
     * 通过key向list尾部添加字符串
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    @Override
    public Long rpush(int indexdb, String key, String... strs) {
        return execute(indexdb, j -> j.rpush(key, strs));
    }

    /**
     * 通过key设置list指定下标位置的value
     * 如果下标超过list里面value的个数则报错
     *
     * @param key
     * @param index 从0开始
     * @param value
     * @return 成功返回OK
     */
    @Override
    public String lset(int indexdb, String key, Long index, String value) {
        return execute(indexdb, j -> j.lset(key, index, value));
    }

    /**
     * 通过key从对应的list中删除指定的count个 和 value相同的元素
     *
     * @param key
     * @param count 当count为0时删除全部
     * @param value
     * @return 返回被删除的个数
     */
    @Override
    public Long lrem(int indexdb, String key, long count, String value) {
        return execute(indexdb, j -> j.lrem(key, count, value));
    }

    /**
     * 通过key保留list中从strat下标开始到end下标结束的value值
     *
     * @param key
     * @param start
     * @param end
     * @return 成功返回OK
     */
    @Override
    public String ltrim(int indexdb, String key, long start, long end) {
        return execute(indexdb, j -> j.ltrim(key, start, end));
    }

    /**
     * 通过key从list的头部删除一个value,并返回该value
     *
     * @param key
     * @return
     */
    @Override
    public synchronized String lpop(int indexdb, String key) {
        return execute(indexdb, j -> j.lpop(key));
    }

    /**
     * 通过key从list尾部删除一个value,并返回该元素
     *
     * @param key
     * @return
     */
    @Override
    public synchronized String rpop(int indexdb, String key) {
        return execute(indexdb, j -> j.rpop(key));
    }

    /**
     * 通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value
     * 如果第一个list为空或者不存在则返回null
     *
     * @param srckey
     * @param dstkey
     * @return
     */
    @Override
    public String rpoplpush(int indexdb, String srckey, String dstkey) {
        return execute(indexdb, j -> j.rpoplpush(srckey, dstkey));
    }

    /**
     * 通过key获取list中指定下标位置的value
     *
     * @param key
     * @param index
     * @return 如果没有返回null
     */
    @Override
    public String lindex(int indexdb, String key, long index) {
        return execute(indexdb, j -> j.lindex(key, index));
    }

    /**
     * 通过key返回list的长度
     *
     * @param key
     * @return
     */
    @Override
    public Long llen(int indexdb, String key) {
        return execute(indexdb, j -> j.llen(key));
    }

    /**
     * 通过key获取list指定下标位置的value
     * 如果start 为 0 end 为 -1 则返回全部的list中的value
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<String> lrange(int indexdb, String key, long start, long end) {
        return execute(indexdb, j -> j.lrange(key, start, end));
    }

    /**
     * 将列表 key 下标为 index 的元素的值设置为 value
     *
     * @param key
     * @param index
     * @param value
     * @return 操作成功返回 ok ，否则返回错误信息
     */
    @Override
    public String lset(int indexdb, String key, long index, String value) {
        return execute(indexdb, j -> j.lset(key, index, value));
    }

    /**
     * 返回给定排序后的结果
     *
     * @param key
     * @param sortingParameters
     * @return 返回列表形式的排序结果
     */
    @Override
    public List<String> sort(int indexdb, String key, SortingParams sortingParameters) {
        return execute(indexdb, j -> j.sort(key, sortingParameters));
    }

    /**
     * 返回排序后的结果，排序默认以数字作为对象，值被解释为双精度浮点数，然后进行比较。
     *
     * @param key
     * @return 返回列表形式的排序结果
     */
    @Override
    public List<String> sort(int indexdb, String key) {
        return execute(indexdb, j -> j.sort(key));
    }

    /**
     * 通过key向指定的set中添加value
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 添加成功的个数
     */
    @Override
    public Long sadd(int indexdb, String key, String... members) {
        return execute(indexdb, j -> j.sadd(key, members));
    }

    /**
     * 通过key删除set中对应的value值
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 删除的个数
     */
    @Override
    public Long srem(int indexdb, String key, String... members) {
        return execute(indexdb, j -> j.srem(key, members));
    }

    /**
     * 通过key随机删除一个set中的value并返回该值
     *
     * @param key
     * @return
     */
    @Override
    public String spop(int indexdb, String key) {
        return execute(indexdb, j -> j.spop(key));
    }

    /**
     * 通过key获取set中的差集
     * 以第一个set为标准
     *
     * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    @Override
    public Set<String> sdiff(int indexdb, String... keys) {
        return execute(indexdb, j -> j.sdiff(keys));
    }

    /**
     * 通过key获取set中的差集并存入到另一个key中
     * 以第一个set为标准
     *
     * @param dstkey 差集存入的key
     * @param keys   可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    @Override
    public Long sdiffstore(int indexdb, String dstkey, String... keys) {
        return execute(indexdb, j -> j.sdiffstore(dstkey, keys));
    }

    /**
     * 通过key获取指定set中的交集
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Set<String> sinter(int indexdb, String... keys) {
        return execute(indexdb, j -> j.sinter(keys));
    }

    /**
     * 通过key获取指定set中的交集 并将结果存入新的set中
     *
     * @param dstkey
     * @param keys   可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Long sinterstore(int indexdb, String dstkey, String... keys) {
        return execute(indexdb, j -> j.sinterstore(dstkey, keys));
    }

    /**
     * 通过key返回所有set的并集
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Set<String> sunion(int indexdb, String... keys) {
        return execute(indexdb, j -> j.sunion(keys));
    }

    /**
     * 通过key返回所有set的并集,并存入到新的set中
     *
     * @param dstkey
     * @param keys   可以是一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Long sunionstore(int indexdb, String dstkey, String... keys) {
        return execute(indexdb, j -> j.sunionstore(dstkey, keys));
    }

    /**
     * 通过key将set中的value移除并添加到第二个set中
     *
     * @param srckey 需要移除的
     * @param dstkey 添加的
     * @param member set中的value
     * @return
     */
    @Override
    public Long smove(int indexdb, String srckey, String dstkey, String member) {
        return execute(indexdb, j -> j.smove(srckey, dstkey, member));
    }

    /**
     * 通过key获取set中value的个数
     *
     * @param key
     * @return
     */
    @Override
    public Long scard(int indexdb, String key) {
        return execute(indexdb, j -> j.scard(key));
    }

    /**
     * 通过key判断value是否是set中的元素
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Boolean sismember(int indexdb, String key, String member) {
        return execute(indexdb, j -> j.sismember(key, member));
    }

    /**
     * 通过key获取set中随机的value,不删除元素
     *
     * @param key
     * @return
     */
    @Override
    public String srandmember(int indexdb, String key) {
        return execute(indexdb, j -> j.srandmember(key));
    }

    /**
     * 通过key获取set中所有的value
     *
     * @param key
     * @return
     */
    @Override
    public Set<String> smembers(int indexdb, String key) {
        return execute(indexdb, j -> j.smembers(key));
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的
     * 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    @Override
    public Long zadd(int indexdb, String key, double score, String member) {
        return execute(indexdb, j -> j.zadd(key, score, member));
    }

    /**
     * 返回有序集 key 中，指定区间内的成员。min=0,max=-1代表所有元素
     *
     * @param key
     * @param min
     * @param max
     * @return 指定区间内的有序集成员的列表。
     */
    @Override
    public Set<String> zrange(int indexdb, String key, long min, long max) {
        return execute(indexdb, j -> j.zrange(key, min, max));
    }

    /**
     * 统计有序集 key 中,值在 min 和 max 之间的成员的数量
     *
     * @param key
     * @param min
     * @param max
     * @return 值在 min 和 max 之间的成员的数量。异常返回0
     */
    @Override
    public Long zcount(int indexdb, String key, double min, double max) {
        return execute(indexdb, j -> j.zcount(key, min, max));
    }

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment 。增量也可以为负数，相当于对给定域进行减法操作。
     * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。
     * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。本操作的值被限制在 64 位(bit)有符号数字表示之内。
     * 将名称为key的hash中field的value增加integer
     *
     * @param key
     * @param value
     * @param increment
     * @return 执行 HINCRBY 命令之后，哈希表 key 中域 field的值。异常返回0
     */
    @Override
    public Long hincrBy(int indexdb, String key, String value, long increment) {
        return execute(indexdb, j -> j.hincrBy(key, value, increment));
    }

    /**
     * 通过key删除在zset中指定的value
     *
     * @param key
     * @param members 可以是一个string 也可以是一个string数组
     * @return
     */
    @Override
    public Long zrem(int indexdb, String key, String... members) {
        return execute(indexdb, j -> j.zrem(key, members));
    }

    /**
     * 通过key增加该zset中value的score的值
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    @Override
    public Double zincrby(int indexdb, String key, double score, String member) {
        return execute(indexdb, j -> j.zincrby(key, score, member));
    }

    /**
     * 通过key返回zset中value的排名
     * 下标从小到大排序
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Long zrank(int indexdb, String key, String member) {
        return execute(indexdb, j -> j.zrank(key, member));
    }

    /**
     * 通过key返回zset中value的排名
     * 下标从大到小排序
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Long zrevrank(int indexdb, String key, String member) {
        return execute(indexdb, j -> j.zrevrank(key, member));
    }

    /**
     * 通过key将获取score从start到end中zset的value
     * socre从大到小排序
     * 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public Set<String> zrevrange(int indexdb, String key, long start, long end) {
        return execute(indexdb, j -> j.zrevrange(key, start, end));
    }

    /**
     * 通过key获取score从start到end中zset的value
     * score从大到小排序
     */
    public Set<Tuple> zrevrangeWithScores(int indexdb, String key, long start, long end) {
        return execute(indexdb, j -> j.zrevrangeWithScores(key, start, end));
    }

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    @Override
    public Set<String> zrangebyscore(int indexdb, String key, String max, String min) {
        return execute(indexdb, j -> j.zrevrangeByScore(key, max, min));
    }

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    @Override
    public Set<String> zrangeByScore(int indexdb, String key, double max, double min) {
        return execute(indexdb, j -> j.zrevrangeByScore(key, max, min));
    }

    /**
     * 返回指定区间内zset中value的数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    @Override
    public Long zcount(int indexdb, String key, String min, String max) {
        return execute(indexdb, j -> j.zcount(key, min, max));
    }

    /**
     * 通过key返回zset中的value个数
     *
     * @param key
     * @return
     */
    @Override
    public Long zcard(int indexdb, String key) {
        return execute(indexdb, j -> j.zcard(key));
    }

    /**
     * 通过key获取zset中value的score值
     *
     * @param key
     * @param member
     * @return
     */
    @Override
    public Double zscore(int indexdb, String key, String member) {
        return execute(indexdb, j -> j.zscore(key, member));
    }

    /**
     * 通过key删除给定区间内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public Long zremrangeByRank(int indexdb, String key, long start, long end) {
        return execute(indexdb, j -> j.zremrangeByRank(key, start, end));
    }

    /**
     * 通过key删除指定score内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    public Long zremrangeByScore(int indexdb, String key, double start, double end) {
        return execute(indexdb, j -> j.zremrangeByScore(key, start, end));
    }

    /**
     * 返回满足pattern表达式的所有key
     *
     * @param pattern
     * @return
     */
    @Override
    public Set<String> keys(int indexdb, String pattern) {
        return execute(indexdb, j -> j.keys(pattern));
    }

    /**
     * 通过key判断值得类型
     *
     * @param key
     * @return
     */
    @Override
    public String type(int indexdb, String key) {
        return execute(indexdb, j -> j.type(key));
    }
}
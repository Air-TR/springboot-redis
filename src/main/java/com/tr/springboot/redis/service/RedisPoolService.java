package com.tr.springboot.redis.service;

import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 连接池接口
 */
public interface RedisPoolService {

    /**
     * 序列化对象
     *
     * @param obj
     * @return 对象需实现Serializable接口
     */
    static byte[] ObjTOSerialize(Object obj) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream byteOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(byteOut);
            oos.writeObject(obj);
            byte[] bytes = byteOut.toByteArray();
            return bytes;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 反序列化对象
     *
     * @param bytes
     * @return 对象需实现Serializable接口
     */
    static Object unserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        try {
            //反序列化
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
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
    String get(int indexdb, String key);

    /**
     * 通过key获取储存在redis中的value
     *
     * @param key
     * @param indexdb 选择redis库 0-15
     * @return 成功返回value 失败返回null
     */
    byte[] get(int indexdb, byte[] key);

    /**
     * 向redis存入key和value,并释放连接资源
     * 如果key已经存在 则覆盖
     *
     * @param key
     * @param value
     * @param indexdb 选择redis库 0-15
     * @return 成功 返回OK 失败返回 0
     */
    String set(int indexdb, String key, String value);

    /**
     * 向redis存入key和value,并释放连接资源
     * 如果key已经存在 则覆盖
     *
     * @param key
     * @param value
     * @param indexdb 选择redis库 0-15
     * @return 成功 返回OK 失败返回 0
     */
    String set(int indexdb, byte[] key, byte[] value);

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param indexdb 选择redis库 0-15
     * @param keys    一个key 也可以使 string 数组
     * @return 返回删除成功的个数
     */
    Long del(int indexdb, String... keys);

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param indexdb 选择redis库 0-15
     * @param keys    一个key 也可以使 string 数组
     * @return 返回删除成功的个数
     */
    Long del(int indexdb, byte[]... keys);

    /**
     * 通过key向指定的value值追加值
     *
     * @param key
     * @param str
     * @return 成功返回 添加后value的长度 失败 返回 添加的 value 的长度 异常返回0L
     */
    Long append(int indexdb, String key, String str);

    /**
     * 判断key是否存在
     *
     * @param key
     * @return true OR false
     */
    Boolean exists(int indexdb, String key);

    /**
     * 清空当前数据库中的所有 key,此命令从不失败。
     *
     * @return 总是返回 OK
     */
    String flushDB(int indexdb);

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     *
     * @param key
     * @param value 过期时间，单位：秒
     * @return 成功返回1 如果存在 和 发生异常 返回 0
     */
    Long expire(int indexdb, String key, int value);

    /**
     * 以秒为单位，返回给定 key 的剩余生存时间
     *
     * @param key
     * @return 当 key 不存在时，返回 -2 。当 key 存在但没有设置剩余生存时间时，返回 -1 。否则，以秒为单位，返回 key
     * 的剩余生存时间。 发生异常 返回 0
     */
    Long ttl(int indexdb, String key);

    /**
     * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』(一个不带生存时间、永不过期的 key )
     *
     * @param key
     * @return 当生存时间移除成功时，返回 1 .如果 key 不存在或 key 没有设置生存时间，返回 0 ， 发生异常 返回 -1
     */
    Long persist(int indexdb, String key);

    /**
     * 设置key value,如果key已经存在则返回0,nx==> not exist
     *
     * @param key
     * @param value
     * @return 成功返回1 如果存在 和 发生异常 返回 0
     */
    Long setnx(int indexdb, String key, String value);

    /**
     * 设置key value并制定这个键值的有效期
     *
     * @param key
     * @param value
     * @param seconds 单位:秒
     * @return 成功返回OK 失败和异常返回null
     */
    String setex(int indexdb, String key, String value, int seconds);

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
    Long setrange(int indexdb, String key, String str, int offset);

    /**
     * 通过批量的key获取批量的value
     *
     * @param keys string数组 也可以是一个key
     * @return 成功返回value的集合, 失败返回null的集合 ,异常返回空
     */
    List<String> mget(int indexdb, String... keys);

    /**
     * 批量的设置key:value,可以一个
     * example: obj.mset(new String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回OK 失败 异常 返回 null
     */
    String mset(int indexdb, String... keysvalues);

    /**
     * 批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚
     * example: obj.msetnx(new String[]{"key2","value1","key2","value2"})
     *
     * @param keysvalues
     * @return 成功返回1 失败返回0
     */
    Long msetnx(int indexdb, String... keysvalues);

    /**
     * 设置key的值,并返回一个旧值
     *
     * @param key
     * @param value
     * @return 旧值 如果key不存在 则返回null
     */
    String getset(int indexdb, String key, String value);

    /**
     * 通过下标 和key 获取指定下标位置的 value
     *
     * @param key
     * @param startOffset 开始位置 从0 开始 负数表示从右边开始截取
     * @param endOffset
     * @return 如果没有返回null
     */
    String getrange(int indexdb, String key, int startOffset, int endOffset);

    /**
     * 通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1
     *
     * @param key
     * @return 加值后的结果
     */
    Long incr(int indexdb, String key);

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     *
     * @param key
     * @param integer
     * @return
     */
    Long incrBy(int indexdb, String key, Long integer);

    /**
     * 对key的值做减减操作,如果key不存在,则设置key为-1
     *
     * @param key
     * @return
     */
    Long decr(int indexdb, String key);

    /**
     * 减去指定的值
     *
     * @param key
     * @param integer
     * @return
     */
    Long decrBy(int indexdb, String key, Long integer);

    /**
     * 通过key获取value值的长度
     *
     * @param key
     * @return 失败返回null
     */
    Long serlen(int indexdb, String key);

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     *
     * @param key
     * @param field 字段
     * @param value
     * @return 如果存在返回0 异常返回null
     */
    Long hset(int indexdb, String key, String field, String value);

    /**
     * 通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hsetnx(int indexdb, String key, String field, String value);

    /**
     * 通过key同时设置 hash的多个field
     *
     * @param key
     * @param hash
     * @return 返回OK 异常返回null
     */
    String hmset(int indexdb, String key, Map<String, String> hash);

    /**
     * 通过key 和 field 获取指定的 value
     *
     * @param key
     * @param field
     * @return 没有返回null
     */
    String hget(int indexdb, String key, String field);

    /**
     * 通过key 和 fields 获取指定的value 如果没有对应的value则返回null
     *
     * @param key
     * @param fields 可以使 一个String 也可以是 String数组
     * @return
     */
    List<String> hmget(int indexdb, String key, String... fields);

    /**
     * 通过key给指定的field的value加上给定的值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hincrby(int indexdb, String key, String field, Long value);

    /**
     * 通过key和field判断是否有指定的value存在
     *
     * @param key
     * @param field
     * @return
     */
    Boolean hexists(int indexdb, String key, String field);

    /**
     * <p>
     * 通过key返回field的数量
     * </p>
     *
     * @param key
     * @return
     */
    Long hlen(int indexdb, String key);

    /**
     * 通过key 删除指定的 field
     *
     * @param key
     * @param fields 可以是 一个 field 也可以是 一个数组
     * @return
     */
    Long hdel(int indexdb, String key, String... fields);

    /**
     * 通过key返回所有的field
     *
     * @param key
     * @return
     */
    Set<String> hkeys(int indexdb, String key);

    /**
     * 通过key返回所有和key有关的value
     *
     * @param key
     * @return
     */
    List<String> hvals(int indexdb, String key);

    /**
     * 通过key获取所有的field和value
     *
     * @param key
     * @return
     */
    Map<String, String> hgetall(int indexdb, String key);

    /**
     * 通过key向list头部添加字符串
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    Long lpush(int indexdb, String key, String... strs);

    /**
     * 通过key向list尾部添加字符串
     *
     * @param key
     * @param strs 可以使一个string 也可以使string数组
     * @return 返回list的value个数
     */
    Long rpush(int indexdb, String key, String... strs);

    /**
     * 通过key设置list指定下标位置的value
     * 如果下标超过list里面value的个数则报错
     *
     * @param key
     * @param index 从0开始
     * @param value
     * @return 成功返回OK
     */
    String lset(int indexdb, String key, Long index, String value);

    /**
     * 通过key从对应的list中删除指定的count个 和 value相同的元素
     *
     * @param key
     * @param count 当count为0时删除全部
     * @param value
     * @return 返回被删除的个数
     */
    Long lrem(int indexdb, String key, long count, String value);

    /**
     * 通过key保留list中从strat下标开始到end下标结束的value值
     *
     * @param key
     * @param start
     * @param end
     * @return 成功返回OK
     */
    String ltrim(int indexdb, String key, long start, long end);

    /**
     * 通过key从list的头部删除一个value,并返回该value
     *
     * @param key
     * @return
     */
    String lpop(int indexdb, String key);

    /**
     * 通过key从list尾部删除一个value,并返回该元素
     *
     * @param key
     * @return
     */
    String rpop(int indexdb, String key);

    /**
     * 通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value
     * 如果第一个list为空或者不存在则返回null
     *
     * @param srckey
     * @param dstkey
     * @return
     */
    String rpoplpush(int indexdb, String srckey, String dstkey);

    /**
     * 通过key获取list中指定下标位置的value
     *
     * @param key
     * @param index
     * @return 如果没有返回null
     */
    String lindex(int indexdb, String key, long index);

    /**
     * 通过key返回list的长度
     *
     * @param key
     * @return
     */
    Long llen(int indexdb, String key);

    /**
     * 通过key获取list指定下标位置的value
     * 如果start 为 0 end 为 -1 则返回全部的list中的value
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<String> lrange(int indexdb, String key, long start, long end);

    /**
     * 将列表 key 下标为 index 的元素的值设置为 value
     *
     * @param key
     * @param index
     * @param value
     * @return 操作成功返回 ok ，否则返回错误信息
     */
    String lset(int indexdb, String key, long index, String value);

    /**
     * 返回给定排序后的结果
     *
     * @param key
     * @param sortingParameters
     * @return 返回列表形式的排序结果
     */
    List<String> sort(int indexdb, String key, SortingParams sortingParameters);

    /**
     * 返回排序后的结果，排序默认以数字作为对象，值被解释为双精度浮点数，然后进行比较。
     *
     * @param key
     * @return 返回列表形式的排序结果
     */
    List<String> sort(int indexdb, String key);

    /**
     * 通过key向指定的set中添加value
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 添加成功的个数
     */
    Long sadd(int indexdb, String key, String... members);

    /**
     * 通过key删除set中对应的value值
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 删除的个数
     */
    Long srem(int indexdb, String key, String... members);

    /**
     * 通过key随机删除一个set中的value并返回该值
     *
     * @param key
     * @return
     */
    String spop(int indexdb, String key);

    /**
     * 通过key获取set中的差集
     * 以第一个set为标准
     *
     * @param keys 可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    Set<String> sdiff(int indexdb, String... keys);

    /**
     * 通过key获取set中的差集并存入到另一个key中
     * 以第一个set为标准
     *
     * @param dstkey 差集存入的key
     * @param keys   可以使一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    Long sdiffstore(int indexdb, String dstkey, String... keys);

    /**
     * 通过key获取指定set中的交集
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    Set<String> sinter(int indexdb, String... keys);

    /**
     * 通过key获取指定set中的交集 并将结果存入新的set中
     *
     * @param dstkey
     * @param keys   可以使一个string 也可以是一个string数组
     * @return
     */
    Long sinterstore(int indexdb, String dstkey, String... keys);

    /**
     * 通过key返回所有set的并集
     *
     * @param keys 可以使一个string 也可以是一个string数组
     * @return
     */
    Set<String> sunion(int indexdb, String... keys);

    /**
     * 通过key返回所有set的并集,并存入到新的set中
     *
     * @param dstkey
     * @param keys   可以是一个string 也可以是一个string数组
     * @return
     */
    Long sunionstore(int indexdb, String dstkey, String... keys);

    /**
     * 通过key将set中的value移除并添加到第二个set中
     *
     * @param srckey 需要移除的
     * @param dstkey 添加的
     * @param member set中的value
     * @return
     */
    Long smove(int indexdb, String srckey, String dstkey, String member);

    /**
     * 通过key获取set中value的个数
     *
     * @param key
     * @return
     */
    Long scard(int indexdb, String key);

    /**
     * 通过key判断value是否是set中的元素
     *
     * @param key
     * @param member
     * @return
     */
    Boolean sismember(int indexdb, String key, String member);

    /**
     * 通过key获取set中随机的value,不删除元素
     *
     * @param key
     * @return
     */
    String srandmember(int indexdb, String key);

    /**
     * 通过key获取set中所有的value
     *
     * @param key
     * @return
     */
    Set<String> smembers(int indexdb, String key);

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的
     * 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    Long zadd(int indexdb, String key, double score, String member);

    /**
     * 返回有序集 key 中，指定区间内的成员。min=0,max=-1代表所有元素
     *
     * @param key
     * @param min
     * @param max
     * @return 指定区间内的有序集成员的列表。
     */
    Set<String> zrange(int indexdb, String key, long min, long max);

    /**
     * 统计有序集 key 中,值在 min 和 max 之间的成员的数量
     *
     * @param key
     * @param min
     * @param max
     * @return 值在 min 和 max 之间的成员的数量。异常返回0
     */
    Long zcount(int indexdb, String key, double min, double max);

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
    Long hincrBy(int indexdb, String key, String value, long increment);

    /**
     * 通过key删除在zset中指定的value
     *
     * @param key
     * @param members 可以是一个string 也可以是一个string数组
     * @return
     */
    Long zrem(int indexdb, String key, String... members);

    /**
     * 通过key增加该zset中value的score的值
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    Double zincrby(int indexdb, String key, double score, String member);

    /**
     * 通过key返回zset中value的排名
     * 下标从小到大排序
     *
     * @param key
     * @param member
     * @return
     */
    Long zrank(int indexdb, String key, String member);

    /**
     * 通过key返回zset中value的排名
     * 下标从大到小排序
     *
     * @param key
     * @param member
     * @return
     */
    Long zrevrank(int indexdb, String key, String member);

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
    Set<String> zrevrange(int indexdb, String key, long start, long end);

    /**
     * 通过key获取score从start到end中zset的value
     * score从大到小排序
     */
    Set<Tuple> zrevrangeWithScores(int indexdb, String key, long start, long end);

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    Set<String> zrangebyscore(int indexdb, String key, String max, String min);

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    Set<String> zrangeByScore(int indexdb, String key, double max, double min);

    /**
     * 返回指定区间内zset中value的数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Long zcount(int indexdb, String key, String min, String max);

    /**
     * 通过key返回zset中的value个数
     *
     * @param key
     * @return
     */
    Long zcard(int indexdb, String key);

    /**
     * 通过key获取zset中value的score值
     *
     * @param key
     * @param member
     * @return
     */
    Double zscore(int indexdb, String key, String member);

    /**
     * 通过key删除给定区间内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    Long zremrangeByRank(int indexdb, String key, long start, long end);

    /**
     * 通过key删除指定score内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    Long zremrangeByScore(int indexdb, String key, double start, double end);

    /**
     * 返回满足pattern表达式的所有key
     *
     * @param pattern
     * @return
     */
    Set<String> keys(int indexdb, String pattern);

    /**
     * 通过key判断值得类型
     *
     * @param key
     * @return
     */
    String type(int indexdb, String key);
}
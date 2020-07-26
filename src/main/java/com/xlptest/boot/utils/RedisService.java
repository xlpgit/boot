package com.xlptest.boot.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlptest.boot.entity.IRedisPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

@Service
public class RedisService {

    protected static Logger logger = Logger.getLogger(RedisService.class.getName());

    @Autowired
    private JedisPool jedisPool;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /*
     * 是否使用redis功能开关
     */
    private boolean useRedis = true;

    /**
     * 是否使用redis
     */
    public boolean isUseRedis() {
        return useRedis;
    }

    /*
     * 正常返还链接
     */
    private void returnResource(Jedis jedis) {
        try {
            if (null != jedis) {
                jedis.close();
            }
        } catch (Exception e) {
        }
    }

    /*
     * 释放错误链接
     */
    private void returnBrokenResource(Jedis jedis, String name, Exception msge) {
        if (jedis != null) {
            try {
                jedis.close();
            } catch (Exception e) {
                logger.info("e");
            }
        }
    }

    /**
     * 设置缓存生命周期
     *
     * @param key
     * @param seconds
     */
    public void expire(String key, int seconds) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.expire(key, seconds);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "expire:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }




    /*
     * 更新缓存里的field字段值
     * @param key
     * @param field
     * @param value 此key是由哪个字段拼接而成的
     */

    public Long hincrBy(String key, String field, int value) {
        Jedis jedis = null;
        boolean sucess = true;
        Long result = -1L;
        try {
            jedis = jedisPool.getResource();
            result = jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hincrBy:" + key + ":field" + field, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }

        return result;
    }

    public void setString(String key, String object) {
        setString(key, object, -1);
    }

    /**
     * 设置
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setNxString(String key, String value, int seconds) throws Exception {
        Jedis jedis = null;
        boolean success = true;
        boolean result = false;
        try {
            jedis = jedisPool.getResource();
            result = (jedis.setnx(key, value) != 0);
            if (seconds > -1) {
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "setNxString", e, false);
            throw e;
        } finally {
            releaseRedisSource(success, jedis);
        }

        return result;

    }

    /**
     * 设置
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setHnxString(String key, String field, String value) throws Exception {
        Jedis jedis = null;
        boolean success = true;
        boolean result = false;
        try {
            jedis = jedisPool.getResource();
            result = (jedis.hsetnx(key, field, value) != 0);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "setHnxString", e, false);
            throw e;
        } finally {
            releaseRedisSource(success, jedis);
        }

        return result;

    }

    /**
     * 必须强制获取成功状态
     *
     * @param key
     * @param field
     * @return
     */
    public String getHgetString(String key, String field) throws Exception {
        Jedis jedis = null;
        boolean success = true;
        String getResult = null;
        try {
            jedis = jedisPool.getResource();
            getResult = jedis.hget(key, field);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "getHString", e, false);
            throw e;
        } finally {
            releaseRedisSource(success, jedis);
        }

        return getResult;
    }

    /**
     * @param key
     * @param field
     * @return
     * @throws Exception
     */
    public String getHgetString(String key, long field) throws Exception {
        return getHgetString(key, "" + field);
    }

    /**
     * 删除key
     *
     * @param key
     */
    public boolean deleteHField(String key, String field) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            jedis.hdel(key, field);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "deleteHField", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }
        return success;
    }


    /**
     * 删除key
     *
     * @param key
     */
    public boolean deleteKey(String key) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "deleteKey", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }

        return success;
    }


    /**
     * 获取所有成员及分数
     *
     * @param key
     */
    public Set<Tuple> zAllMemberWithScore(String key) {
        Jedis jedis = null;
        boolean success = true;
        Set<Tuple> set = null;
        try {
            jedis = jedisPool.getResource();
            set = jedis.zrevrangeWithScores(key, 0, -1);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "zAllMemberWithScore", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }

        return set;
    }


    /**
     * 倒序排序
     *
     * @param key
     * @param start
     * @param end
     * @return 倒序的(从大倒)
     */
    public Set<String> zRevRange(String key, long start, long end) {
        Jedis jedis = null;
        boolean success = true;
        Set<String> set = null;
        try {
            jedis = jedisPool.getResource();
            set = jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "deleteKey", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }

        return set;
    }

    /**
     * 删除key
     */
    public void deleteKeys(String... keys) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(keys);
        } catch (Exception e) {
            returnBrokenResource(jedis, "deleteKey" + keys, e);
        } finally {
            releaseRedisSource(true, jedis);
        }

    }

    /**
     * 释放非正常链接
     *
     * @param jedis
     * @param key
     * @param string
     * @param e
     */
    private void releasBrokenReidsSource(Jedis jedis, String key, String string, Exception e, boolean deleteKeyFlag) {
        returnBrokenResource(jedis, string, e);
        if (deleteKeyFlag) {
            expire(key, 0);
        }
    }

    /**
     * 释放成功链接
     *
     * @param success
     * @param jedis
     */
    private void releaseRedisSource(boolean success, Jedis jedis) {
        if (success && jedis != null) {
            returnResource(jedis);
        }
    }

    public void setString(String key, String value, int seconds) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
            if (seconds > -1) {
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "setString", e);
            // expire(key, 0);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    public boolean setExString(String key, String value, int seconds) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "setExString", e);
            // expire(key, 0);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    public String getString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.get(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getString", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    /**
     * 需要知道是否是成功获取的
     *
     * @param key
     * @return
     */
    public Object[] getStringAndSuccess(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = "";
        try {
            jedis = jedisPool.getResource();
            rt = jedis.get(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getString", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        Object[] object = new Object[2];
        object[0] = sucess;
        object[1] = rt;
        return object;
    }

    public List<String> hvals(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.hvals(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hvals", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return null;
    }

    public boolean hexists(String key, String field) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.hexists(key, field);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hexists", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return false;
    }

    /**
     * 检测是否是成员
     *
     * @param key
     * @param member
     * @return
     */
    public boolean sexists(String key, String member) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.sismember(key, member);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hexists", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return false;
    }

    public Long hdel(String key, String... fields) {
        Jedis jedis = null;
        boolean sucess = true;
        Long rt = -1L;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.hdel(key, fields);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hdel", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }


    public void hset(String key, String field, String value) {
        this.hsetString(key, field, value, 0);
    }

    public void hsetString(String key, String field, String value, int seconds) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.hset(key, field, value);
            if (seconds > 1) {
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hsetString" + key, e);
            expire(key, 0);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    public Map<String, String> hmgetAllString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        Map<String, String> rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.hgetAll(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hmgetAllString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    public String hget(String key, String field) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.hget(key, field);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hmgetString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    public <T> List<T> hgetFromObject(String key, Class<T> clazz) {
        Map<String, String> map = this.hgetAll(key);
        Collection<String> values = map.values();
        List<T> result = new ArrayList<>();
        values.forEach((item) -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            try {
                T cache = mapper.readValue(item, clazz);
                result.add((cache));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        });
        return result;
    }

    //反序列化：字符类型转为对象
    public <T> T hGet(String key, String filed, Class<T> clazz) {
        String json = null;
        try {
            json = this.getHgetString(key, filed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();//解析器支持解析单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);//解析器支持解析结束符
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        T t = null;
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            t = mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return t;

    }

    //序列化：对象转为字符串
    public void hSet(String key, String filed, IRedisPo po) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(po); //返回字符串
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        this.hset(key, filed, json);
    }


    public void hSetFromObject(IRedisPo po) {
        String key = po.getKey();
        String pk = po.getPrimary();
        this.hSet(key, pk, po);
    }

    public <T> T hgetFromObject(String key, String filed, Class<T> clazz) {
        Class<?>[] interfacts = clazz.getInterfaces();
        try {
            Field field = interfacts[0].getDeclaredField("getKey");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


        return null;
    }


    public Map<String, String> hgetAll(String key) {
        Jedis jedis = null;
        boolean sucess = true;

        Map<String, String> rt = new HashMap<>();
        try {
            jedis = jedisPool.getResource();
            rt = jedis.hgetAll(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hmgetString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }


    public long hLen(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        long rt = -1;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.hlen(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "hLen" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    //--------------------------------------------list ----------------------------------------------------------

    /**
     * LPUSH key value [value ...]将一个或多个值 value 插入到列表 key 的表头
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头：
     * 比如说，对空列表 mylist 执行命令 LPUSH mylist a b c ，列表的值将是 c b a ，这等同于原子性地执行 LPUSH mylist a 、 LPUSH mylist b 和 LPUSH mylist c 三个命令。
     *
     * @param key
     */
    public void lpushString(String key, String... strings) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.lpush(key, strings);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "lpushString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }


    /**
     * 查询 list
     *
     * @param key
     * @param beginIndex
     * @param endIndex   截止下标
     * @return [beginIndex, endIndex] 闭区间 list
     */
    public List<String> lrange(String key, long beginIndex, long endIndex) {
        Jedis jedis = null;
        boolean sucess = true;
        List<String> value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.lrange(key, beginIndex, endIndex);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "lrange " + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }


    /**
     * 通过索引设置list值
     *
     * @param key
     * @param index
     * @param value
     */
    public String lset(String key, Long index, String value) {

        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            value = jedis.lset(key, index, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "lset " + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }

    /**
     * 清除list的值
     *
     * @param key
     * @param count
     * @param value
     */
    public void lrem(String key, long count, String value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.lrem(key, count, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "lrem " + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }


    /**
     * 获取某个key长度
     * 时间复杂度  O(1)
     *
     * @param key
     * @return
     */
    public long llen(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        long value = 0;
        try {
            jedis = jedisPool.getResource();
            value = jedis.llen(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "llen" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }

    /**
     * LTRIM
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     * 时间复杂度 O(N)
     *
     * @param key
     * @return
     */
    public String ltrim(String key, long start, long end) {
        Jedis jedis = null;
        boolean sucess = true;
        String value = "";
        try {
            jedis = jedisPool.getResource();
            value = jedis.ltrim(key, start, end);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "llen" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }


    public String lpopString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.lpop(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "lpopString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }

    public String rpopString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.rpop(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "rpopString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }

    public String lindex(String key, int index) {
        Jedis jedis = null;
        boolean sucess = true;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.lindex(key, index);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "rpopString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return value;
    }


    /**
     * 返回集合key的基数(集合中元素的数量)。
     *
     * @param key
     * @return
     */
    public long scardString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        long rt = 0L;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.scard(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "scardString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    public long saddString(String key, String value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            Long ret = jedis.sadd(key, value);
            return ret == null ? -1 : ret.longValue();
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "saddString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return -1;
    }

    public void saddStrings(String key, String... values) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.sadd(key, values);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "saddStrings" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    public void sremString(String key, String value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.srem(key, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "sremString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    public boolean sremStringResult(String key, String value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.srem(key, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "sremString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    public void sremStrings(String key, String... values) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.srem(key, values);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "sremStrings" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    public String spopString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.spop(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "spopString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    public Set<String> smembersString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<String> rt = new HashSet<>();
        try {
            jedis = jedisPool.getResource();
            rt = jedis.smembers(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "smembersString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    public String srandmember(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.srandmember(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "smembersString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    /**
     * 删除zset 的成员
     *
     * @param key
     * @param member
     * @return
     */
    public long zRemByMember(String key, String member) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrem(key, member);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zrangeByScoreWithScores", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return 0;
    }

    /**
     *
     */
    public Set<String> zrangeByScore(String key, long min, long max, int limit) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<String> ret = null;
        try {
            jedis = jedisPool.getResource();
            ret = jedis.zrangeByScore(key, min, max, 0, limit);
            return ret;
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zrangeByScore", e);
            // 我需要在这个里面删除member , 但是担心异常发生.所以如果有异常在这个地方返回set
            return null;
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }


    /**
     *
     */
    public Set<String> zrevrangeByScore(String key, long min, long max, int limit) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<String> ret = null;
        try {
            jedis = jedisPool.getResource();
            ret = jedis.zrevrangeByScore(key, min, max, 0, limit);
            return ret;
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zrangeByScore", e);
            // 我需要在这个里面删除member , 但是担心异常发生.所以如果有异常在这个地方返回set
            return null;
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }


    public boolean zAdd(String key, String member, long value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //记录最后一个心跳时间
            jedis.zadd(key, value, member);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zAdd key:" + key + "member:" + member + "value:" + value, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    /**
     * 增加值
     *
     * @param key
     * @param member
     * @param value
     * @return
     */
    public boolean zIncrBy(String key, String member, long value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //记录最后一个心跳时间
            jedis.zincrby(key, value, member);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zIncrBy key:" + key + "member:" + member + "value:" + value, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }


    public boolean zAddMap(String key, Map<String, Double> scoreMembers) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //记录最后一个心跳时间
            jedis.zadd(key, scoreMembers);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zAddMap key:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    public Long incr(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        long result = -1;
        try {
            jedis = jedisPool.getResource();
            result = jedis.incr(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "incr:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return result;
    }

    /**
     * 自增并且设置过期时间
     *
     * @param key
     * @param expireTime second
     * @return
     */
    public Long incr(String key, int expireTime) {
        Jedis jedis = null;
        boolean sucess = true;
        long result = -1;
        try {
            jedis = jedisPool.getResource();
            result = jedis.incr(key);
            if (expireTime > -1) {
                jedis.expire(key, expireTime);
            }
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "incr:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return result;
    }

    /**
     * 返回是否成功
     *
     * @param key
     * @return
     */
    public boolean decr(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        long result = -1;
        try {
            jedis = jedisPool.getResource();
            jedis.decr(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "decr:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    /**
     * 返回是否成功
     *
     * @param key
     * @return
     */
    public boolean decrBy(String key, int size) {
        Jedis jedis = null;
        boolean sucess = true;
        long result = -1;
        try {
            jedis = jedisPool.getResource();
            jedis.decrBy(key, size);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "decrBy:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }


    public boolean exists(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "exists:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return false;
    }

    /**
     * 返回在分数之类的所有的成员以及分数.
     */
    public Set<Tuple> zrangeByScoreWithScores(String key, long min, long max, int offset, int limit) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<Tuple> ret = null;
        try {
            jedis = jedisPool.getResource();
            ret = jedis.zrangeByScoreWithScores(key, min, max, offset, limit);
            return ret;
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zrangeByScoreWithScores limit", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return ret;
    }

    /**
     * 获取包含这个key的所有redis key
     *
     * @param key
     * @return
     */
    public Set<String> keys(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<String> keys = null;
        try {
            jedis = jedisPool.getResource();
            keys = jedis.keys("*" + key + "*");
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "keys", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return keys;
    }

    /**
     * 获取key的剩余时间
     *
     * @param key
     * @return
     */
    public long getKeyTTL(String key) {
        Jedis jedis = null;
        long result = 0;
        Set<String> keys = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            result = jedis.ttl(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "key ttl", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return result;
    }


    /**
     * 设置
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setNxStringByMillisec(String key, String value, int millisec) {
        Jedis jedis = null;
        boolean success = true;
        boolean result = false;
        try {
            jedis = jedisPool.getResource();
            result = (jedis.setnx(key, value) != 0);
            if (millisec > -1) {
                jedis.pexpire(key, millisec);
            }
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "setNxStringByMillisec", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }

        return result;

    }

    /**
     * 加入所有公会id
     */
    public void addTongIdToList(String key, String str) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //Pipeline pipe = jedis.pipelined();
            jedis.lpush(key, str);
            //pipe.lpush(key, str);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "addTongIdsToListException: ", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    public void addTongIdToList(String key, String[] str) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //Pipeline pipe = jedis.pipelined();
            if (str.length != 0) {
                jedis.lpush(key, str);
            }
            //pipe.lpush(key, str);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "addTongIdsToListException: ", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    /**
     * 从list中lpop
     */
    public List<String> getValueFromList(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        List<String> rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.lrange(key, 0, -1);
            //rt = jedis.lpop(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getValueFromList lpop", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }

    public String popList(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.lpop(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getValueFromList lpop", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }


    public List<String> getValueFromTongids(String key, int start, int end) {
        Jedis jedis = null;
        boolean sucess = true;
        List<String> rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.lrange(key, start, end);
            //rt = jedis.lpop(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getValueFromList lpop", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }


    public long getListSize(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.llen(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getTongIds", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return 0;
    }


    public void deleteFromSet(String key, String v) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.zrem(key, v);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "deleteFromSet", e);
            logger.info(key + " deleteFromSet fail..... " + v);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    //取RANK TONG_ALLPLAYER_ZHANLI_LV5 TONG_ALLPLAYER_ZHANLI_LV10对应的大小
    private static final long MAX_ZHANLI_SUM = 30 * 50000;//最大战力

    public long getZhanliLvSize(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        Long zcount = 0l;
        try {
            jedis = jedisPool.getResource();
            zcount = jedis.zcard(key);
            //zcount = jedis.zcount(key, 0, MAX_ZHANLI_SUM);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "deleteFromList tong_ids", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return zcount;
    }

    /**
     * 从RANK TONG_ALLPLAYER_ZHANLI_LV5 TONG_ALLPLAYER_ZHANLI_LV10取对应tongid
     */
    public Set<String> getValFromZhanliLvOrRank(String key, int start, int end) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<String> set = new HashSet<String>();
        try {
            jedis = jedisPool.getResource();
            set = jedis.zrange(key, start, end);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "deleteFromList tong_ids", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return set;
    }

    /**
     * 获取排行榜排名列表
     */
    public Set<String> getRankList(String key, int start, int end) {

        Jedis jedis = null;
        boolean success = true;
        Set<String> set = new HashSet<String>();
        try {
            jedis = jedisPool.getResource();
            set = jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            success = false;
            returnBrokenResource(jedis, "getRankList", e);
        } finally {
            if (success && jedis != null) {
                returnResource(jedis);
            }
        }
        return set;

    }

    /**
     * 取member具体排名
     */
    public long getRank(String key, String member) {
        Jedis jedis = null;
        boolean success = true;
        long rank = 0;
        try {
            jedis = jedisPool.getResource();
            rank = jedis.zrevrank(key, member) == null ? 0 : jedis.zrevrank(key, member) + 1;
        } catch (Exception e) {
            success = false;
            returnBrokenResource(jedis, "getRank", e);
        } finally {
            if (success && jedis != null) {
                returnResource(jedis);
            }
        }
        return rank;
    }

    public boolean zAddByZhanli(String key, String member, long value) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //记录最后一个心跳时间
            Long zadd = jedis.zadd(key, value, member);
            //pipe.zadd(key,value,member);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zAdd key:" + key + "member:" + member + "value:" + value, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    /**
     * 设置战队/玩家的匹配结果
     */
    public void setMatchString(String key, String value, int seconds) {
        Jedis jedis = null;
        boolean sucess = true;
        seconds = -1;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
            //pipe.set(key, value);
            if (seconds > -1) {
                jedis.expire(key, seconds);
                //pipe.expire(key, seconds);
            }
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "setString", e);
            expire(key, 0);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    /**
     * 判断是否存在该key
     */
    public boolean judguKeyExist(String key) {
        Jedis jedis = null;
        boolean success = true;
        boolean isExist = true;
        try {
            jedis = jedisPool.getResource();
            isExist = jedis.exists(key);
        } catch (Exception e) {
            success = false;
            returnBrokenResource(jedis, "judgeKeyExist", e);
        } finally {
            if (success && jedis != null) {
                returnResource(jedis);
            }
        }
        return isExist;
    }

    /**
     * 倒序
     */
    public Set<Tuple> zRevRangeWithScores(String key, long start, long end) {
        Jedis jedis = null;
        boolean success = true;
        Set<Tuple> set = null;
        try {
            jedis = jedisPool.getResource();
            set = jedis.zrevrangeWithScores(key, start, end);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "hold zRevRangeWithScores", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }

        return set;
    }

    /**
     * 取到具体某个成员的分数
     */
    public double getScore(String key, String tongid) {
        Jedis jedis = null;
        boolean success = true;
        Double zscore = 0d;
        try {
            jedis = jedisPool.getResource();
            zscore = jedis.zscore(key, tongid);
            zscore = (zscore == null ? 0d : zscore);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "hold getScore", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }
        return zscore;
    }

    /**
     * 管道
     */
    public Pipeline getPipe() {
        Jedis jedis = null;
        boolean success = true;
        Pipeline pipe = null;
        try {
            jedis = jedisPool.getResource();
            pipe = jedis.pipelined();
        } catch (Exception e) {
            success = false;
            returnBrokenResource(jedis, "getPipe", e);
        } finally {
            if (success && jedis != null) {
                returnResource(jedis);
            }
        }
        return pipe;

    }


    //--------------- init
    public Set<String> zRevRangeByScore(String key, double max, double min, int offset, int count) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, "zRevRangeByScore", key, e, false);
        } finally {
            if (success && jedis != null) {
                releaseRedisSource(success, jedis);
            }
        }
        return Collections.emptySet();
    }

    //	----------------------------


    //-------------------- 跑马灯 ----------------------------


    /**
     * get  short set(有序列表)
     *
     * @param key
     * @param beginIndex 0 开始.
     * @param endIndex
     * @return 正序排序(从小到大)的 有序列表  [beginIndex endIndex]
     */
    public Set<String> zRange(String key, int beginIndex, int endIndex) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrange(key, beginIndex, endIndex);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, "zrange", key, e, false);
        } finally {
            if (success && jedis != null) {
                releaseRedisSource(success, jedis);
            }
        }
        return Collections.emptySet();
    }


    /**
     * ZCOUNT key beginScore endScore
     * select 统计区间数量
     *
     * @param key
     * @param beginScore
     * @param endScore
     * @return 分数值在 min 和 max 之间的成员的数量。
     */
    public long zcount(String key, long beginScore, long endScore) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcount(key, beginScore, endScore);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, "zcount", key, e, false);
        } finally {
            if (success && jedis != null) {
                releaseRedisSource(success, jedis);
            }
        }
        return -1;
    }

    /**
     * delete  清除有序列表 数据
     *
     * @param key
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public Long zremrangeByRank(String key, long beginIndex, long endIndex) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.zremrangeByRank(key, beginIndex, endIndex);
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, "zrange", key, e, false);
        } finally {
            if (success && jedis != null) {
                releaseRedisSource(success, jedis);
            }
        }
        return -1l;
    }


    /**
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        Jedis jedis = null;
        boolean sucess = true;
        Set<Tuple> ret = null;
        try {
            jedis = jedisPool.getResource();
            ret = jedis.zrangeWithScores(key, start, end);
            return ret;
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zrangeByScoreWithScores", e);
            // 我需要在这个里面删除member , 但是担心异常发生.所以如果有异常在这个地方返回set
            return null;
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    /**
     * 返回集合key的基数(集合中元素的数量)。
     *
     * @param key
     * @return
     */
    public long zcardString(String key) {
        Jedis jedis = null;
        boolean sucess = true;
        long rt = 0L;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.zcard(key);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "scardString" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }
    //-------------------- 跑马灯 ----------------------------


    public long incrby(String key, int val) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.incrBy(key, val);
        } catch (Exception e) {
            success = false;
            returnBrokenResource(jedis, "incrby" + key, e);
        } finally {
            if (success && jedis != null) {
                returnResource(jedis);
            }
        }
        return 0;
    }


    public long rpush(String key, String value) {
        Jedis jedis = null;
        boolean sucess = true;
        long ret = -1;
        try {
            jedis = jedisPool.getResource();
            ret = jedis.rpush(key, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "rpush key:" + key + "value:" + value, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return ret;
    }

    /**
     * 常用于 redis 分布式锁的 死锁获取.
     *
     * @param key
     * @param value
     * @return
     */
    public String getSetString(String key, String value) {
        Jedis jedis = null;
        boolean sucess = true;
        String rt = null;
        try {
            jedis = jedisPool.getResource();
            rt = jedis.getSet(key, value);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "getString", e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return rt;
    }


    /**
     * 取到具体某个成员的排名
     */
    public int zrevrank(String key, String tongid) {
        Jedis jedis = null;
        boolean success = true;
        Long rank = -1l;
        try {
            jedis = jedisPool.getResource();
            rank = jedis.zrevrank(key, tongid);
            if (rank == null) {
                rank = -1L;
            }
        } catch (Exception e) {
            success = false;
            releasBrokenReidsSource(jedis, key, "zrevrank", e, false);
        } finally {
            releaseRedisSource(success, jedis);
        }
        long rankl = rank;
        return (int) rankl;
    }


    /**
     * 增加值
     *
     * @param key
     * @param member
     * @param value
     * @return
     */
    public long zIncrByBackValue(String key, String member, double value) {
        Jedis jedis = null;
        boolean sucess = true;
        long final_value = 0;
        try {
            jedis = jedisPool.getResource();
            //记录最后一个心跳时间
            final_value = jedis.zincrby(key, value, member).longValue();
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zIncrBy key:" + key + "member:" + member + "value:" + value, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return final_value;
    }


    public Long zrank(String key, String member) {
        Jedis jedis = null;
        boolean sucess = true;
        Long ret = -1L;
        try {
            jedis = jedisPool.getResource();
            ret = jedis.zrank(key, member);
            if (ret == null) {
                ret = -1L;
            }
            return ret;
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zrank", e);
            return ret;
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }

    /**
     * 删除ZSet中, 某个区间Score的成员们
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public boolean zremRangeByScore(String key, long min, long max) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            //记录最后一个心跳时间
            jedis.zremrangeByScore(key, min, max);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "zremRangeByScore key:" + key + "min:" + min + "max:" + max, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
        return sucess;
    }

    /**
     * 重命名键名
     */
    public void rename(String oldKey, String newKey) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.rename(oldKey, newKey);
        } catch (Exception e) {
            returnBrokenResource(jedis, "rename old_key:" + oldKey + ", new_key" + newKey, e);
        } finally {
            if (null != jedis) {
                returnResource(jedis);
            }
        }
    }

    public long incrby(String key, long val) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = jedisPool.getResource();
            return jedis.incrBy(key, val);
        } catch (Exception e) {
            success = false;
            returnBrokenResource(jedis, "incrby" + key, e);
        } finally {
            if (success && jedis != null) {
                returnResource(jedis);
            }
        }
        return 0;
    }

    public void pexpire(String key, int seconds) {
        Jedis jedis = null;
        boolean sucess = true;
        try {
            jedis = jedisPool.getResource();
            jedis.pexpire(key, seconds);
        } catch (Exception e) {
            sucess = false;
            returnBrokenResource(jedis, "expire:" + key, e);
        } finally {
            if (sucess && jedis != null) {
                returnResource(jedis);
            }
        }
    }
}



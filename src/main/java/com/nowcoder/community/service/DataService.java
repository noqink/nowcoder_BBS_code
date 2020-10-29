package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将ip存入UV
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVkey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计UV
    public long calculateUV(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数异常");
        }

        String redisKey = RedisKeyUtil.getUVkey(df.format(start), df.format(end));
        List<String> keys = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVkey(df.format(calendar.getTime()));
            keys.add(key);
            calendar.add(Calendar.DATE, 1);
        }
        redisTemplate.opsForHyperLogLog().union(redisKey, keys.toArray());

        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUkey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计DAU
    public long calculateDAU(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数异常");
        }

        String redisKey = RedisKeyUtil.getDAUkey(df.format(start), df.format(end));
        List<byte[]> keys = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUkey(df.format(calendar.getTime()));
            keys.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }
        // 进行OR运算
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keys.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}

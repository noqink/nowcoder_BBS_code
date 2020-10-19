package com.nowcoder.community.util;


public class RedisKeyUtil {

    private static final String SPILIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    // 关注
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    // 某个实体赞的key
    // 格式:  like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPILIT + entityType + SPILIT + entityId;
    }

    // 某个用户的赞
    // 格式:  like:user:userid -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_ENTITY_LIKE + SPILIT + userId;
    }

    // 某个用户关注的实体
    // 格式: followee:userId:entityType -> zset(entityId, now date()) 时间排序
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPILIT + userId + SPILIT + entityType;
    }

    // 某个实体的粉丝
    // 格式: follower:entityType:entityId -> zset(userId, now date()) 时间排序
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWEE + SPILIT + entityType + SPILIT + entityId;
    }

}

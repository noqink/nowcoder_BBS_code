package com.nowcoder.community.util;


public class RedisKeyUtil {

    private static final String SPILIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";

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
        return PREFIX_USER_LIKE + SPILIT + userId;
    }

    // 某个用户关注的实体
    // 格式: followee:userId:entityType -> zset(entityId, now date()) 时间排序
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPILIT + userId + SPILIT + entityType;
    }

    // 某个实体的粉丝
    // 格式: follower:entityType:entityId -> zset(userId, now date()) 时间排序
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPILIT + entityType + SPILIT + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner){
        return KAPTCHA + SPILIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPILIT + ticket;
    }

    // 用户
    // 登录凭证
    public static String getUserKey(int userId){
        return PREFIX_USER + SPILIT + userId;
    }

    public static String getUVkey(String date){
        return PREFIX_UV + SPILIT + date;
    }

    public static String getUVkey(String startDate, String endDate){
        return PREFIX_UV + SPILIT + startDate + SPILIT + endDate;
    }

    public static String getDAUkey(String date){
        return PREFIX_DAU + SPILIT + date;
    }

    public static String getDAUkey(String startDate, String endDate){
        return PREFIX_DAU + SPILIT + startDate + SPILIT + endDate;
    }

    //帖子分数(点赞加精等操作 存入postId)
    public static String getPostScoreKey(){
        return PREFIX_POST + SPILIT + "score";
    }
}

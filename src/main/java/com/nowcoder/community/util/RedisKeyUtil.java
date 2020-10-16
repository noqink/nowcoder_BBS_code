package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPILIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 某个实体赞的key
    // 格式:  like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPILIT + entityType + SPILIT + entityId;
    }

}

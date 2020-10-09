package com.nowcoder.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = -1;

    /**
     * 重复激活
     */

    int ACTIVATION_REPEAT = 0;

    /**
     * 默认状态登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态登录凭证的超时时间
     */

    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 14;
}

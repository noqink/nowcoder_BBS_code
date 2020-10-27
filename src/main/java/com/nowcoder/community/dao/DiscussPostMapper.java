package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DiscussPostMapper {

    // userId为"我发布的帖子"功能调用
    // offset,limit为分页功能调用
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 动态sql在<if>中使用,单一参数,一定要取别名,否则报错
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);
}

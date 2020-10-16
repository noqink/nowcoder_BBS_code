package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId){
        User user = hostHolder.getUsers();
        likeService.like(user.getId(), entityType, entityId);

        // 点赞数量和状态
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeService.findEntityLikeCount(entityType, entityId));
        map.put("likeStatus", likeService.findEntityLikeStatus(user.getId(), entityType, entityId));

        return CommunityUtil.getJSONString(0, null, map);
    }
}

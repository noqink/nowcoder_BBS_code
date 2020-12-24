package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUsers();

        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUsers();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        // 给list加入关注状态
        List<Map<String, Object>> followeesList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followeesList != null){
            for (Map<String, Object> followeeMap : followeesList
                    ) {
                // 加入当前登陆用户对每个关注列表用户的关注状态
                User followeeUser = (User) followeeMap.get("user");
                followeeMap.put("followeeStatus", hasFollow(followeeUser.getId()));
            }
        }

        model.addAttribute("followees", followeesList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        // 给list加入关注状态
        List<Map<String, Object>> followersList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followersList != null){
            for (Map<String, Object> followerMap : followersList
            ) {
                // 加入当前登陆用户对每个关注列表用户的关注状态
                User followerUser = (User) followerMap.get("user");
                System.out.println(followerUser);
                followerMap.put("followerStatus", hasFollow(followerUser.getId()));
            }
        }
        logger.warn(followersList.toString());
        System.out.println(followersList.toString());
        model.addAttribute("followers", followersList);

        return "/site/follower";
    }

    private boolean hasFollow(int userId){
        if (hostHolder.getUsers() == null){
            return false;
        }

        return followService.hasFollow(hostHolder.getUsers().getId(), ENTITY_TYPE_USER, userId);
    }

}

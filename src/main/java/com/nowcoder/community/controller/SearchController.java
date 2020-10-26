package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private LikeService likeService;

    //    /serach?keywords=
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keywords, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> discussPosts
                = elasticSearchService.searchDiscussPost(keywords, page.getCurrent() - 1, page.getLimit());
        List<Map<String, Object>> discussposts = new ArrayList<>();
        for (DiscussPost post: discussPosts
             ) {
            Map<String, Object> map = new HashMap<>();
            // 帖子
            map.put("post", post);
            // 作者
            map.put("user", userService.findUserById(post.getUserId()));
            // 点赞
            map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
            discussposts.add(map);
        }
        // 分页信息
        page.setPath("/search?keywords="+keywords);
        page.setRows(discussPosts == null ? 0 : (int) discussPosts.getTotalElements());

        model.addAttribute("discussPosts", discussposts);
        model.addAttribute("keywords", keywords);
        return "site/search";
    }
}

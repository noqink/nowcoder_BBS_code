package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){

        User user = hostHolder.getUsers();
        if (user == null){
            // 未登陆
            return CommunityUtil.getJSONString(403, "用户未登录！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        // 报错将来统一处理
        discussPostService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Page page, Model model){
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        // 获取user得到username 还可以用关联查询 之后改为redis缓存
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 评论区
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        List<Comment> comments = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> show_comments = new ArrayList<>();
        if (comments != null){
            for (Comment comment: comments
                 ) {
                Map<String,Object> map = new HashMap<>();
                // 评论
                map.put("comment", comment);
                User userOfComment = userService.findUserById(comment.getUserId());
                // 评论作者
                map.put("user", userOfComment);

                // 评论也有评论
                // 回复不进行分页
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> show_reply = new ArrayList<>();
                if (show_reply != null){
                    for (Comment reply: replyList
                         ) {
                        Map<String,Object> replyMap = new HashMap<>();
                        replyMap.put("reply", reply);
                        User userOfReply = userService.findUserById(reply.getUserId());
                        replyMap.put("user", userOfReply );
                        // 回复目标！
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyMap.put("target", target);
                        show_reply.add(replyMap);
                    }
                }
                map.put("replys",show_reply);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                map.put("replyCount",replyCount);
                show_comments.add(map);
            }
        }

        model.addAttribute("comments",show_comments);
        return "site/discuss-detail";
    }
}

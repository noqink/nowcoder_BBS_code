package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/list",method = RequestMethod.GET)
    public String getMessageList(Model model, Page page){
        User user = hostHolder.getUsers();
        // 设置分页信息
        page.setPath("/message/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> show_conversations = new ArrayList<>();
        for (Message m: conversations
             ) {
            Map<String, Object> map = new HashMap<>();
            map.put("conversation", m);
            map.put("unreadCount", messageService.findeLetterUnreadCount(user.getId(), m.getConversationId()));
            map.put("letterCount", messageService.findLetterCount(m.getConversationId()));

            int targetId = user.getId() == m.getFromId() ? m.getToId() : m.getFromId();
            map.put("targetUser", userService.findUserById(targetId));
            show_conversations.add(map);
        }
        model.addAttribute("conversations", show_conversations);
        // 查询未读消息数量
        int unreadCountAll = messageService.findeLetterUnreadCount(user.getId(), null);
        model.addAttribute("unreadCountAll", unreadCountAll);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "site/letter";
    }

    @RequestMapping(path = "/detail/{conversationId}",method = RequestMethod.GET)
    public String getMessageDetail(@PathVariable String conversationId, Model model, Page page){
        page.setPath("/message/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setLimit(5);

        List<Message> letters = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> show_letters = new ArrayList<>();
        for (Message m:letters
             ) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter", m);
            map.put("fromUser", userService.findUserById(m.getFromId()));
            show_letters.add(map);
        }
        model.addAttribute("targetUser", findTargetUser(conversationId));
        model.addAttribute("letters", show_letters);

        // 设置已读
        List<Integer> ids = getMessageIds(letters);
        if (ids != null && !ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "site/letter-detail";
    }

    @RequestMapping(path = "/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName, String content){
        User toUser = userService.findUserByUsername(toName);
        if (toUser == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUsers().getId());
        message.setToId(toUser.getId());
        if (message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.insertLetter(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUsers();
        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if (message != null){
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postid"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unread", unread);
        }else {
            messageVo.put("message", null);
        }
        model.addAttribute("commentNotice", messageVo);
        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (message != null){
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postid"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unread", unread);
        }else {
            messageVo.put("message", null);
        }
        model.addAttribute("likeNotice", messageVo);
        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (message != null){
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
//            messageVo.put("postId", data.get("postid"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unread", unread);
        }else {
            messageVo.put("message", null);
        }
        model.addAttribute("followNotice", messageVo);
        // 查询未读消息数量
        int letterUnreadCount = messageService.findeLetterUnreadCount(user.getId(), null);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic,Page page, Model model){
        User user = hostHolder.getUsers();

        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        page.setPath("/message/notice/detail/"+topic);

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList!=null){
            for (Message notice: noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getMessageIds(noticeList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "site/notice-detail";
    }
    private User findTargetUser(String conversationId){

        String[] s = conversationId.split("_");
        int id0 = Integer.valueOf(s[0]);
        int id1 = Integer.valueOf(s[1]);
        if (hostHolder.getUsers().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }

    }

    private List<Integer> getMessageIds(List<Message> messageList){
        List<Integer> ids = new ArrayList<>();
        if (messageList!=null){
            for (Message message :messageList
                    ) {
                if (hostHolder.getUsers().getId() == message.getToId() && message.getStatus() == 0){
                    // 接收者身份
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

}

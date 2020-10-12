package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/message")
public class MessageController {

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

        return "site/letter-detail";
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

}

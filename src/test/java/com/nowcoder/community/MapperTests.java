package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abcd");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        LoginTicket ticket = loginTicketMapper.selectByTicket("abcd");
        System.out.println(ticket);
        loginTicketMapper.updateStatus(ticket.getTicket(),1);

    }

    @Test
    public void testMessage(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 10);
        for (Message m: messages
             ) {
            System.out.println(m);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        List<Message> messageList = messageMapper.selectLetters("111_112", 0, 5);
        for (Message m: messageList
        ) {
            System.out.println(m);
        }
        System.out.println(messageMapper.selectLetterUnreadCount(111,"111_112"));
        System.out.println(messageMapper.selectLetterCount("111_112"));
    }

}

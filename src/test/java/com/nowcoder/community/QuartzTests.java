package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTests {
    // 删除job！
    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDeleteJob(){
        try {
            boolean b = scheduler.deleteJob(new JobKey("JobTest", "testJobGroup"));
            System.out.println(b);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}

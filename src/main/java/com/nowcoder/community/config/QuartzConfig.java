package com.nowcoder.community.config;

import com.nowcoder.community.quartz.JobTest;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
// 配置 -> 数据库 -> 访问db调度任务
public class QuartzConfig {

    // BeanFactory 是IOC容器顶层接口
    // FactoryBean 可简化Bean的实例化过程:
    // 1.Spring通过FactoryBean封装了Bean的实例化过程
    // 2.将FactoryBean装配到Spring容器中
    // 3.将FactoryBean注入给其他的Bean
    // 4.该Bean得到的是FactoryBean所管理的对象实例

    // 配置JobDetail
    //@Bean
    public JobDetailFactoryBean jobDetailTest(){
        JobDetailFactoryBean jobDetailFactoryBean = new  JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(JobTest.class);
        jobDetailFactoryBean.setName("JobTest");
        jobDetailFactoryBean.setGroup("testJobGroup");
        // 声明任务持久保存
        jobDetailFactoryBean.setDurability(true);
        // 任务是否可恢复
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    // trigger 触发器 与 JobDetail有关系
    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBea(复杂,解决每个月月底何时xxx))
    //@Bean
    public SimpleTriggerFactoryBean triggerTest(JobDetail jobDetailTest){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetailTest);
        factoryBean.setName("triggerTest");
        factoryBean.setGroup("testTriggerGroup");
        // 执行频率
        factoryBean.setRepeatInterval(3000);
        // trigger底层存储job状态
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}

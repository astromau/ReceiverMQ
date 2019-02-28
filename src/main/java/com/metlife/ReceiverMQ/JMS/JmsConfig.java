package com.metlife.ReceiverMQ.JMS;

import com.ibm.mq.jms.MQQueueConnectionFactory; 
import com.ibm.msg.client.wmq.WMQConstants; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.context.annotation.Primary; 
import org.springframework.jms.annotation.EnableJms; 
import org.springframework.jms.config.DefaultJmsListenerContainerFactory; 
import org.springframework.jms.config.JmsListenerContainerFactory; 
import org.springframework.jms.connection.CachingConnectionFactory; 
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter; 
import org.springframework.jms.core.JmsTemplate; 
import org.springframework.jms.listener.SimpleMessageListenerContainer; 
 
 
import javax.jms.MessageListener; 
 
@Configuration 
@EnableJms 
public class JmsConfig { 
 
    @Value("${mq.host}") 
    private String host; 
    @Value("${mq.port}") 
    private Integer port; 
    @Value("${mq.queueManager}") 
    private String queueManager; 
    @Value("${mq.channel}") 
    private String channel; 
    @Value("${mq.queues.dataQueue}") 
    private String queueData; 
    @Value("${mq.queues.statusQueue}") 
    private String queueStatus; 
    @Value("${mq.user}") 
    private String user; 
    @Value("${mq.password}") 
    private String password; 
 
    @Bean 
    public MQQueueConnectionFactory mqQueueConnectionFactory() { 
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory(); 
        try { 
            System.out.println(host + " " + port  + " " + queueManager  + " " + channel  + " " + user  + " " + password); 
            mqQueueConnectionFactory.setHostName(host); 
            mqQueueConnectionFactory.setQueueManager(queueManager); 
            mqQueueConnectionFactory.setPort(port); 
            mqQueueConnectionFactory.setChannel(channel); 
            mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT); 
            mqQueueConnectionFactory.setCCSID(1208); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return mqQueueConnectionFactory; 
    } 
 
    @Bean 
    public UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(MQQueueConnectionFactory mqQueueConnectionFactory) { 
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter(); 
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory); 
        userCredentialsConnectionFactoryAdapter.setUsername(user); 
        userCredentialsConnectionFactoryAdapter.setPassword(password); 
        return userCredentialsConnectionFactoryAdapter; 
    } 
 
    @Bean 
    @Primary 
    public CachingConnectionFactory cachingConnectionFactory(UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) { 
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(); 
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter); 
        cachingConnectionFactory.setSessionCacheSize(500); 
        cachingConnectionFactory.setReconnectOnException(true); 
        return cachingConnectionFactory; 
    } 
 
    @Bean 
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(CachingConnectionFactory cachingConnectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) { 
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory(); 
        configurer.configure(factory, cachingConnectionFactory); 
        factory.setConnectionFactory(cachingConnectionFactory); 
        factory.setConcurrency("4-8"); // core 4 threads and max 8 threads 
        return factory; 
    } 
 
/* 
    @Bean 
    public SimpleMessageListenerContainer dataContainer(CachingConnectionFactory cachingConnectionFactory) { 
        MessageListener listener = new PrintListener(); 
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(); 
        container.setConnectionFactory(cachingConnectionFactory); 
        container.setDestinationName(queueData); 
        container.setMessageListener(listener); 
        container.start(); 
        return container; 
    } 
*/ 
    @Bean 
    public SimpleMessageListenerContainer statusContainer(CachingConnectionFactory cachingConnectionFactory) { 
        MessageListener listener = new JMSListener();
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(); 
        container.setConnectionFactory(cachingConnectionFactory); 
        container.setDestinationName(queueStatus); 
        container.setMessageListener(listener); 
        container.start(); 
        return container; 
    } 
 
    @Bean 
    public JmsTemplate jmsQueueTemplate(CachingConnectionFactory cachingConnectionFactory) { 
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory); 
        jmsTemplate.setConnectionFactory(cachingConnectionFactory); 
        jmsTemplate.setDefaultDestinationName(queueStatus); 
        return jmsTemplate; 
    } 
}

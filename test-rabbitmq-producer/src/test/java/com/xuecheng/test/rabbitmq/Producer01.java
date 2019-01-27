package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.management.Query;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer01 {
    //队列
    private static final String QUEUE = "helloWorld";
    public static void main(String[] args)  {
        //通过连接工厂创建的新连接和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //服务器ip地址本地
        connectionFactory.setHost("127.0.0.1");
        //端口
        connectionFactory.setPort(5672);
        //用户名
        connectionFactory.setUsername("guest");
        //用户密码
        connectionFactory.setPassword("guest");
        //设置虚拟机，一个mq服务可以设置多个虚拟机，每一个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");
        Connection connection = null;
        try {
            //建立连接
            connection = connectionFactory.newConnection();
            //创建会话通道，生产者和mq服务所有同学都在channel通道中完成
            Channel channel = connection.createChannel();
            //声明队列，如果队列在mq中没有则要创建
            /**
             * 参数明细
             * 1.queue队列名称
             * 2.durable是否持久化，如果是持久化，mq重启后队列还在
             * 3.exclusive是否独占连接，队列只允许在该链接中访问，如果connection连接关闭后队列自动删除，如果将此参数设置true可
             * 用于临时队列的创建
             * 4.autoDelete 自动删除,队列不在使用时是否自动删除此队列，如果将此参数和exclusive参数设置为ture
             * 就可以实现为临时对象
             * 5.arguments 参数，可以设置一个小队列的扩展参数，比如：可以设置存活时间
             */
            channel.queueDeclare(QUEUE,true,false,false,null );
            //发送消息
            /**
             * 1.exchange,交换机,如果不指定将使用mq的默认交换机
             * 2.routingKey：路由key，交换机根据路由key来讲消息转发到指定的队列，如果使用默认的交换机，routingKey设置
             * 为队列名称
             * 3.props：消息的属性
             * 4.body：消息内容
             *
             */
            //消息内容
            String message = "hello world 哈哈哈哈";
            channel.basicPublish("", QUEUE,null,message.getBytes());
            System.out.println("send to mq" + message);
            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //关闭连接
            //先关闭通道
            try {
                assert connection != null;
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

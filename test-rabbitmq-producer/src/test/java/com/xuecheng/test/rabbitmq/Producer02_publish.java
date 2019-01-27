package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Producer02_publish {
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";

    public static void main(String[] args) {
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
        Channel channel =null;
        try {
            //建立连接
            connection = connectionFactory.newConnection();
            //创建会话通道，生产者和mq服务所有同学都在channel通道中完成
             channel = connection.createChannel();
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
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            //声明交换机
            /*
               参数明细：
                1.交换机的名称
                2.交换机的类型
                fanout：对应的rabbitmq的工作模式是publish/subscribe
                direct：对应routing动作模式
                topic：对应topics工作模式
                headers:对应headers工作模式
             */
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.FANOUT);
            //交换机和队列进行绑定
            /**
             * 参数明细：
             *  1、queue队列名称
             *  2、exchange交换机名称
             *  3、routingKey路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中协调为空字符串
             */
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_TOPICS_INFORM, "");
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPICS_INFORM, "");
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
            for (int i = 0; i < 5; i++) {
                String message = "send inform message to user";
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "", null, message.getBytes());
                System.out.println("send to mq" + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            //先关闭通道
            try {
                assert channel != null;
                channel.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

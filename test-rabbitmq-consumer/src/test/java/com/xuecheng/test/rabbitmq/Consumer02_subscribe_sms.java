package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Consumer02_subscribe_sms {
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
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
        //建立连接
        try {
            Connection connection = connectionFactory.newConnection();
            //创建会话通道，生产者和mq服务所有同学都在channel通道中完成
            Channel channel = connection.createChannel();
            //监听队列
            /*
             * 参数明细
             * 1.queue队列名称
             * 2.durable是否持久化，如果是持久化，mq重启后队列还在
             * 3.exclusive是否独占连接，队列只允许在该链接中访问，如果connection连接关闭后队列自动删除，如果将此参数设置true可
             * 用于临时队列的创建
             * 4.autoDelete 自动删除,队列不在使用时是否自动删除此队列，如果将此参数和exclusive参数设置为ture
             * 就可以实现为临时对象
             * 5.arguments 参数，可以设置一个小队列的扩展参数，比如：可以设置存活时间
             */
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.FANOUT);
            //交换机和队列进行绑定
            /**
             * 参数明细：
             *  1、queue队列名称
             *  2、exchange交换机名称
             *  3、routingKey路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中协调为空字符串
             */
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPICS_INFORM, "");
            //消费方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {

                /**
                 * 当消息接收到消息后此方法将被调用
                 * @param consumerTag 消费者标签用来标识消费者的，在监听队列时设置channel，basicConsume
                 * @param envelope 信封，通过envelope可以拿到交换机
                 * @param properties 消息属性
                 * @param body 消息内容
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //用来标识id，可用于确认消息已接收
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message = new String(body, "utf-8");
                    System.out.println("receive message" + message);
                }
            };
            //监听队列
            /*
             *  参数明细：
             *      1.queue队列名称
             *      2.autoAck 自动回复，当消费者接收到消息后要告诉mq消息已接收，如果将参数设置为true表示会自动回复mq，如果设置为false要通过编程
             *      实现回复
             *      3.callback：消费方法,当消费者接收到消息要执行的方法
             *
             */
            channel.basicConsume(QUEUE_INFORM_SMS, true, defaultConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

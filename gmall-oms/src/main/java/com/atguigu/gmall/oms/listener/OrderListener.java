package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.ums.vo.UserBoundsVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderListener {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = {"ORDER-DEAD-QUEUE"})
    public void closeOrder(String orderToken) {
        // 如果执行了关单操作 ，解锁库存
        if (this.orderDao.updateCloseOrder(orderToken) == 1) {
            this.amqpTemplate.convertAndSend("GMALL_ORDER_EXCHANGE", "stock.unLock", orderToken);

        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-PAY_QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL_ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken) {


        // 更新订单状态
       if (this.orderDao.payOrder(orderToken) == 1) {
           // 减库存
           this.amqpTemplate.convertAndSend("GMALL_ORDER_EXCHANGE", "stock.minus", orderToken);

           // 加积分
           UserBoundsVo userBoundsVo = new UserBoundsVo();
           OrderEntity orderEntity = this.orderDao.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
           if (orderEntity != null) {
               userBoundsVo.setMemberId(orderEntity.getMemberId());
               userBoundsVo.setGrowth(orderEntity.getGrowth());
               userBoundsVo.setIntegration(orderEntity.getIntegration());
           }

           // TODO
           //this.amqpTemplate.convertAndSend("GMALL_ORDER_EXCHANGE", "user.bounds", userBoundsVo);
       }
    }
}

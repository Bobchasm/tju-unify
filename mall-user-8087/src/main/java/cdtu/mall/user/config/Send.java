package cdtu.mall.user.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Send {

    @Bean
    public Queue queue()
    {
        return new Queue("queue");
    }
}

package config.redis;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by yxs on 2019/1/12.
 * Redis配置类
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisAuthConfiguration {

//    @Autowired
//    private JedisConnectionFactory con;
//
//    @Bean
//    public RedisTemplate<String, RoleInfo> baseRoleTemplate() {
//        RedisTemplate<String, RoleInfo> template = new RedisTemplate();
//        template.setConnectionFactory(con);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<String, MenuInfo> baseModelTemplate() {
//        RedisTemplate<String, MenuInfo> template = new RedisTemplate();
//        template.setConnectionFactory(con);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
//    @Bean
//    public RedisTemplate<String, DeptInfo> baseDeptTemplate() {
//        RedisTemplate<String, DeptInfo> template = new RedisTemplate();
//        template.setConnectionFactory(con);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new RedisObjectSerializer());
//        return template;
//    }
    /**
     * 配置自定义redisTemplate
     * @return
     */
    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(new RedisObjectSerializer());
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}

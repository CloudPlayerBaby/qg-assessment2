package com.yuriyuri.platform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PlatformApplicationTests {

    @Test
    void contextLoads() {
    }


    private Jedis jedis;

    @BeforeEach
    public void setUp() {
        // 每个测试前执行：连接 Redis
        jedis = new Jedis("localhost", 6379);
        // 如果有密码，取消注释
         jedis.auth("123321");
    }

    @AfterEach
    public void tearDown() {
        // 每个测试后执行：关闭连接
        if (jedis != null) {
            jedis.close();
        }
    }

    @Test
    public void testConnection() {
        // 测试连接是否成功
        String pong = jedis.ping();
        assertEquals("PONG", pong);
        System.out.println("连接成功！");
    }

    @Test
    public void testSetAndGet() {
        // 测试存值和取值
        jedis.set("name", "猫娘");
        String value = jedis.get("name");

        assertNotNull(value);
        assertEquals("猫娘", value);
        System.out.println("存值取值测试通过：" + value);
    }

}

package com.example.thesisconnectback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 毕业论文选题双向选择系统启动类
 */
@SpringBootApplication()
@MapperScan("com.example.thesisconnectback.mapper")
@EnableAsync
@EnableScheduling
public class ThesisConnectBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThesisConnectBackApplication.class, args);
        System.out.println("=================================");
        System.out.println("毕业论文选题双向选择系统启动成功！");
        System.out.println("系统访问地址：http://localhost:8080");
        System.out.println("API文档地址：http://localhost:8080/swagger-ui.html");
        System.out.println("=================================");
    }

}

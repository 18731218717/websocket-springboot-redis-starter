package org.dsm.trainingsystem.rest.websocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @ Author     ：zhengkai.
 * @ Date       ：Created in 15:58 2020/5/19
 * @ Description：WebSocket配置类
 * @ Modified By：
 * @Version: 1.0$
 */

@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public EndpointConfigure newConfigure() {
        return new EndpointConfigure();
    }
}
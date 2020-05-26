package org.dsm.trainingsystem.rest.websocket;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.websocket.server.ServerEndpointConfig;

/**
 * @ Author     ：zhengkai.
 * @ Date       ：Created in 15:57 2020/5/19
 * @ Description：节点配置类
 * @ Modified By：
 * @Version: 1.0$
 */
public class EndpointConfigure extends ServerEndpointConfig.Configurator implements ApplicationContextAware {
    private static volatile BeanFactory context;

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return context.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EndpointConfigure.context = applicationContext;
    }
}

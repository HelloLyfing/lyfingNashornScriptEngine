package com.lyfing.demo.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean的容器及工厂
 * @author lyfing
 */
public abstract class BeanContainerAdapter {

    private static Logger log = LoggerFactory.getLogger(BeanContainerAdapter.class);

    public abstract <T> T getBean(String name, Class<T> requiredType);

}

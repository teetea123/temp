package com.bacon.web;

import java.util.ArrayList;
import java.util.List;

import org.cat.spring.mvc.RequestJsonBodyMethodArgumentResolver;
import org.cat.spring.mvc.RequestJsonParamMethodArgumentResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.SessionAttributeMethodArgumentResolver;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

@Component
public class Reconfig implements BeanPostProcessor {

    private static FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
    private static RequestJsonParamMethodArgumentResolver requestJsonParamMethodArgumentResolver = new RequestJsonParamMethodArgumentResolver();
    private static RequestJsonBodyMethodArgumentResolver requestJsonBodyMethodArgumentResolver = new RequestJsonBodyMethodArgumentResolver();

    public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            RequestMappingHandlerAdapter rmha = (RequestMappingHandlerAdapter) bean;
            reconfigRequestMappingHandlerAdapter(rmha);
        }
        return bean;
    }

    private void reconfigRequestMappingHandlerAdapter(RequestMappingHandlerAdapter rmha) {
        List<HttpMessageConverter<?>> messageConverters = rmha.getMessageConverters();
        messageConverters.add(fastJsonHttpMessageConverter);
        //
        List<HandlerMethodArgumentResolver> argResolversNew = new ArrayList<HandlerMethodArgumentResolver>();
        argResolversNew.add(requestJsonParamMethodArgumentResolver);
        argResolversNew.add(requestJsonBodyMethodArgumentResolver);
        argResolversNew.add(new SessionAttributeMethodArgumentResolver());//TODO 兼容旧版本
        argResolversNew.addAll(rmha.getArgumentResolvers());
        rmha.setArgumentResolvers(argResolversNew);
    }
}

package org.cat.spring.mvc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

//com.fasterxml.jackson
//org.codehaus.jackson
public class RequestJsonParamMethodArgumentResolver implements WebArgumentResolver, HandlerMethodArgumentResolver {

    @Override
    public Object resolveArgument(MethodParameter parameter, NativeWebRequest request) throws Exception {
        if (!supportsParameter(parameter)) {
            return WebArgumentResolver.UNRESOLVED;
        }
        RequestJsonParam anno = parameter.getParameterAnnotation(RequestJsonParam.class);
        String name = anno.value() !=null ? anno.value() : parameter.getParameterName();
        String[] paramValues = request.getParameterValues(name);
        if (paramValues == null || paramValues.length == 0) {
            return null;
        }
        if (paramValues.length > 1) {
            throw new UnsupportedOperationException("too many request json parameter values for '" //
                    + name + "'");
        }
        String jsonStr = paramValues[0];
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }

        Class<?> paramType = parameter.getParameterType();
        try {
            if (JSONObject.class.isAssignableFrom(paramType)) {
                JSONObject result = JSON.parseObject(jsonStr);
                return result;
            }
            return JSON.parseObject(jsonStr, paramType);
        } catch (Exception e) {
            throw new RuntimeException("Could not read request json parameter", e);
        }

    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestJsonParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return resolveArgument(parameter, webRequest);
    }

}

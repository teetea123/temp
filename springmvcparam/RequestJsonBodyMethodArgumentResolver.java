package org.cat.spring.mvc;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class RequestJsonBodyMethodArgumentResolver implements WebArgumentResolver, HandlerMethodArgumentResolver {

    @Override
    public Object resolveArgument(MethodParameter parameter, NativeWebRequest request) throws Exception {
        if (!supportsParameter(parameter)) {
            return WebArgumentResolver.UNRESOLVED;
        }
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        Boolean isParsed = (Boolean) servletRequest.getAttribute("RequestJsonBody.isParsed");
        String jsonStr = null;
        JSONObject root = null;
        if (isParsed == null) {
            servletRequest.setAttribute("RequestJsonBody.isParsed", true);
            HttpInputMessage inputMessage = new ServletServerHttpRequest(servletRequest);
            byte[] bytes = IOUtils.toByteArray(inputMessage.getBody());
            jsonStr = new String(bytes, "UTF-8");
            servletRequest.setAttribute("RequestJsonBody.jsonStr", jsonStr);
            if (StringUtils.isBlank(jsonStr)) {
                return null;
            }
            try {
                root = JSON.parseObject(jsonStr);
                servletRequest.setAttribute("RequestJsonBody.root", root);
            } catch(Exception e) {
                throw new RuntimeException("Could not read request json body:" + jsonStr, e);
            }
        } else {
            jsonStr = (String) servletRequest.getAttribute("RequestJsonBody.jsonStr");
            root = (JSONObject) servletRequest.getAttribute("RequestJsonBody.root");
        }
        //
        if (root == null) {
            return null;
        }
        Class<?> paramType = parameter.getParameterType();
        RequestJsonBody anno = parameter.getParameterAnnotation(RequestJsonBody.class);

        try {
            if (anno.value().isEmpty()) {
                if (JSONObject.class.isAssignableFrom(paramType)) {
                    return root;
                }
                return JSON.parseObject(jsonStr, paramType);
            } else {
                Object value = root.get(anno.value());
                if (value == null) {
                    return null;
                }
                return JSONObject.parseObject(JSONObject.toJSONString(value), paramType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read request json body:" + jsonStr, e);
        }

    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestJsonBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, //
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return resolveArgument(parameter, webRequest);
    }

}

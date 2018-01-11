package com.bacon.web;

import java.io.Writer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cat.project.NoNeedLogin;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSONObject;
import com.bacon.service.SysPmsService;
import com.bacon.util.BaconConfig;
import com.bacon.util.BaconConst;
import com.bacon.util.ErrorCode;
import com.bacon.util.security.PmsNeed;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaconInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private BaconConfig baconConfig;
    @Resource
    private SysPmsService pmsSer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if (!(handler instanceof HandlerMethod)) {
//            return true;
//        }
//        HandlerMethod hd = (HandlerMethod) handler;
//        NoNeedLogin noNeedLogin = hd.getMethodAnnotation(NoNeedLogin.class);
//        if (noNeedLogin != null) {
//            return true;
//        }
//        HttpSession session = request.getSession();
//        if (session == null || session.getAttribute(BaconConst.SessionLoginId) == null) {
//            JSONObject result = new JSONObject();
//            ErrorCode.setError(result, ErrorCode.MustLogin);
//            writeResponse(result, response);
//            return false;
//        }
//        return checkPms(hd, session, response);
    	return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            log.error(ex.getMessage(), ex);
            JSONObject result = new JSONObject();
            ErrorCode.setError(result, ErrorCode.Fail, ex.getMessage());
            writeResponse(result, response);
        }
    }

    private boolean checkPms(HandlerMethod hd, HttpSession session, HttpServletResponse response) throws Exception {
        PmsNeed pmsNeedAnno = hd.getMethodAnnotation(PmsNeed.class);
        if (pmsNeedAnno == null) {
            return true;
        }
        long pmsId = pmsNeedAnno.value();
        if (pmsId == 0) {
            return true;
        }
        Long userId = (Long) session.getAttribute(BaconConst.SessionLoginId);
        if (!pmsSer.isUserHasPms(userId, pmsId)) {
            JSONObject result = new JSONObject();
            ErrorCode.setError(result, ErrorCode.MustHavePms);
            writeResponse(result, response);
            return false;
        }
        return true;
    }

    private void writeResponse(JSONObject result, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, baconConfig.baconwwwHost);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        Writer sw = response.getWriter();
        sw.write(result.toJSONString());
        sw.flush();
        sw.close();
    }

}

package com.billy.cc.core.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 组件调用链，用于管理拦截器的运行顺序
 * @author billy.qi
 */
public class Chain {
    private final List<ICCInterceptor> interceptors = new ArrayList<>();
    private final CC cc;
    private int index;

    Chain(CC cc) {
        this.cc = cc;
        if (cc != null && cc.getInterceptors() != null) {
            interceptors.addAll(cc.getInterceptors());
        }
        this.index = 0;
    }

    void addInterceptor(ICCInterceptor interceptor) {
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
    }

    void setInterceptors(ICCInterceptor... interceptors) {
        //异常情况：这里有可能把null添加进来
        this.interceptors.clear();
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    public CCResult proceed() {
        if (interceptors == null || index >= interceptors.size()) {
            return CCResult.defaultNullResult();
        }
        ICCInterceptor interceptor = interceptors.get(index++);
        if (interceptor == null) {
            //处理异常情况：如果为拦截器为null，则执行下一个
            return proceed();
        }
        String name = interceptor.getClass().getName();
        String callId = cc.getCallId();
        if (CC.VERBOSE_LOG) {
            CC.verboseLog(callId, "start interceptor:" + name);
        }
        CCResult result;
        try {
            result = interceptor.intercept(this);
        } catch(Throwable e) {
            //防止拦截器抛出异常
            result = CCResult.defaultExceptionResult(e);
        }
        if (CC.VERBOSE_LOG) {
            CC.verboseLog(callId, "end interceptor:" + name + ".CCResult:" + result);
        }
        //拦截器理论上不应该返回null，但为了防止意外，此处保持CCResult不为null
        //消灭NPE
        if (result == null) {
            result = CCResult.defaultNullResult();
        }
        return result;
    }

    public CC getCC() {
        return cc;
    }
}
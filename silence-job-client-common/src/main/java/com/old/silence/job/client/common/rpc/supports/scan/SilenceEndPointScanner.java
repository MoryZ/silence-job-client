package com.old.silence.job.client.common.rpc.supports.scan;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.annotation.SilenceEndPoint;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
public class SilenceEndPointScanner implements ApplicationContextAware {

    private ApplicationContext context;

    public List<EndPointInfo> doScan() {
        return scanEndPoint();
    }

    private List<EndPointInfo> scanEndPoint() {

        List<EndPointInfo> endPointInfoList = new ArrayList<>();
        String[] beanDefinitionNames = context.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = context.getBean(beanDefinitionName);
            Class executorNotProxy = AopProxyUtils.ultimateTargetClass(bean);
            String executorClassName = executorNotProxy.getName();

            // 扫描类的注解
            SilenceEndPoint jobExecutor = bean.getClass().getAnnotation(SilenceEndPoint.class);
            if (Objects.nonNull(jobExecutor)) {
                Map<Method, Mapping> annotatedMethods = null;
                try {
                    annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                            (MethodIntrospector.MetadataLookup<Mapping>) method -> AnnotatedElementUtils
                                    .findMergedAnnotation(method, Mapping.class));
                } catch (Throwable ex) {
                    SilenceJobLog.LOCAL.error("{} Mapping加载异常：{}", beanDefinitionName, ex);
                }

                for (Map.Entry<Method, Mapping> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    Mapping mapping = entry.getValue();
                    endPointInfoList.add(new EndPointInfo(executorClassName,
                            method, bean, mapping.method(), mapping.path()));
                }
            }

        }

        return endPointInfoList;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}

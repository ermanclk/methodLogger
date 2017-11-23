package com.comodo.nucal.methodlogging.aspect;


import net.logstash.logback.encoder.org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
public class LoggerAdvice {
    private Logger logger = LoggerFactory.getLogger(LoggerAdvice.class);

    @Pointcut("execution( public * com.comodo.nucal..*(..))")
    public void logMethods() {
    }

    @Before("logMethods()")
    public void logBeforeMethodCall(JoinPoint joinPoint) {
        Method targetMethod = getTargetMethod(joinPoint);
        logger.debug("Executing method, " +getSimpleMethodName(targetMethod) + " Input params: " + listParams(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "logMethods()", returning = "result")
    public void logAfterMethodCall(JoinPoint joinPoint, Object result) {
        Method targetMethod = getTargetMethod(joinPoint);

        String returning = (result != null)? convertToString(result): "void";
        logger.debug("Method executed,  "+getSimpleMethodName(targetMethod) + "  Return value:  :" + returning);
    }

    @AfterThrowing(pointcut = "logMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) throws Throwable {
        Method targetMethod = getTargetMethod(joinPoint);

        logger.error("<<error:monitoring>>" + targetMethod + " execution failed. Throwed exception. " + ex.getMessage());
    }

    @Before("@annotation(com.comodo.nucal.methodlogging.aspect.LogMe)")
    public void logAnnotatedMethodInfo(JoinPoint joinPoint) throws Throwable {
        Method targetMethod = getTargetMethod(joinPoint);

        String methodName = getSimpleMethodName(targetMethod);
        LogMe annotation = targetMethod.getAnnotation(LogMe.class);
        if (annotation != null) {
            logger.info(methodName + " " + annotation.value());
        }

        logger.info("<<info:monitoring>> executing " + methodName + " Input params: " + listParams(joinPoint.getArgs()));
    }
    @AfterReturning(pointcut= "@annotation(com.comodo.nucal.methodlogging.aspect.LogMe)", returning = "result")
    public void logAfterAnnotatedMethodInfo(JoinPoint joinPoint, Object result) throws Throwable {
        Method targetMethod = getTargetMethod(joinPoint);
        String methodName = getSimpleMethodName(targetMethod);
        String returning = (result != null)? convertToString(result): "void";
        logger.info("executed " + methodName + " Return value: " + returning);
    }


    private Method getTargetMethod(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod();
    }

    private String getSimpleMethodName(Method method) {
        return "["+method.getDeclaringClass().getSimpleName()+ "." + method.getName()+"]";
    }

    private String listParams(Object[] objList) {
        StringBuilder params = new StringBuilder();
        if (objList != null) {
            for (Object arg : objList) {
                params.append(" " + convertToString(arg));
            }
        }
        return params.toString();
    }


    public String convertToString(Object obj) {
        return ReflectionToStringBuilder.toString(obj);
    }
}

package com.weikey.multifindhub.aop;

import cn.hutool.core.util.StrUtil;
import com.weikey.multifindhub.annotation.RequestKeyParam;
import com.weikey.multifindhub.annotation.RequestLock;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.common.ResultUtils;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 接口防抖 AOP
 *
 * @author wei-key
 * 
 **/
@Aspect
@Component
@Slf4j
public class RequestLockAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 执行拦截
     */
    @Around("@annotation(com.weikey.multifindhub.annotation.RequestLock)")
    public Object doLock(ProceedingJoinPoint point) throws Throwable {
        // 方法对象
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        RequestLock requestLock = method.getAnnotation(RequestLock.class);
        // 生成key
        String key = generateKey(point);
        log.info("key: {}", key);

        // 生成 key 后，使用 setIfAbsent 将 key 保存到 redis 中、记得设置过期时间；
        // 如果 set 成功，说明 redis 中不存在，则将 key 缓存、请求可以放行；
        // 如果 set 失败，说明 redis 中已存在，则拒绝请求。

        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", requestLock.timeout(), requestLock.unit());

        if (success) {
            // 成功，请求放行、执行原方法
            Object result = point.proceed();
            return result;
        } else {
            // 失败
            return ResultUtils.success(null);
        }
    }

    /**
     * 生成key【前缀 + ip + 方法参数】
     * @param point
     * @return
     */
    private String generateKey(ProceedingJoinPoint point) {
        // 挨个遍历方法参数。
        // 如果hasRequestBody==true、并且参数上有 @RequestBody 注解，遍历此参数（请求对象）的各个字段，获取其中有 @RequestKeyParam 的字段，拼接生成 key；并且不用再往后遍历其他的方法参数；
        // 如果方法参数有 @RequestKeyParam，设置hasRequestBody=false（上一步的判断以后不再执行），将参数拼接进 key；
        // 继续遍历上一个方法参数。

        boolean hasRequestBody = true;
        StringBuilder sb = new StringBuilder();
        // 方法对象
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        // 方法参数
        Object[] args = point.getArgs();
        // 方法参数上的注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        RequestLock requestLock = method.getAnnotation(RequestLock.class);
        // key分隔符
        String delimiter = requestLock.delimiter();
        // key前缀
        String prefix = requestLock.prefix();
        if (StrUtil.isBlank(prefix)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "@RequestLock注解的prefix不能为空");
        }
        sb.append(prefix);

        // key拼接上ip
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        String ipAddress = NetUtils.getIpAddress(httpServletRequest);
        sb.append(delimiter).append(ipAddress);

        // 挨个遍历方法参数。
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class<?> cls = arg.getClass();

            Annotation[] arr = parameterAnnotations[i];
            // 参数上没有注解
            if (arr.length == 0) {
                continue;
            }

            // 参数上是否有 @RequestBody 注解
            if ( hasRequestBody && arr[0] instanceof RequestBody) {
                // 遍历各个字段
                Field[] fields = cls.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(RequestKeyParam.class)) {
                        // 允许访问私有字段
                        field.setAccessible(true);
                        try {
                            Object value = field.get(arg);
                            sb.append(delimiter).append(value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 退出外层循环
                break;
            }
            // 如果方法参数有 @RequestKeyParam
            if (arr[0] instanceof RequestKeyParam) {
                // @RequestBody 注解的判断以后不再执行
                hasRequestBody=false;
                sb.append(delimiter).append(arg);
            }
        }
        return sb.toString();
    }
}


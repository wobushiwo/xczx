package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一的异常捕获类
 */
@ControllerAdvice//控制增强
public class ExceptionCatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);
    //定义一个map，配置异常类型所对应的错误代码
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //定义该map的builder对象，去构建ImmutableMap
    protected  static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder =ImmutableMap.builder();

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException c){
        LOGGER.error("catch exception{}",c.getMessage());
        ResultCode resultCode = c.getResultCode();
        return new ResponseResult(resultCode);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception e){

        //记录日志
        LOGGER.error("catch exception{}",e.getMessage());
        if (EXCEPTIONS == null){
            EXCEPTIONS = builder.build();//EXCEPTIONS构建成功
        }
        //从EXCEPTIONS中寻找异常类型所对应的错误代码，如果找到了将错误代码响应个用户，如果找不到响应99999
        ResultCode resultCode = EXCEPTIONS.get(e.getClass());
        if (resultCode != null){
            return new ResponseResult(resultCode);
        }else {
            //返回99999异常
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }

    }

    static {
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }
}

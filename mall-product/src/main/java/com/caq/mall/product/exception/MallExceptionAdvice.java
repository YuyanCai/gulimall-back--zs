package com.caq.mall.product.exception;

import com.caq.common.exception.BizCodeEnum;
import com.caq.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.caq.mall.product.controller")
public class MallExceptionAdvice {
    //指定的包下所有的校验异常都会被这个方法捕捉
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException exception) {
        //定义map，存放所有错误信息
        Map<String, String> map = new HashMap<>();
        //通过BindResult捕获校验结果
        BindingResult bindingResult = exception.getBindingResult();
        //遍历校验结果中所有字段的错误，字段为key，错误信息为value存放到map中
        bindingResult.getFieldErrors().forEach(fieldError -> {
            String message = fieldError.getDefaultMessage();
            String field = fieldError.getField();
            map.put(field, message);
        });
//        控制台打印错误信息
        log.error("数据校验出现问题{},异常类型{}", exception.getMessage(), exception.getClass());
//        返回错误结果，并显示所有错误的数据
        return R.error(400, "数据校验出现问题").put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("未知异常{},异常类型{}",throwable.getMessage(),throwable.getClass());
        return R.error(BizCodeEnum.UNKNOW_EXEPTION.getCode(),BizCodeEnum.UNKNOW_EXEPTION.getMsg());
    }
}

package cn.net.wangshifu.web;

import cn.net.wangshifu.exception.ForbiddenException;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns domain exceptions into pages instead of stack traces.
 *
 * <p>The 2018 version had no exception handling at all: {@code /article?articleId=abc} returned a
 * Tomcat 500 page and an unknown catalog id produced a NullPointerException.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(NotFoundException e) {
        ModelAndView mav = new ModelAndView("404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("message", e.getMessage());
        return mav;
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView handleForbidden(ForbiddenException e) {
        ModelAndView mav = new ModelAndView("403");
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("message", e.getMessage());
        return mav;
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    public ModelAndView handleBadRequest(RuntimeException e) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("status", 400);
        mav.addObject("message", e.getMessage());
        return mav;
    }

    @ExceptionHandler(NumberFormatException.class)
    public ModelAndView handleBadNumber(NumberFormatException e) {
        return badRequest("参数格式不正确");
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class,
    })
    public ModelAndView handleBadParameter(Exception e) {
        return badRequest("请求参数不正确");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleTooLarge(MaxUploadSizeExceededException e) {
        return badRequest("图片太大了，最大 5MB");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResource(NoResourceFoundException e) {
        ModelAndView mav = new ModelAndView("404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("message", "页面不存在");
        return mav;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleWrongMethod(HttpRequestMethodNotSupportedException e) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
        mav.addObject("status", 405);
        mav.addObject("message", "不支持的请求方法");
        return mav;
    }

    private static ModelAndView badRequest(String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("status", 400);
        mav.addObject("message", message);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception e) {
        log.error("unhandled exception", e);
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("status", 500);
        mav.addObject("message", "服务器开小差了，请稍后再试");
        return mav;
    }
}
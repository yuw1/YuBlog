package cn.net.wangshifu.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Reads blog.* strings from the {@code bloginfo} resource bundle.
 *
 * <p>The 2018 code called {@code ResourceBundle.getBundle("bloginfo")} in six controllers; now the
 * bundle is a normal Spring {@link MessageSource} and the locale comes from the request.
 */
@Service
public class BlogInfoService {

    private final MessageSource messageSource;

    public BlogInfoService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    public String blogName() {
        return get("blog.name");
    }
}
package cn.net.wangshifu.service;

import cn.net.wangshifu.model.User;
import cn.net.wangshifu.security.AuthUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

/**
 * Resolves the logged in {@link User} once per request and caches it as a request attribute.
 *
 * <p>In 2018 every controller called {@code new ClassPathXmlApplicationContext(...)} to look this
 * up; this component is the single replacement for those 25 call sites.
 */
@Component
public class CurrentUser {

    private static final String ATTRIBUTE = CurrentUser.class.getName();

    private final UserService userService;

    public CurrentUser(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> find() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return resolve(auth);
        }
        Object cached = attributes.getAttribute(ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (cached instanceof Optional<?> optional) {
            @SuppressWarnings("unchecked")
            Optional<User> user = (Optional<User>) optional;
            return user;
        }
        Optional<User> resolved = resolve(auth);
        attributes.setAttribute(ATTRIBUTE, resolved, RequestAttributes.SCOPE_REQUEST);
        return resolved;
    }

    /** The principal carries the id, so a rename does not lose the session. */
    private Optional<User> resolve(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthUser authUser) {
            return Optional.ofNullable(userService.findById(authUser.getId()));
        }
        return Optional.ofNullable(userService.findByUsername(auth.getName()));
    }

    public User require() {
        return find().orElseThrow(() -> new IllegalStateException("当前请求没有登录用户"));
    }
}
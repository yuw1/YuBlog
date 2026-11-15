package cn.net.wangshifu.security;

import cn.net.wangshifu.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * The logged in principal.
 *
 * <p>It carries the numeric user id on purpose: the rest of the application resolves the current
 * user by id, so renaming yourself does not invalidate the session (the 2018-style lookup by
 * username broke every page right after a rename).
 */
public class AuthUser implements UserDetails, Serializable {

    private final int id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public AuthUser(int id, String username, String password, boolean enabled, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public static AuthUser of(User user, List<String> roles) {
        return new AuthUser(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.isEnabled(), roles);
    }

    public int getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
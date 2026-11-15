package cn.net.wangshifu.config;

import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.dao.UserRoleDao;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.security.AuthUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Spring Security 6 configuration.
 *
 * <p>Two deliberate differences with the 2018 XML configuration:
 * <ul>
 *   <li>the URLs that need a role are listed explicitly instead of relying on the {@code "/admin**"}
 *       Ant pattern, which worked by accident;</li>
 *   <li>{@code anyRequest().authenticated()} is the default, so a controller that forgets its
 *       check is not silently public.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ADMIN_PATHS = {
            "/admin",
            "/admin-disable-user",
            "/admin-enable-user",
            "/admin-clear-all-article-comment",
            "/admin-delete-article-by-id",
            "/admin-delete-comment-by-id",
            "/admin-add-catalog",
            "/admin-rename-catalog",
            "/admin-delete-catalog-by-id",
    };

    private static final String[] OWNER_PATHS = {
            "/owner",
            "/owner-deleteAdminRole",
            "/owner-passAdminApply",
    };

    @Bean
    public UserDetailsService userDetailsService(UserDao userDao, UserRoleDao userRoleDao) {
        return username -> {
            User user = userDao.selectUserByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在");
            }
            List<String> roles = userRoleDao.selectRolesByUserId(user.getId());
            return AuthUser.of(user, roles);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/fonts/**",
                                "/avatars/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers(OWNER_PATHS).hasRole("OWNER")
                        .requestMatchers(ADMIN_PATHS).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("user-name")
                        .passwordParameter("pwd")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"))
                .sessionManagement(session -> session.sessionFixation().migrateSession())
                .userDetailsService(userDetailsService);
        return http.build();
    }
}
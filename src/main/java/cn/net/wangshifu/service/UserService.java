package cn.net.wangshifu.service;

import cn.net.wangshifu.config.YuBlogProperties;
import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.dao.UserRoleDao;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OWNER = "ROLE_OWNER";

    private static final Pattern USERNAME = Pattern.compile("[\\p{L}\\p{N}_]{2,20}");
    private static final int PASSWORD_MIN = 6;
    /** BCrypt only looks at the first 72 bytes. */
    private static final int PASSWORD_MAX = 72;

    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final cn.net.wangshifu.dao.ArticleDao articleDao;
    private final cn.net.wangshifu.dao.CommentDao commentDao;
    private final PasswordEncoder passwordEncoder;
    private final YuBlogProperties properties;

    public UserService(UserDao userDao, UserRoleDao userRoleDao,
                       cn.net.wangshifu.dao.ArticleDao articleDao,
                       cn.net.wangshifu.dao.CommentDao commentDao,
                       PasswordEncoder passwordEncoder, YuBlogProperties properties) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.articleDao = articleDao;
        this.commentDao = commentDao;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Transactional
    public User register(String username, String rawPassword) {
        String name = normalizeUsername(username);
        validatePassword(rawPassword);
        if (userDao.countByUsername(name) > 0) {
            throw new ValidationException("用户名已被占用");
        }
        User user = new User();
        user.setUsername(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setUserPic(properties.getDefaultAvatar());
        user.setEnabled(true);
        userDao.insertUser(user);
        userRoleDao.insertRole(user.getId(), ROLE_USER);
        return user;
    }

    public User findByUsername(String username) {
        return username == null ? null : userDao.selectUserByUsername(username);
    }

    /** Null when the row does not exist (used for denormalised author references). */
    public User findById(int id) {
        return userDao.selectUserById(id);
    }

    public User getById(int id) {
        User user = userDao.selectUserById(id);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        return user;
    }

    @Transactional
    public void updateProfile(int id, String username, String avatarUrl) {
        String name = normalizeUsername(username);
        User current = getById(id);
        if (!current.getUsername().equals(name) && userDao.countByUsername(name) > 0) {
            throw new ValidationException("用户名已被占用");
        }
        String avatar = avatarUrl == null || avatarUrl.isBlank() ? current.getUserPic() : avatarUrl;
        userDao.updateProfile(id, name, avatar);
        // article.author_username / comment.username / comment.user_pic are denormalised copies;
        // without this a rename leaves the old name on every post and comment
        articleDao.updateAuthorUsername(id, name);
        commentDao.updateCommenterName(id, name);
        commentDao.updateCommenterPic(id, avatar);
    }

    @Transactional
    public void changePassword(int id, String rawPassword) {
        validatePassword(rawPassword);
        getById(id);
        userDao.updatePassword(id, passwordEncoder.encode(rawPassword));
    }

    @Transactional
    public void setEnabled(int id, boolean enabled) {
        getById(id);
        userDao.updateEnabled(id, enabled);
    }

    public List<User> listCommonUsers() {
        List<Integer> ids = userRoleDao.selectCommonUserIds();
        return ids.isEmpty() ? List.of() : userDao.selectUsersByIds(ids);
    }

    public List<User> listDisabledUsers() {
        return userDao.selectDisabledUsers();
    }

    public List<User> listAdmins() {
        List<Integer> ids = userRoleDao.selectAdminUserIds();
        return ids.isEmpty() ? List.of() : userDao.selectUsersByIds(ids);
    }

    public List<String> rolesOf(int userId) {
        return userRoleDao.selectRolesByUserId(userId);
    }

    public boolean hasRole(int userId, String role) {
        return rolesOf(userId).contains(role);
    }

    public boolean isAdminOrOwner(int userId) {
        List<String> roles = rolesOf(userId);
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_OWNER);
    }

    @Transactional
    public void grantRole(int userId, String role) {
        if (!rolesOf(userId).contains(role)) {
            userRoleDao.insertRole(userId, role);
        }
    }

    @Transactional
    public void revokeRole(int userId, String role) {
        userRoleDao.deleteRole(userId, role);
    }

    public static String normalizeUsername(String username) {
        String name = username == null ? "" : username.trim();
        if (!USERNAME.matcher(name).matches()) {
            throw new ValidationException("用户名需为 2-20 位的字母、数字、下划线或中文");
        }
        return name;
    }

    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ValidationException("密码不能为空");
        }
        if (rawPassword.length() < PASSWORD_MIN) {
            throw new ValidationException("密码至少 " + PASSWORD_MIN + " 位");
        }
        if (rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > PASSWORD_MAX) {
            throw new ValidationException("密码过长");
        }
    }
}
-- =====================================================================
-- YuBlog 2.0 schema (MySQL 8, utf8mb4)
--
-- 这是【生产库】初始化脚本，只执行一次。它不会 DROP 任何表。
-- 本地体验请用 --spring.profiles.active=demo（H2 内存库 + schema-h2.sql）。
--
-- 与 2018 版的区别：
--   * 业务逻辑从存储过程 / 触发器移到了 Java（见 service 包），库里只剩表和索引；
--   * user.password_md5 改为 password_hash（BCrypt）；
--   * 表 user 改名为 users（user 是 H2/SQL 标准保留字，改名后测试可以直接跑）；
--   * article.abstract 改为 article_abstract，comment.comment 改为 comment.content；
--   * user_role 增加 (user_id, role) 主键，重复授权不会再插入重复行。
--
-- 初始账号（请登录后立刻修改密码）：
--   owner / owner123456  （站长）
--   admin / admin123456  （管理员）
--   alice / alice123456  （普通用户）
-- =====================================================================

CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(20)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    user_pic      VARCHAR(255) NOT NULL DEFAULT '/img/user-pic/default.jpg',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_modified TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_role (
    user_id INT         NOT NULL,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE catalog_tree (
    id        INT         NOT NULL AUTO_INCREMENT,
    name      VARCHAR(50) NOT NULL,
    parent_id INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_catalog_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE article (
    id              INT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(120) NOT NULL,
    content         TEXT         NULL,
    article_abstract VARCHAR(300) NULL,
    author_id       INT          NOT NULL,
    author_username VARCHAR(20)  NULL,
    catalog_id      INT          NOT NULL,
    catalog_name    VARCHAR(50)  NULL,
    last_modified   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_article_author (author_id),
    KEY idx_article_catalog (catalog_id),
    KEY idx_article_modified (last_modified)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE comment (
    id             INT          NOT NULL AUTO_INCREMENT,
    content        TEXT         NULL,
    article_id     INT          NOT NULL,
    article_title  VARCHAR(120) NULL,
    author         VARCHAR(20)  NULL,
    user_id        INT          NOT NULL,
    username       VARCHAR(20)  NULL,
    user_pic       VARCHAR(255) NULL,
    last_modified  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_comment_article (article_id),
    KEY idx_comment_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE admin_apply (
    id              INT         NOT NULL AUTO_INCREMENT,
    user_id         INT         NOT NULL,
    user_name       VARCHAR(20) NULL,
    last_apply_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_apply_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------- seed
INSERT INTO catalog_tree (name, parent_id) VALUES ('博客', 0);
INSERT INTO catalog_tree (name, parent_id)
SELECT '技术', id FROM catalog_tree WHERE name = '博客';
INSERT INTO catalog_tree (name, parent_id)
SELECT '生活', id FROM catalog_tree WHERE name = '博客';

INSERT INTO users (username, password_hash, user_pic) VALUES
    ('owner', '$2a$10$y.4zACG0KhQ8TxUwFGniAuupdyb.F5fPRIs/Nk0YtE23f9xf3uaBa', '/img/user-pic/default.jpg'),
    ('admin', '$2a$10$kFTO7s39roYe8JauCEq3KOgRpVuTsYEWpliJBmqVAi1yNSbqOseGe', '/img/user-pic/default.jpg'),
    ('alice', '$2a$10$PRHCaUkbUF4LlGvGdc8ZI.dcBpMay92rwcOYNM3CMEdsQK6bWRsbu', '/img/user-pic/default.jpg');

INSERT INTO user_role (user_id, role)
SELECT id, 'ROLE_USER' FROM users WHERE username IN ('owner', 'admin', 'alice');
INSERT INTO user_role (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username IN ('owner', 'admin');
INSERT INTO user_role (user_id, role)
SELECT id, 'ROLE_OWNER' FROM users WHERE username = 'owner';

INSERT INTO article (title, content, article_abstract, author_id, author_username, catalog_id, catalog_name)
SELECT '欢迎来到 YuBlog',
       '<p class="lead">这是 2018 年那个 Spring + MyBatis 博客系统迁移到 Spring Boot 3 之后的第一篇文章。</p><p>业务逻辑已经从 MySQL 存储过程和触发器搬回了 Service 层，密码换成了 BCrypt，文章正文在写入时会做 HTML 白名单过滤。</p>',
       '迁移到 Spring Boot 3 之后的第一篇文章',
       u.id, u.username, c.id, c.name
FROM users u, catalog_tree c
WHERE u.username = 'alice' AND c.name = '技术';
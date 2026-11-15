-- =====================================================================
-- YuBlog 2.0 schema for H2 in MySQL compatibility mode.
-- 用于 demo profile 和集成测试；内容和 schema.sql 保持一致，只是多了 DROP，
-- 这样同一个 JVM 里可以反复初始化。生产库请用 schema.sql。
-- =====================================================================

DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS admin_apply;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS catalog_tree;

CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(20)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    user_pic      VARCHAR(255) NOT NULL DEFAULT '/img/user-pic/default.jpg',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_modified TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE user_role (
    user_id INT         NOT NULL,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE catalog_tree (
    id        INT         NOT NULL AUTO_INCREMENT,
    name      VARCHAR(50) NOT NULL,
    parent_id INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_catalog_parent ON catalog_tree (parent_id);

CREATE TABLE article (
    id               INT          NOT NULL AUTO_INCREMENT,
    title            VARCHAR(120) NOT NULL,
    content          TEXT         NULL,
    article_abstract VARCHAR(300) NULL,
    author_id        INT          NOT NULL,
    author_username  VARCHAR(20)  NULL,
    catalog_id       INT          NOT NULL,
    catalog_name     VARCHAR(50)  NULL,
    last_modified    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_article_author ON article (author_id);
CREATE INDEX idx_article_catalog ON article (catalog_id);
CREATE INDEX idx_article_modified ON article (last_modified);

CREATE TABLE comment (
    id            INT          NOT NULL AUTO_INCREMENT,
    content       TEXT         NULL,
    article_id    INT          NOT NULL,
    article_title VARCHAR(120) NULL,
    author        VARCHAR(20)  NULL,
    user_id       INT          NOT NULL,
    username      VARCHAR(20)  NULL,
    user_pic      VARCHAR(255) NULL,
    last_modified TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE INDEX idx_comment_article ON comment (article_id);
CREATE INDEX idx_comment_user ON comment (user_id);

CREATE TABLE admin_apply (
    id              INT         NOT NULL AUTO_INCREMENT,
    user_id         INT         NOT NULL,
    user_name       VARCHAR(20) NULL,
    last_apply_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (user_id)
);

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
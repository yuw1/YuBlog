create table article
(
  id int auto_increment
    primary key,
  content varchar(20000) null,
  author_id int null,
  author_nickname varchar(20) null,
  lase_modified timestamp default CURRENT_TIMESTAMP not null
)
;

create index article_user_id_fk
  on article (author_id)
;

create trigger before_article_add
before INSERT on article
for each row
  BEGIN
    SET NEW.author_nickname = (
      SELECT nickname FROM user WHERE id = NEW.author_id
    );
  END;

create trigger after_article_add
after INSERT on article
for each row
  BEGIN
    INSERT INTO user_article(user_id, article_id) VALUES (NEW.author_id, NEW.id);
  END;

create table comment
(
  id int auto_increment
    primary key,
  comment varchar(100) null,
  article_id int null,
  user_id int null,
  user_nickname varchar(20) null,
  constraint fk_comment_article
  foreign key (article_id) references article (id)
)
;

create index fk_comment_article
  on comment (article_id)
;

create index fk_comment_user
  on comment (user_id)
;

create trigger comment_add
before INSERT on comment
for each row
  BEGIN
    SET NEW.user_nickname = (
      SELECT nickname FROM user WHERE id = NEW.user_id
    );
  END;

create table user
(
  id int auto_increment
    primary key,
  nickname varchar(20) default '' not null,
  password_sha1_nickname varchar(60) not null,
  last_modified timestamp default CURRENT_TIMESTAMP not null
)
;

alter table article
  add constraint article_user_id_fk
foreign key (author_id) references user (id)
;

alter table comment
  add constraint fk_comment_user
foreign key (user_id) references user (id)
;

create table user_article
(
  id int auto_increment
    primary key,
  user_id int null,
  article_id int null,
  constraint user_article_ibfk_1
  foreign key (user_id) references user (id),
  constraint user_article_ibfk_2
  foreign key (article_id) references article (id)
)
;

create index article_id
  on user_article (article_id)
;

create index user_id
  on user_article (user_id)
;

create procedure user_add (IN `_nickname` varchar(20), IN `_password` varchar(30), OUT `_id` int)
  BEGIN
    declare _password_sha1_nickname varchar(52);
    set _password_sha1_nickname = concat(sha1(_password),_nickname);
    INSERT INTO user (username, password_sha1_nickname) VALUES (_nickname, _password_sha1_nickname);
    SET _id = LAST_INSERT_ID();
  END;

create procedure user_auth (IN `_id` int, IN `_password` varchar(30), OUT `_token` varchar(60))
  BEGIN
    DECLARE _nickname VARCHAR(20);
    DECLARE _password_sha1_nickname VARCHAR(60);
    DECLARE user_count INT DEFAULT 0;
    SELECT count(*) FROM user WHERE id = _id INTO user_count;
    IF user_count = 1 THEN
      SELECT nickname FROM user WHERE id = _id INTO _nickname;
      SET _password_sha1_nickname = concat(sha1(_password),_nickname);
      IF _password_sha1_nickname = (SELECT password_sha1_nickname FROM user WHERE id = _id) THEN
        SET _token = 2;
      ELSE
        SET _token = 1;
      END IF;
    ELSE
      SET _token = 0;
    END IF;
  END;

create procedure user_delete (IN `_id` int)
  BEGIN
    DELETE FROM user WHERE id = _id ;
  END;

create procedure user_select_by_id (IN `_id` int, OUT `_nickname` varchar(20))
  BEGIN
    SELECT nickname INTO _nickname FROM user WHERE id = _id;
  END;

create procedure user_update (IN `_id` int, IN `_nickname` varchar(20), IN `_password` varchar(30))
  BEGIN
    declare _password_sha1_nickname varchar(60);
    set _password_sha1_nickname = concat(sha1(_password),_nickname);
    UPDATE user
    SET username = _nickname, password_sha1_nickname = _password_sha1_nickname
    WHERE id = _id;
  END;


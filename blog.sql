create table user
(
	id int auto_increment
		primary key,
	nickname varchar(20) default '' not null,
	password_sha1_nickname varchar(60) not null
);

create procedure user_add (IN `_nickname` varchar(20), IN `_password` varchar(30), OUT `_id` int)
  BEGIN
    declare _password_sha1_nickname varchar(52);
    set _password_sha1_nickname = concat(sha1(_password),_nickname);
    INSERT INTO user(nickname,password_sha1_nickname) VALUES(_nickname,_password_sha1_nickname);
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

create procedure user_delete (IN _id int)
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
    UPDATE user SET nickname = _nickname, password_sha1_nickname = _password_sha1_nickname WHERE id = _id;
  END;


DROP PROCEDURE IF EXISTS user_add;
CREATE PROCEDURE user_add(IN _nickname VARCHAR(20), IN _password VARCHAR(30), OUT _id INT)
  BEGIN
    declare _password_sha1_nickname varchar(52);
    set _password_sha1_nickname = concat(sha1(_password),_nickname);
    INSERT INTO user(nickname,password_sha1_nickname) VALUES(_nickname,_password_sha1_nickname);
    SET _id = LAST_INSERT_ID();
  END;

DROP PROCEDURE IF EXISTS user_adder;
DELIMITER ;
CREATE PROCEDURE user_adder(IN nickname VARCHAR(20), IN password VARCHAR(30))
  BEGIN
    declare password_sha1_nickname varchar(52);
    set password_sha1_nickname = concat(sha1(password),nickname);
    INSERT INTO user(nickname,password_sha1_nickname) VALUES(nickname,password_sha1_nickname);
END;
DELIMITER ;
USE blog;
DROP PROCEDURE IF EXISTS user_auth;
CREATE PROCEDURE user_auth(IN id VARCHAR(20), IN password VARCHAR(30), OUT token INT)
  BEGIN
    DECLARE password_sha1_nickname VARCHAR(60);
    DECLARE nickname VARCHAR(20);
    IF (SELECT count(*) FROM user WHERE id = id) = 0 
    THEN
      SET token = 0;
    ELSE
      SELECT nickname INTO nickname FROM user WHERE id = 10001 LIMIT 1;
      set password_sha1_nickname = concat(sha1(password),nickname);
      IF (SELECT password_sha1_nickname FROM user WHERE id = id) != password_sha1_nickname 
      THEN 
        SET token = 1;
      ELSE
        SET token = 2;
      END IF;
    END IF;

  END;

USE blog;
DROP PROCEDURE IF EXISTS user_auth;
CREATE PROCEDURE user_auth(
  IN _id INT,
  IN _password VARCHAR(30),
  OUT _token VARCHAR(60)
)
  BEGIN
    DECLARE _nickname VARCHAR(20);
    DECLARE password_sha1_nickname1 VARCHAR(60);
    DECLARE password_sha1_nickname2 VARCHAR(60);
    DECLARE user_count INT DEFAULT 0;
    SELECT count(*) FROM user WHERE id = _id INTO user_count;
    IF user_count = 1 THEN
      SELECT nickname FROM user WHERE id = _id INTO _nickname;
      SET password_sha1_nickname1 = concat(sha1(_password),_nickname);
      SELECT password_sha1_nickname FROM user WHERE id = _id INTO password_sha1_nickname2;
      IF password_sha1_nickname1 = password_sha1_nickname2 THEN
        SET _token = 2;
      ELSE
        SET _token = 1;
      END IF;
    ELSE
      SET _token = 0;
    END IF;
  END;

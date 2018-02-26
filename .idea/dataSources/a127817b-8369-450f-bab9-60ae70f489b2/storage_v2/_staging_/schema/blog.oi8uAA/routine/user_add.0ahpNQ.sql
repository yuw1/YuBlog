DROP PROCEDURE user_add;
CREATE PROCEDURE user_add(IN `_username` VARCHAR(20), IN `_password` VARCHAR(30), OUT `_id` VARCHAR(20))
  BEGIN
    declare _password_md5_id varchar(52);
    INSERT INTO user(username) VALUES(_username);
    SET _id = LAST_INSERT_ID();
    set _password_md5_id = MD5(concat(concat(concat(_password,'{'),`_id`),'}'));
    UPDATE user SET password_md5 = _password_md5_id WHERE id=`_id`;
    INSERT INTO user_role(id,role) VALUES (`_id`,'ROLE_USER');
  END;


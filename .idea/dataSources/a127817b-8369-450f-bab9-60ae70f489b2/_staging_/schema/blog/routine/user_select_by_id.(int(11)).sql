DROP PROCEDURE IF EXISTS user_select_by_id;
CREATE PROCEDURE user_select_by_id(IN _id INT, OUT _nickname VARCHAR(20))
  BEGIN
    SELECT nickname INTO _nickname FROM user WHERE id = _id;
  END;

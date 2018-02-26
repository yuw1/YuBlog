DROP PROCEDURE IF EXISTS user_select_by_id;
CREATE PROCEDURE user_select_by_id(IN _id INT, OUT _username VARCHAR(20))
  BEGIN
    SELECT username INTO _username FROM user WHERE id = _id;
  END;

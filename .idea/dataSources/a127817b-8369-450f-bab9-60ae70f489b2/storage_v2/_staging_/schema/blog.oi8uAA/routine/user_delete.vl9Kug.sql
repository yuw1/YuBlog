DROP PROCEDURE IF EXISTS user_delete;
CREATE PROCEDURE user_delete(IN _id INT)
  BEGIN
    DELETE FROM user WHERE id = _id ;
  END;

USE blog;
DROP PROCEDURE IF EXISTS admin_apply_add;
CREATE PROCEDURE admin_apply_add(IN `_user_id` INT)
  BEGIN
    declare _user_name varchar(20);
    IF ((SELECT COUNT(*) FROM user_role WHERE user_role.id = _user_id AND role = "ROLE_ADMIN") = 0)
      THEN
        IF ((SELECT count(*) FROM admin_apply WHERE admin_apply.user_id = _user_id) = 0)
          THEN
            SET _user_name = (
              SELECT username FROM user WHERE id = _user_id
            );
            INSERT INTO admin_apply(user_id, user_name, last_apply_time) VALUE (_user_id, _user_name, now());
          ELSE
            UPDATE admin_apply SET last_apply_time = now() WHERE admin_apply.user_id = `_user_id`;
        END IF;
    END IF;
  END;


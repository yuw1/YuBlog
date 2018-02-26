USE blog;
DROP TRIGGER IF EXISTS comment_add;
CREATE TRIGGER comment_add
  BEFORE INSERT
  ON comment
  FOR EACH ROW
  BEGIN
    SET NEW.username = (
      SELECT username FROM user WHERE id = NEW.user_id
    );
    SET NEW.last_modified = now();
    SET NEW.article_title = (
      SELECT title FROM article WHERE article.id = NEW.article_id
    );
    SET NEW.author = (
      SELECT author_username FROM article WHERE article.id = NEW.article_id
    );
    SET NEW.user_pic = (
      SELECT user_pic FROM user WHERE user.id = NEW.user_id
    );
  END;


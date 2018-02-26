USE blog;
DROP TRIGGER IF EXISTS before_article_add;
CREATE TRIGGER before_article_add
  BEFORE INSERT
  ON article
  FOR EACH ROW
  BEGIN
    SET NEW.author_username = (
      SELECT username FROM user WHERE user.id = NEW.author_id
    );
    SET NEW.catalog_name = (
      SELECT name FROM catalog_tree WHERE catalog_tree.id = NEW.catalog_id
    );
  END;


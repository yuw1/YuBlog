USE blog;
DROP TRIGGER IF EXISTS article_add;
CREATE TRIGGER after_article_add
AFTER INSERT ON article
FOR EACH ROW
  BEGIN
    INSERT INTO user_article(user_id, article_id) VALUES (NEW.author_id, NEW.id);
  END;
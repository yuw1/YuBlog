package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentDao {
    void addComment(Comment comment);

    List<Comment> selectCommentByArticleId(int id);

    List<Comment> selectCommentByCommenterId(int id);

    List<Comment> selectAllComment();

    void deleteAllCommentByUserId(int userId);

    void deleteCommentById(int commentId);

    int selectUserIdByCommentId(@Param("commentId") int commentId);
}

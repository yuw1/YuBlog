package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentDao {

    void insertComment(Comment comment);

    List<Comment> selectByArticleId(int articleId);

    List<Comment> selectByUserId(int userId);

    List<Comment> selectAll();

    Integer selectUserId(int commentId);

    void updateCommenterName(@Param("userId") int userId, @Param("username") String username);

    void updateCommenterPic(@Param("userId") int userId, @Param("userPic") String userPic);

    /** Deletes comments left by other people on the articles of one author. */
    void deleteCommentsOnArticlesOf(int authorId);

    void deleteCommentById(int commentId);

    void deleteByArticleId(int articleId);

    void deleteByUserId(int userId);
}
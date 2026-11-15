package cn.net.wangshifu.controller;

import cn.net.wangshifu.service.CommentService;
import cn.net.wangshifu.service.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final CurrentUser currentUser;

    public CommentController(CommentService commentService, CurrentUser currentUser) {
        this.commentService = commentService;
        this.currentUser = currentUser;
    }

    @PostMapping("/comment")
    public String addComment(@RequestParam("comment_text") String text,
                             @RequestParam("article_id") int articleId) {
        commentService.add(articleId, currentUser.require().getId(), text);
        return "redirect:/article?articleId=" + articleId + "&msg=评论发表成功";
    }

    @PostMapping("/delete-comment-by-id")
    public String deleteComment(@RequestParam("comment-id") int commentId) {
        commentService.delete(commentId, currentUser.require());
        return "redirect:/user?page=comment";
    }
}
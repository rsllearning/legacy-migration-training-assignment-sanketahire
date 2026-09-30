package com.social.modern.repository;

import com.social.modern.domain.Post;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {
    List<Post> findByUserId(Integer userId);

    @Query("SELECT id, user_id, content, created_at, is_deleted FROM posts WHERE user_id = :userId AND is_deleted = false ORDER BY created_at DESC LIMIT 2")
    List<Post> findFeedPostsByUserId(@Param("userId") Integer userId);
}

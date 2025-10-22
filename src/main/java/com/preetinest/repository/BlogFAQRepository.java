package com.preetinest.repository;

import com.preetinest.entity.BlogFAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlogFAQRepository extends JpaRepository<BlogFAQ, Long> {
    Optional<BlogFAQ> findByUuid(String uuid);

    @Query("SELECT bf FROM BlogFAQ bf WHERE bf.blog.id = :blogId")
    List<BlogFAQ> findByBlogId(Long blogId);
}
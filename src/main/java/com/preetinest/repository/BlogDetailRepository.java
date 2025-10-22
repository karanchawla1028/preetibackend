package com.preetinest.repository;

import com.preetinest.entity.BlogDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogDetailRepository extends JpaRepository<BlogDetail, Long> {
    Optional<BlogDetail> findByUuid(String uuid);

    @Query("SELECT bd FROM BlogDetail bd WHERE bd.blog.id = :blogId AND bd.deleteStatus = 2")
    List<BlogDetail> findByBlogId(@Param("blogId") Long blogId);
}
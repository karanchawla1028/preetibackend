package com.preetinest.repository;

import com.preetinest.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByUuid(String uuid);

    Optional<Blog> findBySlug(String slug);

    @Query("SELECT b FROM Blog b WHERE b.deleteStatus = 2 AND b.active = true AND b.displayStatus = true")
    List<Blog> findAllActiveBlogs();

    @Query("SELECT b FROM Blog b WHERE b.service.id = :serviceId AND b.deleteStatus = 2")
    List<Blog> findByServiceId(@Param("serviceId") Long serviceId);
}
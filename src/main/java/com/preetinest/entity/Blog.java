package com.preetinest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "blogs", uniqueConstraints = @UniqueConstraint(columnNames = {"uuid", "slug"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String excerpt;

    @Column(length = 255, nullable = false)
    private String metaTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaKeyword;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaDescription;

    @Column(length = 100, nullable = false)
    private String slug;

    @Column(length = 255)
    private String thumbnailUrl;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "boolean default true COMMENT 'display status'")
    private boolean displayStatus = true;

    @Column(nullable = false, columnDefinition = "boolean default false COMMENT 'show on home'")
    private boolean showOnHome = false;

    @Column(nullable = false, columnDefinition = "integer default 2 COMMENT '1 deleted, 2 not deleted'")
    private int deleteStatus = 2;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Services service;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogDetail> blogDetails = new ArrayList<>();

    @Column(length = 255)
    private String imageUrl;
}
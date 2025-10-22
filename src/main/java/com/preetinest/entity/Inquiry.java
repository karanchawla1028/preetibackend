package com.preetinest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inquiries", uniqueConstraints = @UniqueConstraint(columnNames = {"uuid"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String pageName;

    @Column(length = 50)
    private String pageType; // e.g., "SERVICE", "BLOG", "CLIENT"

    @Column(length = 100)
    private String slug;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "boolean default true COMMENT 'display status'")
    private boolean displayStatus = true;

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
}
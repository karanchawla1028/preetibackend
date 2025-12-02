package com.preetinest.repository;

import com.preetinest.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Optional<Inquiry> findByUuid(String uuid);

    @Query("SELECT i FROM Inquiry i WHERE i.deleteStatus = 2 AND i.active = true AND i.displayStatus = true")
    Page<Inquiry> findAllActiveInquiries(Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE i.deleteStatus = 2 AND i.active = true AND i.displayStatus = true")
    List<Inquiry> findAllActiveInquiries();
}
package com.preetinest.repository;

import com.preetinest.entity.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Clients, Long> {
    Optional<Clients> findByUuid(String uuid);

    Optional<Clients> findBySlug(String slug);

    @Query("SELECT c FROM Clients c WHERE c.deleteStatus = 2 AND c.active = true AND c.displayStatus = true")
    List<Clients> findAllActiveClients();
}
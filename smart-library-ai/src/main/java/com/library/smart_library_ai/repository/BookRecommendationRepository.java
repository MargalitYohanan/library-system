package com.library.smart_library_ai.repository;

import com.library.smart_library_ai.entity.BookRecommendation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRecommendationRepository extends JpaRepository<BookRecommendation,Integer>
{
    Optional<BookRecommendation> findByUserId(UUID userId);

    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);

    // לא צריך לכתוב - קיימים אוטומטית מ-JpaRepository:
    // save()       → ADD או UPDATE
    // findById()   → GET
    // deleteById() → DELETE
}

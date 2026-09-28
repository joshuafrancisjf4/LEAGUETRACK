package com.example.myproject.repository;

import com.example.myproject.model.MatchFixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<MatchFixture, Long> {
    List<MatchFixture> findAllByOrderByRoundAscIdAsc();
}
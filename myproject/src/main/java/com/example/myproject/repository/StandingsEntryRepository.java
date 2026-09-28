package com.example.myproject.repository;

import com.example.myproject.model.StandingsEntry;
import com.example.myproject.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StandingsEntryRepository extends JpaRepository<StandingsEntry, Long> {
    Optional<StandingsEntry> findByTeam(Team team);
}
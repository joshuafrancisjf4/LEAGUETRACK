package com.example.myproject.controller;

import com.example.myproject.dto.MatchScoreDTO;
import com.example.myproject.model.*;
import com.example.myproject.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TournamentController {

    @Autowired
    private TournamentService service;

    // ================= TABLE 1: TEAMS CRUD =================
    @PostMapping("/teams")
    public Team createTeam(@RequestBody Team team) { return service.createTeam(team); }

    @GetMapping("/teams")
    public List<Team> getAllTeams() { return service.getAllTeams(); }

    @PutMapping("/teams/{id}")
    public Team updateTeam(@PathVariable Long id, @RequestBody Team team) { return service.updateTeam(id, team); }

    @DeleteMapping("/teams/{id}")
    public String deleteTeam(@PathVariable Long id) { service.deleteTeam(id); return "Team deleted"; }

    // ================= TABLE 2: FIXTURES CRUD =================
    @PostMapping("/fixtures")
    public Fixture createFixture(@RequestBody Fixture fixture) { return service.createFixture(fixture); }

    @GetMapping("/fixtures")
    public List<Fixture> getAllFixtures() { return service.getAllFixtures(); }

    @PutMapping("/fixtures/{id}")
    public Fixture updateFixture(@PathVariable Long id, @RequestBody Fixture f) { return service.updateFixture(id, f); }

    @DeleteMapping("/fixtures/{id}")
    public String deleteFixture(@PathVariable Long id) { service.deleteFixture(id); return "Fixture deleted"; }

    // ================= TABLE 3: MATCHES CRUD & AUTOMATION =================
    @PostMapping("/matches")
    public Match createMatch(@RequestBody Match match) { return service.createMatch(match); }

    @GetMapping("/matches")
    public List<Match> getAllMatches() { return service.getAllMatches(); }

    @PostMapping("/matches/generate")
    public List<Match> generateFixtures() { return service.generateRoundRobinFixtures(); }

    @PutMapping("/matches/{id}/score")
    public Match recordScore(@PathVariable Long id, @RequestBody MatchScoreDTO scoreDTO) {
        return service.recordMatchResult(matchId(id), scoreDTO);
    }

    private Long matchId(Long id) { return id; }

    @DeleteMapping("/matches/{id}")
    public String deleteMatch(@PathVariable Long id) { service.deleteMatch(id); return "Match deleted"; }

    // ================= TABLE 4: STANDINGS CRUD =================
    @GetMapping("/standings")
    public List<StandingsEntry> getStandings() { return service.getStandings(); }

    @PutMapping("/standings/{id}")
    public StandingsEntry updateStanding(@PathVariable Long id, @RequestBody StandingsEntry entry) {
        return service.updateStanding(id, entry);
    }

    @DeleteMapping("/standings/{id}")
    public String deleteStanding(@PathVariable Long id) { service.deleteStanding(id); return "Standing deleted"; }

    // ================= TOURNAMENT RESET =================
    @DeleteMapping("/tournament/reset")
    public String resetTournament() { service.resetTournament(); return "Tournament successfully reset"; }
}
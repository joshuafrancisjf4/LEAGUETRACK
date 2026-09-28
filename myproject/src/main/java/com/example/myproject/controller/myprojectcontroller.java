package com.example.myproject.controller;

import com.example.myproject.model.MatchFixture;
import com.example.myproject.model.Team;
import com.example.myproject.service.myprojectservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class myprojectcontroller {

    @Autowired
    private myprojectservice service;

    // Register a Team
    @PostMapping("/teams")
    public Team registerTeam(@RequestBody Team team) {
        return service.addTeam(team);
    }

    // View All Teams
    @GetMapping("/teams")
    public List<Team> getAllTeams() {
        return service.getAllTeams();
    }

    // Delete a Team
    @DeleteMapping("/teams/{id}")
    public void deleteTeam(@PathVariable Long id) {
        service.deleteTeam(id);
    }

    // Reset Tournament (Clears All)
    @DeleteMapping("/tournament/reset")
    public void resetTournament() {
        service.resetTournament();
    }

    // Auto Generate Round Robin Fixtures
    @PostMapping("/fixtures/generate")
    public List<MatchFixture> generateFixtures() {
        return service.generateRoundRobinFixtures();
    }

    // View All Fixtures
    @GetMapping("/fixtures")
    public List<MatchFixture> getFixtures() {
        return service.getAllFixtures();
    }

    // Record Match Result
    @PutMapping("/fixtures/{id}/score")
    public MatchFixture recordScore(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        int scoreA = payload.get("scoreA");
        int scoreB = payload.get("scoreB");
        return service.recordMatchScore(id, scoreA, scoreB);
    }

    // View Standings
    @GetMapping("/standings")
    public List<Team> getStandings() {
        return service.getStandings();
    }
}
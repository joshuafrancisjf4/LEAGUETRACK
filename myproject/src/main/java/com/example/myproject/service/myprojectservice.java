package com.example.myproject.service;

import com.example.myproject.model.MatchFixture;
import com.example.myproject.model.Team;
import com.example.myproject.repository.MatchRepository;
import com.example.myproject.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class myprojectservice {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    // 1. Team CRUD operations
    public Team addTeam(Team team) {
        team.setPlayed(0);
        team.setWon(0);
        team.setDrawn(0);
        team.setLost(0);
        team.setPoints(0);
        return teamRepository.save(team);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    // Delete single team and reset fixtures
    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
        matchRepository.deleteAll(); // Old fixtures become invalid if a team is deleted
    }

    // Reset entire tournament
    public void resetTournament() {
        matchRepository.deleteAll();
        teamRepository.deleteAll();
    }

    // 2. Auto-generate Round Robin Fixtures
    public List<MatchFixture> generateRoundRobinFixtures() {
        matchRepository.deleteAll();

        List<Team> registeredTeams = teamRepository.findAll();
        for (Team t : registeredTeams) {
            t.setPlayed(0);
            t.setWon(0);
            t.setDrawn(0);
            t.setLost(0);
            t.setPoints(0);
        }
        teamRepository.saveAll(registeredTeams);

        List<String> teamNames = new ArrayList<>();
        for (Team t : registeredTeams) {
            teamNames.add(t.getName());
        }

        if (teamNames.size() < 2) {
            throw new RuntimeException("At least 2 teams are required to generate fixtures");
        }

        if (teamNames.size() % 2 != 0) {
            teamNames.add("BYE");
        }

        int totalTeams = teamNames.size();
        int totalRounds = totalTeams - 1;
        int matchesPerRound = totalTeams / 2;

        List<MatchFixture> fixtures = new ArrayList<>();

        for (int round = 0; round < totalRounds; round++) {
            for (int match = 0; match < matchesPerRound; match++) {
                String home = teamNames.get(match);
                String away = teamNames.get(totalTeams - 1 - match);

                if (!home.equals("BYE") && !away.equals("BYE")) {
                    fixtures.add(new MatchFixture(round + 1, home, away));
                }
            }
            Collections.rotate(teamNames.subList(1, teamNames.size()), 1);
        }

        return matchRepository.saveAll(fixtures);
    }

    public List<MatchFixture> getAllFixtures() {
        return matchRepository.findAllByOrderByRoundAscIdAsc();
    }

    // 3. Record Match Score & Auto-update Standings
    public MatchFixture recordMatchScore(Long matchId, int scoreA, int scoreB) {
        MatchFixture fixture = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + matchId));

        if (fixture.isCompleted()) {
            throw new RuntimeException("Score has already been recorded for this match");
        }

        fixture.setScoreA(scoreA);
        fixture.setScoreB(scoreB);
        fixture.setCompleted(true);
        matchRepository.save(fixture);

        Team teamA = teamRepository.findByName(fixture.getTeamA());
        Team teamB = teamRepository.findByName(fixture.getTeamB());

        if (teamA != null && teamB != null) {
            teamA.setPlayed(teamA.getPlayed() + 1);
            teamB.setPlayed(teamB.getPlayed() + 1);

            if (scoreA > scoreB) {
                teamA.setWon(teamA.getWon() + 1);
                teamA.setPoints(teamA.getPoints() + 3);
                teamB.setLost(teamB.getLost() + 1);
            } else if (scoreB > scoreA) {
                teamB.setWon(teamB.getWon() + 1);
                teamB.setPoints(teamB.getPoints() + 3);
                teamA.setLost(teamA.getLost() + 1);
            } else {
                teamA.setDrawn(teamA.getDrawn() + 1);
                teamA.setPoints(teamA.getPoints() + 1);
                teamB.setDrawn(teamB.getDrawn() + 1);
                teamB.setPoints(teamB.getPoints() + 1);
            }

            teamRepository.save(teamA);
            teamRepository.save(teamB);
        }

        return fixture;
    }

    // 4. Standings ordered by points descending
    public List<Team> getStandings() {
        return teamRepository.findAllByOrderByPointsDescWonDesc();
    }
}
package com.example.myproject.service;

import com.example.myproject.dto.MatchScoreDTO;
import com.example.myproject.model.*;
import com.example.myproject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TournamentService {

    @Autowired private TeamRepository teamRepository;
    @Autowired private FixtureRepository fixtureRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private StandingsEntryRepository standingsRepository;

    // --- TEAMS CRUD ---
    public Team createTeam(Team team) {
        Team saved = teamRepository.save(team);
        standingsRepository.save(new StandingsEntry(saved));
        return saved;
    }

    public List<Team> getAllTeams() { return teamRepository.findAll(); }

    public Team updateTeam(Long id, Team updated) {
        return teamRepository.findById(id).map(t -> {
            t.setName(updated.getName());
            t.setCoachName(updated.getCoachName());
            t.setContactEmail(updated.getContactEmail());
            return teamRepository.save(t);
        }).orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public void deleteTeam(Long id) {
        Team t = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        standingsRepository.findByTeam(t).ifPresent(standingsRepository::delete);
        teamRepository.delete(t);
    }

    // --- FIXTURES CRUD ---
    public Fixture createFixture(Fixture fixture) { return fixtureRepository.save(fixture); }
    public List<Fixture> getAllFixtures() { return fixtureRepository.findAll(); }
    public Fixture updateFixture(Long id, Fixture f) {
        return fixtureRepository.findById(id).map(fix -> {
            fix.setRoundNumber(f.getRoundNumber());
            fix.setScheduledDate(f.getScheduledDate());
            fix.setVenue(f.getVenue());
            fix.setStatus(f.getStatus());
            return fixtureRepository.save(fix);
        }).orElseThrow(() -> new RuntimeException("Fixture not found"));
    }
    public void deleteFixture(Long id) { fixtureRepository.deleteById(id); }

    // --- MATCHES CRUD & AUTOMATION ---
    public List<Match> getAllMatches() { return matchRepository.findAll(); }
    public Match createMatch(Match match) { return matchRepository.save(match); }
    public void deleteMatch(Long id) { matchRepository.deleteById(id); }

    @Transactional
    public List<Match> generateRoundRobinFixtures() {
        matchRepository.deleteAll();
        fixtureRepository.deleteAll();

        List<Team> teams = teamRepository.findAll();
        if (teams.size() < 2) {
            throw new RuntimeException("At least 2 teams required to generate schedule.");
        }

        List<Match> scheduledMatches = new ArrayList<>();
        int round = 1;
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Fixture fixture = new Fixture(
                        round,
                        LocalDateTime.now().plusDays(round),
                        "Court " + round,
                        "SCHEDULED"
                );
                Fixture savedFixture = fixtureRepository.save(fixture);

                Match match = new Match();
                match.setFixture(savedFixture);
                match.setTeamA(teams.get(i));
                match.setTeamB(teams.get(j));
                scheduledMatches.add(matchRepository.save(match));
                round++;
            }
        }
        return scheduledMatches;
    }

    @Transactional
    public Match recordMatchResult(Long matchId, MatchScoreDTO scoreDTO) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (Boolean.TRUE.equals(match.getResultRecorded())) {
            throw new RuntimeException("Result already recorded for this match.");
        }

        match.setScoreA(scoreDTO.getScoreA());
        match.setScoreB(scoreDTO.getScoreB());
        match.getFixture().setStatus("COMPLETED");

        StandingsEntry sA = standingsRepository.findByTeam(match.getTeamA())
                .orElseGet(() -> standingsRepository.save(new StandingsEntry(match.getTeamA())));
        StandingsEntry sB = standingsRepository.findByTeam(match.getTeamB())
                .orElseGet(() -> standingsRepository.save(new StandingsEntry(match.getTeamB())));

        sA.setPlayed(sA.getPlayed() + 1);
        sB.setPlayed(sB.getPlayed() + 1);

        if (scoreDTO.getScoreA() > scoreDTO.getScoreB()) {
            match.setWinnerTeam(match.getTeamA());
            sA.setWon(sA.getWon() + 1);
            sA.setPoints(sA.getPoints() + 3);
            sB.setLost(sB.getLost() + 1);
        } else if (scoreDTO.getScoreB() > scoreDTO.getScoreA()) {
            match.setWinnerTeam(match.getTeamB());
            sB.setWon(sB.getWon() + 1);
            sB.setPoints(sB.getPoints() + 3);
            sA.setLost(sA.getLost() + 1);
        } else {
            match.setWinnerTeam(null);
            sA.setDrawn(sA.getDrawn() + 1);
            sA.setPoints(sA.getPoints() + 1);
            sB.setDrawn(sB.getDrawn() + 1);
            sB.setPoints(sB.getPoints() + 1);
        }

        match.setResultRecorded(true);
        standingsRepository.save(sA);
        standingsRepository.save(sB);
        return matchRepository.save(match);
    }

    // --- STANDINGS CRUD ---
    public List<StandingsEntry> getStandings() {
        return standingsRepository.findAll(Sort.by(Sort.Direction.DESC, "points", "won"));
    }
    public StandingsEntry updateStanding(Long id, StandingsEntry entry) {
        return standingsRepository.findById(id).map(s -> {
            s.setPoints(entry.getPoints());
            s.setPlayed(entry.getPlayed());
            s.setWon(entry.getWon());
            s.setDrawn(entry.getDrawn());
            s.setLost(entry.getLost());
            return standingsRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Standing not found"));
    }
    public void deleteStanding(Long id) { standingsRepository.deleteById(id); }

    @Transactional
    public void resetTournament() {
        matchRepository.deleteAll();
        fixtureRepository.deleteAll();
        for (StandingsEntry s : standingsRepository.findAll()) {
            s.setPlayed(0);
            s.setWon(0);
            s.setDrawn(0);
            s.setLost(0);
            s.setPoints(0);
            standingsRepository.save(s);
        }
    }
}
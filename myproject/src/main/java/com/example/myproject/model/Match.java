package com.example.myproject.model;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fixture_id")
    private Fixture fixture;

    @ManyToOne
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamA;

    @ManyToOne
    @JoinColumn(name = "team_b_id", nullable = false)
    private Team teamB;

    private Integer scoreA;
    private Integer scoreB;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    private Boolean resultRecorded = false;

    public Match() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Fixture getFixture() { return fixture; }
    public void setFixture(Fixture fixture) { this.fixture = fixture; }

    public Team getTeamA() { return teamA; }
    public void setTeamA(Team teamA) { this.teamA = teamA; }

    public Team getTeamB() { return teamB; }
    public void setTeamB(Team teamB) { this.teamB = teamB; }

    public Integer getScoreA() { return scoreA; }
    public void setScoreA(Integer scoreA) { this.scoreA = scoreA; }

    public Integer getScoreB() { return scoreB; }
    public void setScoreB(Integer scoreB) { this.scoreB = scoreB; }

    public Team getWinnerTeam() { return winnerTeam; }
    public void setWinnerTeam(Team winnerTeam) { this.winnerTeam = winnerTeam; }

    public Boolean getResultRecorded() { return resultRecorded; }
    public void setResultRecorded(Boolean resultRecorded) { this.resultRecorded = resultRecorded; }
}
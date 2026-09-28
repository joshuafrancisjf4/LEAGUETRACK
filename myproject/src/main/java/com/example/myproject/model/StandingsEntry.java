package com.example.myproject.model;

import jakarta.persistence.*;

@Entity
@Table(name = "standings_entries")
public class StandingsEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    private Team team;

    private Integer played = 0;
    private Integer won = 0;
    private Integer drawn = 0;
    private Integer lost = 0;
    private Integer points = 0;

    public StandingsEntry() {}

    public StandingsEntry(Team team) {
        this.team = team;
        this.played = 0;
        this.won = 0;
        this.drawn = 0;
        this.lost = 0;
        this.points = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Integer getPlayed() { return played; }
    public void setPlayed(Integer played) { this.played = played; }

    public Integer getWon() { return won; }
    public void setWon(Integer won) { this.won = won; }

    public Integer getDrawn() { return drawn; }
    public void setDrawn(Integer drawn) { this.drawn = drawn; }

    public Integer getLost() { return lost; }
    public void setLost(Integer lost) { this.lost = lost; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}
const API_BASE = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
  loadTeams();
  loadFixtures();
  loadStandings();

  document.getElementById("addTeamBtn").addEventListener("click", registerTeam);
  document.getElementById("generateFixturesBtn").addEventListener("click", generateFixtures);
  document.getElementById("resetAllBtn").addEventListener("click", resetTournament);
});

// Fetch & Display Registered Teams with Delete Icon
async function loadTeams() {
  try {
    const res = await fetch(`${API_BASE}/teams`);
    const teams = await res.json();
    const list = document.getElementById("teamsList");
    list.innerHTML = "";
    teams.forEach(t => {
      const badge = document.createElement("span");
      badge.className = "badge";
      badge.innerHTML = `
        ${t.name}
        <button class="delete-badge-btn" onclick="deleteTeam(${t.id})">&times;</button>
      `;
      list.appendChild(badge);
    });
  } catch (err) {
    console.error("Error loading teams:", err);
  }
}

// Register a New Team
async function registerTeam() {
  const input = document.getElementById("teamNameInput");
  const name = input.value.trim();
  if (!name) {
    alert("Please enter a team name");
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/teams`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name })
    });

    if (res.ok) {
      input.value = "";
      loadTeams();
      loadStandings();
    } else {
      alert("Failed to add team. The name might already exist.");
    }
  } catch (err) {
    console.error("Error adding team:", err);
  }
}

// Delete a single team
async function deleteTeam(teamId) {
  if (!confirm("Are you sure you want to delete this team? Existing fixtures will be reset.")) return;

  try {
    const res = await fetch(`${API_BASE}/teams/${teamId}`, { method: "DELETE" });
    if (res.ok) {
      loadTeams();
      loadFixtures();
      loadStandings();
    }
  } catch (err) {
    console.error("Error deleting team:", err);
  }
}

// Reset Entire Tournament
async function resetTournament() {
  if (!confirm("Reset entire tournament? All teams, fixtures, and scores will be removed.")) return;

  try {
    const res = await fetch(`${API_BASE}/tournament/reset`, { method: "DELETE" });
    if (res.ok) {
      loadTeams();
      loadFixtures();
      loadStandings();
    }
  } catch (err) {
    console.error("Error resetting tournament:", err);
  }
}

// Generate Round Robin Fixtures
async function generateFixtures() {
  try {
    const res = await fetch(`${API_BASE}/fixtures/generate`, { method: "POST" });
    if (res.ok) {
      loadFixtures();
      loadStandings();
    } else {
      alert("Need at least 2 teams to generate fixtures.");
    }
  } catch (err) {
    console.error("Error generating fixtures:", err);
  }
}

// Fetch & Render Fixtures List
async function loadFixtures() {
  try {
    const res = await fetch(`${API_BASE}/fixtures`);
    const fixtures = await res.json();
    const container = document.getElementById("fixturesContainer");
    container.innerHTML = "";

    if (!fixtures || fixtures.length === 0) {
      container.innerHTML = `<p class="empty-msg">No fixtures generated yet. Add teams and click 'Generate Fixtures'.</p>`;
      return;
    }

    fixtures.forEach(f => {
      const div = document.createElement("div");
      div.className = "fixture-card";

      if (f.completed) {
        div.innerHTML = `
          <div><strong>Round ${f.round}:</strong> ${f.teamA} vs ${f.teamB}</div>
          <div class="completed-badge">${f.scoreA} - ${f.scoreB} (Final)</div>
        `;
      } else {
        div.innerHTML = `
          <div><strong>Round ${f.round}:</strong> ${f.teamA} vs ${f.teamB}</div>
          <div class="fixture-score-inputs">
            <input type="number" id="scoreA_${f.id}" min="0" placeholder="0">
            <span>-</span>
            <input type="number" id="scoreB_${f.id}" min="0" placeholder="0">
            <button onclick="submitScore(${f.id})">Save</button>
          </div>
        `;
      }
      container.appendChild(div);
    });
  } catch (err) {
    console.error("Error loading fixtures:", err);
  }
}

// Submit Match Score
async function submitScore(matchId) {
  const inputA = document.getElementById(`scoreA_${matchId}`);
  const inputB = document.getElementById(`scoreB_${matchId}`);

  if (inputA.value === "" || inputB.value === "") {
    alert("Please enter scores for both teams.");
    return;
  }

  const scoreA = parseInt(inputA.value, 10);
  const scoreB = parseInt(inputB.value, 10);

  try {
    const res = await fetch(`${API_BASE}/fixtures/${matchId}/score`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ scoreA, scoreB })
    });

    if (res.ok) {
      loadFixtures();
      loadStandings();
    } else {
      alert("Failed to record score.");
    }
  } catch (err) {
    console.error("Error saving score:", err);
  }
}

// Fetch & Render Sorted Standings Table
async function loadStandings() {
  try {
    const res = await fetch(`${API_BASE}/standings`);
    const standings = await res.json();
    const tbody = document.getElementById("standingsBody");
    tbody.innerHTML = "";

    if (!standings || standings.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="empty-msg">No standings available.</td></tr>`;
      return;
    }

    standings.forEach((team, index) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${index + 1}</td>
        <td><strong>${team.name}</strong></td>
        <td>${team.played}</td>
        <td>${team.won}</td>
        <td>${team.drawn}</td>
        <td>${team.lost}</td>
        <td><strong>${team.points}</strong></td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("Error loading standings:", err);
  }
}
const API_BASE = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
  refreshAll();

  // Register Team
  document.getElementById("team-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const teamName = document.getElementById("team-name").value.trim();
    const coachName = document.getElementById("coach-name").value.trim();
    const contactEmail = document.getElementById("contact-email").value.trim();

    if (!teamName) return;

    try {
      const res = await fetch(`${API_BASE}/teams`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: teamName, coachName, contactEmail })
      });
      if (res.ok) {
        document.getElementById("team-form").reset();
        await refreshAll();
      } else {
        alert("Failed to add team. Make sure the team name is unique.");
      }
    } catch (err) {
      console.error(err);
      alert("Error communicating with server.");
    }
  });

  // Auto-Generate Schedule
  document.getElementById("generate-btn").addEventListener("click", async () => {
    try {
      const res = await fetch(`${API_BASE}/matches/generate`, { method: "POST" });
      if (!res.ok) {
        const err = await res.json();
        alert(err.message || "Failed to generate schedule. Ensure you have at least 2 teams.");
      } else {
        await refreshAll();
      }
    } catch (err) {
      console.error(err);
    }
  });

  // Reset Tournament
  document.getElementById("reset-btn").addEventListener("click", async () => {
    if (confirm("Reset the tournament? This clears all matches and resets team standings.")) {
      await fetch(`${API_BASE}/tournament/reset`, { method: "DELETE" });
      await refreshAll();
    }
  });
});

async function refreshAll() {
  await Promise.all([loadTeams(), loadMatches(), loadStandings()]);
}

// 1. Load Teams (Table: teams)
async function loadTeams() {
  try {
    const res = await fetch(`${API_BASE}/teams`);
    const teams = await res.json();
    
    // Update KPI counter
    const counter = document.getElementById("teams-count");
    if (counter) counter.textContent = teams.length;

    const tbody = document.querySelector("#teams-table tbody");
    if (teams.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color: var(--text-light); padding: 1.5rem;">No teams registered yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = teams.map(t => `
      <tr>
        <td style="color: var(--text-light); font-weight: 600;">#${t.id}</td>
        <td style="font-weight: 700; color: var(--text-heavy);">${escapeHtml(t.name)}</td>
        <td style="color: var(--text-muted);">${escapeHtml(t.coachName || '-')}</td>
        <td style="text-align: right;">
          <button class="btn-danger btn-sm" onclick="deleteTeam(${t.id})">Delete</button>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    console.error("Error loading teams:", err);
  }
}

async function deleteTeam(id) {
  if (confirm("Delete this team?")) {
    await fetch(`${API_BASE}/teams/${id}`, { method: "DELETE" });
    await refreshAll();
  }
}

// 2. Load Matches & Fixtures (Tables: fixtures, matches)
async function loadMatches() {
  try {
    const res = await fetch(`${API_BASE}/matches`);
    const matches = await res.json();
    
    // Update KPI counter
    const counter = document.getElementById("matches-count");
    if (counter) counter.textContent = matches.length;

    const tbody = document.querySelector("#matches-table tbody");
    if (matches.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-light); padding: 1.5rem;">No fixtures available. Click 'Generate Fixtures'.</td></tr>`;
      return;
    }

    tbody.innerHTML = matches.map(m => {
      const isCompleted = m.resultRecorded;
      return `
        <tr>
          <td style="font-weight: 700; color: var(--brand-accent);">M-${m.id}</td>
          <td style="font-size: 0.8rem; color: var(--text-muted);">
            Round ${m.fixture?.roundNumber || '-'}<br>
            <span style="color: var(--text-light);">${m.fixture?.venue || 'Court 1'}</span>
          </td>
          <td>
            <div class="team-matchup">
              <span>${escapeHtml(m.teamA.name)}</span>
              <span class="vs-badge">VS</span>
              <span>${escapeHtml(m.teamB.name)}</span>
            </div>
          </td>
          <td>
            <span class="status-badge ${isCompleted ? 'status-completed' : 'status-scheduled'}">
              ${isCompleted ? 'Completed' : 'Scheduled'}
            </span>
          </td>
          <td>
            ${isCompleted ? 
              `<span class="score-display">${m.scoreA} :${m.scoreB}</span>` : 
              `<div class="score-editor">
                 <input type="number" id="scA-${m.id}" class="score-input" min="0" value="0">
                 <span style="color: var(--text-light); font-weight: 700;">-</span>
                 <input type="number" id="scB-${m.id}" class="score-input" min="0" value="0">
               </div>`
            }
          </td>
          <td style="text-align: right;">
            ${!isCompleted ? 
              `<button class="btn-primary btn-sm" onclick="submitScore(${m.id})">Save</button>` : 
              `<span style="font-size: 0.75rem; color: var(--badge-green); font-weight: 600;">Recorded</span>`
            }
          </td>
        </tr>
      `;
    }).join("");
  } catch (err) {
    console.error("Error loading matches:", err);
  }
}

async function submitScore(matchId) {
  const scoreA = parseInt(document.getElementById(`scA-${matchId}`).value);
  const scoreB = parseInt(document.getElementById(`scB-${matchId}`).value);

  if (isNaN(scoreA) || isNaN(scoreB) || scoreA < 0 || scoreB < 0) {
    alert("Please enter valid match scores.");
    return;
  }

  await fetch(`${API_BASE}/matches/${matchId}/score`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ scoreA, scoreB })
  });
  await refreshAll();
}

// 3. Load Standings (Table: standings_entries)
async function loadStandings() {
  try {
    const res = await fetch(`${API_BASE}/standings`);
    const standings = await res.json();
    const tbody = document.querySelector("#standings-table tbody");

    if (standings.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-light); padding: 1.5rem;">No standings calculated yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = standings.map((s, index) => {
      let rankClass = "rank-other";
      if (index === 0) rankClass = "rank-1";
      else if (index === 1) rankClass = "rank-2";
      else if (index === 2) rankClass = "rank-3";

      return `
        <tr>
          <td><span class="rank-pill ${rankClass}">${index + 1}</span></td>
          <td style="font-weight: 700; color: var(--text-heavy);">${escapeHtml(s.team.name)}</td>
          <td class="num-col">${s.played}</td>
          <td class="num-col text-win">${s.won}</td>
          <td class="num-col text-draw">${s.drawn}</td>
          <td class="num-col text-loss">${s.lost}</td>
          <td class="num-col text-pts">${s.points}</td>
        </tr>
      `;
    }).join("");
  } catch (err) {
    console.error("Error loading standings:", err);
  }
}

function escapeHtml(str) {
  return String(str || '').replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[m]);
}
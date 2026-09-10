(() => {
  const $ = (selector) => document.querySelector(selector);
  const $$ = (selector) => [...document.querySelectorAll(selector)];

  const STORAGE_KEY = 'wojthom6-web-state-v1';
  const formatter = new Intl.DateTimeFormat('pl-PL', { day: '2-digit', month: '2-digit', year: 'numeric' });

  const state = {
    header: 'Lista Czasu Pracy',
    logs: '',
    entries: [],
    history: [],
    theme: 'dark'
  };

  function uid() {
    return crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }

  function loadState() {
    try {
      const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
      Object.assign(state, saved);
    } catch (_) {}
    $('#headerInput').value = state.header || 'Lista Czasu Pracy';
    $('#logsInput').value = state.logs || '';
    applyTheme(state.theme || 'dark');
  }

  function saveState() {
    state.header = $('#headerInput').value.trim() || 'Lista Czasu Pracy';
    state.logs = $('#logsInput').value;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }

  function pad(n) { return String(n).padStart(2, '0'); }

  function parseDate(rawDay, rawMonth, rawYear) {
    const day = Number(rawDay);
    const month = Number(rawMonth);
    let year = rawYear ? Number(rawYear) : new Date().getFullYear();
    if (rawYear && String(rawYear).length === 2) year += 2000;
    const d = new Date(year, month - 1, day);
    if (d.getFullYear() !== year || d.getMonth() !== month - 1 || d.getDate() !== day) return null;
    return `${year}-${pad(month)}-${pad(day)}`;
  }

  function decimalHoursToMinutes(value) {
    const n = Number(String(value).replace(',', '.'));
    if (!Number.isFinite(n) || n < 0 || n > 24) return 0;
    return Math.round(n * 60);
  }

  function parseLine(source) {
    let rest = source.trim();
    let date = null;
    let start = '-';
    let end = '-';
    let minutes = 0;

    const dateMatch = rest.match(/^(\d{1,2})[.\-/\s]+(\d{1,2})(?:[.\-/\s]+(\d{2,4}))?\b/);
    if (dateMatch) {
      date = parseDate(dateMatch[1], dateMatch[2], dateMatch[3]);
      rest = rest.slice(dateMatch[0].length).trim();
    }

    const rangeMatch = rest.match(/\b(\d{1,2})[:.](\d{2})\s*-\s*(\d{1,2})[:.](\d{2})\b/);
    if (rangeMatch) {
      const sh = Number(rangeMatch[1]);
      const sm = Number(rangeMatch[2]);
      const eh = Number(rangeMatch[3]);
      const em = Number(rangeMatch[4]);
      if (sh <= 23 && eh <= 23 && sm <= 59 && em <= 59) {
        start = `${pad(sh)}:${pad(sm)}`;
        end = `${pad(eh)}:${pad(em)}`;
        const startMinutes = sh * 60 + sm;
        let endMinutes = eh * 60 + em;
        if (endMinutes < startMinutes) endMinutes += 24 * 60;
        minutes = endMinutes - startMinutes;
      }
      rest = rest.replace(rangeMatch[0], '').trim();
    } else {
      const hourMatch = rest.match(/\b(\d+(?:[.,]\d+)?)\s*[hHtT]\b/);
      const clockMatch = rest.match(/\b(\d{1,2})[:.]([0-5]\d)\b/);
      const decimalMatch = rest.match(/(?:^|\s)(\d+(?:[.,]\d+)?)\s*$/);

      if (hourMatch) {
        minutes = decimalHoursToMinutes(hourMatch[1]);
        rest = rest.replace(hourMatch[0], '').trim();
      } else if (clockMatch) {
        const h = Number(clockMatch[1]);
        const m = Number(clockMatch[2]);
        if (h >= 0 && m <= 59) minutes = h * 60 + m;
        rest = rest.replace(clockMatch[0], '').trim();
      } else if (decimalMatch) {
        minutes = decimalHoursToMinutes(decimalMatch[1]);
        rest = rest.slice(0, decimalMatch.index).trim();
      }
    }

    const client = rest.replace(/^[\s|\-]+|[\s|\-]+$/g, '');
    return {
      id: uid(),
      date,
      client,
      start,
      end,
      minutes,
      source,
      valid: Boolean(date && client && minutes > 0)
    };
  }

  function parseLogs(raw) {
    return raw.split(/\r?\n/).map(v => v.trim()).filter(Boolean).map(parseLine);
  }

  function duration(minutes) {
    return `${Math.floor(minutes / 60)}:${pad(minutes % 60)}`;
  }

  function dateText(iso) {
    if (!iso) return 'Nieprawidłowa data';
    const [y, m, d] = iso.split('-').map(Number);
    return formatter.format(new Date(y, m - 1, d));
  }

  function escapeHtml(value) {
    return String(value)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function renderEntries() {
    const list = $('#entriesList');
    const total = state.entries.reduce((sum, e) => sum + e.minutes, 0);
    const valid = state.entries.filter(e => e.valid).length;

    $('#totalTime').textContent = `${duration(total)} h`;
    $('#entryCount').textContent = state.entries.length;
    $('#validCount').textContent = valid;
    $('#validationBadge').textContent = `${valid}/${state.entries.length} poprawnych`;
    $('#emptyState').hidden = state.entries.length > 0;

    list.innerHTML = state.entries.map(entry => `
      <article class="entry-card ${entry.valid ? '' : 'invalid'}" data-id="${entry.id}">
        <div class="entry-main">
          <div class="entry-title">${escapeHtml(entry.client || 'Brak klienta')}</div>
          <div class="entry-meta">
            <span>${escapeHtml(dateText(entry.date))}</span>
            <span>${entry.start !== '-' && entry.end !== '-' ? `${entry.start} – ${entry.end}` : 'czas podany ręcznie'}</span>
            <span class="entry-time">${duration(entry.minutes)} h</span>
          </div>
          ${entry.valid ? '' : '<div class="invalid-note">Wpis wymaga poprawy przed eksportem.</div>'}
        </div>
        <div class="entry-actions">
          <button class="mini-btn edit" type="button" data-action="edit">Edytuj</button>
          <button class="mini-btn delete" type="button" data-action="delete">Usuń</button>
        </div>
      </article>
    `).join('');

    saveState();
    renderStats();
  }

  function renderHistory() {
    const list = $('#historyList');
    $('#historyEmpty').hidden = state.history.length > 0;
    list.innerHTML = [...state.history].reverse().map(item => `
      <article class="entry-card" data-history-id="${item.id}">
        <div class="entry-main">
          <div class="entry-title">${escapeHtml(item.header)}</div>
          <div class="entry-meta">
            <span>${escapeHtml(item.savedAt)}</span>
            <span>${item.entries.length} wpisów</span>
            <span class="entry-time">${duration(item.totalMinutes)} h</span>
          </div>
        </div>
        <div class="entry-actions">
          <button class="mini-btn" type="button" data-action="load-history">Wczytaj</button>
          <button class="mini-btn delete" type="button" data-action="delete-history">Usuń</button>
        </div>
      </article>
    `).join('');
  }

  function renderStats() {
    const allEntries = state.history.flatMap(h => h.entries);
    const total = allEntries.reduce((sum, e) => sum + e.minutes, 0);
    $('#statsTotal').textContent = `${duration(total)} h`;
    $('#statsReports').textContent = state.history.length;
    $('#statsEntries').textContent = allEntries.length;
  }

  function editEntry(id) {
    const entry = state.entries.find(e => e.id === id);
    if (!entry) return;
    const client = prompt('Klient / firma:', entry.client);
    if (client === null) return;
    const work = prompt('Czas pracy (np. 08:00 albo 7.5):', duration(entry.minutes));
    if (work === null) return;

    let minutes = 0;
    if (/^\d{1,2}:\d{2}$/.test(work.trim())) {
      const [h, m] = work.split(':').map(Number);
      if (m >= 0 && m <= 59) minutes = h * 60 + m;
    } else {
      minutes = decimalHoursToMinutes(work);
    }

    entry.client = client.trim();
    entry.minutes = minutes;
    entry.valid = Boolean(entry.date && entry.client && entry.minutes > 0);
    renderEntries();
  }

  function saveToHistory() {
    if (!state.entries.length) return;
    const totalMinutes = state.entries.reduce((sum, e) => sum + e.minutes, 0);
    state.history.push({
      id: uid(),
      savedAt: new Date().toLocaleString('pl-PL'),
      header: $('#headerInput').value.trim() || 'Lista Czasu Pracy',
      totalMinutes,
      entries: structuredClone(state.entries)
    });
    saveState();
    renderHistory();
    renderStats();
  }

  function applyTheme(theme) {
    state.theme = theme === 'light' ? 'light' : 'dark';
    document.documentElement.classList.toggle('light', state.theme === 'light');
    $('#themeBtn').textContent = state.theme === 'light' ? '☾' : '☀';
  }

  $('#generateBtn').addEventListener('click', () => {
    state.logs = $('#logsInput').value;
    state.header = $('#headerInput').value.trim() || 'Lista Czasu Pracy';
    state.entries = parseLogs(state.logs);
    renderEntries();
  });

  $('#clearBtn').addEventListener('click', () => {
    if ((state.entries.length || $('#logsInput').value.trim()) && !confirm('Wyczyścić obecną listę?')) return;
    state.entries = [];
    state.logs = '';
    $('#logsInput').value = '';
    renderEntries();
  });

  $('#saveHistoryBtn').addEventListener('click', saveToHistory);
  $('#clearHistoryBtn').addEventListener('click', () => {
    if (!state.history.length || confirm('Trwale usunąć całą historię?')) {
      state.history = [];
      saveState();
      renderHistory();
      renderStats();
    }
  });

  $('#entriesList').addEventListener('click', (event) => {
    const card = event.target.closest('[data-id]');
    const action = event.target.closest('[data-action]')?.dataset.action;
    if (!card || !action) return;
    if (action === 'delete') {
      state.entries = state.entries.filter(e => e.id !== card.dataset.id);
      renderEntries();
    }
    if (action === 'edit') editEntry(card.dataset.id);
  });

  $('#historyList').addEventListener('click', (event) => {
    const card = event.target.closest('[data-history-id]');
    const action = event.target.closest('[data-action]')?.dataset.action;
    if (!card || !action) return;
    const item = state.history.find(h => h.id === card.dataset.historyId);
    if (!item) return;

    if (action === 'load-history') {
      state.header = item.header;
      state.entries = structuredClone(item.entries).map(e => ({ ...e, id: uid() }));
      $('#headerInput').value = item.header;
      renderEntries();
      activateView('workView');
    }
    if (action === 'delete-history') {
      state.history = state.history.filter(h => h.id !== item.id);
      saveState();
      renderHistory();
      renderStats();
    }
  });

  function activateView(viewId) {
    $$('.view').forEach(view => view.classList.toggle('active', view.id === viewId));
    $$('.nav-item').forEach(btn => btn.classList.toggle('active', btn.dataset.view === viewId));
  }

  $$('.nav-item').forEach(btn => btn.addEventListener('click', () => activateView(btn.dataset.view)));
  $('#themeBtn').addEventListener('click', () => {
    applyTheme(state.theme === 'dark' ? 'light' : 'dark');
    saveState();
  });
  $('#headerInput').addEventListener('input', saveState);
  $('#logsInput').addEventListener('input', saveState);

  loadState();
  renderEntries();
  renderHistory();
  renderStats();
})();

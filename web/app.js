(() => {
  const $ = (selector) => document.querySelector(selector);
  const $$ = (selector) => [...document.querySelectorAll(selector)];

  const STORAGE_KEY = 'wojthom6-web-state-v2';

  const I18N = {
    pl: {
      totalTime: 'Łączny czas', entries: 'Wpisy', valid: 'Poprawne', newListEyebrow: 'NOWA LISTA',
      enterWorkTime: 'Wprowadź czas pracy', clear: 'Wyczyść', reportHeader: 'Nagłówek raportu',
      logsEntries: 'Logi / wpisy', logsPlaceholder: '15.02.2026 Firma A 08:00 - 16:00\n16.02 Firma B 7.5h\n17.02 Firma C 08:30',
      range: 'Zakres', hours: 'Godziny', time: 'Czas', generateList: 'Generuj listę',
      saveHistory: 'Zapisz do historii', exportPdf: 'Eksportuj PDF', workTimeEyebrow: 'CZAS PRACY',
      noEntries: 'Brak wpisów', emptyHint: 'Wklej godziny powyżej i wybierz „Generuj listę”.',
      archive: 'ARCHIWUM', history: 'Historia', clearHistory: 'Usuń historię',
      noSavedLists: 'Nie zapisano jeszcze żadnej listy.', analysis: 'ANALIZA', stats: 'Statystyki',
      total: 'Łącznie', reports: 'Raporty',
      statsNext: 'Rozbudowane statystyki tygodniowe, miesięczne i norma 37,5 h będą kolejnym modułem.',
      appEyebrow: 'APLIKACJA', settings: 'Ustawienia', appLanguage: 'Język aplikacji',
      languageHelp: 'Zmiana języka nie zmienia języka PDF — wybierasz go osobno przy eksporcie.',
      theme: 'Motyw', themeHelp: 'Jasny / ciemny — przycisk w prawym górnym rogu',
      data: 'Dane', dataHelp: 'Zapisywane lokalnie w przeglądarce',
      pdf: 'PDF', pdfHelp: 'Polski / English / Norsk — wybór przy każdym eksporcie',
      version: 'Wersja', work: 'Praca', pdfLanguageTitle: 'Język dokumentu PDF',
      pdfLanguageText: 'Wybierz język, w którym ma zostać zapisany raport. Język aplikacji pozostanie bez zmian.',
      cancel: 'Anuluj', invalidDate: 'Nieprawidłowa data', noClient: 'Brak klienta',
      manualTime: 'czas podany ręcznie', invalidEntry: 'Wpis wymaga poprawy przed eksportem.',
      edit: 'Edytuj', delete: 'Usuń', validCount: 'poprawnych', load: 'Wczytaj',
      reportCount: 'wpisów', editClient: 'Klient / firma:', editWork: 'Czas pracy (np. 08:00 albo 7.5):',
      confirmClear: 'Wyczyścić obecną listę?', confirmHistory: 'Trwale usunąć całą historię?',
      noPdfEntries: 'Brak wpisów do eksportu.', invalidPdfEntries: 'Popraw wszystkie błędne wpisy przed eksportem PDF.',
      defaultHeader: 'Lista Czasu Pracy', themeAria: 'Zmień motyw', navAria: 'Główna nawigacja',
      pdfDate: 'Data', pdfClient: 'Klient / firma', pdfStart: 'Od', pdfEnd: 'Do', pdfWork: 'Czas pracy',
      pdfTotal: 'Łączny czas', pdfEntries: 'Liczba wpisów', pdfGenerated: 'Wygenerowano',
      pdfFooter: 'Wygenerowano w WojThom 6.0 Web', printHint: 'Wybierz „Zapisz jako PDF” w oknie drukowania.'
    },
    en: {
      totalTime: 'Total time', entries: 'Entries', valid: 'Valid', newListEyebrow: 'NEW LIST',
      enterWorkTime: 'Enter work time', clear: 'Clear', reportHeader: 'Report title',
      logsEntries: 'Logs / entries', logsPlaceholder: '15.02.2026 Company A 08:00 - 16:00\n16.02 Company B 7.5h\n17.02 Company C 08:30',
      range: 'Range', hours: 'Hours', time: 'Time', generateList: 'Generate list',
      saveHistory: 'Save to history', exportPdf: 'Export PDF', workTimeEyebrow: 'WORK TIME',
      noEntries: 'No entries', emptyHint: 'Paste work hours above and choose “Generate list”.',
      archive: 'ARCHIVE', history: 'History', clearHistory: 'Clear history',
      noSavedLists: 'No saved lists yet.', analysis: 'ANALYSIS', stats: 'Statistics',
      total: 'Total', reports: 'Reports',
      statsNext: 'Advanced weekly/monthly statistics and the 37.5 h target will be added in the next module.',
      appEyebrow: 'APPLICATION', settings: 'Settings', appLanguage: 'Application language',
      languageHelp: 'Changing the app language does not change the PDF language — you choose it separately when exporting.',
      theme: 'Theme', themeHelp: 'Light / dark — use the button in the top-right corner',
      data: 'Data', dataHelp: 'Stored locally in your browser',
      pdf: 'PDF', pdfHelp: 'Polski / English / Norsk — choose on every export',
      version: 'Version', work: 'Work', pdfLanguageTitle: 'PDF document language',
      pdfLanguageText: 'Choose the language for the saved report. The application language will remain unchanged.',
      cancel: 'Cancel', invalidDate: 'Invalid date', noClient: 'No client',
      manualTime: 'time entered manually', invalidEntry: 'This entry must be fixed before export.',
      edit: 'Edit', delete: 'Delete', validCount: 'valid', load: 'Load',
      reportCount: 'entries', editClient: 'Client / company:', editWork: 'Work time (e.g. 08:00 or 7.5):',
      confirmClear: 'Clear the current list?', confirmHistory: 'Permanently delete the entire history?',
      noPdfEntries: 'There are no entries to export.', invalidPdfEntries: 'Fix all invalid entries before exporting PDF.',
      defaultHeader: 'Work Time List', themeAria: 'Change theme', navAria: 'Main navigation',
      pdfDate: 'Date', pdfClient: 'Client / company', pdfStart: 'Start', pdfEnd: 'End', pdfWork: 'Work time',
      pdfTotal: 'Total time', pdfEntries: 'Number of entries', pdfGenerated: 'Generated',
      pdfFooter: 'Generated with WojThom 6.0 Web', printHint: 'Choose “Save as PDF” in the print dialog.'
    },
    no: {
      totalTime: 'Total tid', entries: 'Oppføringer', valid: 'Gyldige', newListEyebrow: 'NY LISTE',
      enterWorkTime: 'Registrer arbeidstid', clear: 'Tøm', reportHeader: 'Rapportoverskrift',
      logsEntries: 'Logger / oppføringer', logsPlaceholder: '15.02.2026 Firma A 08:00 - 16:00\n16.02 Firma B 7.5h\n17.02 Firma C 08:30',
      range: 'Intervall', hours: 'Timer', time: 'Tid', generateList: 'Generer liste',
      saveHistory: 'Lagre i historikk', exportPdf: 'Eksporter PDF', workTimeEyebrow: 'ARBEIDSTID',
      noEntries: 'Ingen oppføringer', emptyHint: 'Lim inn arbeidstider over og velg «Generer liste».',
      archive: 'ARKIV', history: 'Historikk', clearHistory: 'Slett historikk',
      noSavedLists: 'Ingen lagrede lister ennå.', analysis: 'ANALYSE', stats: 'Statistikk',
      total: 'Totalt', reports: 'Rapporter',
      statsNext: 'Avansert uke-/månedsstatistikk og 37,5 t norm kommer i neste modul.',
      appEyebrow: 'APPLIKASJON', settings: 'Innstillinger', appLanguage: 'App-språk',
      languageHelp: 'Endring av app-språk endrer ikke PDF-språket — det velges separat ved eksport.',
      theme: 'Tema', themeHelp: 'Lys / mørk — bruk knappen øverst til høyre',
      data: 'Data', dataHelp: 'Lagres lokalt i nettleseren',
      pdf: 'PDF', pdfHelp: 'Polski / English / Norsk — velges ved hver eksport',
      version: 'Versjon', work: 'Arbeid', pdfLanguageTitle: 'Språk for PDF-dokument',
      pdfLanguageText: 'Velg språket rapporten skal lagres på. App-språket forblir uendret.',
      cancel: 'Avbryt', invalidDate: 'Ugyldig dato', noClient: 'Ingen kunde',
      manualTime: 'tid angitt manuelt', invalidEntry: 'Oppføringen må rettes før eksport.',
      edit: 'Rediger', delete: 'Slett', validCount: 'gyldige', load: 'Last inn',
      reportCount: 'oppføringer', editClient: 'Kunde / firma:', editWork: 'Arbeidstid (f.eks. 08:00 eller 7.5):',
      confirmClear: 'Tømme gjeldende liste?', confirmHistory: 'Slette hele historikken permanent?',
      noPdfEntries: 'Ingen oppføringer å eksportere.', invalidPdfEntries: 'Rett alle ugyldige oppføringer før PDF-eksport.',
      defaultHeader: 'Arbeidstidsliste', themeAria: 'Bytt tema', navAria: 'Hovednavigasjon',
      pdfDate: 'Dato', pdfClient: 'Kunde / firma', pdfStart: 'Fra', pdfEnd: 'Til', pdfWork: 'Arbeidstid',
      pdfTotal: 'Total tid', pdfEntries: 'Antall oppføringer', pdfGenerated: 'Generert',
      pdfFooter: 'Generert med WojThom 6.0 Web', printHint: 'Velg «Lagre som PDF» i utskriftsdialogen.'
    }
  };

  const state = { header: I18N.pl.defaultHeader, logs: '', entries: [], history: [], theme: 'dark', language: 'pl' };

  function t(key, lang = state.language) { return I18N[lang]?.[key] ?? I18N.pl[key] ?? key; }
  function localeFor(lang = state.language) { return lang === 'en' ? 'en-GB' : lang === 'no' ? 'nb-NO' : 'pl-PL'; }
  function uid() { return crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(16).slice(2)}`; }

  function loadState() {
    try { Object.assign(state, JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')); } catch (_) {}
    if (!I18N[state.language]) state.language = 'pl';
    $('#headerInput').value = state.header || t('defaultHeader');
    $('#logsInput').value = state.logs || '';
    $('#languageSelect').value = state.language;
    applyTheme(state.theme || 'dark');
    applyLanguage();
  }

  function saveState() {
    state.header = $('#headerInput').value.trim() || t('defaultHeader');
    state.logs = $('#logsInput').value;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }

  function setLanguage(lang) {
    if (!I18N[lang] || lang === state.language) return;
    const oldDefault = t('defaultHeader');
    const headerWasDefault = !$('#headerInput').value.trim() || $('#headerInput').value.trim() === oldDefault;
    state.language = lang;
    if (headerWasDefault) { state.header = t('defaultHeader'); $('#headerInput').value = state.header; }
    applyLanguage();
    saveState();
    renderEntries();
    renderHistory();
    renderStats();
  }

  function applyLanguage() {
    document.documentElement.lang = state.language === 'no' ? 'nb' : state.language;
    document.title = `WojThom 6.0 Web — ${state.language.toUpperCase()}`;
    $$('[data-i18n]').forEach(el => { el.textContent = t(el.dataset.i18n); });
    $$('[data-i18n-placeholder]').forEach(el => { el.placeholder = t(el.dataset.i18nPlaceholder); });
    $('#themeBtn').setAttribute('aria-label', t('themeAria'));
    $('#bottomNav').setAttribute('aria-label', t('navAria'));
    $('#languageSelect').value = state.language;
  }

  function pad(n) { return String(n).padStart(2, '0'); }

  function parseDate(rawDay, rawMonth, rawYear) {
    const day = Number(rawDay), month = Number(rawMonth);
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
    let rest = source.trim(), date = null, start = '-', end = '-', minutes = 0;
    const dateMatch = rest.match(/^(\d{1,2})[.\-/\s]+(\d{1,2})(?:[.\-/\s]+(\d{2,4}))?\b/);
    if (dateMatch) { date = parseDate(dateMatch[1], dateMatch[2], dateMatch[3]); rest = rest.slice(dateMatch[0].length).trim(); }

    const rangeMatch = rest.match(/\b(\d{1,2})[:.](\d{2})\s*-\s*(\d{1,2})[:.](\d{2})\b/);
    if (rangeMatch) {
      const sh = Number(rangeMatch[1]), sm = Number(rangeMatch[2]), eh = Number(rangeMatch[3]), em = Number(rangeMatch[4]);
      if (sh <= 23 && eh <= 23 && sm <= 59 && em <= 59) {
        start = `${pad(sh)}:${pad(sm)}`; end = `${pad(eh)}:${pad(em)}`;
        const startMinutes = sh * 60 + sm; let endMinutes = eh * 60 + em;
        if (endMinutes < startMinutes) endMinutes += 24 * 60;
        minutes = endMinutes - startMinutes;
      }
      rest = rest.replace(rangeMatch[0], '').trim();
    } else {
      const hourMatch = rest.match(/\b(\d+(?:[.,]\d+)?)\s*[hHtT]\b/);
      const clockMatch = rest.match(/\b(\d{1,2})[:.]([0-5]\d)\b/);
      const decimalMatch = rest.match(/(?:^|\s)(\d+(?:[.,]\d+)?)\s*$/);
      if (hourMatch) { minutes = decimalHoursToMinutes(hourMatch[1]); rest = rest.replace(hourMatch[0], '').trim(); }
      else if (clockMatch) { const h = Number(clockMatch[1]), m = Number(clockMatch[2]); if (h >= 0 && h <= 24 && m <= 59) minutes = h * 60 + m; rest = rest.replace(clockMatch[0], '').trim(); }
      else if (decimalMatch) { minutes = decimalHoursToMinutes(decimalMatch[1]); rest = rest.slice(0, decimalMatch.index).trim(); }
    }
    const client = rest.replace(/^[\s|\-]+|[\s|\-]+$/g, '');
    return { id: uid(), date, client, start, end, minutes, source, valid: Boolean(date && client && minutes > 0) };
  }

  function parseLogs(raw) { return raw.split(/\r?\n/).map(v => v.trim()).filter(Boolean).map(parseLine); }
  function duration(minutes) { return `${Math.floor(minutes / 60)}:${pad(minutes % 60)}`; }

  function dateText(iso, lang = state.language) {
    if (!iso) return t('invalidDate', lang);
    const [y, m, d] = iso.split('-').map(Number);
    return new Intl.DateTimeFormat(localeFor(lang), { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(y, m - 1, d));
  }

  function escapeHtml(value) {
    return String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;');
  }

  function renderEntries() {
    const list = $('#entriesList');
    const total = state.entries.reduce((sum, e) => sum + e.minutes, 0);
    const valid = state.entries.filter(e => e.valid).length;
    $('#totalTime').textContent = `${duration(total)} h`;
    $('#entryCount').textContent = state.entries.length;
    $('#validCount').textContent = valid;
    $('#validationBadge').textContent = `${valid}/${state.entries.length} ${t('validCount')}`;
    $('#emptyState').hidden = state.entries.length > 0;
    list.innerHTML = state.entries.map(entry => `
      <article class="entry-card ${entry.valid ? '' : 'invalid'}" data-id="${entry.id}">
        <div class="entry-main"><div class="entry-title">${escapeHtml(entry.client || t('noClient'))}</div><div class="entry-meta"><span>${escapeHtml(dateText(entry.date))}</span><span>${entry.start !== '-' && entry.end !== '-' ? `${entry.start} – ${entry.end}` : t('manualTime')}</span><span class="entry-time">${duration(entry.minutes)} h</span></div>${entry.valid ? '' : `<div class="invalid-note">${escapeHtml(t('invalidEntry'))}</div>`}</div>
        <div class="entry-actions"><button class="mini-btn edit" type="button" data-action="edit">${escapeHtml(t('edit'))}</button><button class="mini-btn delete" type="button" data-action="delete">${escapeHtml(t('delete'))}</button></div>
      </article>`).join('');
    saveState(); renderStats();
  }

  function renderHistory() {
    const list = $('#historyList');
    $('#historyEmpty').hidden = state.history.length > 0;
    list.innerHTML = [...state.history].reverse().map(item => {
      const date = new Date(item.savedAtIso || item.savedAt || Date.now());
      return `<article class="entry-card" data-history-id="${item.id}"><div class="entry-main"><div class="entry-title">${escapeHtml(item.header)}</div><div class="entry-meta"><span>${escapeHtml(date.toLocaleString(localeFor()))}</span><span>${item.entries.length} ${escapeHtml(t('reportCount'))}</span><span class="entry-time">${duration(item.totalMinutes)} h</span></div></div><div class="entry-actions"><button class="mini-btn" type="button" data-action="load-history">${escapeHtml(t('load'))}</button><button class="mini-btn delete" type="button" data-action="delete-history">${escapeHtml(t('delete'))}</button></div></article>`;
    }).join('');
  }

  function renderStats() {
    const allEntries = state.history.flatMap(h => h.entries);
    const total = allEntries.reduce((sum, e) => sum + e.minutes, 0);
    $('#statsTotal').textContent = `${duration(total)} h`; $('#statsReports').textContent = state.history.length; $('#statsEntries').textContent = allEntries.length;
  }

  function editEntry(id) {
    const entry = state.entries.find(e => e.id === id); if (!entry) return;
    const client = prompt(t('editClient'), entry.client); if (client === null) return;
    const work = prompt(t('editWork'), duration(entry.minutes)); if (work === null) return;
    let minutes = 0;
    if (/^\d{1,2}:\d{2}$/.test(work.trim())) { const [h, m] = work.split(':').map(Number); if (m >= 0 && m <= 59) minutes = h * 60 + m; }
    else minutes = decimalHoursToMinutes(work);
    entry.client = client.trim(); entry.minutes = minutes; entry.valid = Boolean(entry.date && entry.client && entry.minutes > 0); renderEntries();
  }

  function saveToHistory() {
    if (!state.entries.length) return;
    const totalMinutes = state.entries.reduce((sum, e) => sum + e.minutes, 0);
    state.history.push({ id: uid(), savedAtIso: new Date().toISOString(), header: $('#headerInput').value.trim() || t('defaultHeader'), totalMinutes, entries: structuredClone(state.entries) });
    saveState(); renderHistory(); renderStats();
  }

  function applyTheme(theme) {
    state.theme = theme === 'light' ? 'light' : 'dark';
    document.documentElement.classList.toggle('light', state.theme === 'light');
    $('#themeBtn').textContent = state.theme === 'light' ? '☾' : '☀';
  }

  function sanitizeFileName(value) {
    return String(value).normalize('NFKD').replace(/[^\w\s.-]/g, '').trim().replace(/\s+/g, '_').slice(0, 80) || 'WojThom_Report';
  }

  function buildPdfHtml(lang) {
    const total = state.entries.reduce((sum, e) => sum + e.minutes, 0);
    const header = $('#headerInput').value.trim() || t('defaultHeader', lang);
    const generated = new Date().toLocaleString(localeFor(lang));
    const rows = state.entries.map((entry, index) => `<tr><td>${index + 1}</td><td>${escapeHtml(dateText(entry.date, lang))}</td><td>${escapeHtml(entry.client)}</td><td>${entry.start === '-' ? '—' : escapeHtml(entry.start)}</td><td>${entry.end === '-' ? '—' : escapeHtml(entry.end)}</td><td class="num">${duration(entry.minutes)} h</td></tr>`).join('');
    return `<!doctype html><html lang="${lang === 'no' ? 'nb' : lang}"><head><meta charset="utf-8"><title>${escapeHtml(sanitizeFileName(header))}</title><style>@page{size:A4;margin:14mm}*{box-sizing:border-box}body{margin:0;color:#142433;font:12px/1.45 Arial,Helvetica,sans-serif}.brand{display:flex;justify-content:space-between;align-items:flex-end;padding-bottom:14px;border-bottom:3px solid #087ea4}.brand h1{margin:0;font-size:26px;letter-spacing:-.04em}.brand .mark{color:#087ea4}.brand small{color:#667b89}.title{margin:24px 0 15px}.title h2{margin:0 0 4px;font-size:20px}.title p{margin:0;color:#667b89}table{width:100%;border-collapse:collapse;margin-top:15px}th{background:#eaf4f8;color:#173647;text-align:left;font-size:10px;text-transform:uppercase;letter-spacing:.05em}th,td{padding:9px 8px;border-bottom:1px solid #d7e3e9;vertical-align:top}th:first-child,td:first-child{width:28px;text-align:center}.num{text-align:right;font-weight:700;white-space:nowrap}.summary{margin-top:20px;margin-left:auto;width:min(320px,100%);border:1px solid #cbdde6;border-radius:10px;overflow:hidden}.summary div{display:flex;justify-content:space-between;gap:20px;padding:9px 12px;border-bottom:1px solid #e1eaef}.summary div:last-child{border-bottom:0;background:#eff8fb}.summary strong{color:#087ea4}footer{margin-top:28px;padding-top:10px;border-top:1px solid #d7e3e9;color:#718592;font-size:10px;display:flex;justify-content:space-between;gap:20px}.screen-note{display:none}@media screen{body{max-width:900px;margin:24px auto;padding:24px;background:white;box-shadow:0 10px 40px rgba(0,0,0,.12)}.screen-note{display:block;margin:0 0 18px;padding:10px 12px;background:#eaf4f8;border-radius:8px;color:#315368}}</style></head><body><div class="screen-note">${escapeHtml(t('printHint', lang))}</div><header class="brand"><div><h1>WojThom <span class="mark">6.0</span></h1><small>WORK TIME STUDIO</small></div><small>${escapeHtml(t('pdfGenerated', lang))}: ${escapeHtml(generated)}</small></header><section class="title"><h2>${escapeHtml(header)}</h2><p>${escapeHtml(t('pdfFooter', lang))}</p></section><table><thead><tr><th>#</th><th>${escapeHtml(t('pdfDate', lang))}</th><th>${escapeHtml(t('pdfClient', lang))}</th><th>${escapeHtml(t('pdfStart', lang))}</th><th>${escapeHtml(t('pdfEnd', lang))}</th><th class="num">${escapeHtml(t('pdfWork', lang))}</th></tr></thead><tbody>${rows}</tbody></table><section class="summary"><div><span>${escapeHtml(t('pdfEntries', lang))}</span><b>${state.entries.length}</b></div><div><span>${escapeHtml(t('pdfTotal', lang))}</span><strong>${duration(total)} h</strong></div></section><footer><span>WojThom 6.0 Web</span><span>${escapeHtml(t('pdfFooter', lang))}</span></footer><script>window.addEventListener('load',()=>setTimeout(()=>window.print(),250));<\/script></body></html>`;
  }

  function exportPdf(lang) {
    if (!state.entries.length) { alert(t('noPdfEntries')); return; }
    if (state.entries.some(e => !e.valid)) { alert(t('invalidPdfEntries')); return; }
    const printWindow = window.open('', '_blank');
    if (!printWindow) { alert(t('printHint', lang)); return; }
    printWindow.document.open(); printWindow.document.write(buildPdfHtml(lang)); printWindow.document.close();
  }

  $('#generateBtn').addEventListener('click', () => { state.logs = $('#logsInput').value; state.header = $('#headerInput').value.trim() || t('defaultHeader'); state.entries = parseLogs(state.logs); renderEntries(); });
  $('#clearBtn').addEventListener('click', () => { if ((state.entries.length || $('#logsInput').value.trim()) && !confirm(t('confirmClear'))) return; state.entries = []; state.logs = ''; $('#logsInput').value = ''; renderEntries(); });
  $('#saveHistoryBtn').addEventListener('click', saveToHistory);
  $('#exportPdfBtn').addEventListener('click', () => { if (!state.entries.length) { alert(t('noPdfEntries')); return; } $('#pdfLangDialog').showModal(); });
  $$('[data-pdf-lang]').forEach(btn => btn.addEventListener('click', () => { $('#pdfLangDialog').close(); exportPdf(btn.dataset.pdfLang); }));
  $('#clearHistoryBtn').addEventListener('click', () => { if (!state.history.length || confirm(t('confirmHistory'))) { state.history = []; saveState(); renderHistory(); renderStats(); } });

  $('#entriesList').addEventListener('click', (event) => {
    const card = event.target.closest('[data-id]'), action = event.target.closest('[data-action]')?.dataset.action;
    if (!card || !action) return;
    if (action === 'delete') { state.entries = state.entries.filter(e => e.id !== card.dataset.id); renderEntries(); }
    if (action === 'edit') editEntry(card.dataset.id);
  });

  $('#historyList').addEventListener('click', (event) => {
    const card = event.target.closest('[data-history-id]'), action = event.target.closest('[data-action]')?.dataset.action;
    if (!card || !action) return;
    const item = state.history.find(h => h.id === card.dataset.historyId); if (!item) return;
    if (action === 'load-history') { state.header = item.header; state.entries = structuredClone(item.entries).map(e => ({ ...e, id: uid() })); $('#headerInput').value = item.header; renderEntries(); activateView('workView'); }
    if (action === 'delete-history') { state.history = state.history.filter(h => h.id !== item.id); saveState(); renderHistory(); renderStats(); }
  });

  function activateView(viewId) { $$('.view').forEach(view => view.classList.toggle('active', view.id === viewId)); $$('.nav-item').forEach(btn => btn.classList.toggle('active', btn.dataset.view === viewId)); }
  $$('.nav-item').forEach(btn => btn.addEventListener('click', () => activateView(btn.dataset.view)));
  $('#themeBtn').addEventListener('click', () => { applyTheme(state.theme === 'dark' ? 'light' : 'dark'); saveState(); });
  $('#languageSelect').addEventListener('change', event => setLanguage(event.target.value));
  $('#headerInput').addEventListener('input', saveState);
  $('#logsInput').addEventListener('input', saveState);

  loadState(); renderEntries(); renderHistory(); renderStats();
})();

(() => {
  const STORAGE_KEY = 'wojthom6-web-state-v3';
  let premiumTarget = null;

  const REPORT_TEXT = {
    pl: {
      report: 'Raport czasu pracy', period: 'Okres', generated: 'Wygenerowano',
      no: 'Lp.', date: 'Data', client: 'Klient / firma', start: 'Od', end: 'Do', duration: 'Czas',
      total: 'Łączny czas', entries: 'Liczba wpisów', workDays: 'Dni pracy', average: 'Średnio / dzień',
      footer: 'Wygenerowano w WojThom 6.0 APEX Web', hint: 'Wybierz „Zapisz jako PDF” w oknie drukowania.'
    },
    en: {
      report: 'Work time report', period: 'Period', generated: 'Generated',
      no: 'No.', date: 'Date', client: 'Client / company', start: 'Start', end: 'End', duration: 'Duration',
      total: 'Total time', entries: 'Entries', workDays: 'Work days', average: 'Avg / day',
      footer: 'Generated with WojThom 6.0 APEX Web', hint: 'Choose “Save as PDF” in the print dialog.'
    },
    nb: {
      report: 'Arbeidstidsrapport', period: 'Periode', generated: 'Generert',
      no: 'Nr.', date: 'Dato', client: 'Kunde / firma', start: 'Fra', end: 'Til', duration: 'Tid',
      total: 'Total tid', entries: 'Antall oppføringer', workDays: 'Arbeidsdager', average: 'Snitt / dag',
      footer: 'Generert med WojThom 6.0 APEX Web', hint: 'Velg «Lagre som PDF» i utskriftsdialogen.'
    }
  };

  const localeFor = (lang) => lang === 'en' ? 'en-GB' : lang === 'nb' ? 'nb-NO' : 'pl-PL';
  const pad = (n) => String(n).padStart(2, '0');
  const duration = (minutes) => `${Math.floor(minutes / 60)}:${pad(minutes % 60)} h`;
  const esc = (value) => String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

  function readState() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
    } catch (_) {
      return {};
    }
  }

  function dateText(iso, lang) {
    if (!iso) return '-';
    const [y, m, d] = String(iso).split('-').map(Number);
    const date = new Date(y, m - 1, d);
    if (Number.isNaN(date.getTime())) return '-';
    return new Intl.DateTimeFormat(localeFor(lang), {
      day: '2-digit', month: '2-digit', year: 'numeric'
    }).format(date);
  }

  function periodText(entries, lang) {
    const dates = entries.map(e => e.date).filter(Boolean).sort();
    if (!dates.length) return '-';
    const first = dateText(dates[0], lang);
    const last = dateText(dates[dates.length - 1], lang);
    return first === last ? first : `${first} – ${last}`;
  }

  function generatedText(lang) {
    return new Intl.DateTimeFormat(localeFor(lang), {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    }).format(new Date());
  }

  function selectCurrentReport() {
    const state = readState();
    const entries = Array.isArray(state.entries) ? state.entries : [];
    const header = document.querySelector('#headerInput')?.value?.trim() || state.header || '';
    premiumTarget = { header, entries };
  }

  function selectHistoryReport(event) {
    const action = event.target.closest('[data-action="pdf-history"]');
    if (!action) return;
    const card = action.closest('[data-history-id]');
    if (!card) return;
    const state = readState();
    const history = Array.isArray(state.history) ? state.history : [];
    const item = history.find(h => h.id === card.dataset.historyId);
    if (item) premiumTarget = { header: item.header || '', entries: item.entries || [] };
  }

  function openPremiumPdf(lang) {
    const target = premiumTarget;
    if (!target || !Array.isArray(target.entries) || !target.entries.length) return;

    const text = REPORT_TEXT[lang] || REPORT_TEXT.pl;
    const entries = target.entries;
    const totalMinutes = entries.reduce((sum, e) => sum + (Number(e.minutes) || 0), 0);
    const workDays = new Set(entries.map(e => e.date).filter(Boolean)).size;
    const averageMinutes = workDays ? Math.floor(totalMinutes / workDays) : 0;
    const title = target.header?.trim() || (lang === 'en' ? 'Work Time List' : lang === 'nb' ? 'Arbeidstidsliste' : 'Lista Czasu Pracy');
    const period = periodText(entries, lang);
    const generated = generatedText(lang);

    const rows = entries.map((entry, index) => `
      <tr>
        <td class="number">${index + 1}</td>
        <td>${esc(dateText(entry.date, lang))}</td>
        <td class="client">${esc(entry.client || '-')}</td>
        <td>${esc(entry.start || '-')}</td>
        <td>${esc(entry.end || '-')}</td>
        <td class="time"><strong>${esc(duration(Number(entry.minutes) || 0))}</strong></td>
      </tr>`).join('');

    const win = window.open('', '_blank', 'noopener,noreferrer');
    if (!win) return;

    win.document.write(`<!doctype html>
<html lang="${lang}">
<head>
<meta charset="utf-8">
<title>${esc(title)} — WojThom APEX</title>
<style>
  @page { size: A4 portrait; margin: 12mm 13mm 14mm; }
  * { box-sizing: border-box; }
  html { background: #eef4f7; }
  body {
    margin: 0; color: #18272f; background: #fff;
    font-family: "Segoe UI", Arial, sans-serif;
    font-size: 11px; line-height: 1.35;
    -webkit-print-color-adjust: exact; print-color-adjust: exact;
  }
  .report { width: 100%; }
  .hero {
    background: linear-gradient(135deg, #082a3c 0%, #0d3b50 100%);
    color: #fff; border-radius: 14px; padding: 18px 20px;
    display: grid; grid-template-columns: 1fr 170px; gap: 18px; align-items: center;
    position: relative; overflow: hidden;
  }
  .hero:after {
    content: ""; position: absolute; right: -60px; top: -90px; width: 220px; height: 220px;
    border-radius: 50%; border: 28px solid rgba(83,204,244,.08);
  }
  .brand { color: #53ccf4; font-size: 9px; font-weight: 800; letter-spacing: .16em; }
  h1 { margin: 5px 0 3px; font-size: 24px; line-height: 1.1; letter-spacing: -.02em; }
  .report-type { color: #d8f3fc; font-size: 10px; }
  .meta { display: grid; gap: 9px; position: relative; z-index: 1; }
  .meta div { border-left: 2px solid #53ccf4; padding-left: 9px; }
  .meta span { display: block; color: #7edcf8; font-size: 7.5px; font-weight: 800; text-transform: uppercase; letter-spacing: .08em; }
  .meta strong { display: block; margin-top: 2px; color: #fff; font-size: 10px; font-weight: 650; }
  .subbar {
    margin: 8px 0 14px; padding: 7px 11px; border-radius: 7px;
    background: #f1f9fc; color: #637780; font-size: 8.5px;
  }
  table { width: 100%; border-collapse: separate; border-spacing: 0; table-layout: fixed; }
  thead { display: table-header-group; }
  th {
    padding: 8px 7px; background: #0879a6; color: #fff; text-align: left;
    font-size: 8px; letter-spacing: .02em;
  }
  th:first-child { border-radius: 6px 0 0 6px; }
  th:last-child { border-radius: 0 6px 6px 0; }
  td { padding: 8px 7px; border-bottom: 1px solid #d5e4ea; vertical-align: middle; font-size: 9px; }
  tbody tr:nth-child(odd) { background: #e8f5fa; }
  tr { break-inside: avoid; page-break-inside: avoid; }
  .number { width: 5%; color: #637780; }
  .date { width: 13%; }
  .client { width: 41%; overflow-wrap: anywhere; }
  .start, .end { width: 11%; }
  .time { width: 14%; color: #0879a6; white-space: nowrap; }
  .summary {
    margin-top: 14px; padding: 9px; border-radius: 11px; background: #103b50;
    display: grid; grid-template-columns: repeat(4, 1fr); gap: 7px;
    break-inside: avoid; page-break-inside: avoid;
  }
  .summary article { min-height: 58px; padding: 9px 10px; border-radius: 8px; background: #17485e; color: #fff; }
  .summary article:first-child { background: #0e6f96; }
  .summary span { display: block; color: #d7f2fb; font-size: 7.5px; font-weight: 800; text-transform: uppercase; }
  .summary strong { display: block; margin-top: 7px; font-size: 17px; line-height: 1; }
  .summary article:not(:first-child) strong { font-size: 15px; }
  .footer {
    margin-top: 16px; padding-top: 8px; border-top: 1px solid #d5e4ea;
    display: flex; justify-content: space-between; color: #637780; font-size: 8px;
  }
  .footer b { color: #0879a6; letter-spacing: .08em; }
  .hint { margin: 8px 0 0; color: #637780; font-size: 8px; text-align: center; }
  @media print {
    html, body { background: #fff; }
    .hint { display: none; }
  }
</style>
</head>
<body>
  <main class="report">
    <section class="hero">
      <div>
        <div class="brand">WOJTHOM APEX</div>
        <h1>${esc(title)}</h1>
        <div class="report-type">${esc(text.report)}</div>
      </div>
      <div class="meta">
        <div><span>${esc(text.period)}</span><strong>${esc(period)}</strong></div>
        <div><span>${esc(text.generated)}</span><strong>${esc(generated)}</strong></div>
      </div>
    </section>
    <div class="subbar">WojThom 6.0 APEX • ${esc(text.footer)}</div>

    <table>
      <colgroup>
        <col style="width:5%"><col style="width:13%"><col style="width:41%"><col style="width:11%"><col style="width:11%"><col style="width:19%">
      </colgroup>
      <thead><tr>
        <th>${esc(text.no)}</th><th>${esc(text.date)}</th><th>${esc(text.client)}</th>
        <th>${esc(text.start)}</th><th>${esc(text.end)}</th><th>${esc(text.duration)}</th>
      </tr></thead>
      <tbody>${rows}</tbody>
    </table>

    <section class="summary">
      <article><span>${esc(text.total)}</span><strong>${esc(duration(totalMinutes))}</strong></article>
      <article><span>${esc(text.entries)}</span><strong>${entries.length}</strong></article>
      <article><span>${esc(text.workDays)}</span><strong>${workDays}</strong></article>
      <article><span>${esc(text.average)}</span><strong>${esc(duration(averageMinutes))}</strong></article>
    </section>

    <footer class="footer"><span>${esc(text.footer)}</span><b>WOJTHOM APEX</b></footer>
    <p class="hint">${esc(text.hint)}</p>
  </main>
  <script>setTimeout(() => window.print(), 300)<\/script>
</body>
</html>`);

    win.document.close();
    document.querySelector('#pdfLangDialog')?.close();
    premiumTarget = null;
  }

  document.addEventListener('click', (event) => {
    if (event.target.closest('#exportPdfBtn')) {
      selectCurrentReport();
      return;
    }

    if (event.target.closest('[data-action="pdf-history"]')) {
      selectHistoryReport(event);
      return;
    }

    const languageButton = event.target.closest('[data-pdf-lang]');
    if (languageButton && premiumTarget?.entries?.length) {
      event.preventDefault();
      event.stopImmediatePropagation();
      openPremiumPdf(languageButton.dataset.pdfLang || 'pl');
    }
  }, true);
})();

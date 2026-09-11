(() => {
  const $ = (s) => document.querySelector(s);
  const $$ = (s) => [...document.querySelectorAll(s)];
  const STORAGE_KEY = 'wojthom6-web-state-v3';
  const OLD_KEYS = ['wojthom6-web-state-v2', 'wojthom6-web-state-v1'];
  const WEEK_TARGET = 37 * 60 + 30;

  const I18N = {
    pl: {
      studio:'Studio Czasu Pracy',totalTime:'Łączny czas',entries:'Wpisy',valid:'Poprawne',newList:'NOWA LISTA',dashboard:'Panel pracy',reportHeader:'Nagłówek raportu',logs:'Logi / wpisy',logsPlaceholder:'15.02.2026 Firma A 08:00 - 16:00\n16.02 Firma B 7.5h\n17.02 Firma C 08:30',generate:'Generuj listę',saveHistory:'Zapisz do historii',exportPdf:'Eksportuj PDF',clear:'Wyczyść',work:'Praca',searchEntries:'Szukaj w bieżącej liście',noEntries:'Brak wpisów',emptyHint:'Wklej godziny i wygeneruj listę.',archive:'ARCHIWUM',history:'Historia',historyDesc:'Zapisane listy czasu pracy.',searchHistory:'Szukaj w historii',clearHistory:'Usuń historię',noSavedLists:'Nie zapisano jeszcze żadnej listy.',analysis:'ANALIZA',stats:'Statystyki',weeklyTarget:'Norma tygodniowa 37,5 h',thisMonth:'Ten miesiąc',total:'Łącznie',reports:'Raporty',clients:'Klienci',months:'Miesiące',clearStats:'Wyczyść statystyki',app:'APLIKACJA',settings:'Ustawienia',appLanguage:'Język aplikacji',languageHelp:'Interfejs po polsku, angielsku lub norwesku.',theme:'Wygląd',themeHelp:'Jasny lub ciemny motyw.',dark:'Ciemny',light:'Jasny',data:'Dane lokalne',dataHelp:'Bieżąca lista, historia i statystyki są zapisywane w tej przeglądarce.',clearAll:'Wyczyść dane aplikacji',about:'O aplikacji',aboutText:'WojThom 6.0 APEX — przebudowana kontynuacja WojThom 5.3.',pdfLanguageTitle:'Język dokumentu PDF',pdfLanguageText:'Wybierz język raportu niezależnie od języka aplikacji.',cancel:'Anuluj',close:'Zamknij',invalidDate:'Nieprawidłowa data',noClient:'Brak klienta',manualTime:'czas podany ręcznie',invalidEntry:'Wpis wymaga poprawy.',edit:'Edytuj',delete:'Usuń',load:'Wczytaj',saved:'Zapisano',confirmClear:'Wyczyścić obecną listę?',confirmHistory:'Trwale usunąć całą historię?',confirmStats:'Trwale wyczyścić statystyki?',confirmAll:'Usunąć wszystkie lokalne dane WojThom?',remaining:'Do normy',over:'Ponad normę',targetReached:'Norma osiągnięta',monthEntries:'wpisów',pdfDate:'Data',pdfClient:'Klient / firma',pdfStart:'Od',pdfEnd:'Do',pdfWork:'Czas',pdfTotal:'Łączny czas',pdfEntries:'Liczba wpisów',pdfFooter:'Wygenerowano w WojThom 6.0 APEX Web',printHint:'Wybierz „Zapisz jako PDF” w oknie drukowania.',defaultHeader:'Lista Czasu Pracy',editClient:'Klient / firma:',editWork:'Czas pracy (np. 08:00 albo 7.5):',noPdfEntries:'Brak wpisów do eksportu.',invalidPdfEntries:'Popraw błędne wpisy przed eksportem PDF.'
    },
    en: {
      studio:'Work Time Studio',totalTime:'Total time',entries:'Entries',valid:'Valid',newList:'NEW LIST',dashboard:'Work dashboard',reportHeader:'Report header',logs:'Logs / entries',logsPlaceholder:'15.02.2026 Company A 08:00 - 16:00\n16.02 Company B 7.5h\n17.02 Company C 08:30',generate:'Generate list',saveHistory:'Save to history',exportPdf:'Export PDF',clear:'Clear',work:'Work',searchEntries:'Search current list',noEntries:'No entries',emptyHint:'Paste work hours and generate the list.',archive:'ARCHIVE',history:'History',historyDesc:'Saved work-time lists.',searchHistory:'Search history',clearHistory:'Clear history',noSavedLists:'No saved lists yet.',analysis:'ANALYSIS',stats:'Statistics',weeklyTarget:'Weekly target 37.5 h',thisMonth:'This month',total:'All time',reports:'Reports',clients:'Clients',months:'Months',clearStats:'Clear statistics',app:'APPLICATION',settings:'Settings',appLanguage:'Application language',languageHelp:'Interface available in Polish, English and Norwegian.',theme:'Appearance',themeHelp:'Choose light or dark mode.',dark:'Dark',light:'Light',data:'Local data',dataHelp:'Current list, history and statistics are stored in this browser.',clearAll:'Clear application data',about:'About',aboutText:'WojThom 6.0 APEX — rebuilt successor to WojThom 5.3.',pdfLanguageTitle:'PDF document language',pdfLanguageText:'Choose the report language independently from the application language.',cancel:'Cancel',close:'Close',invalidDate:'Invalid date',noClient:'No client',manualTime:'time entered manually',invalidEntry:'Entry needs correction.',edit:'Edit',delete:'Delete',load:'Load',saved:'Saved',confirmClear:'Clear the current list?',confirmHistory:'Permanently clear all history?',confirmStats:'Permanently clear statistics?',confirmAll:'Delete all local WojThom data?',remaining:'Remaining',over:'Above target',targetReached:'Target reached',monthEntries:'entries',pdfDate:'Date',pdfClient:'Client / company',pdfStart:'Start',pdfEnd:'End',pdfWork:'Duration',pdfTotal:'Total time',pdfEntries:'Entries',pdfFooter:'Generated with WojThom 6.0 APEX Web',printHint:'Choose “Save as PDF” in the print dialog.',defaultHeader:'Work Time List',editClient:'Client / company:',editWork:'Work time (e.g. 08:00 or 7.5):',noPdfEntries:'There are no entries to export.',invalidPdfEntries:'Fix invalid entries before exporting PDF.'
    },
    nb: {
      studio:'Arbeidstidsstudio',totalTime:'Total tid',entries:'Oppføringer',valid:'Gyldige',newList:'NY LISTE',dashboard:'Arbeidspanel',reportHeader:'Rapportoverskrift',logs:'Logger / oppføringer',logsPlaceholder:'15.02.2026 Firma A 08:00 - 16:00\n16.02 Firma B 7.5t\n17.02 Firma C 08:30',generate:'Generer liste',saveHistory:'Lagre i historikk',exportPdf:'Eksporter PDF',clear:'Tøm',work:'Arbeid',searchEntries:'Søk i gjeldende liste',noEntries:'Ingen oppføringer',emptyHint:'Lim inn arbeidstider og generer listen.',archive:'ARKIV',history:'Historikk',historyDesc:'Lagrede arbeidstidslister.',searchHistory:'Søk i historikk',clearHistory:'Tøm historikk',noSavedLists:'Ingen lagrede lister ennå.',analysis:'ANALYSE',stats:'Statistikk',weeklyTarget:'Ukesnorm 37,5 t',thisMonth:'Denne måneden',total:'Totalt',reports:'Rapporter',clients:'Kunder',months:'Måneder',clearStats:'Tøm statistikk',app:'APPLIKASJON',settings:'Innstillinger',appLanguage:'Appspråk',languageHelp:'Grensesnitt på polsk, engelsk eller norsk.',theme:'Utseende',themeHelp:'Velg lyst eller mørkt tema.',dark:'Mørkt',light:'Lyst',data:'Lokale data',dataHelp:'Gjeldende liste, historikk og statistikk lagres i denne nettleseren.',clearAll:'Tøm appdata',about:'Om appen',aboutText:'WojThom 6.0 APEX — en nybygd fortsettelse av WojThom 5.3.',pdfLanguageTitle:'Språk for PDF-dokument',pdfLanguageText:'Velg rapportspråk uavhengig av appspråket.',cancel:'Avbryt',close:'Lukk',invalidDate:'Ugyldig dato',noClient:'Ingen kunde',manualTime:'tid angitt manuelt',invalidEntry:'Oppføringen må korrigeres.',edit:'Rediger',delete:'Slett',load:'Last inn',saved:'Lagret',confirmClear:'Tømme gjeldende liste?',confirmHistory:'Slette hele historikken permanent?',confirmStats:'Tømme statistikken permanent?',confirmAll:'Slette alle lokale WojThom-data?',remaining:'Gjenstår',over:'Over normen',targetReached:'Normen er nådd',monthEntries:'oppføringer',pdfDate:'Dato',pdfClient:'Kunde / firma',pdfStart:'Fra',pdfEnd:'Til',pdfWork:'Tid',pdfTotal:'Total tid',pdfEntries:'Antall oppføringer',pdfFooter:'Generert med WojThom 6.0 APEX Web',printHint:'Velg «Lagre som PDF» i utskriftsdialogen.',defaultHeader:'Arbeidstidsliste',editClient:'Kunde / firma:',editWork:'Arbeidstid (f.eks. 08:00 eller 7.5):',noPdfEntries:'Ingen oppføringer å eksportere.',invalidPdfEntries:'Rett ugyldige oppføringer før PDF-eksport.'
    }
  };

  const state = { header:I18N.pl.defaultHeader, logs:'', entries:[], history:[], statsEntries:[], theme:'dark', language:'pl' };
  let pdfTarget = null;

  const t = (key, lang = state.language) => I18N[lang]?.[key] ?? I18N.pl[key] ?? key;
  const localeFor = (lang = state.language) => lang === 'en' ? 'en-GB' : lang === 'nb' ? 'nb-NO' : 'pl-PL';
  const uid = () => crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  const pad = (n) => String(n).padStart(2,'0');
  const duration = (m) => `${Math.floor(m/60)}:${pad(m%60)}`;
  const escapeHtml = (v) => String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#039;');

  function loadState(){
    let saved = null;
    try { saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null'); } catch(_) {}
    if (!saved) {
      for (const key of OLD_KEYS) {
        try { saved = JSON.parse(localStorage.getItem(key) || 'null'); } catch(_) {}
        if (saved) break;
      }
    }
    if (saved) Object.assign(state, saved);
    if (state.language === 'no') state.language = 'nb';
    if (!I18N[state.language]) state.language = 'pl';
    if (!Array.isArray(state.statsEntries)) state.statsEntries = mergeStats([], state.history.flatMap(h => h.entries || []));
    $('#headerInput').value = state.header || t('defaultHeader');
    $('#logsInput').value = state.logs || '';
    $('#languageSelect').value = state.language;
    applyTheme(state.theme || 'dark');
    applyLanguage();
  }

  function saveState(){
    state.header = $('#headerInput').value.trim() || t('defaultHeader');
    state.logs = $('#logsInput').value;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }

  function setLanguage(lang){
    if (lang === 'no') lang = 'nb';
    if (!I18N[lang]) return;
    const oldDefault = t('defaultHeader');
    const wasDefault = !$('#headerInput').value.trim() || $('#headerInput').value.trim() === oldDefault;
    state.language = lang;
    if (wasDefault) $('#headerInput').value = t('defaultHeader');
    applyLanguage(); saveState(); renderAll();
  }

  function applyLanguage(){
    document.documentElement.lang = state.language;
    document.title = `WojThom 6.0 APEX Web — ${state.language.toUpperCase()}`;
    $$('[data-i18n]').forEach(el => el.textContent = t(el.dataset.i18n));
    $$('[data-i18n-placeholder]').forEach(el => el.placeholder = t(el.dataset.i18nPlaceholder));
    $('#languageSelect').value = state.language;
  }

  function applyTheme(theme){
    state.theme = theme === 'light' ? 'light' : 'dark';
    document.documentElement.classList.toggle('light', state.theme === 'light');
    $('#themeBtn').textContent = state.theme === 'light' ? '☾' : '☀';
    $('#darkThemeBtn')?.classList.toggle('primary', state.theme === 'dark');
    $('#lightThemeBtn')?.classList.toggle('primary', state.theme === 'light');
  }

  function parseDate(dayRaw, monthRaw, yearRaw){
    const day=Number(dayRaw), month=Number(monthRaw); let year=yearRaw?Number(yearRaw):new Date().getFullYear();
    if(yearRaw && String(yearRaw).length===2) year+=2000;
    const d=new Date(year,month-1,day);
    if(d.getFullYear()!==year||d.getMonth()!==month-1||d.getDate()!==day) return null;
    return `${year}-${pad(month)}-${pad(day)}`;
  }

  function decimalHoursToMinutes(value){
    const n=Number(String(value).replace(',','.'));
    if(!Number.isFinite(n)||n<0||n>24) return 0;
    return Math.round(n*60);
  }

  function parseLine(source){
    let rest=source.trim(),date=null,start='-',end='-',minutes=0;
    const dm=rest.match(/^(\d{1,2})[.\-/\s]+(\d{1,2})(?:[.\-/\s]+(\d{2,4}))?\b/);
    if(dm){ date=parseDate(dm[1],dm[2],dm[3]); rest=rest.slice(dm[0].length).trim(); }
    const rm=rest.match(/\b(\d{1,2})[:.](\d{2})\s*-\s*(\d{1,2})[:.](\d{2})\b/);
    if(rm){
      const sh=+rm[1],sm=+rm[2],eh=+rm[3],em=+rm[4];
      if(sh<=23&&eh<=23&&sm<=59&&em<=59){ start=`${pad(sh)}:${pad(sm)}`; end=`${pad(eh)}:${pad(em)}`; const s=sh*60+sm; let e=eh*60+em; if(e<s)e+=1440; minutes=e-s; }
      rest=rest.replace(rm[0],'').trim();
    } else {
      const hm=rest.match(/\b(\d+(?:[.,]\d+)?)\s*[hHtT]\b/), cm=rest.match(/\b(\d{1,2})[:.]([0-5]\d)\b/), dec=rest.match(/(?:^|\s)(\d+(?:[.,]\d+)?)\s*$/);
      if(hm){ minutes=decimalHoursToMinutes(hm[1]); rest=rest.replace(hm[0],'').trim(); }
      else if(cm){ const h=+cm[1],m=+cm[2]; if(h>=0&&m<=59)minutes=h*60+m; rest=rest.replace(cm[0],'').trim(); }
      else if(dec){ minutes=decimalHoursToMinutes(dec[1]); rest=rest.slice(0,dec.index).trim(); }
    }
    const client=rest.replace(/^[\s|\-]+|[\s|\-]+$/g,'');
    return {id:uid(),date,client,start,end,minutes,source,valid:Boolean(date&&client&&minutes>0)};
  }

  const parseLogs=(raw)=>raw.split(/\r?\n/).map(v=>v.trim()).filter(Boolean).map(parseLine);
  function dateText(iso){ if(!iso)return t('invalidDate'); const [y,m,d]=iso.split('-').map(Number); return new Intl.DateTimeFormat(localeFor(),{day:'2-digit',month:'2-digit',year:'numeric'}).format(new Date(y,m-1,d)); }

  function mergeStats(existing,incoming){
    const map=new Map();
    [...existing,...incoming].filter(e=>e?.valid!==false&&e?.date&&e?.client&&e?.minutes>0).forEach(e=>{
      const key=[e.date,e.client.trim().toLowerCase(),e.start,e.end,e.minutes,(e.source||'').trim()].join('|');
      if(!map.has(key)) map.set(key,{...e});
    });
    return [...map.values()];
  }

  function renderEntries(){
    const q=$('#entrySearch').value.trim().toLowerCase();
    const visible=state.entries.filter(e=>!q||e.client.toLowerCase().includes(q)||(e.source||'').toLowerCase().includes(q)||(e.date||'').includes(q));
    const total=state.entries.reduce((s,e)=>s+e.minutes,0), valid=state.entries.filter(e=>e.valid).length;
    $('#totalTime').textContent=`${duration(total)} h`; $('#entryCount').textContent=state.entries.length; $('#validCount').textContent=valid; $('#validationBadge').textContent=`${valid}/${state.entries.length}`;
    $('#emptyState').hidden=state.entries.length>0;
    $('#entriesList').innerHTML=visible.map(e=>`<article class="entry-card ${e.valid?'':'invalid'}" data-id="${e.id}"><div><div class="entry-title">${escapeHtml(e.client||t('noClient'))}</div><div class="entry-meta"><span>${escapeHtml(dateText(e.date))}</span><span>${e.start!=='-'&&e.end!=='-'?`${e.start} – ${e.end}`:escapeHtml(t('manualTime'))}</span><span class="entry-time">${duration(e.minutes)} h</span></div>${e.valid?'':`<small style="color:var(--danger)">${escapeHtml(t('invalidEntry'))}</small>`}</div><div class="entry-actions"><button class="mini-btn" data-action="edit">${escapeHtml(t('edit'))}</button><button class="mini-btn delete" data-action="delete">${escapeHtml(t('delete'))}</button></div></article>`).join('');
    saveState();
  }

  function renderHistory(){
    const q=$('#historySearch').value.trim().toLowerCase();
    const items=[...state.history].reverse().filter(h=>!q||(h.header||'').toLowerCase().includes(q)||(h.entries||[]).some(e=>(e.client||'').toLowerCase().includes(q)));
    $('#historyCount').textContent=state.history.length; $('#historyEmpty').hidden=items.length>0;
    $('#historyList').innerHTML=items.map(h=>`<article class="entry-card" data-history-id="${h.id}"><div><div class="entry-title">${escapeHtml(h.header)}</div><div class="entry-meta"><span>${escapeHtml(new Date(h.savedAtIso||h.savedAt||Date.now()).toLocaleString(localeFor()))}</span><span>${h.entries.length} ${escapeHtml(t('entries').toLowerCase())}</span><span class="entry-time">${duration(h.totalMinutes)} h</span></div></div><div class="entry-actions"><button class="mini-btn" data-action="load-history">${escapeHtml(t('load'))}</button><button class="mini-btn" data-action="pdf-history">PDF</button><button class="mini-btn delete" data-action="delete-history">${escapeHtml(t('delete'))}</button></div></article>`).join('');
  }

  function mondayStart(date){ const d=new Date(date); d.setHours(0,0,0,0); const day=(d.getDay()+6)%7; d.setDate(d.getDate()-day); return d; }
  function statsData(){
    const entries=state.statsEntries.filter(e=>e.date&&e.minutes>0);
    const now=new Date(), start=mondayStart(now), end=new Date(start); end.setDate(end.getDate()+7);
    const thisWeek=entries.filter(e=>{const d=new Date(`${e.date}T12:00:00`);return d>=start&&d<end;}).reduce((s,e)=>s+e.minutes,0);
    const ym=`${now.getFullYear()}-${pad(now.getMonth()+1)}`;
    const thisMonth=entries.filter(e=>e.date.startsWith(ym)).reduce((s,e)=>s+e.minutes,0);
    const total=entries.reduce((s,e)=>s+e.minutes,0);
    const clients=new Set(entries.map(e=>e.client.trim().toLowerCase()).filter(Boolean)).size;
    const months=new Map(); entries.forEach(e=>{const key=e.date.slice(0,7);const v=months.get(key)||{minutes:0,count:0};v.minutes+=e.minutes;v.count++;months.set(key,v);});
    return {entries,thisWeek,thisMonth,total,clients,months:[...months.entries()].sort((a,b)=>b[0].localeCompare(a[0]))};
  }

  function renderStats(){
    const s=statsData();
    $('#statsEntries').textContent=s.entries.length; $('#statsWeek').textContent=`${duration(s.thisWeek)} h`; $('#statsMonth').textContent=`${duration(s.thisMonth)} h`; $('#statsTotal').textContent=`${duration(s.total)} h`; $('#statsReports').textContent=state.history.length; $('#statsClients').textContent=s.clients; $('#weeklyProgress').value=Math.min(s.thisWeek,WEEK_TARGET);
    const diff=s.thisWeek-WEEK_TARGET; $('#weeklyStatus').textContent=diff===0?t('targetReached'):diff<0?`${t('remaining')}: ${duration(-diff)} h`:`${t('over')}: ${duration(diff)} h`;
    $('#monthStats').innerHTML=s.months.map(([key,v])=>{const [y,m]=key.split('-').map(Number);const name=new Intl.DateTimeFormat(localeFor(),{month:'long',year:'numeric'}).format(new Date(y,m-1,1));return `<div class="month-row"><div><b>${escapeHtml(name)}</b><small>${v.count} ${escapeHtml(t('monthEntries'))}</small></div><strong>${duration(v.minutes)} h</strong></div>`;}).join('');
  }

  function renderAll(){ renderEntries(); renderHistory(); renderStats(); }

  function saveToHistory(){
    if(!state.entries.length)return;
    const snapshot={id:uid(),savedAtIso:new Date().toISOString(),header:$('#headerInput').value.trim()||t('defaultHeader'),totalMinutes:state.entries.reduce((s,e)=>s+e.minutes,0),entries:structuredClone(state.entries)};
    state.history.push(snapshot); state.statsEntries=mergeStats(state.statsEntries,state.entries); saveState(); renderHistory(); renderStats();
  }

  function editEntry(id){
    const e=state.entries.find(x=>x.id===id); if(!e)return;
    const client=prompt(t('editClient'),e.client); if(client===null)return;
    const work=prompt(t('editWork'),duration(e.minutes)); if(work===null)return;
    let minutes=0; const c=work.trim().match(/^(\d{1,3}):([0-5]\d)$/); if(c)minutes=+c[1]*60 + +c[2]; else minutes=decimalHoursToMinutes(work);
    e.client=client.trim(); e.minutes=minutes; e.start='-'; e.end='-'; e.valid=Boolean(e.date&&e.client&&e.minutes>0); renderEntries();
  }

  function activateView(id){ $$('.view').forEach(v=>v.classList.toggle('active',v.id===id)); $$('.nav-item').forEach(b=>b.classList.toggle('active',b.dataset.view===id)); }

  function requestPdf(header,entries){
    if(!entries.length){alert(t('noPdfEntries'));return;} if(entries.some(e=>!e.valid)){alert(t('invalidPdfEntries'));return;}
    pdfTarget={header,entries}; $('#pdfLangDialog').showModal();
  }

  function openPdf(lang){
    if(!pdfTarget)return; const d=I18N[lang]||I18N.pl; const total=pdfTarget.entries.reduce((s,e)=>s+e.minutes,0);
    const rows=pdfTarget.entries.map(e=>`<tr><td>${escapeHtml(dateTextPdf(e.date,lang))}</td><td>${escapeHtml(e.client)}</td><td>${escapeHtml(e.start)}</td><td>${escapeHtml(e.end)}</td><td><b>${duration(e.minutes)} h</b></td></tr>`).join('');
    const title=(pdfTarget.header||'').trim()||d.defaultHeader; const w=window.open('','_blank','noopener,noreferrer'); if(!w)return;
    w.document.write(`<!doctype html><html><head><meta charset="utf-8"><title>${escapeHtml(title)}</title><style>@page{size:A4;margin:16mm}*{box-sizing:border-box}body{font-family:Arial,sans-serif;color:#17232b;margin:0}.head{background:#09283a;color:white;padding:22px;border-radius:10px}.head small{color:#9edfff}.brand{float:right;color:#63d2ff;font-weight:bold}table{width:100%;border-collapse:collapse;margin-top:18px;font-size:12px}th{background:#0c5c86;color:white;text-align:left;padding:9px}td{padding:9px;border-bottom:1px solid #d5e2e8}tr:nth-child(even){background:#eaf6fb}.sum{margin-top:18px;background:#eaf6fb;border-radius:8px;padding:14px;display:flex;gap:70px}.sum b{display:block;color:#0c5c86;font-size:20px}.foot{margin-top:25px;padding-top:8px;border-top:1px solid #d5e2e8;color:#61727c;font-size:10px}.hint{font-size:10px;color:#61727c}@media print{.hint{display:none}}</style></head><body><div class="head"><span class="brand">WOJTHOM APEX</span><h1>${escapeHtml(title)}</h1><small>${escapeHtml(d.pdfFooter)}</small></div><table><thead><tr><th>${escapeHtml(d.pdfDate)}</th><th>${escapeHtml(d.pdfClient)}</th><th>${escapeHtml(d.pdfStart)}</th><th>${escapeHtml(d.pdfEnd)}</th><th>${escapeHtml(d.pdfWork)}</th></tr></thead><tbody>${rows}</tbody></table><div class="sum"><div>${escapeHtml(d.pdfTotal)}<b>${duration(total)} h</b></div><div>${escapeHtml(d.pdfEntries)}<b>${pdfTarget.entries.length}</b></div></div><div class="foot">${escapeHtml(d.pdfFooter)}</div><p class="hint">${escapeHtml(d.printHint)}</p><script>setTimeout(()=>window.print(),250)<\/script></body></html>`); w.document.close();
    $('#pdfLangDialog').close(); pdfTarget=null;
  }

  function dateTextPdf(iso,lang){ if(!iso)return '-'; const [y,m,d]=iso.split('-').map(Number); return new Intl.DateTimeFormat(localeFor(lang),{day:'2-digit',month:'2-digit',year:'numeric'}).format(new Date(y,m-1,d)); }

  $('#generateBtn').addEventListener('click',()=>{state.entries=parseLogs($('#logsInput').value);renderEntries();});
  $('#saveHistoryBtn').addEventListener('click',saveToHistory);
  $('#exportPdfBtn').addEventListener('click',()=>requestPdf($('#headerInput').value.trim()||t('defaultHeader'),state.entries));
  $('#clearBtn').addEventListener('click',()=>{if((state.entries.length||$('#logsInput').value.trim())&&!confirm(t('confirmClear')))return;state.entries=[];state.logs='';$('#logsInput').value='';renderEntries();});
  $('#entrySearch').addEventListener('input',renderEntries); $('#historySearch').addEventListener('input',renderHistory);
  $('#entriesList').addEventListener('click',(ev)=>{const card=ev.target.closest('[data-id]'),action=ev.target.closest('[data-action]')?.dataset.action;if(!card||!action)return;if(action==='delete'){state.entries=state.entries.filter(e=>e.id!==card.dataset.id);renderEntries();}if(action==='edit')editEntry(card.dataset.id);});
  $('#historyList').addEventListener('click',(ev)=>{const card=ev.target.closest('[data-history-id]'),action=ev.target.closest('[data-action]')?.dataset.action;if(!card||!action)return;const h=state.history.find(x=>x.id===card.dataset.historyId);if(!h)return;if(action==='load-history'){state.entries=structuredClone(h.entries).map(e=>({...e,id:uid()}));state.header=h.header;$('#headerInput').value=h.header;renderEntries();activateView('workView');}if(action==='delete-history'){state.history=state.history.filter(x=>x.id!==h.id);saveState();renderHistory();renderStats();}if(action==='pdf-history')requestPdf(h.header,h.entries);});
  $('#clearHistoryBtn').addEventListener('click',()=>{if(state.history.length&&confirm(t('confirmHistory'))){state.history=[];saveState();renderHistory();renderStats();}});
  $('#clearStatsBtn').addEventListener('click',()=>{if(state.statsEntries.length&&confirm(t('confirmStats'))){state.statsEntries=[];saveState();renderStats();}});
  $('#clearAllBtn').addEventListener('click',()=>{if(!confirm(t('confirmAll')))return;const lang=state.language,theme=state.theme;Object.assign(state,{header:t('defaultHeader'),logs:'',entries:[],history:[],statsEntries:[],language:lang,theme});$('#headerInput').value=state.header;$('#logsInput').value='';saveState();renderAll();activateView('workView');});
  $('#languageSelect').addEventListener('change',(e)=>setLanguage(e.target.value));
  $('#themeBtn').addEventListener('click',()=>{applyTheme(state.theme==='dark'?'light':'dark');saveState();}); $('#darkThemeBtn').addEventListener('click',()=>{applyTheme('dark');saveState();}); $('#lightThemeBtn').addEventListener('click',()=>{applyTheme('light');saveState();});
  $$('.nav-item').forEach(b=>b.addEventListener('click',()=>activateView(b.dataset.view)));
  $('#systemBtn').addEventListener('click',()=>$('#systemDialog').showModal()); $$('#systemDialog [data-open-view]').forEach(b=>b.addEventListener('click',()=>{$('#systemDialog').close();activateView(b.dataset.openView);}));
  $$('#pdfLangDialog [data-pdf-lang]').forEach(b=>b.addEventListener('click',()=>openPdf(b.dataset.pdfLang)));
  $('#headerInput').addEventListener('input',saveState); $('#logsInput').addEventListener('input',saveState);

  loadState(); renderAll();
})();

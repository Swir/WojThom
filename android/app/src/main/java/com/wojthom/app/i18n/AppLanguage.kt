package com.wojthom.app.i18n

enum class AppLanguage(val code: String, val nativeLabel: String) {
    POLISH("pl", "Polski"),
    ENGLISH("en", "English"),
    NORWEGIAN("no", "Norsk");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: POLISH
    }
}

data class UiStrings(
    val work: String,
    val history: String,
    val statistics: String,
    val settings: String,
    val workTimeStudio: String,
    val newList: String,
    val defaultHeader: String,
    val header: String,
    val pasteHours: String,
    val hoursExample: String,
    val generateList: String,
    val clear: String,
    val exportPdf: String,
    val noEntries: String,
    val entries: String,
    val totalTime: String,
    val missingClient: String,
    val invalidDate: String,
    val workTime: String,
    val needsCorrection: String,
    val delete: String,
    val historyDescription: String,
    val statisticsDescription: String,
    val settingsDescription: String,
    val modulePlaceholder: String,
    val applicationLanguage: String,
    val applicationLanguageDescription: String,
    val pdfLanguageTitle: String,
    val pdfLanguageDescription: String,
    val cancel: String,
    val pdfSaved: String,
    val pdfSaveError: String,
    val pdfFileName: String
)

fun strings(language: AppLanguage): UiStrings = when (language) {
    AppLanguage.POLISH -> UiStrings(
        work = "Praca",
        history = "Historia",
        statistics = "Statystyki",
        settings = "Ustawienia",
        workTimeStudio = "Studio Czasu Pracy",
        newList = "Nowa lista",
        defaultHeader = "Lista Czasu Pracy",
        header = "Nagłówek",
        pasteHours = "Wklej godziny pracy",
        hoursExample = "Np. 15.02.2026 Firma A 08:00 - 16:00 • 16.02 Firma B 7.5h",
        generateList = "Generuj listę",
        clear = "Wyczyść",
        exportPdf = "Eksportuj PDF",
        noEntries = "Brak wpisów. Wklej logi i wybierz „Generuj listę”.",
        entries = "Wpisy",
        totalTime = "Łączny czas",
        missingClient = "Brak klienta",
        invalidDate = "Nieprawidłowa data",
        workTime = "Czas pracy",
        needsCorrection = "Wpis wymaga poprawy",
        delete = "Usuń",
        historyDescription = "Tu trafi archiwum zapisanych list.",
        statisticsDescription = "Tu trafi analiza tygodni, miesięcy i normy 37,5 h.",
        settingsDescription = "Język, motyw, eksport i ustawienia aplikacji.",
        modulePlaceholder = "Moduł przygotowany do dalszego przenoszenia funkcji WojThom 5.x.",
        applicationLanguage = "Język aplikacji",
        applicationLanguageDescription = "Wybierz język całego interfejsu. Ustawienie zostanie zapamiętane.",
        pdfLanguageTitle = "W jakim języku zapisać PDF?",
        pdfLanguageDescription = "Język PDF jest niezależny od języka aplikacji.",
        cancel = "Anuluj",
        pdfSaved = "PDF został zapisany.",
        pdfSaveError = "Nie udało się zapisać PDF.",
        pdfFileName = "WojThom_czas_pracy.pdf"
    )

    AppLanguage.ENGLISH -> UiStrings(
        work = "Work",
        history = "History",
        statistics = "Statistics",
        settings = "Settings",
        workTimeStudio = "Work Time Studio",
        newList = "New list",
        defaultHeader = "Work Time List",
        header = "Header",
        pasteHours = "Paste work hours",
        hoursExample = "E.g. 15.02.2026 Company A 08:00 - 16:00 • 16.02 Company B 7.5h",
        generateList = "Generate list",
        clear = "Clear",
        exportPdf = "Export PDF",
        noEntries = "No entries. Paste your logs and choose “Generate list”.",
        entries = "Entries",
        totalTime = "Total time",
        missingClient = "No client",
        invalidDate = "Invalid date",
        workTime = "Work time",
        needsCorrection = "Entry needs correction",
        delete = "Delete",
        historyDescription = "Saved work lists will appear here.",
        statisticsDescription = "Weekly, monthly and 37.5-hour norm analysis will appear here.",
        settingsDescription = "Language, theme, export and application settings.",
        modulePlaceholder = "Module prepared for further migration of WojThom 5.x features.",
        applicationLanguage = "Application language",
        applicationLanguageDescription = "Choose the language of the entire interface. The setting will be remembered.",
        pdfLanguageTitle = "Which language should the PDF use?",
        pdfLanguageDescription = "The PDF language is independent of the application language.",
        cancel = "Cancel",
        pdfSaved = "PDF has been saved.",
        pdfSaveError = "Could not save the PDF.",
        pdfFileName = "WojThom_work_time.pdf"
    )

    AppLanguage.NORWEGIAN -> UiStrings(
        work = "Arbeid",
        history = "Historikk",
        statistics = "Statistikk",
        settings = "Innstillinger",
        workTimeStudio = "Arbeidstidsstudio",
        newList = "Ny liste",
        defaultHeader = "Arbeidstidsliste",
        header = "Overskrift",
        pasteHours = "Lim inn arbeidstimer",
        hoursExample = "F.eks. 15.02.2026 Firma A 08:00 - 16:00 • 16.02 Firma B 7.5t",
        generateList = "Generer liste",
        clear = "Tøm",
        exportPdf = "Eksporter PDF",
        noEntries = "Ingen oppføringer. Lim inn loggene og velg «Generer liste».",
        entries = "Oppføringer",
        totalTime = "Total tid",
        missingClient = "Ingen kunde",
        invalidDate = "Ugyldig dato",
        workTime = "Arbeidstid",
        needsCorrection = "Oppføringen må korrigeres",
        delete = "Slett",
        historyDescription = "Lagrede arbeidslister vises her.",
        statisticsDescription = "Analyse av uker, måneder og normen på 37,5 t vises her.",
        settingsDescription = "Språk, tema, eksport og appinnstillinger.",
        modulePlaceholder = "Modulen er klargjort for videre overføring av funksjoner fra WojThom 5.x.",
        applicationLanguage = "Appspråk",
        applicationLanguageDescription = "Velg språk for hele grensesnittet. Innstillingen blir lagret.",
        pdfLanguageTitle = "Hvilket språk skal PDF-en lagres på?",
        pdfLanguageDescription = "Språket i PDF-en er uavhengig av appspråket.",
        cancel = "Avbryt",
        pdfSaved = "PDF-en er lagret.",
        pdfSaveError = "Kunne ikke lagre PDF-en.",
        pdfFileName = "WojThom_arbeidstid.pdf"
    )
}

data class PdfStrings(
    val title: String,
    val date: String,
    val client: String,
    val start: String,
    val end: String,
    val duration: String,
    val total: String,
    val entries: String,
    val generatedBy: String
)

fun pdfStrings(language: AppLanguage): PdfStrings = when (language) {
    AppLanguage.POLISH -> PdfStrings(
        title = "Lista Czasu Pracy",
        date = "Data",
        client = "Klient / firma",
        start = "Od",
        end = "Do",
        duration = "Czas",
        total = "Łączny czas",
        entries = "Liczba wpisów",
        generatedBy = "Wygenerowano w WojThom 6.0"
    )
    AppLanguage.ENGLISH -> PdfStrings(
        title = "Work Time List",
        date = "Date",
        client = "Client / company",
        start = "Start",
        end = "End",
        duration = "Duration",
        total = "Total time",
        entries = "Entries",
        generatedBy = "Generated with WojThom 6.0"
    )
    AppLanguage.NORWEGIAN -> PdfStrings(
        title = "Arbeidstidsliste",
        date = "Dato",
        client = "Kunde / firma",
        start = "Fra",
        end = "Til",
        duration = "Tid",
        total = "Total tid",
        entries = "Antall oppføringer",
        generatedBy = "Generert med WojThom 6.0"
    )
}

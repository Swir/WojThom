# WojThom Web

Responsive browser version of WojThom 6.0, developed in parallel with the native Android app.

## Run

Open `index.html` directly in a modern browser or serve the `web/` directory with any static HTTP server.

## Current features

- responsive desktop/mobile UI
- Work / History / Statistics / Settings navigation
- full application UI in Polish, English and Norwegian
- remembered application language
- PDF language selected independently on every export: Polish / English / Norwegian
- printable professional A4 report with translated column names, totals and footer
- parser compatible with the Android version
- ranges such as `08:00 - 16:00`
- decimal hours such as `7.5h`
- durations such as `08:30`
- automatic totals and validation
- edit/delete entries
- local history with restore/delete
- basic statistics
- light/dark theme
- localStorage persistence

## PDF export

Choose **Export PDF**, then select the document language. The selected PDF language is independent from the current application language. The browser opens a clean A4 report and immediately shows the print dialog; choose **Save as PDF** / the equivalent option in your browser.

The report supports Polish, English and Norwegian characters using the browser's native PDF/print engine and does not require an external PDF library.

## Next modules

- advanced weekly/monthly statistics
- 37.5 h weekly target visualisation
- import/export backup
- PWA installation/offline mode
- automated parser regression tests shared with Android examples

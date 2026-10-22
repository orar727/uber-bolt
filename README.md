# Calculator Cursă

Aplicație Android care citește oferta din Bolt Driver / Uber Driver și arată deasupra ei profitul net, lei/km, lei/oră și verdictul (Acceptă / Limită / Refuză).

## Cum obții APK-ul

1. Creează pe github.com un repository nou (privat e ok).
2. Încarcă tot conținutul acestui folder (Add file > Upload files). Folderul ascuns `.github` trebuie să ajungă în repository. Dacă nu apare după upload, creează manual fișierul `.github/workflows/build.yml` (Add file > Create new file) și lipește conținutul din ZIP.
3. Deschide tab-ul **Actions**. Build-ul pornește singur. Dacă nu, alege „Build APK” > Run workflow.
4. După 3–5 minute, intră în rularea terminată și descarcă **CalculatorCursa-apk** din secțiunea Artifacts. Dezarhivează și instalează `app-debug.apk` pe telefon.

## Prima pornire

1. Instalează APK-ul (permite instalarea din sursă necunoscută).
2. Deschide aplicația, apasă „Deschide setările de accesibilitate” și activează „Calculator Cursă”.
3. Dacă Android 13+ blochează activarea: Setări > Aplicații > Calculator Cursă > ⋮ > Permite setări restricționate. Apoi activează din nou.
4. Completează prețul combustibilului, consumul, uzura și pragurile tale, apoi Salvează.
5. Deschide Bolt/Uber Driver. La o ofertă apare banda colorată sus.

## Note

- Sumele din Bolt („NET, taxe incluse”) și Uber („Câștig net”) sunt deja nete, deci comisionul din setări rămâne 0.
- La Uber, bonusul „pentru preluare” este considerat inclus în suma afișată.
- Aplicația nu cere internet și nu trimite date.
- Dacă nu apare nimic: Bolt/Uber pot ascunde textul de serviciile de accesibilitate sau pot schimba machetele. Atunci trimite-mi eroarea sau o captură și adaptez citirea (OCR pe captură de ecran).

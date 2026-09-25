@echo off
cd /d "%~dp0"
(
echo === Pulizia ===
if exist "car-catalog\" rmdir /s /q "car-catalog"
if exist "README-1.md" del /q "README-1.md"
echo === Commit ===
git add -A -- . ":!pubblica-github.bat"
git commit -m "Dockerfile: installa curl per il Maven Wrapper (fix deploy Render); rimuove copia annidata e README duplicato"
echo === Pull ===
git pull --rebase origin main
echo === Push ===
git push origin main
echo === Stato ===
git status --short
git log --oneline -3
) > pubblica-github.log 2>&1

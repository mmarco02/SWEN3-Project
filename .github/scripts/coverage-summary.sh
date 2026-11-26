#!/usr/bin/env bash
# Liest den JaCoCo-Report (CSV) und schreibt die Coverage als Tabelle in die
# Zusammenfassung des GitHub-Actions-Laufs (sichtbar auf der Seite des Runs).
#
# Aufruf: coverage-summary.sh <Name> <Pfad zu jacoco.csv>
set -euo pipefail

name="$1"
csv="$2"
summary="${GITHUB_STEP_SUMMARY:-/dev/stdout}"

if [[ ! -f "$csv" ]]; then
  echo "### Coverage $name: kein Report gefunden (Build fehlgeschlagen?)" >> "$summary"
  exit 0
fi

# Spalten der CSV: 4/5 = Instructions missed/covered, 6/7 = Branches, 8/9 = Lines
awk -F, -v name="$name" '
  NR > 1 { im += $4; ic += $5; bm += $6; bc += $7; lm += $8; lc += $9 }
  function pct(covered, missed) { return (covered + missed) ? sprintf("%.1f %%", 100 * covered / (covered + missed)) : "-" }
  END {
    print "### Coverage " name
    print ""
    print "| Zeilen | Branches | Instructions |"
    print "|---|---|---|"
    print "| " pct(lc, lm) " | " pct(bc, bm) " | " pct(ic, im) " |"
    print ""
  }
' "$csv" >> "$summary"

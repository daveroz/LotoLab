#!/usr/bin/env python3
"""Keeps the bundled draw history (app/src/main/assets/history/*.csv) up to date.

Sources (all public, no key, no account):
  * Dorado Mañana / Tarde / Noche, Paisita Día / Noche / 3: astroluna.co. Its game page lists the last 100
    draws (number + quinta); each per-date page links to the draw before it, so --backfill walks back
    to the first draw the site still has (early 2023).
  * Baloto + Revancha: baloto.com/resultados (official, paginated back to May 2021).
  * MiLoto: baloto.com/miloto/resultados (official, paginated back to its first draws in 2023).

CSV columns: date (YYYY-MM-DD), draw (official draw number, may be empty), numbers (space separated;
a chance result is one 4-digit string), extra (quinta for chance, superbalota for Baloto/Revancha).

Usage:  python3 scripts/update_history.py            # add the newest draws
        python3 scripts/update_history.py --backfill # also walk back through the whole archive
Standard library only, so it runs on a bare GitHub Actions runner.
"""
import csv
import html
import os
import re
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "history")
UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
MONTHS = {"enero": 1, "febrero": 2, "marzo": 3, "abril": 4, "mayo": 5, "junio": 6, "julio": 7, "agosto": 8,
          "septiembre": 9, "setiembre": 9, "octubre": 10, "noviembre": 11, "diciembre": 12}
CHANCE = {  # file id -> astroluna slug
    "dorado-manana": "dorado-manana",
    "dorado-tarde": "dorado-tarde",
    "dorado-noche": "dorado-noche",
    "paisita-dia": "paisita-dia",
    "paisita-noche": "paisita-noche",
    "paisita-3": "paisita-tres",
}
PAUSE = 0.6  # seconds between requests to the same site


def log(*a):
    print(*a, file=sys.stderr, flush=True)


def get(url, tries=3):
    for i in range(tries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept-Language": "es-CO,es;q=0.9"})
            with urllib.request.urlopen(req, timeout=30) as r:
                if r.geturl().rstrip("/") != url.rstrip("/") and "resultado-" in url:
                    return None  # redirected away: the page doesn't exist
                return r.read().decode("utf-8", "replace")
        except urllib.error.HTTPError as e:
            if e.code in (404, 410):
                return None
            log("  HTTP", e.code, url)
        except Exception as e:  # network hiccup: try again
            log("  error", e, url)
        time.sleep(2 + 3 * i)
    return None


def iso(day, month, year):
    m = MONTHS.get(month.lower())
    if not m:
        raise ValueError("month " + month)
    return "%04d-%02d-%02d" % (int(year), m, int(day))


# ---------------------------------------------------------------- CSV store

def load(name):
    rows = {}
    path = os.path.join(OUT, name + ".csv")
    if os.path.exists(path):
        with open(path, newline="", encoding="utf-8") as f:
            for r in csv.DictReader(f):
                rows[r["date"]] = r
    return rows


def save(name, rows):
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, name + ".csv")
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["date", "draw", "numbers", "extra"])
        for d in sorted(rows):
            r = rows[d]
            w.writerow([d, r.get("draw", ""), r["numbers"], r.get("extra", "")])


def merge(rows, date, numbers, draw="", extra=""):
    """Adds a draw; fills in a missing draw number / extra on one we already have. True if anything changed."""
    old = rows.get(date)
    if old is None:
        rows[date] = {"date": date, "draw": draw, "numbers": numbers, "extra": extra}
        return True
    changed = False
    if old["numbers"] != numbers:
        log("  ! %s changed from %s to %s (keeping the newer source)" % (date, old["numbers"], numbers))
        old["numbers"] = numbers
        changed = True
    for k, v in (("draw", draw), ("extra", extra)):
        if v and not old.get(k):
            old[k] = v
            changed = True
    return changed


# ---------------------------------------------------------------- chance (astroluna)

ROW = re.compile(r"<tr>\s*<td>\s*\w+\s+(\d{1,2})\s+([a-zA-Z]+)\s+(\d{4})\s*</td>\s*"
                 r"<td class=\"td-resultado\">\s*(\d{4})\s*</td>\s*<td>\s*(\d?)\s*</td>", re.S)
DAY = re.compile(r"del <b>\s*\w+\s+(\d{1,2}) de ([a-zA-Z]+) de (\d{4})\s*</b>\s*fue:</p>\s*"
                 r"<p class=\"text-center\"><span id=\"caja-resultado\">\s*(\d{4})\s*</span>", re.S)


def chance_latest(name, slug, rows):
    page = get("https://www.astroluna.co/" + slug)
    if not page:
        log(name, ": game page unavailable")
        return 0
    n = 0
    for d, mo, y, num, quinta in ROW.findall(page):
        n += merge(rows, iso(d, mo, y), num, extra=quinta)
    return n


MONTH_NAMES = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre",
               "octubre", "noviembre", "diciembre"]


def day_url(slug, date):
    y, m, d = date.split("-")
    return "https://www.astroluna.co/ultimos-sorteos/resultado-%s-%s-%s-%s" % (slug, d, MONTH_NAMES[int(m) - 1], y)


def chance_backfill(name, slug, rows, stop_at_known=False):
    """Walks back day by day through the per-date pages until the site has no older one. A full backfill
    resumes from the oldest draw already stored, so it can be stopped and started again."""
    link = re.compile(r"href=\"(https://www\.astroluna\.co/ultimos-sorteos/resultado-" + re.escape(slug)
                      + r"-\d{1,2}-[a-z]+-\d{4})\"")
    if not stop_at_known and rows:
        url = day_url(slug, min(rows))
    else:
        page = get("https://www.astroluna.co/" + slug)
        m = link.search(page or "")
        url = m.group(1) if m else None
    n, seen, empty = 0, set(), 0
    while url and url not in seen:
        seen.add(url)
        time.sleep(PAUSE)
        page = get(url)
        if not page:
            break
        days = DAY.findall(page)
        if days:
            empty = 0
            d, mo, y, num = days[0]
            date = iso(d, mo, y)
            known = date in rows
            n += merge(rows, date, num)
            if stop_at_known and known:
                break
        else:
            # A holiday with no draw (the page only says the result "will be published"): keep walking back.
            empty += 1
            if empty > 12:
                log(name, ": no results for a while, stopping at", url)
                break
        nxt = [u for u in link.findall(page) if u != url]
        url = nxt[0] if nxt else None
        if n and n % 100 == 0:
            log("  %s: %d added, now at %s" % (name, n, date))
            save(name, rows)
    return n


# ---------------------------------------------------------------- Baloto / Revancha / MiLoto (baloto.com)

BALOTO_ROW = re.compile(r"<td class=\"creation-date-results\">\s*(\d{1,2}) de ([A-Za-z]+) de (\d{4})\s*</td>\s*"
                        r"<td[^>]*>\s*([\d\s\-]+?)\s*-?\s*<span class=\"balota-red-results\">\s*(\d+)\s*</span>\s*</td>\s*"
                        r"<td>\s*<a[^>]*href=\"/resultados-(baloto|revancha)/(\d+)\"", re.S)
MILOTO_ROW = re.compile(r"<tr>\s*<td>\s*(\d{1,2}) de ([A-Za-z]+) de (\d{4})\s*</td>\s*"
                        r"<td class=\"td-results\">(.*?)</td>\s*<td>\s*<a[^>]*href=\"/miloto/resultados-miloto/(\d+)/\"", re.S)
PAGES = re.compile(r"P[áa]gina\s+\d+\s+de\s+(\d+)")


def balls(text):
    return " ".join("%02d" % int(x) for x in re.findall(r"\d+", text))


def baloto(rows_b, rows_r, everything):
    n, page_no, last = 0, 1, 1
    while page_no <= last:
        page = get("https://www.baloto.com/resultados?page=%d" % page_no)
        if not page:
            break
        m = PAGES.search(page)
        last = int(m.group(1)) if m and everything else 1
        new_here = 0
        for d, mo, y, nums, sb, kind, draw in BALOTO_ROW.findall(page):
            rows = rows_b if kind == "baloto" else rows_r
            new_here += merge(rows, iso(d, mo, y), balls(nums), draw, "%02d" % int(sb))
        n += new_here
        if not everything and new_here and page_no < 30:
            last = page_no + 1  # keep going while pages still bring new draws
        page_no += 1
        time.sleep(PAUSE)
    return n


def miloto(rows, everything):
    n, page_no, last = 0, 1, 1
    while page_no <= last:
        page = get("https://www.baloto.com/miloto/resultados/?page=%d" % page_no)
        if not page:
            break
        m = PAGES.search(page)
        last = int(m.group(1)) if m and everything else 1
        new_here = 0
        for d, mo, y, cells, draw in MILOTO_ROW.findall(page):
            new_here += merge(rows, iso(d, mo, y), balls(cells), draw)
        n += new_here
        if not everything and new_here and page_no < 30:
            last = page_no + 1
        page_no += 1
        time.sleep(PAUSE)
    return n


def main():
    backfill = "--backfill" in sys.argv
    jobs = []

    def chance_job(name, slug):
        rows = load(name)
        before = len(rows)
        added = chance_latest(name, slug, rows)
        added += chance_backfill(name, slug, rows, stop_at_known=not backfill)
        save(name, rows)
        return "%s: %d rows (+%d), %s .. %s" % (name, len(rows), len(rows) - before,
                                               min(rows) if rows else "-", max(rows) if rows else "-")

    def lotto_job():
        b, r, m = load("baloto"), load("revancha"), load("miloto")
        nb = baloto(b, r, backfill or len(b) < 50)
        nm = miloto(m, backfill or len(m) < 50)
        save("baloto", b)
        save("revancha", r)
        save("miloto", m)
        return "baloto %d / revancha %d / miloto %d rows (+%d, +%d)" % (len(b), len(r), len(m), nb, nm)

    with ThreadPoolExecutor(max_workers=6) as ex:
        jobs = [ex.submit(chance_job, k, v) for k, v in CHANCE.items()] + [ex.submit(lotto_job)]
        for j in jobs:
            print(j.result())


if __name__ == "__main__":
    main()

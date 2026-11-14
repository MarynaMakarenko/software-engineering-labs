"""Генерує табл. 2 і 3 звіту лаб. 5 з результатів Playwright (JSON-репортер).

Джерела: docs/lab5/data/results-r2.json (критичний UC R2 -> табл. 2),
         docs/lab5/data/results-functions.json (решта функцій -> табл. 3).
Заголовок кожного тесту: "<ID> | <потік/UC> | <Коректні/Некоректні> | <сценарій>";
анотації тесту: steps, expected, data (необов'язкова).

Результат вставляється у звіт між маркерами <!-- T2:BEGIN --> ... <!-- T2:END -->
і <!-- T3:BEGIN --> ... <!-- T3:END -->.

Запуск: .venv/bin/python docs/lab5/src/gen_tables.py
"""
import json
import re
from pathlib import Path

from gridtable import grid

LAB = Path(__file__).resolve().parent.parent
DATA = LAB / "data"
REPORT = LAB / "Лаба5_Код_Тест_JobMatch_Макаренко.md"
SEED = LAB.parent / "jobmatch/src/main/resources/db/seed/V2__seed.sql"

# Пароль демо-облікових записів у звіт не виводимо (див. коментар у V2__seed.sql).
_m = re.search(r"Пароль усіх демонстраційних облікових записів:\s*(\S+)", SEED.read_text(encoding="utf-8"))
DEMO_PASSWORD = _m.group(1) if _m else None

# Уточнення назв сценаріїв, у яких заголовок тесту не відповідає його даним.
LABEL_FIX = {
    "T2-08": "максимальний поріг 100 % без кандидатів",
}

FLOW_ORDER = ["Основний", "Основний (крок 4)", "A1", "A2", "A3", "A4", "A5", "A6", "A7"]

# Групи табл. 3 (частини таблиці для книжкової орієнтації аркуша).
T3_PARTS = [
    ("Вхід, вихід, довідка, вакансії, реєстрація (U1–U3, G1–G2)", ("U1", "U2", "U3", "G1", "G2")),
    ("Функції Шукача (C1–C5)", ("C1", "C2", "C3", "C4", "C5")),
    ("Функції Представника компанії (E1–E4)", ("E1", "E2", "E3", "E4")),
    ("Функції Рекрутера та розмежування доступу (R1, R3–R7, FR-2, FR-3)", ("R1", "R3", "R4", "R5", "R6", "R7", "Доступ", "Неіснуюча")),
]

STATUS_UA = {"passed": "пройдено", "failed": "відмова", "timedOut": "відмова (тайм-аут)", "skipped": "пропущено"}


def load(name):
    d = json.loads((DATA / name).read_text(encoding="utf-8"))
    rows = []

    def walk(suite):
        for spec in suite.get("specs", []):
            for t in spec["tests"]:
                ann = {a["type"]: a.get("description", "") for a in t.get("annotations", [])}
                res = t["results"][-1] if t["results"] else {"status": "skipped"}
                tid, flow, kind, scen = [p.strip() for p in spec["title"].split("|", 3)]
                rows.append({"id": tid, "flow": flow, "kind": kind, "scenario": LABEL_FIX.get(tid, scen),
                             "steps": ann.get("steps", ""), "expected": ann.get("expected", ""),
                             "data": ann.get("data", ""), "status": res["status"]})
        for s in suite.get("suites", []):
            walk(s)

    for s in d["suites"]:
        walk(s)
    return d["stats"], rows


def cell(s):
    s = s.replace("\n", " ")
    if DEMO_PASSWORD:
        s = s.replace("пароль " + DEMO_PASSWORD, "пароль (демо)").replace(DEMO_PASSWORD, "(демо-пароль)")
    return s


def id_key(tid):
    return [int(x) for x in re.findall(r"\d+", tid)]


def table2(stats, rows):
    rows = sorted(rows, key=lambda r: (FLOW_ORDER.index(r["flow"]), r["kind"] != "Коректні", id_key(r["id"])))
    header = ["№", "Умова виконання", "Потік", "Кроки тесту («що натиснути, які дані в які поля ввести»)",
              "Очікувана реакція Веб-застосунку"]
    body, last = [], None
    for r in rows:
        grp = (r["flow"], r["kind"])
        if grp != last:
            data_kind = "коректні дані" if r["kind"] == "Коректні" else "некоректні дані"
            if r["flow"] == "Основний":
                flow_name = "Базовий потік"
            elif r["flow"] == "Основний (крок 4)":
                flow_name = "Базовий потік, крок 4 (розширення R4)"
            else:
                flow_name = "Альтернативний потік " + r["flow"]
            body.append(f"**{flow_name} — {data_kind}**")
            last = grp
        dup = r["data"].replace(":", "").replace(" =", "") in r["scenario"].replace(":", "")
        cond = r["scenario"] + (f" ({r['data']})" if r["data"] and not dup else "")
        flow = "Базовий" if r["flow"].startswith("Основний") else r["flow"]
        body.append([r["id"], cell(cond), flow, cell(r["steps"]), cell(r["expected"])])
    return grid(header, body, [7, 17, 10, 32, 30])


def table3(stats, rows):
    parts = []
    used = set()
    header = ["№", "Функція", "Умови виконання", "Кроки тесту («що натиснути, які дані в які поля ввести»)",
              "Очікувана реакція Веб-застосунку"]
    for i, (title, prefixes) in enumerate(T3_PARTS, 1):
        sel = [r for r in rows if r["flow"].split()[0] in prefixes]
        sel.sort(key=lambda r: id_key(r["id"]))
        used.update(r["id"] for r in sel)
        cap = (f"Таблиця 3 — Тести для функцій Веб-застосунку JobMatch, частина {i}: {title}" if i == 1
               else f"Таблиця 3, частина {i}: {title}")
        body, last_fn = [], None
        for r in sel:
            fn = r["flow"] if r["flow"] != last_fn else ""
            last_fn = r["flow"]
            cond = ("Коректні дані" if r["kind"] == "Коректні" else "Некоректні дані") + " — " + r["scenario"]
            body.append([r["id"], cell(fn), cell(cond), cell(r["steps"]), cell(r["expected"])])
        parts.append(cap + "\n\n" + grid(header, body, [7, 11, 17, 33, 28]))
    missing = [r["id"] for r in rows if r["id"] not in used]
    if missing:
        raise SystemExit(f"Тести поза групами табл. 3: {missing}")
    return "\n\n".join(parts)


def plural(n):
    if n % 10 == 1 and n % 100 != 11:
        return f"{n} тест"
    if 2 <= n % 10 <= 4 and not 12 <= n % 100 <= 14:
        return f"{n} тести"
    return f"{n} тестів"


def summary(stats, rows, name):
    by = {}
    for r in rows:
        by[r["status"]] = by.get(r["status"], 0) + 1
    st = ", ".join(f"{STATUS_UA.get(k, k)} — {v}" for k, v in sorted(by.items()))
    return f"{name}: {plural(len(rows))} ({st}); запуск {stats['startTime'][:19].replace('T', ' ')} UTC, тривалість {stats['duration'] / 1000:.1f} с."


def replace(text, tag, body):
    pat = re.compile(rf"(<!-- {tag}:BEGIN -->\n).*?(<!-- {tag}:END -->)", re.S)
    if not pat.search(text):
        raise SystemExit(f"маркер {tag} не знайдено у звіті")
    return pat.sub(lambda m: m.group(1) + body + "\n" + m.group(2), text)


def main():
    s2, r2 = load("results-r2.json")
    s3, r3 = load("results-functions.json")
    bad = [r["id"] for r in r2 + r3 if r["status"] != "passed"]
    text = REPORT.read_text(encoding="utf-8")
    text = replace(text, "T2", table2(s2, r2))
    text = replace(text, "T2S", summary(s2, r2, "results-r2.json"))
    text = replace(text, "T3", table3(s3, r3))
    text = replace(text, "T3S", summary(s3, r3, "results-functions.json"))
    REPORT.write_text(text, encoding="utf-8")
    print(f"T2: {len(r2)} тестів, T3: {len(r3)} тестів, неуспішні: {bad or 'немає'}")
    flows = {}
    for r in r2:
        flows.setdefault(r["flow"], {"Коректні": 0, "Некоректні": 0})[r["kind"]] += 1
    for f in FLOW_ORDER:
        if f in flows:
            print(f"  {f}: {flows[f]}")


if __name__ == "__main__":
    main()

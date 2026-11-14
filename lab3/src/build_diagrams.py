"""Лаб. 3 JobMatch: генерування User Flow (Graphviz) і Wireflow (HTML -> headless Chrome).

Запуск: python3 lab3/src/build_diagrams.py
Вихід: lab3/src/uf_*.dot, *.svg; lab3/img/uf_*.png, wf_*.png
Нотація Гаррета/BPMN: сторінка — прямокутник; група сторінок — три злиті прямокутники
(дорисовуються після dot); процес — прямокутник з подвійними короткими сторонами;
рішення — ромб; початок/результат — овал; з'єднувач — коло; умовний перехід — пунктир;
незворотний перехід — стрілка з поперечною рискою.
"""
import re
import subprocess
from pathlib import Path

SRC = Path(__file__).resolve().parent
IMG = SRC.parent / "img"
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
FONT = "Arial"


def esc(s: str) -> str:
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


class G:
    def __init__(self, name, rankdir="TB", nodesep=0.35, ranksep=0.45):
        self.name = name
        self.lines = [
            f"digraph {name} {{",
            f'  graph [rankdir={rankdir}, fontname="{FONT}", fontsize=12, nodesep={nodesep}, '
            f'ranksep={ranksep}, pad=0.35, newrank=true, compound=true];',
            f'  node [fontname="{FONT}", fontsize=11, style=filled, fillcolor=white, color=black, penwidth=1.2];',
            f'  edge [fontname="{FONT}", fontsize=9.5, color="#222222", arrowsize=0.8];',
        ]
        self.indent = "  "

    def add(self, s):
        self.lines.append(self.indent + s)

    # --- вузли ---
    def _label(self, name, url=None, acts=()):
        rows = f'<TR><TD><B>{esc(name).replace(chr(92) + "n", "<BR/>")}</B></TD></TR>'
        if url:
            rows += f'<TR><TD><FONT POINT-SIZE="9" COLOR="#555555">{esc(url)}</FONT></TD></TR>'
        if acts:
            body = "<BR/>".join("• " + esc(a) for a in acts)
            rows += f'<HR/><TR><TD ALIGN="LEFT" BALIGN="LEFT"><FONT POINT-SIZE="10">{body}</FONT></TD></TR>'
        return f'<<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="2">{rows}</TABLE>>'

    def page(self, nid, name, url=None, acts=()):
        self.add(f'{nid} [shape=box, margin="0.08,0.04", class="page", label={self._label(name, url, acts)}];')

    def group(self, nid, name, url=None, acts=()):
        self.add(f'{nid} [shape=box, margin="0.08,0.04", class="group", label={self._label(name, url, acts)}];')

    def process(self, nid, text):
        self.add(f'{nid} [shape=box, class="process", margin="0.22,0.06", fillcolor="#f2f2f2", label="{text}"];')

    def decision(self, nid, text):
        self.add(f'{nid} [shape=diamond, fillcolor="#e9e9e9", fontsize=10, margin="0.02,0.02", label="{text}"];')

    def start(self, nid, text):
        self.add(f'{nid} [shape=ellipse, fillcolor="#dddddd", penwidth=1.4, fontsize=11, label="{text}"];')

    def end(self, nid, text):
        self.add(f'{nid} [shape=ellipse, fillcolor="#dddddd", penwidth=2.6, fontsize=11, label="{text}"];')

    def conn(self, nid, text, note=None):
        x = f', xlabel="{note}", fontsize=11' if note else ""
        self.add(f'{nid} [shape=circle, width=0.36, fixedsize=true, fontsize=11, label="{text}"{x}];')

    # --- переходи ---
    def e(self, a, b, label=None, kind="n", extra="", back=False):
        attrs = ["constraint=false"] if back else []
        if label:
            attrs.append(f'label="{label}"')
        if kind == "c":
            attrs.append("style=dashed")
        elif kind == "i":
            attrs.append("arrowhead=normaltee, arrowsize=0.9")
        if extra:
            attrs.append(extra)
        self.add(f'{a} -> {b} [{", ".join(attrs)}];')

    def cluster(self, cid, label):
        self.add(f"subgraph cluster_{cid} {{")
        self.indent += "  "
        self.add(f'label="{label}"; labeljust=l; style="dashed,rounded"; color="#777777"; '
                 f'fontsize=11; fontname="{FONT} Bold"; margin=10;')

    def close(self):
        self.indent = self.indent[:-2]
        self.add("}")

    def rank(self, *ids):
        self.add("{rank=same; " + "; ".join(ids) + ";}")

    def text(self):
        return "\n".join(self.lines + ["}"])


# ---------------------------------------------------------------- легенда
def legend_svg() -> str:
    """Легенда нотації — статичний SVG (ті самі стилі, що й у діаграмах)."""
    rows = [
        ("page", "Сторінка: ім'я, URL; під рискою — дії користувача на сторінці"),
        ("group", "Група сторінок (три злиті прямокутники)"),
        ("process", "Процес — дія з вкладеними діями, яку виконує застосунок"),
        ("decision", "Розгалуження — рішення користувача або перевірка застосунку"),
        ("start", "Початок — ціль актора"),
        ("end", "Завершення — значущий результат (потовщений контур)"),
        ("conn", "З'єднувач: потоки з однаковим результатом; однакові літери — одна точка"),
        ("e_n", "Перехід між діями та сторінками (підпис — елемент керування)"),
        ("e_c", "Перехід за умовою (пунктир; умова — підпис)"),
        ("e_i", "Незворотний перехід (стрілка з поперечною рискою)"),
        ("cl", "Рамка завдання актора (Task Flow) — додатковий елемент"),
    ]
    H, W, cx, tx = 64, 860, 105, 215
    st = 'stroke="black" stroke-width="1.4"'
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H * len(rows) + 30}" '
           f'font-family="{FONT}" font-size="15">',
           '<defs><marker id="ah" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">'
           '<path d="M0,0 L10,4 L0,8 z" fill="#222"/></marker></defs>',
           f'<rect width="{W}" height="{H * len(rows) + 30}" fill="white"/>']
    for i, (k, desc) in enumerate(rows):
        y = 15 + i * H + H / 2
        if k == "page":
            out.append(f'<rect x="{cx-55}" y="{y-26}" width="110" height="52" fill="white" {st}/>'
                       f'<text x="{cx}" y="{y-10}" text-anchor="middle" font-weight="bold" font-size="13">Сторінка</text>'
                       f'<text x="{cx}" y="{y+3}" text-anchor="middle" font-size="10" fill="#555">/url</text>'
                       f'<line x1="{cx-48}" y1="{y+7}" x2="{cx+48}" y2="{y+7}" stroke="black"/>'
                       f'<text x="{cx-46}" y="{y+20}" font-size="11">• дія</text>')
        elif k == "group":
            for d in (8, 4, 0):
                out.append(f'<rect x="{cx-55+d}" y="{y-20-d}" width="110" height="40" fill="white" {st}/>')
            out.append(f'<text x="{cx}" y="{y+5}" text-anchor="middle" font-weight="bold" font-size="13">Група</text>')
        elif k == "process":
            out.append(f'<rect x="{cx-55}" y="{y-20}" width="110" height="40" fill="#f2f2f2" {st}/>'
                       f'<line x1="{cx-48}" y1="{y-20}" x2="{cx-48}" y2="{y+20}" {st}/>'
                       f'<line x1="{cx+48}" y1="{y-20}" x2="{cx+48}" y2="{y+20}" {st}/>'
                       f'<text x="{cx}" y="{y+5}" text-anchor="middle" font-size="13">Процес</text>')
        elif k == "decision":
            out.append(f'<polygon points="{cx-50},{y} {cx},{y-25} {cx+50},{y} {cx},{y+25}" fill="#e9e9e9" {st}/>'
                       f'<text x="{cx}" y="{y+5}" text-anchor="middle" font-size="12">Умова?</text>')
        elif k in ("start", "end"):
            sw = 1.4 if k == "start" else 3
            label = "Ціль: …" if k == "start" else "Результат: …"
            out.append(f'<ellipse cx="{cx}" cy="{y}" rx="62" ry="22" fill="#dddddd" stroke="black" stroke-width="{sw}"/>'
                       f'<text x="{cx}" y="{y+5}" text-anchor="middle" font-size="13">{label}</text>')
        elif k == "conn":
            out.append(f'<circle cx="{cx}" cy="{y}" r="15" fill="white" {st}/>'
                       f'<text x="{cx}" y="{y+5}" text-anchor="middle" font-size="13">А</text>')
        elif k.startswith("e_"):
            dash = ' stroke-dasharray="6,4"' if k == "e_c" else ""
            out.append(f'<line x1="{cx-60}" y1="{y}" x2="{cx+58}" y2="{y}" stroke="#222" stroke-width="1.4"{dash} '
                       f'marker-end="url(#ah)"/>')
            if k == "e_c":
                out.append(f'<text x="{cx}" y="{y-7}" text-anchor="middle" font-size="11">так</text>')
            if k == "e_i":
                out.append(f'<line x1="{cx+40}" y1="{y-8}" x2="{cx+40}" y2="{y+8}" stroke="#222" stroke-width="2"/>')
        elif k == "cl":
            out.append(f'<rect x="{cx-62}" y="{y-22}" width="124" height="44" rx="8" fill="white" stroke="#777" '
                       f'stroke-width="1.2" stroke-dasharray="5,3"/>'
                       f'<text x="{cx-54}" y="{y-6}" font-size="11" font-weight="bold">Завдання: …</text>')
        out.append(f'<text x="{tx}" y="{y+5}">{esc(desc)}</text>')
    out.append("</svg>")
    return "\n".join(out)


def render_legend(zoom=2.0):
    svg = SRC / "uf_legend.svg"
    png = IMG / "uf_legend.png"
    svg.write_text(legend_svg(), encoding="utf-8")
    subprocess.run(["rsvg-convert", "-z", str(zoom), "-b", "white", str(svg), "-o", str(png)], check=True)
    print("UF:", png.name)


# ---------------------------------------------------------------- рис. 1 огляд
def overview():
    g = G("uf_overview", nodesep=0.45, ranksep=0.5)
    g.start("st", "Початок: відвідувач\\nвідкриває JobMatch")
    g.page("home", "Головна", "/", ["пошук: посада, місто", "переходи до розділів"])
    g.group("vac", "Вакансії · Вакансія", "/vacancies, /vacancies/{id}", ["G1 перегляд і пошук", "C4 відгук (Шукач)"])
    g.page("help", "Допомога", "/help", ["U3 FAQ, Настанова PDF"])
    g.page("about", "Про агенцію й контакти", "/about")
    g.page("login", "Вхід", "/login", ["U1 email, пароль"])
    g.page("reg", "Реєстрація", "/register", ["G2 вибір ролі, дані"])
    g.process("auth", "Перевірка email і пароля")
    g.decision("dauth", "Дані\\nправильні?")
    g.decision("drole", "Роль?")
    g.decision("dreg", "Дані коректні,\\nemail вільний?")
    g.process("regp", "Створення облікового запису")
    g.group("cabC", "Кабінет шукача — рис. 3", "/me/…  (8 сторінок)", ["C1–C5, M1"])
    g.group("cabE", "Кабінет компанії — рис. 4", "/company/…  (7 сторінок)", ["E1–E4, M1"])
    g.group("cabR", "Кабінет рекрутера — рис. 5", "/recruiter/…  (14 сторінок)", ["R1–R7, M1"])
    g.conn("cOut", "В")
    g.process("logout", "Завершення сесії (U2)")
    g.end("fin", "Результат: сесію завершено,\\nГоловна «Ви вийшли із застосунку»")
    g.conn("cErr", "П")
    g.page("err", "Помилка 404 / 500", "будь-яка URL", ["«На головну»"])

    g.e("st", "home")
    g.e("home", "vac", "«Вакансії»")
    g.e("home", "help", "«Допомога»")
    g.e("help", "about", "«Про агенцію»")
    g.e("home", "login", "«Увійти»")
    g.e("home", "reg", "«Реєстрація»")
    g.e("vac", "login", "відгук\\nбез входу", "c")
    g.e("login", "auth", "«Увійти»")
    g.e("auth", "dauth")
    g.e("dauth", "login", "ні: повідомлення", "c")
    g.e("dauth", "drole", "так")
    g.e("drole", "cabC", "Шукач")
    g.e("drole", "cabE", "Представник\\nкомпанії")
    g.e("drole", "cabR", "Рекрутер")
    g.e("reg", "dreg", "«Зареєструватися»")
    g.e("dreg", "reg", "ні: діагностика", "c")
    g.e("dreg", "regp", "так")
    g.e("regp", "login", "«Реєстрацію\\nзавершено»", "i")
    for c in ("cabC", "cabE", "cabR"):
        g.e(c, "cOut", "«Вийти»" if c == "cabE" else None)
        g.e(c, "cErr", "id не знайдено /\\nзбій" if c == "cabE" else None, "c")
    g.e("cabC", "vac", "«Вакансії»\\nу шапці")
    g.e("cOut", "logout", None, "i")
    g.e("logout", "fin")
    g.e("cErr", "err")
    g.conn("Gt", "Г")
    g.conn("Gb", "Г")
    g.e("Gt", "home")
    g.e("err", "Gb", "«На головну»")
    g.rank("cabC", "cabE", "cabR")
    return g


# ---------------------------------------------------------------- рис. 2 гість
def guest():
    g = G("uf_guest", nodesep=0.4, ranksep=0.42)
    g.start("st", "Ціль: знайти вакансію,\\nстати користувачем")
    g.page("home", "Головна", "/", ["форма «Знайти»: посада, місто", "«Вакансії», «Реєстрація», «Увійти»"])
    g.cluster("t1", "Завдання: знайти відповідну вакансію (G1)")
    g.page("vac", "Вакансії", "/vacancies", ["фільтри: посада, компанія, місто, зарплата від",
                                               "«Знайти», «Скинути фільтри»"])
    g.decision("dres", "Є вакансії?")
    g.page("card", "Вакансія", "/vacancies/{id}", ["посада, компанія, зарплата, місто, вимоги",
                                                    "«Увійдіть, щоб відгукнутися»"])
    g.page("err", "Помилка 404", None, ["«До вакансій»"])
    g.end("e1", "Результат:\\nзнайдено вакансію")
    g.close()
    g.cluster("t2", "Завдання: отримати доступ до кабінету (G2)")
    g.decision("dacc", "Є обліковий\\nзапис?")
    g.page("reg", "Реєстрація", "/register", ["email, пароль, повтор пароля",
                                              "роль: шукач / представник компанії",
                                              "шукач: ПІБ, телефон, місто, адреса, згода",
                                              "компанія: назва, місто, адреса, телефон, email"])
    g.decision("dreg", "Дані коректні, email\\nі назва компанії вільні?")
    g.process("regp", "Створення облікового запису\\nі резюме / профілю компанії")
    g.page("login", "Вхід", "/login", ["«Реєстрацію завершено. Увійдіть…»", "email, пароль"])
    g.conn("c1", "1", note="до рис. 1")
    g.close()
    g.cluster("t3", "Завдання: дізнатися про агенцію й правила (U3)")
    g.page("help", "Допомога", "/help", ["FAQ за ролями", "«Завантажити Настанову (PDF)»"])
    g.page("about", "Про агенцію й контакти", "/about", ["адреса, телефон, email, графік"])
    g.conn("cI", "І")
    g.end("e3", "Результат:\\nотримано відповідь")
    g.close()

    g.e("st", "home")
    g.e("home", "vac", "«Знайти» / «Вакансії»")
    g.e("vac", "dres")
    g.e("dres", "card", "так: відкрити картку")
    g.e("dres", "vac", "ні: «Вакансій за заданими\\nумовами не знайдено»", "c", back=True)
    g.e("card", "e1")
    g.e("card", "err", "id не знайдено", "c")
    g.e("err", "vac", "«До вакансій»", back=True)
    g.e("card", "dacc", "хоче відгукнутися", "c")
    g.e("home", "dacc", "«Реєстрація» /\\n«Увійти»")
    g.e("dacc", "login", "так")
    g.e("dacc", "reg", "ні")
    g.e("reg", "dreg", "«Зареєструватися»")
    g.e("dreg", "reg", "ні: діагностика", "c", back=True)
    g.e("dreg", "regp", "так")
    g.e("regp", "login", None, "i")
    g.e("login", "c1", "«Увійти»")
    g.e("home", "help", "«Допомога»")
    g.e("help", "about", "«Про агенцію»")
    g.e("help", "cI")
    g.e("about", "cI")
    g.e("cI", "e3")
    return g


# ---------------------------------------------------------------- рис. 3а шукач: резюме
def candidate_a():
    g = G("uf_candidate_a", nodesep=0.35, ranksep=0.4)
    g.start("st", "Ціль: представити себе\\nроботодавцям (після входу, рис. 1)")
    g.cluster("t1", "Завдання: вести резюме та історію роботи (C1, C2)")
    g.page("res", "Моє резюме", "/me/resume", ["особисті дані, статус, бажані умови",
                                               "освіта: «Додати освіту», «Видалити»", "«Редагувати»"])
    g.page("resEdit", "Редагування резюме", "/me/resume/edit",
           ["ПІБ, дата народж., телефон, email,", "місто, адреса, статус пошуку,", "бажана посада, зарплата, про себе"])
    g.decision("dv1", "Дані\\nкоректні?")
    g.page("edu", "Додавання освіти", "/me/education/new", ["рівень, заклад,", "спеціальність, рік"])
    g.decision("dv2", "Дані\\nкоректні?")
    g.decision("dDelE", "Підтвердити\\nвидалення?")
    g.page("work", "Історія роботи", "/me/work", ["компанія, посада, зарплата, дати; стаж",
                                                  "«Додати», «Редагувати», «Видалити»"])
    g.group("wForm", "Новий запис роботи ·\\nРедагування запису роботи", "/me/work/new, /me/work/{id}/edit",
            ["компанія, посада, зарплата,", "дата початку, дата завершення"])
    g.decision("dv3", "Дати й поля\\nкоректні?")
    g.decision("dDelW", "Підтвердити\\nвидалення?")
    g.end("e1", "Результат:\\nрезюме актуальне")
    g.close()

    g.e("st", "res")
    g.conn("Rt", "Р")
    g.conn("Rb", "Р")
    g.e("Rt", "res")
    g.e("res", "resEdit", "«Редагувати»")
    g.e("resEdit", "dv1", "«Зберегти»")
    g.e("dv1", "Rb", "так: «Збережено»")
    g.e("dv1", "resEdit", "ні", "c", back=True)
    g.e("res", "edu", "«Додати освіту»")
    g.e("edu", "dv2", "«Зберегти»")
    g.e("dv2", "Rb", "так")
    g.e("dv2", "edu", "ні", "c", back=True)
    g.e("res", "dDelE", "«Видалити»\\nосвіту")
    g.e("dDelE", "Rb", "так", "i")
    g.e("dDelE", "Rb", "ні", "c")
    g.e("res", "work", "меню «Історія роботи»", extra="minlen=3")
    g.conn("It", "І")
    g.conn("Ib", "І")
    g.e("It", "work")
    g.e("work", "wForm", "«Додати» /\\n«Редагувати»")
    g.e("wForm", "dv3", "«Зберегти»")
    g.e("dv3", "Ib", "так")
    g.e("dv3", "wForm", "ні", "c", back=True)
    g.e("work", "dDelW", "«Видалити»")
    g.e("dDelW", "Ib", "так", "i")
    g.e("dDelW", "Ib", "ні", "c")
    g.e("work", "e1")
    return g


# ---------------------------------------------------------------- рис. 3б шукач: пошук і відгуки
def candidate_b():
    g = G("uf_candidate_b", nodesep=0.35, ranksep=0.4)
    g.start("st", "Ціль: знайти роботу\\n(після входу, рис. 1)")
    g.page("res", "Моє резюме", "/me/resume (рис. 3а)", ["меню ролі, шапка"])
    g.cluster("t2", "Завдання: знайти роботу й відгукнутися (C3, G1, C4)")
    g.process("m1", "Оцінювання відповідності (M1)")
    g.decision("dm", "Є вакансії\\n≥ 50 %?")
    g.page("mat", "Відповідні вакансії", "/me/matches", ["посада, компанія, зарплата,", "оцінка, складові"])
    g.page("vac", "Вакансії", "/vacancies", ["фільтри G1"])
    g.conn("cV", "В")
    g.page("card", "Вакансія", "/vacancies/{id}", ["вимоги, умови", "«Відгукнутися»"])
    g.decision("dap", "Вакансія відкрита,\\nвідгуку ще немає?")
    g.process("apc", "Створення кандидатури\\n(«Нова», джерело «Шукач»)")
    g.close()
    g.cluster("t3", "Завдання: стежити за відгуками (C5)")
    g.page("apps", "Мої відгуки", "/me/applications", ["вакансія, компанія, дата, статус"])
    g.end("e2", "Результат: відгук\\nнадіслано, статус відомий")
    g.close()

    g.e("st", "res")
    g.e("res", "m1", "меню «Відповідні\\nвакансії»")
    g.e("m1", "dm")
    g.e("dm", "mat", "так")
    g.e("dm", "res", "ні: порожній стан,\\n«Доповніть резюме»", "c", back=True)
    g.e("res", "vac", "«Вакансії»\\nу шапці")
    g.e("mat", "cV", "посада вакансії")
    g.e("vac", "cV", "картка")
    g.e("cV", "card")
    g.e("card", "dap", "«Відгукнутися»")
    g.e("dap", "apc", "так")
    g.e("dap", "card", "ні: показано\\nстатус відгуку", "c", back=True)
    g.e("apc", "apps", "«Відгук надіслано»", "i")
    g.e("res", "apps", "меню «Мої відгуки»")
    g.e("apps", "e2")
    return g


# ---------------------------------------------------------------- рис. 4 представник компанії
def employer():
    g = G("uf_employer", nodesep=0.35, ranksep=0.4)
    g.start("st", "Ціль: закрити вакансії компанії\\nкандидатами (після входу, рис. 1)")
    g.cluster("t1", "Завдання: підтримувати профіль компанії (E1)")
    g.page("comp", "Моя компанія", "/company", ["назва, місто, адреса, контакти, опис", "«Редагувати»"])
    g.page("compEdit", "Редагування моєї компанії", "/company/edit",
           ["назва, місто, адреса, телефон,", "email, сайт, опис"])
    g.decision("dv1", "Дані коректні,\\nназва унікальна?")
    g.close()
    g.cluster("t2", "Завдання: вести вакансії компанії (E2)")
    g.page("cvac", "Вакансії компанії", "/company/vacancies",
           ["посада, зарплата, місто, статус, кандидатури", "«Додати», «Редагувати»",
            "«Закрити» / «Відкрити», «Видалити»", "«Відповідні резюме», «Кандидатури»"])
    g.group("vForm", "Нова вакансія · Редагування вакансії", "/company/vacancies/new, …/{id}/edit",
            ["посада, зарплата, мін. освіта,", "мін. досвід, місто, вимоги"])
    g.decision("dv2", "Дані\\nкоректні?")
    g.decision("dDel", "Підтвердити\\nвидалення?")
    g.end("e2", "Результат:\\nвакансії актуальні")
    g.close()
    g.cluster("t3", "Завдання: знайти й відібрати кандидатів (E3, E4)")
    g.process("m1", "Оцінювання відповідності (M1)")
    g.decision("dm", "Є резюме\\n≥ 50 %?")
    g.page("mat", "Відповідні резюме", "/company/vacancies/{id}/matches",
           ["ім'я, перша літера прізвища, посада,", "зарплата, освіта, стаж, оцінка, складові", "«Мін. оцінка, %», «Оновити»"])
    g.end("e3a", "Результат: знайдено\\nрелевантні резюме")
    g.page("apps", "Кандидатури на вакансію", "/company/vacancies/{id}/applications",
           ["ПІБ, джерело, коментар рекрутера,", "дата, статус", "«Запросити», «Відхилити»"])
    g.decision("dec", "Рішення?")
    g.end("e3", "Результат:\\nкандидатури розглянуто")
    g.close()

    g.e("st", "comp")
    g.e("comp", "compEdit", "«Редагувати»")
    g.e("compEdit", "dv1", "«Зберегти»")
    g.e("dv1", "comp", "так", back=True)
    g.e("dv1", "compEdit", "ні: діагностика", "c", back=True)
    g.e("comp", "cvac", "меню «Вакансії»")
    g.add("dv1 -> cvac [style=invis];")
    g.e("cvac", "vForm", "«Додати» /\\n«Редагувати»")
    g.e("vForm", "dv2", "«Зберегти»")
    g.conn("Vt", "В")
    g.conn("Vb", "В")
    g.e("Vt", "cvac")
    g.e("dv2", "Vb", "так")
    g.e("dv2", "vForm", "ні: діагностика", "c", back=True)
    g.e("cvac", "dDel", "«Видалити»")
    g.e("dDel", "Vb", "так", "i")
    g.e("dDel", "Vb", "ні", "c")
    g.e("cvac", "cvac", "«Закрити» / «Відкрити»")
    g.e("cvac", "e2")
    g.add("Vb -> m1 [style=invis]; Vb -> apps [style=invis];")
    g.e("cvac", "m1", "«Відповідні резюме»")
    g.e("m1", "dm")
    g.e("dm", "mat", "так")
    g.conn("Vb2", "В")
    g.e("dm", "Vb2", "ні: порожній стан,\\nпорада переглянути вимоги", "c")
    g.e("mat", "e3a")
    g.e("cvac", "apps", "«Кандидатури»")
    g.e("apps", "dec")
    g.e("dec", "apps", "«Запросити»:\\n«Запрошено»", back=True)
    g.e("dec", "apps", "«Відхилити»:\\n«Відхилено»", "i", back=True)
    g.e("dec", "e3")
    return g


# ---------------------------------------------------------------- рис. 5а рекрутер: R2 + R7
def recruiter_r2():
    g = G("uf_recruiter_r2", nodesep=0.35, ranksep=0.42)
    g.start("st", "Ціль: працевлаштувати кандидатів\\nна вакансії клієнтів (після входу, рис. 1)")
    g.page("panel", "Панель рекрутера", "/recruiter",
           ["відкриті вакансії: компанія, посада,", "зарплата, місто, кандидатури", "«Підібрати кандидатів»"])
    g.page("cc", "Картка компанії", "/recruiter/companies/{id} (рис. 5б)", ["«Підібрати кандидатів»"])

    g.cluster("t1", "Завдання: підібрати й запропонувати кандидатів (R2 — критичний; R4)")
    g.conn("cP", "П")
    g.decision("dOpen", "Вакансія\\nвідкрита?")
    g.process("m1", "Оцінювання відповідності (M1)")
    g.decision("dEmp", "Є кандидати\\n≥ порогу?")
    g.page("match", "Підбір кандидатів", "/recruiter/vacancies/{id}/matches",
           ["параметри вакансії; «Мін. оцінка, %», «Оновити»",
            "ПІБ, посада, зарплата, освіта, стаж, оцінка",
            "«Запропонувати» / «Запропоновано: …»"])
    g.decision("dThr", "Поріг — ціле\\n0–100?")
    g.page("person", "Картка людини", "/recruiter/people/{id}", ["особисті дані, освіта,", "історія роботи, стаж (R4)"])
    g.decision("dConf", "Форма пропозиції:\\nрішення?")
    g.decision("dCom", "Коментар ≤ 500,\\nкандидатури\\nще немає?")
    g.process("apc", "Створення кандидатури\\n(«Нова», джерело «Рекрутер»)")
    g.end("e1", "Результат: кандидатів\\nзапропоновано компанії")
    g.page("err", "Помилка 404", None, ["«На панель»"])
    g.close()

    g.cluster("t5", "Завдання: зареєструвати працевлаштування (R7)")
    g.page("apps", "Кандидатури", "/recruiter/applications",
           ["вакансія, компанія, ПІБ, джерело, статус", "фільтр за статусом",
            "«Зареєструвати працевлаштування»"])
    g.decision("dHire", "Форма: дата початку;\\nпідтвердити?")
    g.process("hire", "Запис в історію роботи, закриття\\nвакансії, статус людини «не шукає»")
    g.end("e5", "Результат: «Працевлаштування\\n<ПІБ> … зареєстровано»")
    g.close()

    g.e("st", "panel")
    g.e("panel", "cP", "«Підібрати\\nкандидатів»")
    g.e("cc", "cP", "«Підібрати\\nкандидатів»")
    g.e("cP", "dOpen")
    g.conn("Nt", "Н")
    g.conn("Nb", "Н")
    g.conn("Nb2", "Н")
    g.e("Nt", "panel")
    g.e("dOpen", "Nb", "ні: «Вакансію закрито —\\nпідбір недоступний» (A4)", "c")
    g.e("dOpen", "m1", "так")
    g.e("m1", "dEmp")
    g.e("dEmp", "match", "так")
    g.e("dEmp", "m1", "ні: «Показати часткові\\nзбіги (від 25 %)» (A1)", "c", back=True)
    g.e("match", "dThr", "«Оновити» (A2)")
    g.e("dThr", "m1", "так", back=True)
    g.conn("St", "С")
    g.conn("Sb", "С")
    g.e("St", "match")
    g.e("dThr", "Sb", "ні: діагностика", "c")
    g.e("match", "person", "ПІБ (крок 4)")
    g.e("person", "Sb", "«Назад до підбору»")
    g.e("match", "dConf", "«Запропонувати»")
    g.e("dConf", "Sb", "«Скасувати» (A6)", "c")
    g.e("dConf", "dCom", "«Підтвердити»")
    g.e("dCom", "Sb", "ні: діагностика (A5) /\\n«Кандидатура вже існує» (A3)", "c")
    g.e("dCom", "apc", "так")
    g.e("apc", "Sb", "повідомлення,\\n«Запропоновано: Нова»", "i")
    g.e("match", "e1", "завершено (крок 8)")
    g.e("match", "err", "запис видалено (A7)", "c")
    g.e("err", "Nb2", "«На панель»")
    g.e("panel", "apps", "меню «Кандидатури»")
    g.e("apps", "dHire", "«Зареєструвати\\nпрацевлаштування»")
    g.e("dHire", "apps", "ні", "c", back=True)
    g.e("dHire", "hire", "так", "i")
    g.e("hire", "e5")
    return g


# ---------------------------------------------------------------- рис. 5б рекрутер: дані
def recruiter_data():
    g = G("uf_recruiter_data", nodesep=0.35, ranksep=0.42)
    g.start("st", "Ціль: підтримувати базу людей і компаній,\\nпідбирати вакансії шукачам (після входу)")
    g.page("panel", "Панель рекрутера", "/recruiter", ["меню «Резюме», «Компанії»"])

    g.cluster("t1", "Завдання: знайти людей і вести їхні дані (R1, R4, R5)")
    g.page("people", "Резюме", "/recruiter/people",
           ["фільтри R1: освіта від, компанія, посада,", "зарплата від–до, статус", "«Знайти», «Додати людину»"])
    g.decision("dRes", "Є резюме?")
    g.page("person", "Картка людини", "/recruiter/people/{id}",
           ["особисті дані, освіта, історія роботи, стаж,", "кандидатури людини", "«Редагувати», «Видалити», «Підібрати вакансії»"])
    g.group("pForm", "Нова людина · Редагування людини", "/recruiter/people/new, …/{id}/edit",
            ["резюме, освіта, історія роботи"])
    g.decision("dvP", "Дані\\nкоректні?")
    g.decision("dDelP", "Підтвердити\\nвидалення?")
    g.end("e1", "Результат: дані\\nлюдини актуальні")
    g.close()

    g.cluster("t3", "Завдання: підібрати вакансії шукачу (R3)")
    g.process("m1b", "Оцінювання відповідності (M1)")
    g.page("pm", "Підбір вакансій", "/recruiter/people/{id}/matches",
           ["відкриті вакансії ≥ 50 %: посада,", "компанія, зарплата, оцінка, складові"])
    g.end("e3", "Результат: шукачу\\nпідібрано вакансії")
    g.close()

    g.cluster("t4", "Завдання: вести компанії та їхні вакансії (R6)")
    g.page("comps", "Компанії", "/recruiter/companies", ["пошук за назвою", "назва, місто, відкриті вакансії", "«Додати компанію»"])
    g.page("cc", "Картка компанії", "/recruiter/companies/{id}",
           ["дані компанії, її вакансії", "«Редагувати», «Видалити»", "«Додати вакансію», «Закрити» / «Відкрити»", "«Підібрати кандидатів»"])
    g.group("cForm", "Нова компанія ·\\nРедагування компанії", "/recruiter/companies/new, …/{id}/edit",
            ["назва, місто, адреса, контакти, опис"])
    g.group("vForm", "Нова вакансія компанії ·\\nРедагування вакансії компанії",
            "…/companies/{id}/vacancies/new, /recruiter/vacancies/{id}/edit",
            ["посада, зарплата, мін. освіта,", "мін. досвід, місто, вимоги"])
    g.decision("dvC", "Дані\\nкоректні?")
    g.decision("dvV", "Дані\\nкоректні?")
    g.decision("dDelC", "Підтвердити\\nвидалення?")
    g.conn("cP", "П", note="до рис. 5а")
    g.end("e4", "Результат: дані компаній\\nі вакансій актуальні")
    g.close()

    g.e("st", "panel")
    g.e("panel", "people", "меню «Резюме»")
    g.e("people", "dRes", "«Знайти»")
    g.e("dRes", "person", "так: ПІБ")
    g.e("dRes", "people", "ні: порожній стан,\\n«Скинути фільтри»", "c", back=True)
    g.e("people", "pForm", "«Додати людину»")
    g.e("person", "pForm", "«Редагувати»")
    g.e("pForm", "dvP", "«Зберегти»")
    g.conn("Kt", "К")
    g.conn("Kb", "К")
    g.e("Kt", "person")
    g.e("dvP", "Kb", "так")
    g.e("dvP", "pForm", "ні: діагностика", "c", back=True)
    g.e("person", "dDelP", "«Видалити»")
    g.conn("Lt", "Л")
    g.conn("Lb", "Л")
    g.e("Lt", "people")
    g.e("dDelP", "Lb", "так", "i")
    g.e("dDelP", "Kb", "ні", "c")
    g.e("person", "e1")
    g.e("person", "m1b", "«Підібрати вакансії»")
    g.e("m1b", "pm")
    g.e("pm", "e3")
    g.conn("cP2", "П", note="до рис. 5а")
    g.e("pm", "cP2", "посада вакансії\\n(R2)")
    g.e("panel", "comps", "меню «Компанії»")
    g.e("comps", "cc", "назва")
    g.e("comps", "cForm", "«Додати компанію»")
    g.e("cc", "cForm", "«Редагувати»")
    g.e("cc", "vForm", "«Додати вакансію» /\\n«Редагувати» вакансію")
    g.e("cForm", "dvC", "«Зберегти»")
    g.conn("Ft", "Ф")
    g.conn("Fb", "Ф")
    g.e("Ft", "cc")
    g.e("dvC", "Fb", "так")
    g.e("dvC", "cForm", "ні", "c", back=True)
    g.e("vForm", "dvV", "«Зберегти»")
    g.e("dvV", "Fb", "так")
    g.e("dvV", "vForm", "ні", "c", back=True)
    g.e("cc", "dDelC", "«Видалити»\\n(компанію / вакансію)")
    g.conn("Tt", "Т")
    g.conn("Tb", "Т")
    g.e("Tt", "comps")
    g.e("dDelC", "Tb", "так", "i")
    g.e("dDelC", "Fb", "ні", "c")
    g.e("cc", "cP", "«Підібрати\\nкандидатів»")
    g.e("cc", "e4")
    g.add("Kb -> Tt [style=invis]; Lb -> Tt [style=invis]; dvP -> Tt [style=invis];")
    return g


# ---------------------------------------------------------------- пост-обробка SVG
PT = re.compile(r'points="([^"]+)"')


def _poly(points):
    return [tuple(map(float, p.split(","))) for p in points.split()]


def postprocess(svg: str) -> str:
    out = []
    pos = 0
    for m in re.finditer(r'<g id="[^"]+" class="node (group|process)">(.*?)</g>', svg, flags=re.S):
        kind, body = m.group(1), m.group(2)
        pm = re.search(r'<polygon[^>]*points="([^"]+)"[^>]*/>', body)
        if not pm:
            continue
        pts = _poly(pm.group(1))
        xs, ys = [p[0] for p in pts], [p[1] for p in pts]
        x0, x1, y0, y1 = min(xs), max(xs), min(ys), max(ys)
        if kind == "group":
            extra = ""
            for d in (8, 4):
                shifted = " ".join(f"{x + d:.2f},{y - d:.2f}" for x, y in pts)
                extra += f'<polygon fill="white" stroke="black" stroke-width="1.2" points="{shifted}"/>\n'
            new_body = body[:pm.start()] + extra + body[pm.start():]
        else:
            k = 7
            extra = (f'<line x1="{x0 + k:.2f}" y1="{y0:.2f}" x2="{x0 + k:.2f}" y2="{y1:.2f}" stroke="black" stroke-width="1.2"/>'
                     f'<line x1="{x1 - k:.2f}" y1="{y0:.2f}" x2="{x1 - k:.2f}" y2="{y1:.2f}" stroke="black" stroke-width="1.2"/>\n')
            end = pm.end()
            new_body = body[:end] + extra + body[end:]
        out.append(svg[pos:m.start(2)])
        out.append(new_body)
        pos = m.end(2)
    out.append(svg[pos:])
    return "".join(out)


def render(g: G, zoom=2.5):
    dot = SRC / f"{g.name}.dot"
    svg = SRC / f"{g.name}.svg"
    png = IMG / f"{g.name}.png"
    dot.write_text(g.text(), encoding="utf-8")
    raw = subprocess.run(["dot", "-Tsvg", str(dot)], capture_output=True, check=True).stdout.decode("utf-8")
    svg.write_text(postprocess(raw), encoding="utf-8")
    subprocess.run(["rsvg-convert", "-z", str(zoom), "-b", "white", str(svg), "-o", str(png)], check=True)
    print("UF:", png.name)


def shoot(html: str, w: int, h: int):
    src = SRC / html
    png = IMG / (Path(html).stem + ".png")
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                    "--force-device-scale-factor=2", f"--screenshot={png}", f"--window-size={w},{h}",
                    src.as_uri()], check=True, capture_output=True)
    print("WF:", png.name)


if __name__ == "__main__":
    import sys
    what = sys.argv[1:] or ["uf", "wf"]
    if "uf" in what:
        render_legend()
        for fn in (overview, guest, candidate_a, candidate_b, employer, recruiter_r2, recruiter_data):
            render(fn())
    if "wf" in what:
        for html, w, h in (("wf_main.html", 1180, 1390), ("wf_alt1.html", 1180, 840), ("wf_alt2.html", 1180, 840)):
            if (SRC / html).exists():
                shoot(html, w, h)

"""Формування grid-таблиць pandoc з об'єднаними рядками-заголовками груп.

rows: список рядків; рядок — list[str] (по клітинці на стовпець) або str (рядок на всю ширину).
widths: ширина стовпців у символах (визначає відносну ширину у docx).
"""
import textwrap


def _wrap(text, width):
    text = (text or "").replace("|", "/").replace("\n", " ")
    return textwrap.wrap(text, width, break_long_words=False, break_on_hyphens=False) or [""]


def grid(header, rows, widths):
    # розширити стовпець, якщо в ньому є слово, довше за ширину
    widths = list(widths)
    for r in [header] + [r for r in rows if not isinstance(r, str)]:
        for i, c in enumerate(r):
            for tok in (c or "").split():
                widths[i] = max(widths[i], len(tok))
    total = sum(widths) + 3 * (len(widths) - 1)

    def sep(ch="-", span=False):
        if span:
            return "+" + ch * (total + 2) + "+"
        return "+" + "+".join(ch * (w + 2) for w in widths) + "+"

    def line(cells):
        wrapped = [_wrap(c, w) for c, w in zip(cells, widths)]
        h = max(len(x) for x in wrapped)
        out = []
        for k in range(h):
            out.append("| " + " | ".join((x[k] if k < len(x) else "").ljust(w) for x, w in zip(wrapped, widths)) + " |")
        return out

    def span_line(text):
        return ["| " + s.ljust(total) + " |" for s in _wrap(text, total)]

    out = [sep()] + line(header) + [sep("=")]
    prev_span = False
    for i, r in enumerate(rows):
        if isinstance(r, str):
            out += span_line(r)
            nxt = rows[i + 1] if i + 1 < len(rows) else None
            out.append(sep(span=isinstance(nxt, str)))
            prev_span = True
        else:
            out += line(r)
            out.append(sep())
            prev_span = False
    return "\n".join(out)

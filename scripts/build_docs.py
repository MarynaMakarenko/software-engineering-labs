"""Build lab reports: labN/*.md -> labN/<name>.docx + .pdf

Usage: .venv/bin/python scripts/build_docs.py [lab1 lab2 ...]
Each labN folder contains one or more .md files; the output file name = md file stem.
Images are referenced relative to the md file.
"""
import subprocess
import sys
from pathlib import Path

from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Pt, RGBColor, Cm

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT
REF = ROOT / "scripts" / "reference.docx"
FONT = "Times New Roman"


def make_reference():
    raw = subprocess.run(["pandoc", "--print-default-data-file", "reference.docx"],
                         capture_output=True, check=True).stdout
    REF.write_bytes(raw)
    doc = Document(str(REF))
    for st in doc.styles:
        try:
            f = st.font
        except AttributeError:
            continue
        if f is None:
            continue
        f.name = FONT
        rpr = st.element.get_or_add_rPr()
        rfonts = rpr.find(qn("w:rFonts"))
        if rfonts is None:
            rfonts = OxmlElement("w:rFonts")
            rpr.append(rfonts)
        for a in ("w:ascii", "w:hAnsi", "w:cs", "w:eastAsia"):
            rfonts.set(qn(a), FONT)
        for a in ("w:asciiTheme", "w:hAnsiTheme", "w:cstheme", "w:eastAsiaTheme"):
            if rfonts.get(qn(a)) is not None:
                del rfonts.attrib[qn(a)]
        if st.name.startswith("Heading") or st.name == "Title":
            f.color.rgb = RGBColor(0, 0, 0)
            f.bold = True
    sizes = {"Normal": 13, "Body Text": 13, "First Paragraph": 13, "Compact": 12,
             "Title": 16, "Subtitle": 14, "Heading 1": 15, "Heading 2": 14, "Heading 3": 13,
             "Heading 4": 13, "Source Code": 10, "Table": 11, "Image Caption": 12, "Caption": 12}
    for name, size in sizes.items():
        try:
            doc.styles[name].font.size = Pt(size)
        except KeyError:
            pass
    for name in ("Body Text", "First Paragraph"):
        try:
            pf = doc.styles[name].paragraph_format
            pf.space_after = Pt(4)
            pf.line_spacing = 1.15
        except KeyError:
            pass
    for sec in doc.sections:
        sec.left_margin = Cm(2.5)
        sec.right_margin = Cm(1.5)
        sec.top_margin = Cm(2)
        sec.bottom_margin = Cm(2)
    doc.save(str(REF))


def border_tables(path: Path):
    doc = Document(str(path))
    for t in doc.tables:
        t.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl_pr = t._tbl.tblPr
        borders = OxmlElement("w:tblBorders")
        for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
            el = OxmlElement(f"w:{edge}")
            el.set(qn("w:val"), "single")
            el.set(qn("w:sz"), "4")
            el.set(qn("w:space"), "0")
            el.set(qn("w:color"), "000000")
            borders.append(el)
        old = tbl_pr.find(qn("w:tblBorders"))
        if old is not None:
            tbl_pr.remove(old)
        tbl_pr.append(borders)
        for row in t.rows:
            for cell in row.cells:
                for p in cell.paragraphs:
                    for r in p.runs:
                        r.font.size = Pt(11)
        # header row bold
        if t.rows:
            for cell in t.rows[0].cells:
                for p in cell.paragraphs:
                    for r in p.runs:
                        r.bold = True
    doc.save(str(path))


def build(md: Path):
    docx = md.parent / (md.stem + ".docx")
    subprocess.run(["pandoc", md.name, "-o", str(docx), "--reference-doc", str(REF),
                    "--resource-path", ".", "-f", "markdown+pipe_tables+grid_tables+multiline_tables"],
                   cwd=md.parent, check=True)
    border_tables(docx)
    subprocess.run(["soffice", "--headless", "--convert-to", "pdf", "--outdir", str(md.parent), str(docx)],
                   check=True, capture_output=True)
    print("built", docx.name)


if __name__ == "__main__":
    make_reference()
    labs = sys.argv[1:] or sorted(p.name for p in DOCS.iterdir() if p.is_dir() and p.name.startswith("lab"))
    for lab in labs:
        for md in sorted((DOCS / lab).glob("*.md")):
            if md.name.startswith("_"):
                continue
            build(md)

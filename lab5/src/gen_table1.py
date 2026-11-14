"""Табл. 1 звіту лаб. 5 — модульні тести класів R2 (дані взято з тестів jobmatch/src/test/java/**).

Вставляється у звіт між маркерами <!-- T1:BEGIN --> ... <!-- T1:END -->.
Запуск: .venv/bin/python docs/lab5/src/gen_table1.py
"""
import re
from pathlib import Path

from gridtable import grid

REPORT = Path(__file__).resolve().parent.parent / "Лаба5_Код_Тест_JobMatch_Макаренко.md"

OK, BAD = "*Коректні дані*", "*Некоректні дані*"
D, P = "desiredPosition; position", "desiredSalary; salary"

CLASSES = [
    ("Клас 1 PositionCriterion",
     "критерій «Посада» (55 / 25 / 0) для кроку 3. Змінні: person.desiredPosition, person.workRecords, vacancy.position. "
     "Техніки: еквівалентне розбиття (повний збіг, частковий збіг, немає збігу), граничні значення (спільне слово рівно 4 символи), "
     "припущення про помилки (регістр, підрядок усередині слова).",
     [OK,
      ["evaluate", D, "«Java-розробник»; «Java-розробник»", "55 (точний збіг, типовий)"],
      ["evaluate", D, "«JAVA-РОЗРОБНИК»; «java-розробник»", "55 (регістр не враховується)"],
      ["evaluate", D, "«Java»; «Java-розробник»", "55 (назва входить цілим словом)"],
      ["evaluate", D, "«QA»; «QA-інженер»", "55 (мінімальна назва, 2 символи)"],
      ["evaluate", D, "«Розробник ПЗ»; «Java-розробник»", "25 (спільне слово ≥ 4 символів)"],
      ["evaluate", D, "«Java-тестувальник»; «Java-розробник»", "25 (спільне слово «java» — рівно 4 символи, межа)"],
      ["evaluate", "desiredPosition; workRecords; position", "null; [«Java-розробник» з 01.01.2020]; «Java-розробник»", "55 (посада з історії роботи)"],
      BAD,
      ["evaluate", D, "«Агроном»; «Java-розробник»", "0 (немає збігу)"],
      ["evaluate", D, "«ІТ»; «Архітектор рішень»", "0 («іт» усередині слова не є збігом; регресійний тест дефекту 2)"],
      ["evaluate", "desiredPosition; workRecords; position", "null; []; «Логіст»", "0 (посад немає)"],
      ["compare", "a; b", "«pm lead»; «pm assistant»", "0 (слова коротші за 4 символи не враховуються)"]]),
    ("Клас 2 SalaryCriterion",
     "критерій «Зарплата» (15 / 7 / 0). Змінні: person.desiredSalary, vacancy.salary. "
     "Техніки: граничні значення (100 %, 115 %), еквівалентне розбиття.",
     [OK,
      ["evaluate", P, "1000; 1000", "15 (мінімальні значення, рівність)"],
      ["evaluate", P, "85000; 85000", "15 (рівність, типовий)"],
      ["evaluate", P, "null; 5000", "15 (бажану зарплату не вказано)"],
      ["evaluate", P, "85001; 85000", "7 (на 1 грн вище)"],
      ["evaluate", P, "97750; 85000", "7 (рівно 115 %, межа)"],
      BAD,
      ["evaluate", P, "97751; 85000", "0 (115 % + 1 грн)"],
      ["evaluate", P, "1000000; 1000", "0 (максимальна бажана при мінімальній запропонованій; без переповнення int)"]]),
    ("Клас 3 EducationCriterion",
     "критерій «Освіта» (15 / 0). Змінні: рівні освіти людини (person.educations), vacancy.requiredEducation. "
     "Техніки: еквівалентне розбиття за рівнями, граничні значення (рівний рівень, на один нижче).",
     [OK,
      ["evaluate", "educations; requiredEducation", "[Бакалавр]; Бакалавр", "15 (рівність, межа)"],
      ["evaluate", "educations; requiredEducation", "[Магістр]; Бакалавр", "15"],
      ["evaluate", "educations; requiredEducation", "[PhD]; PhD", "15 (максимальний рівень)"],
      ["evaluate", "educations; requiredEducation", "[]; null", "15 (вимоги немає)"],
      ["evaluate", "educations; requiredEducation", "[Бакалавр, Магістр]; Магістр", "15 (береться найвищий рівень)"],
      BAD,
      ["evaluate", "educations; requiredEducation", "[Середня]; Професійно-технічна", "0 (на один рівень нижче)"],
      ["evaluate", "educations; requiredEducation", "[Бакалавр]; Магістр", "0"],
      ["evaluate", "educations; requiredEducation", "[]; Середня", "0 (освіту не вказано)"]]),
    ("Клас 4 ExperienceCriterion",
     "критерій «Досвід» (15 / 7 / 0). Змінні: стаж з person.workRecords на дату today = 14.11.2026, "
     "vacancy.requiredExperienceYears. Техніки: граничні значення (0, рівно вимога, рівно половина, 40 років).",
     [OK,
      ["evaluate", "стаж; requiredExperienceYears", "0; 0", "15 (мінімальні значення)"],
      ["evaluate", "стаж; requiredExperienceYears", "3 р. (14.11.2023–14.11.2026); 3", "15 (рівно вимога)"],
      ["evaluate", "стаж; requiredExperienceYears", "40 р. (поточний запис, endDate = null); 40", "15 (максимальна вимога; поточний запис — до сьогодні)"],
      ["evaluate", "стаж; requiredExperienceYears", "18 міс.; 3", "7 (рівно половина, межа)"],
      BAD,
      ["evaluate", "стаж; requiredExperienceYears", "17 міс.; 3", "0 (на місяць менше половини)"]]),
    ("Клас 5 MatchingService",
     "Facade M1: оцінка пари «резюме — вакансія» і підбір кандидатів (R2, кроки 2–3, A1, A2); репозиторії — Mockito. "
     "Техніки: еквівалентне розбиття, граничні значення порогу, припущення про помилки (порожній список, сортування).",
     [OK,
      ["evaluate", "person; vacancy", "Java-розробник, 80 000, Бакалавр, 5 р.; Java-розробник, 85 000, Бакалавр, 3 р.", "score = 100; складові [55, 15, 15, 15]"],
      ["evaluate", "person; vacancy", "Агроном, 30 000, Магістр, 10 р.; та сама вакансія", "score = 45 < 50 (без збігу посади — нижче порогу; регресійний тест дефекту 1)"],
      ["findCandidates", "люди «шукає»; minScore", "[15 %, 77 %, 100 %]; 50", "[100, 77] за спаданням; fetchWorkRecords викликано один раз"],
      ["findCandidates", "люди «шукає»; minScore", "[Агроном, 15 %]; 50 і 0", "[] (A1) і 1 результат"],
      ["findCandidates", "люди «шукає»; minScore", "[]; 50", "[]; fetchWorkRecords не викликано"],
      ["findVacancies", "відкриті вакансії; minScore", "[QA-інженер, Java-розробник]; 50", "лише «Java-розробник» (id 1)"],
      ["validateThreshold (через findCandidates)", "minScore", "0, 1, 50, 99, 100", "виняток не виникає; 1 результат"],
      BAD,
      ["validateThreshold (через findCandidates)", "minScore", "−1, 101, Integer.MIN_VALUE, Integer.MAX_VALUE", "IllegalArgumentException"]]),
    ("Клас 6 ApplicationService",
     "створення кандидатури рекрутером (R2, кроки 5–7, A3, A4, A5, A7) і реєстрація працевлаштування (R7); репозиторії — Mockito. "
     "Техніки: граничні значення довжини коментаря і дати, припущення про помилки (дублікат, відсутні записи).",
     [OK,
      ["propose", "vacancyId; personId; comment", "1; 3; null", "кандидатура NEW, джерело RECRUITER, коментар null; save викликано"],
      ["propose", "vacancyId; personId; comment", "1; 3; «a» (1 символ)", "кандидатуру збережено з коментарем «a»"],
      ["propose", "vacancyId; personId; comment", "1; 3; «к» × 500", "кандидатуру збережено, коментар 500 символів"],
      ["hire", "applicationId; startDate", "7 (статус INVITED); сьогодні + 90 днів", "статус HIRED; вакансію закрито; людина «не шукає»; один поточний запис роботи «Java-розробник»"],
      BAD,
      ["propose", "comment", "«к» × 501", "BusinessException «…500…»; save не викликано (A5)"],
      ["propose", "existsByVacancyIdAndPersonId", "true", "BusinessException «…вже існує»; save не викликано (A3)"],
      ["propose", "vacancy.status", "CLOSED", "BusinessException «…закрито…» (A4)"],
      ["propose", "vacancyId", "99 (немає в БД)", "NotFoundException (A7)"],
      ["propose", "personId", "99 (немає в БД)", "NotFoundException (A7)"],
      ["hire", "статус кандидатури", "NEW", "BusinessException «…«Запрошено»…»"],
      ["hire", "startDate", "сьогодні + 91 день", "BusinessException; вакансія лишається відкритою"]]),
    ("Клас 7 Person, Application",
     "стаж і освіта кандидата для таблиці підбору (крок 3) і картки (крок 4), статуси кандидатури. "
     "Техніки: граничні значення (немає записів), припущення про помилки (поточний запис без дати завершення).",
     [OK,
      ["experienceText, experienceYears", "workRecords; today", "01.07.2019–31.03.2022 (32 міс.) і 01.04.2022–досі (55 міс.); 14.11.2026", "«7 р. 3 міс.»; 7.3"],
      ["experienceText", "workRecords", "[]", "«0 р. 0 міс.»"],
      ["highestEducation", "educations", "[]", "Optional.empty()"],
      ["hire", "поточний запис; company, position, salary, startDate", "«Old» з 01.01.2020; ТехноСофт, «New», 50000, 14.11.2026", "2 записи, поточний лише «New»; isSearching() = false"],
      BAD,
      ["Application.changeStatus", "поточний; новий статус", "REJECTED; INVITED", "IllegalStateException (остаточний статус не змінюється)"]]),
]


def build():
    rows = []
    for name, purpose, body in CLASSES:
        rows.append(f"**{name}** — {purpose}")
        rows.extend(body)
    return grid(["Метод", "Ім'я змінної", "Значення змінної", "Очікувана реакція методу"], rows, [14, 20, 30, 28])


def main():
    text = REPORT.read_text(encoding="utf-8")
    pat = re.compile(r"(<!-- T1:BEGIN -->\n).*?(<!-- T1:END -->)", re.S)
    if not pat.search(text):
        raise SystemExit("маркер T1 не знайдено")
    text = pat.sub(lambda m: m.group(1) + build() + "\n" + m.group(2), text)
    REPORT.write_text(text, encoding="utf-8")
    print("T1:", sum(1 for _, _, b in CLASSES for r in b if not isinstance(r, str)), "рядків")


if __name__ == "__main__":
    main()

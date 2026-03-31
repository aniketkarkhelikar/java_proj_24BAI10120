# Student Expense Tracker (Java BYOP)

## Overview
The Student Expense Tracker is an advanced command-line Java application designed to help students manage their daily financial expenditures efficiently. It supports adding, viewing, editing, deleting, searching, and categorising expenses, complete with date tracking, notes, configurable budget limits, and a visual spending summary.

This project was developed as the Bring Your Own Project (BYOP) capstone activity for the Programming in Java course.

---

## Problem Statement
Managing daily expenses is a common challenge for university students. Without proper tracking, it becomes difficult to understand spending patterns, adhere to a budget, or control unnecessary expenses.

**Solution:** This project provides a powerful yet lightweight local solution using Object-Oriented Java concepts, dynamic data structures, persistent file I/O, and date-aware budget analysis.

---

## Features
* **Add Expense** – record name, amount, category, date (defaults to today), and optional notes
* **View All Expenses** – sorted by date (latest first), with formatted tabular display
* **View by Category** – filter expenses for a specific category with budget remaining shown
* **Edit Expense** – modify any field of an existing expense in-place
* **Delete Expense** – remove an expense by number with confirmation
* **Category Summary & Budget Status** – per-category spending vs. configurable limits plus a text-based spending-distribution bar chart
* **Set Budget Limits** – customise the overall monthly budget and per-category limits, persisted across sessions
* **Search Expenses** – keyword search across name, category, and notes
* **Persistent storage** – expenses saved in `expenses.txt`; budget config saved in `budget_config.txt`
* **Backward-compatible file loading** – seamlessly reads the older 3-field expense format
* **Robust input handling** – no `nextInt()` pitfalls; all input read as strings and parsed safely

---

## Categories
`Food` | `Travel` | `Shopping` | `Entertainment` | `Education` | `Others`

---

## Technologies & Concepts Used
* Java (Core Concepts & Application Structure)
* Encapsulation – private fields with getters/setters
* Collections Framework – `ArrayList`, `LinkedHashMap`
* Java Streams – filtering, mapping, aggregation
* `java.time.LocalDate` / `DateTimeFormatter` – date handling
* Exception Handling – `try-catch`, `NumberFormatException`, `DateTimeParseException`
* File Handling – `FileWriter`, `Scanner`, `File`

---

## How to Run the Project

### 1. Clone the Repository
```bash
git clone git@github.com:YOUR_USERNAME/java_proj_24BAI10120.git
cd java_proj_24BAI10120
```

### 2. Compile the Code
Navigate to the `src` folder and compile:
```bash
cd src
javac Main.java Expense.java
```

### 3. Run the Program
```bash
java Main
```

---

## Menu
```
============================================
        Student Expense Tracker
============================================
1. Add Expense
2. View All Expenses
3. View by Category
4. Edit Expense
5. Delete Expense
6. Category Summary & Budget Status
7. Set Budget Limits
8. Search Expenses
9. Exit
```

*Expenses are automatically saved to `expenses.txt` and budget settings to `budget_config.txt` in the runtime directory.*

---

## Data Files
| File | Purpose |
|------|---------|
| `expenses.txt` | Persists all expenses (`name,amount,category,date,notes`) |
| `budget_config.txt` | Persists monthly budget and category limits |

---

## Author
* **Name:** Aniket Atul Karkhelikar
* **Registration Number:** 24BAI10120
* **Course Activity:** Programming in Java – Bring Your Own Project (BYOP)

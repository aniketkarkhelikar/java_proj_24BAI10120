import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Main {

    static final String FILE_NAME = "expenses.txt";
    static final String CONFIG_FILE = "budget_config.txt";

    static Map<String, Double> categoryLimits = new LinkedHashMap<>();
    static double monthlyBudget = 3000.0;

    static {
        categoryLimits.put("Food", 500.0);
        categoryLimits.put("Travel", 200.0);
        categoryLimits.put("Shopping", 1000.0);
        categoryLimits.put("Entertainment", 300.0);
        categoryLimits.put("Education", 500.0);
        categoryLimits.put("Others", 300.0);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Expense> expenses = new ArrayList<>();

        loadExpenses(expenses);
        loadConfig();

        while (true) {
            System.out.println("\n============================================");
            System.out.println("        Student Expense Tracker");
            System.out.println("============================================");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. View by Category");
            System.out.println("4. Edit Expense");
            System.out.println("5. Delete Expense");
            System.out.println("6. Category Summary & Budget Status");
            System.out.println("7. Set Budget Limits");
            System.out.println("8. Search Expenses");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input! Please enter a number between 1 and 9.");
                continue;
            }

            switch (choice) {
                case 1: addExpense(scanner, expenses); break;
                case 2: viewAllExpenses(expenses); break;
                case 3: viewByCategory(scanner, expenses); break;
                case 4: editExpense(scanner, expenses); break;
                case 5: deleteExpense(scanner, expenses); break;
                case 6: showSummary(expenses); break;
                case 7: setBudgetLimits(scanner); break;
                case 8: searchExpenses(scanner, expenses); break;
                case 9:
                    System.out.println("Goodbye! Keep tracking your expenses!");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice! Please select between 1 and 9.");
            }
        }
    }

    // ── Add ──────────────────────────────────────────────────────────────────

    static void addExpense(Scanner scanner, ArrayList<Expense> expenses) {
        System.out.print("Enter expense name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }

        double amount = 0;
        while (true) {
            System.out.print("Enter amount (Rs.): ");
            try {
                amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount <= 0) { System.out.println("Amount must be positive."); continue; }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        }

        System.out.println("Categories: Food | Travel | Shopping | Entertainment | Education | Others");
        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        if (!categoryLimits.containsKey(category)) {
            System.out.println("Unknown category. Saved as 'Others'.");
            category = "Others";
        }

        System.out.print("Enter date (YYYY-MM-DD) or press Enter for today: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate date;
        if (dateStr.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr, Expense.DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Using today's date.");
                date = LocalDate.now();
            }
        }

        System.out.print("Enter notes (optional, press Enter to skip): ");
        String notes = scanner.nextLine().trim();

        Expense e = new Expense(name, amount, category, date, notes);
        expenses.add(e);
        saveAllExpenses(expenses);
        System.out.println("Expense added successfully!");

        // Category budget warning
        final String finalCategory = category;
        double catTotal = expenses.stream()
                .filter(ex -> ex.getCategory().equals(finalCategory))
                .mapToDouble(Expense::getAmount).sum();
        double catLimit = categoryLimits.get(finalCategory);
        if (catTotal > catLimit) {
            System.out.printf("Warning: Total %s spending (Rs.%.2f) has exceeded the limit of Rs.%.2f!%n",
                    finalCategory, catTotal, catLimit);
        } else {
            System.out.printf("  Budget remaining for %s: Rs.%.2f%n", finalCategory, catLimit - catTotal);
        }

        // Monthly budget warning
        String month = monthKey(date);
        double monthTotal = expenses.stream()
                .filter(ex -> monthKey(ex.getDate()).equals(month))
                .mapToDouble(Expense::getAmount).sum();
        if (monthTotal > monthlyBudget) {
            System.out.printf("Warning: Monthly spending (Rs.%.2f) has exceeded your budget of Rs.%.2f!%n",
                    monthTotal, monthlyBudget);
        } else {
            System.out.printf("  Monthly budget remaining: Rs.%.2f%n", monthlyBudget - monthTotal);
        }
    }

    // ── View All ─────────────────────────────────────────────────────────────

    static void viewAllExpenses(ArrayList<Expense> expenses) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }
        expenses.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        double total = 0;
        System.out.println("\n--- All Expenses (Latest First) ---");
        System.out.printf("%-4s %-22s %-12s %-15s %-12s %s%n",
                "#", "Name", "Amount", "Category", "Date", "Notes");
        System.out.println("-".repeat(82));
        for (int i = 0; i < expenses.size(); i++) {
            Expense exp = expenses.get(i);
            System.out.printf("%-4d %-22s Rs.%-9.2f %-15s %-12s %s%n",
                    (i + 1), truncate(exp.getName(), 22), exp.getAmount(),
                    exp.getCategory(), exp.getDate(), exp.getNotes());
            total += exp.getAmount();
        }
        System.out.println("-".repeat(82));
        System.out.printf("Total Spending: Rs.%.2f  |  %d expense(s)%n", total, expenses.size());
    }

    // ── View by Category ─────────────────────────────────────────────────────

    static void viewByCategory(Scanner scanner, ArrayList<Expense> expenses) {
        System.out.println("Categories: Food | Travel | Shopping | Entertainment | Education | Others");
        System.out.print("Enter category to filter: ");
        String category = scanner.nextLine().trim();

        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getCategory().equalsIgnoreCase(category)) filtered.add(e);
        }
        if (filtered.isEmpty()) {
            System.out.println("No expenses found for category: " + category);
            return;
        }
        filtered.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        double total = 0;
        System.out.println("\n--- Expenses: " + category + " ---");
        System.out.printf("%-4s %-22s %-12s %-12s %s%n", "#", "Name", "Amount", "Date", "Notes");
        System.out.println("-".repeat(65));
        for (int i = 0; i < filtered.size(); i++) {
            Expense exp = filtered.get(i);
            System.out.printf("%-4d %-22s Rs.%-9.2f %-12s %s%n",
                    (i + 1), truncate(exp.getName(), 22), exp.getAmount(),
                    exp.getDate(), exp.getNotes());
            total += exp.getAmount();
        }
        System.out.println("-".repeat(65));
        System.out.printf("Total for %s: Rs.%.2f%n", category, total);
        if (categoryLimits.containsKey(category)) {
            double limit = categoryLimits.get(category);
            double remaining = limit - total;
            if (remaining >= 0) {
                System.out.printf("Budget remaining: Rs.%.2f / Rs.%.2f%n", remaining, limit);
            } else {
                System.out.printf("Over budget by: Rs.%.2f (Limit: Rs.%.2f)%n", -remaining, limit);
            }
        }
    }

    // ── Edit ─────────────────────────────────────────────────────────────────

    static void editExpense(Scanner scanner, ArrayList<Expense> expenses) {
        if (expenses.isEmpty()) { System.out.println("No expenses to edit."); return; }
        viewAllExpenses(expenses);
        System.out.print("\nEnter expense number to edit (0 to cancel): ");
        int index;
        try {
            index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException ex) { System.out.println("Invalid input."); return; }
        if (index == -1) { System.out.println("Cancelled."); return; }
        if (index < 0 || index >= expenses.size()) { System.out.println("Invalid number."); return; }

        Expense e = expenses.get(index);
        System.out.println("Editing: " + e);
        System.out.println("(Press Enter to keep the current value)");

        System.out.print("New name [" + e.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) e.setName(name);

        System.out.print("New amount [" + e.getAmount() + "]: ");
        String amtStr = scanner.nextLine().trim();
        if (!amtStr.isEmpty()) {
            try {
                double amt = Double.parseDouble(amtStr);
                if (amt > 0) e.setAmount(amt);
                else System.out.println("Invalid amount; keeping original.");
            } catch (NumberFormatException ex) { System.out.println("Invalid amount; keeping original."); }
        }

        System.out.println("Categories: Food | Travel | Shopping | Entertainment | Education | Others");
        System.out.print("New category [" + e.getCategory() + "]: ");
        String cat = scanner.nextLine().trim();
        if (!cat.isEmpty()) {
            if (categoryLimits.containsKey(cat)) e.setCategory(cat);
            else System.out.println("Unknown category; keeping original.");
        }

        System.out.print("New date (YYYY-MM-DD) [" + e.getDate() + "]: ");
        String dateStr = scanner.nextLine().trim();
        if (!dateStr.isEmpty()) {
            try { e.setDate(LocalDate.parse(dateStr, Expense.DATE_FMT)); }
            catch (DateTimeParseException ex) { System.out.println("Invalid date; keeping original."); }
        }

        System.out.print("New notes [" + e.getNotes() + "]: ");
        String notes = scanner.nextLine().trim();
        if (!notes.isEmpty()) e.setNotes(notes);

        saveAllExpenses(expenses);
        System.out.println("Expense updated successfully!");
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    static void deleteExpense(Scanner scanner, ArrayList<Expense> expenses) {
        if (expenses.isEmpty()) { System.out.println("Nothing to delete."); return; }
        viewAllExpenses(expenses);
        System.out.print("\nEnter expense number to delete (0 to cancel): ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim());
            if (index == 0) { System.out.println("Cancelled."); return; }
            if (index > 0 && index <= expenses.size()) {
                String deletedName = expenses.get(index - 1).getName();
                expenses.remove(index - 1);
                saveAllExpenses(expenses);
                System.out.println("Deleted: " + deletedName);
            } else {
                System.out.println("Invalid number.");
            }
        } catch (NumberFormatException ex) { System.out.println("Invalid input."); }
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    static void showSummary(ArrayList<Expense> expenses) {
        if (expenses.isEmpty()) { System.out.println("No expenses recorded."); return; }

        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        String currentMonth = monthKey(LocalDate.now());
        double thisMonthTotal = expenses.stream()
                .filter(e -> monthKey(e.getDate()).equals(currentMonth))
                .mapToDouble(Expense::getAmount).sum();

        System.out.println("\n============================================");
        System.out.println("     Expense Summary & Budget Status");
        System.out.println("============================================");
        System.out.printf("Total All-Time Spending : Rs.%.2f%n", total);
        System.out.printf("This Month's Spending   : Rs.%.2f / Rs.%.2f%n", thisMonthTotal, monthlyBudget);
        if (thisMonthTotal > monthlyBudget) {
            System.out.printf("Over monthly budget by Rs.%.2f!%n", thisMonthTotal - monthlyBudget);
        } else {
            System.out.printf("Monthly budget remaining: Rs.%.2f%n", monthlyBudget - thisMonthTotal);
        }

        // Per-category totals
        Map<String, Double> catTotals = new LinkedHashMap<>();
        for (String cat : categoryLimits.keySet()) catTotals.put(cat, 0.0);
        for (Expense e : expenses) catTotals.merge(e.getCategory(), e.getAmount(), Double::sum);

        System.out.println("\n--- Category Breakdown ---");
        System.out.printf("%-16s %-12s %-12s %-12s %s%n",
                "Category", "Spent", "Limit", "Remaining", "Status");
        System.out.println("-".repeat(65));
        for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
            String cat = entry.getKey();
            double spent = entry.getValue();
            double limit = categoryLimits.getOrDefault(cat, 0.0);
            double remaining = limit - spent;
            String status = remaining >= 0 ? "OK" : "OVER LIMIT";
            System.out.printf("%-16s Rs.%-9.2f Rs.%-9.2f Rs.%-9.2f %s%n",
                    cat, spent, limit, remaining, status);
        }
        System.out.println("-".repeat(65));

        // Spending distribution bar chart
        System.out.println("\n--- Spending Distribution ---");
        for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
            if (entry.getValue() > 0) {
                int barLen = (int) (entry.getValue() / total * 30);
                String bar = "#".repeat(barLen);
                System.out.printf("%-16s [%-30s] %.1f%%%n",
                        entry.getKey(), bar, (entry.getValue() / total * 100));
            }
        }
    }

    // ── Set Budget Limits ────────────────────────────────────────────────────

    static void setBudgetLimits(Scanner scanner) {
        System.out.println("\n--- Set Budget Limits ---");
        System.out.printf("Current monthly budget: Rs.%.2f%n", monthlyBudget);
        System.out.print("Enter new monthly budget (press Enter to keep): ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                double newBudget = Double.parseDouble(input);
                if (newBudget > 0) { monthlyBudget = newBudget; System.out.println("Monthly budget updated."); }
                else System.out.println("Budget must be positive.");
            } catch (NumberFormatException e) { System.out.println("Invalid; keeping current budget."); }
        }

        System.out.println("\nCurrent category limits:");
        for (Map.Entry<String, Double> entry : categoryLimits.entrySet()) {
            System.out.printf("  %-15s Rs.%.2f%n", entry.getKey(), entry.getValue());
        }

        System.out.println("\nEnter category name to update (or press Enter to finish):");
        while (true) {
            System.out.print("Category: ");
            String cat = scanner.nextLine().trim();
            if (cat.isEmpty()) break;
            if (!categoryLimits.containsKey(cat)) {
                System.out.println("Unknown category. Available: "
                        + String.join(" | ", categoryLimits.keySet()));
                continue;
            }
            System.out.printf("Current limit for %s: Rs.%.2f. New limit: ", cat, categoryLimits.get(cat));
            try {
                double newLimit = Double.parseDouble(scanner.nextLine().trim());
                if (newLimit > 0) {
                    categoryLimits.put(cat, newLimit);
                    System.out.printf("%s limit updated to Rs.%.2f%n", cat, newLimit);
                } else { System.out.println("Limit must be positive."); }
            } catch (NumberFormatException e) { System.out.println("Invalid input."); }
        }
        saveConfig();
        System.out.println("Budget settings saved.");
    }

    // ── Search ───────────────────────────────────────────────────────────────

    static void searchExpenses(Scanner scanner, ArrayList<Expense> expenses) {
        System.out.print("Enter search keyword (matches name, category, or notes): ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        if (keyword.isEmpty()) { System.out.println("No keyword entered."); return; }

        List<Expense> results = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getName().toLowerCase().contains(keyword)
                    || e.getCategory().toLowerCase().contains(keyword)
                    || e.getNotes().toLowerCase().contains(keyword)) {
                results.add(e);
            }
        }
        if (results.isEmpty()) {
            System.out.println("No expenses found matching: " + keyword);
            return;
        }
        results.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        System.out.println("\n--- Search Results for: \"" + keyword + "\" ---");
        double total = 0;
        for (int i = 0; i < results.size(); i++) {
            Expense e = results.get(i);
            System.out.printf("%d. %-22s Rs.%-9.2f %-15s %-12s %s%n",
                    (i + 1), truncate(e.getName(), 22), e.getAmount(),
                    e.getCategory(), e.getDate(), e.getNotes());
            total += e.getAmount();
        }
        System.out.printf("Total: Rs.%.2f  |  %d result(s)%n", total, results.size());
    }

    // ── File I/O ─────────────────────────────────────────────────────────────

    public static void saveAllExpenses(ArrayList<Expense> expenses) {
        try (FileWriter fw = new FileWriter(FILE_NAME)) {
            for (Expense e : expenses) {
                fw.write(e.toFileString() + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Error saving expenses to " + FILE_NAME + ": " + ex.getMessage());
        }
    }

    public static void loadExpenses(ArrayList<Expense> expenses) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length == 3) {
                    // Legacy format: name,amount,category
                    expenses.add(new Expense(parts[0],
                            Double.parseDouble(parts[1]), parts[2], LocalDate.now(), ""));
                } else if (parts.length >= 5) {
                    // New format: name,amount,category,date,notes
                    expenses.add(new Expense(parts[0],
                            Double.parseDouble(parts[1]), parts[2],
                            LocalDate.parse(parts[3], Expense.DATE_FMT), parts[4]));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading expenses from " + FILE_NAME + ": " + e.getMessage());
        }
    }

    static void saveConfig() {
        try (FileWriter fw = new FileWriter(CONFIG_FILE)) {
            fw.write("monthly_budget=" + monthlyBudget + "\n");
            for (Map.Entry<String, Double> entry : categoryLimits.entrySet()) {
                fw.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving budget configuration to " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    if (parts[0].equals("monthly_budget")) {
                        monthlyBudget = Double.parseDouble(parts[1]);
                    } else if (categoryLimits.containsKey(parts[0])) {
                        categoryLimits.put(parts[0], Double.parseDouble(parts[1]));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading budget configuration from " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    static String monthKey(LocalDate d) {
        return d.getYear() + "-" + String.format("%02d", d.getMonthValue());
    }

    static String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + ".." : s;
    }
}

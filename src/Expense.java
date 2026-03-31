import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Expense {
    private String name;
    private double amount;
    private String category;
    private LocalDate date;
    private String notes;

    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Expense(String name, double amount, String category, LocalDate date, String notes) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = (notes == null) ? "" : notes;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** Serialises the expense to a CSV line safe for storage. */
    public String toFileString() {
        String safeName = name.replace(",", ";");
        String safeNotes = notes.replace(",", ";");
        return safeName + "," + amount + "," + category + ","
                + date.format(DATE_FMT) + "," + safeNotes;
    }

    @Override
    public String toString() {
        return String.format("%s | Rs.%.2f | %s | %s | %s",
                name, amount, category, date, notes);
    }
}

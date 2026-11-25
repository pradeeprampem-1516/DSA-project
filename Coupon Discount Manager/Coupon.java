import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Stores one coupon and knows whether it is valid and how much it discounts. */
public class Coupon {
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final String code;
    private final String type;      // PERCENT or FLAT
    private final double value;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int usageLimit;
    private int usedCount;

    public Coupon(String code, String type, double value, LocalDate startDate,
                  LocalDate endDate, int usageLimit, int usedCount) {
        this.code = code.trim().toUpperCase();
        this.type = type.trim().toUpperCase();
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
    }

    public String getCode() { return code; }
    public String getType() { return type; }
    public double getValue() { return value; }
    public int getUsedCount() { return usedCount; }
    public int getRemainingUses() { return Math.max(0, usageLimit - usedCount); }
    public void use() { usedCount++; }

    public boolean hasValidType() { return type.equals("PERCENT") || type.equals("FLAT"); }

    /** Valid = type ok, today within start..end (inclusive), and uses left. */
    public boolean isValid(LocalDate today) {
        return hasValidType()
                && !today.isBefore(startDate)
                && !today.isAfter(endDate)
                && usedCount < usageLimit;
    }

    /** Discount for a bill, never more than the bill itself. */
    public double calculateDiscount(double bill) {
        double d = type.equals("PERCENT") ? bill * value / 100.0 : value;
        return Math.min(d, bill);
    }

    /** One line of coupons.txt. */
    public String toFileLine() {
        return String.join(",", code, type, fmt(value), startDate.format(FMT),
                endDate.format(FMT), String.valueOf(usageLimit), String.valueOf(usedCount));
    }

    public static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}

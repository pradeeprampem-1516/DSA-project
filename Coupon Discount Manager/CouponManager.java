import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Manages the BST, the HashMap, greedy selection, reports and file handling. */
public class CouponManager {
    private final CouponBST bst = new CouponBST();
    private final Map<String, Coupon> codeMap = new HashMap<>();
    private final String fileName;
    private final LocalDate today;

    public CouponManager(String fileName) { this(fileName, LocalDate.now()); }

    public CouponManager(String fileName, LocalDate today) {
        this.fileName = fileName;
        this.today = today;
    }

    // ---------- file handling ----------
    public void loadFromFile() {
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("(" + fileName + " not found - starting with no coupons)");
            return;
        }
        int loaded = 0, skipped = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    String[] p = line.split(",");
                    Coupon c = new Coupon(p[0], p[1], Double.parseDouble(p[2]),
                            LocalDate.parse(p[3].trim(), Coupon.FMT),
                            LocalDate.parse(p[4].trim(), Coupon.FMT),
                            Integer.parseInt(p[5].trim()), Integer.parseInt(p[6].trim()));
                    if (c.hasValidType() && addCoupon(c)) loaded++; else skipped++;
                } catch (Exception e) {
                    skipped++;
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
        }
        System.out.println("Loaded " + loaded + " coupon(s)" + (skipped > 0 ? ", skipped " + skipped + " bad line(s)" : "") + ".");
    }

    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("# code,type,value,startDate,endDate,usageLimit,usedCount");
            for (Coupon c : bst.inorder()) pw.println(c.toFileLine());
            System.out.println("Saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Could not save " + fileName + ": " + e.getMessage());
        }
    }

    // ---------- operations ----------
    /** Rejects a duplicate code (HashMap check), else stores in both structures. */
    public boolean addCoupon(Coupon c) {
        if (codeMap.containsKey(c.getCode())) return false;
        bst.insert(c);
        codeMap.put(c.getCode(), c);
        return true;
    }

    /** Applies one coupon. Returns the discount, or -1 if it could not be applied. */
    public double applyCoupon(String code, double bill) {
        code = code.trim().toUpperCase();
        Coupon c = codeMap.get(code);                 // O(1) average
        if (c == null) {
            System.out.println("Coupon " + code + " not found.");
            return -1;
        }
        if (!c.isValid(today)) {
            System.out.println("Coupon " + code + " is not valid (expired, not started or usage limit reached).");
            return -1;
        }
        double d = c.calculateDiscount(bill);
        c.use();
        System.out.println("Discount: " + money(d) + " | Final bill: " + money(bill - d));
        return d;
    }

    /** Greedy: check every valid coupon and take the largest saving. O(n). */
    public double applyBestCoupon(double bill) {
        Coupon best = null;
        double bestDiscount = -1;
        for (Coupon c : bst.inorder()) {
            if (!c.isValid(today)) continue;
            double d = c.calculateDiscount(bill);
            if (d > bestDiscount) { bestDiscount = d; best = c; }
        }
        if (best == null) {
            System.out.println("No valid coupon available.");
            return -1;
        }
        best.use();
        System.out.println("Best coupon: " + best.getCode());
        System.out.println("Best discount applied: " + money(bestDiscount) + " | Final bill: " + money(bill - bestDiscount));
        return bestDiscount;
    }

    /** sortBy: 1 = code, 2 = remaining uses (high to low), 3 = value (high to low). */
    public void printReport(int sortBy) {
        List<Coupon> list = bst.inorder();             // already sorted by code
        if (sortBy == 2) list.sort((a, b) -> b.getRemainingUses() - a.getRemainingUses());
        else if (sortBy == 3) list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (list.isEmpty()) { System.out.println("No coupons."); return; }
        System.out.printf("%-5s %-12s %-8s %-7s %-14s %s%n", "Rank", "Code", "Type", "Value", "Remaining Uses", "Status");
        int rank = 1;
        for (Coupon c : list)
            System.out.printf("%-5d %-12s %-8s %-7s %-14d %s%n", rank++, c.getCode(), c.getType(),
                    Coupon.fmt(c.getValue()), c.getRemainingUses(), c.isValid(today) ? "valid" : "not valid");
    }

    private static String money(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format("%.2f", v);
    }
}

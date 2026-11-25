import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        CouponManager mgr = new CouponManager("coupons.txt");
        mgr.loadFromFile();
        while (true) {
            System.out.println("\n===== Coupon & Discount Manager =====");
            System.out.println("1. Add coupon\n2. Apply coupon\n3. Apply best coupon\n4. View report\n5. Exit (save)");
            System.out.print("Choice: ");
            if (!in.hasNextLine()) { mgr.saveToFile(); return; }
            String ch = in.nextLine().trim();
            switch (ch) {
                case "1": add(mgr); break;
                case "2": {
                    System.out.print("Coupon code: ");
                    String code = in.nextLine();
                    double bill = readBill();
                    if (bill > 0) mgr.applyCoupon(code, bill);
                    break;
                }
                case "3": {
                    double bill = readBill();
                    if (bill > 0) mgr.applyBestCoupon(bill);
                    break;
                }
                case "4": {
                    System.out.print("Sort by 1) code  2) remaining uses  3) value: ");
                    int s = 1;
                    try { s = Integer.parseInt(in.nextLine().trim()); } catch (Exception e) { }
                    mgr.printReport(s);
                    break;
                }
                case "5": mgr.saveToFile(); System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice. Enter 1-5.");
            }
        }
    }

    static double readBill() {
        System.out.print("Bill amount: ");
        try {
            double b = Double.parseDouble(in.nextLine().trim());
            if (b > 0) return b;
        } catch (Exception e) { }
        System.out.println("Invalid bill amount.");
        return -1;
    }

    static void add(CouponManager mgr) {
        try {
            System.out.print("Code: ");           String code = in.nextLine().trim();
            System.out.print("Type (PERCENT/FLAT): "); String type = in.nextLine().trim();
            System.out.print("Value: ");          double val = Double.parseDouble(in.nextLine().trim());
            System.out.print("Start date (d/M/yyyy): "); LocalDate s = LocalDate.parse(in.nextLine().trim(), Coupon.FMT);
            System.out.print("End date (d/M/yyyy): ");   LocalDate e = LocalDate.parse(in.nextLine().trim(), Coupon.FMT);
            System.out.print("Usage limit: ");    int lim = Integer.parseInt(in.nextLine().trim());
            Coupon c = new Coupon(code, type, val, s, e, lim, 0);
            if (code.isEmpty() || !c.hasValidType() || val <= 0 || lim <= 0 || e.isBefore(s)) {
                System.out.println("Invalid coupon details."); return;
            }
            if (c.getType().equals("PERCENT") && val > 100) { System.out.println("Percent cannot exceed 100."); return; }
            if (mgr.addCoupon(c))
                System.out.println("Coupon added: " + c.getCode() + " | " + Coupon.fmt(val) + (c.getType().equals("PERCENT") ? "%" : " flat"));
            else System.out.println("Duplicate code " + c.getCode() + " rejected.");
        } catch (Exception ex) {
            System.out.println("Invalid input, coupon not added.");
        }
    }
}

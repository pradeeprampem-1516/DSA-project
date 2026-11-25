import java.util.ArrayList;
import java.util.List;

/** Binary Search Tree of coupons ordered by code. */
public class CouponBST {
    private BSTNode root;

    /** O(log n) average, O(n) worst case. Duplicate codes are ignored. */
    public void insert(Coupon c) { root = insert(root, c); }

    private BSTNode insert(BSTNode node, Coupon c) {
        if (node == null) return new BSTNode(c);
        int cmp = c.getCode().compareTo(node.coupon.getCode());
        if (cmp < 0) node.left = insert(node.left, c);
        else if (cmp > 0) node.right = insert(node.right, c);
        return node;
    }

    /** Returns the coupon or null if the code is not in the tree. */
    public Coupon search(String code) {
        BSTNode cur = root;
        while (cur != null) {
            int cmp = code.compareTo(cur.coupon.getCode());
            if (cmp == 0) return cur.coupon;
            cur = cmp < 0 ? cur.left : cur.right;
        }
        return null;
    }

    /** Left, node, right: coupons in sorted order of code. O(n). */
    public List<Coupon> inorder() {
        List<Coupon> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    private void inorder(BSTNode node, List<Coupon> out) {
        if (node == null) return;
        inorder(node.left, out);
        out.add(node.coupon);
        inorder(node.right, out);
    }
}

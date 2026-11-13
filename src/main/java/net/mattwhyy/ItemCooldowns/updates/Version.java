package net.mattwhyy.itemcooldowns.updates;

public final class Version {
    private static final int PARTS = 4;

    private Version() {
    }

    public static int compare(String left, String right) {
        int[] a = parse(left);
        int[] b = parse(right);
        for (int i = 0; i < PARTS; i++) {
            if (a[i] != b[i]) {
                return Integer.compare(a[i], b[i]);
            }
        }
        return 0;
    }

    private static int[] parse(String raw) {
        int[] parts = new int[PARTS];
        if (raw == null) {
            return parts;
        }
        String clean = raw.trim();
        if (clean.startsWith("v") || clean.startsWith("V")) {
            clean = clean.substring(1);
        }
        int cut = clean.indexOf('-');
        if (cut >= 0) {
            clean = clean.substring(0, cut);
        }
        cut = clean.indexOf('+');
        if (cut >= 0) {
            clean = clean.substring(0, cut);
        }
        String[] pieces = clean.split("\\.");
        for (int i = 0; i < PARTS && i < pieces.length; i++) {
            try {
                parts[i] = Integer.parseInt(pieces[i].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {
                parts[i] = 0;
            }
        }
        return parts;
    }
}

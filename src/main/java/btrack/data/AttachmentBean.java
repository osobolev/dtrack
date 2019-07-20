package btrack.data;

public final class AttachmentBean {

    public final int id;
    private final String name;
    private final long size;

    public AttachmentBean(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    private static String round1(double x) {
        long x10 = Math.round(x * 10);
        long whole = x10 / 10;
        long frac = x10 % 10;
        if (frac == 0) {
            return String.valueOf(whole);
        } else {
            return whole + "." + frac;
        }
    }

    private static String renderSize(long size) {
        if (size < 1024)
            return size + " байт";
        String[] units = {"Кб", "Мб", "Гб"};
        double s = size;
        for (int i = 0; i < units.length; i++) {
            String unit = units[i];
            s /= 1024;
            if (s < 1024 || i == units.length - 1)
                return round1(s) + " " + unit;
        }
        throw new IllegalStateException();
    }

    public String getSize() {
        return renderSize(size);
    }
}

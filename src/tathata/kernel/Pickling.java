package tathata.kernel;

public class Pickling {
    public static String stackTraceToString(Throwable tbl) {
        if (tbl == null) return "";

        StringBuilder sb = new StringBuilder();

        for (StackTraceElement element : tbl.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}

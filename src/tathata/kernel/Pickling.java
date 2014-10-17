package tathata.kernel;

import java.util.function.Function;

public class Pickling {
    public static String stackTraceToString(Throwable tbl) {
        return Pickling.stackTraceToString(tbl, (StackTraceElement element) -> element.toString());
    }

    public static String stackTraceToString(Throwable tbl, Function<StackTraceElement, String> pickler) {
        if (tbl == null) return "";

        StringBuilder sb = new StringBuilder();

        for (StackTraceElement element : tbl.getStackTrace()) {
            sb.append(pickler.apply(element));
            sb.append("\n");
        }

        return sb.toString();
    }
}

package tathata.kernel;

public class Pickling {
    public String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}

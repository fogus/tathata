package tathata;

import tathata.kernel.Pickling;

public class Main {

    public static void main(String[] args) {
	    Explosion ex = new Explosion();

        System.out.println("null exception is: " + Pickling.stackTraceToString(null));

        System.out.println("This is what happened:");

        try {
            ex.foo(0l);
        }
        catch (Exception e) {
            System.out.println(Pickling.stackTraceToString(e));
        }

    }

    static class Explosion {
        public int foo(Long f) {
            return this.bar(f).hashCode();
        }

        public String bar(Long b) {
            return baz(b.toString());
        }

        private String baz(String s) {
            if (s.equals("0")) throw new RuntimeException("You gave a zero");
            return s.concat("---");
        }

    }
}

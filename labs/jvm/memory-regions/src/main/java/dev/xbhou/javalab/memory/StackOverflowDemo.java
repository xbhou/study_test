package dev.xbhou.javalab.memory;

public class StackOverflowDemo {

    private static int depth;

    public static void main(String[] args) {
        try {
            recurse();
        } catch (StackOverflowError error) {
            System.out.println("caught: " + error.getClass().getSimpleName());
            System.out.println("approximate recursion depth=" + depth);
        }
    }

    private static void recurse() {
        depth++;

        long a = depth;
        long b = a + 1;
        long c = b + 1;
        long d = c + 1;

        if (d == Long.MIN_VALUE) {
            System.out.println("unreachable");
        }

        recurse();
    }
}

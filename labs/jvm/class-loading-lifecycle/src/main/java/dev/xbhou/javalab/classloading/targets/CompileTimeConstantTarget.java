package dev.xbhou.javalab.classloading.targets;

public class CompileTimeConstantTarget {

    public static final String VALUE = "CONST";

    static {
        System.out.println(
                "CompileTimeConstantTarget initialized"
        );
    }
}

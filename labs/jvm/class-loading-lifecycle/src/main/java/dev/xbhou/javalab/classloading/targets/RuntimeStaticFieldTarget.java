package dev.xbhou.javalab.classloading.targets;

public class RuntimeStaticFieldTarget {

    public static int VALUE = 42;

    static {
        System.out.println(
                "RuntimeStaticFieldTarget initialized"
        );
    }
}

package dev.xbhou.javalab.classloading.targets;

public class ParentTarget {

    public static int PARENT_VALUE = 10;

    static {
        System.out.println(
                "ParentTarget initialized"
        );
    }
}

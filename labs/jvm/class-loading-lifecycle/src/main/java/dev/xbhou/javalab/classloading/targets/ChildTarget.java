package dev.xbhou.javalab.classloading.targets;

public class ChildTarget extends ParentTarget {

    public static int CHILD_VALUE = 20;

    static {
        System.out.println(
                "ChildTarget initialized"
        );
    }
}

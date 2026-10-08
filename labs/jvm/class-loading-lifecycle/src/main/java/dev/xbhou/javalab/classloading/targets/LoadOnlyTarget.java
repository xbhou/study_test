package dev.xbhou.javalab.classloading.targets;

public class LoadOnlyTarget {

    static {
        System.out.println(
                "LoadOnlyTarget initialized"
        );
    }
}

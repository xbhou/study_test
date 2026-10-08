package dev.xbhou.javalab.classloading;

import dev.xbhou.javalab.classloading.targets.ChildTarget;
import dev.xbhou.javalab.classloading.targets.CompileTimeConstantTarget;
import dev.xbhou.javalab.classloading.targets.RuntimeStaticFieldTarget;

public class App {

    public static void main(String[] args) throws Exception {
        compileTimeConstantScenario();
        runtimeStaticFieldScenario();
        loadClassScenario();
        classForNameScenario();
        parentChildScenario();
    }

    private static void compileTimeConstantScenario() {
        System.out.println("=== Scenario 1: compile-time constant ===");
        System.out.println(
                "value=" + CompileTimeConstantTarget.VALUE
        );
        System.out.println(
                "If no target initialization line appeared, the constant was inlined."
        );
        System.out.println();
    }

    private static void runtimeStaticFieldScenario() {
        System.out.println("=== Scenario 2: non-constant static field ===");
        System.out.println(
                "value=" + RuntimeStaticFieldTarget.VALUE
        );
        System.out.println();
    }

    private static void loadClassScenario() throws Exception {
        System.out.println("=== Scenario 3: ClassLoader.loadClass ===");

        String className =
                "dev.xbhou.javalab.classloading.targets.LoadOnlyTarget";

        ClassLoader loader = App.class.getClassLoader();

        Class<?> type = loader.loadClass(className);

        System.out.println("loaded type: " + type.getName());
        System.out.println(
                "No static initialization should have happened yet."
        );

        System.out.println("initializing the already-loaded class...");
        Class.forName(className, true, loader);
        System.out.println();
    }

    private static void classForNameScenario() throws Exception {
        System.out.println("=== Scenario 4: Class.forName initialize flag ===");

        String className =
                "dev.xbhou.javalab.classloading.targets.ForNameTarget";

        ClassLoader loader = App.class.getClassLoader();

        Class.forName(className, false, loader);

        System.out.println(
                "Class.forName(..., false, ...) completed without initialization."
        );

        Class.forName(className, true, loader);

        System.out.println(
                "Class.forName(..., true, ...) triggered initialization."
        );
        System.out.println();
    }

    private static void parentChildScenario() {
        System.out.println("=== Scenario 5: parent field through child ===");

        System.out.println(
                "parent value via child name=" + ChildTarget.PARENT_VALUE
        );

        System.out.println(
                "ChildTarget should not be initialized yet."
        );

        System.out.println();
        System.out.println("=== Scenario 6: child initialization ===");

        System.out.println(
                "child value=" + ChildTarget.CHILD_VALUE
        );
        System.out.println();
    }
}

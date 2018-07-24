/**
 * @author xiaobin.hou
 * @create 2018-05-10 14:17
 **/
package cn.fraudmetrix.j2se.js.nashorn;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CorrectTest {

    static ExecutorService pool = Executors.newCachedThreadPool();
    static String script = "function xx(a, m, n) { " +
            " var x = a + 1; java.lang.Thread.sleep(2); " +
            " var y = x * 2 + m; java.lang.Thread.sleep(3);" +
            " var z = y * 3 - n; java.lang.Thread.sleep(1);" +
            " return z;" +
            "} " +
            "xx(parameters.b, parameters.c, parameters.d);";

    public static void main(String[] argu) throws ScriptException,FileNotFoundException {

        ScriptEngineManager sm = new ScriptEngineManager();

        NashornScriptEngineFactory factory = null;
        for (ScriptEngineFactory f : sm.getEngineFactories()) {
            if (f.getEngineName().equalsIgnoreCase("Oracle Nashorn")) {
                factory = (NashornScriptEngineFactory)f;
                break;
            }
        }

        String[] stringArray = new String[]{"-doe", "--global-per-engine"};
        ScriptEngine scriptEngine = factory.getScriptEngine(stringArray);
        final CompiledScript compiled = ((Compilable)scriptEngine).compile(script);


        long l0 = System.currentTimeMillis();
        AtomicInteger counter = new AtomicInteger();
        for (int i=0; i<1024; i++) {
            final int v = i;
            pool.submit(() -> {
                Bindings bindings = new SimpleBindings();
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("b", v);
                parameters.put("c", v + 1);
                parameters.put("d", v + 2) ;
                bindings.put("parameters", parameters);
                int expected = getExpected(v, v + 1, v + 2);
                Double o = null;
                try {
                    o = (Double)compiled.eval(bindings);
                    if (o.intValue() == (expected)) {
                        // System.out.println("OK " + o);
                        counter.incrementAndGet();
                    } else {
                        System.err.println("FAILED!!!" + o + " " + (v + 1));
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            });
        }

        pool.shutdown();
        try {
            boolean r = pool.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Terminated " + r);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long l1 = System.currentTimeMillis();
        System.out.println(l1 - l0 + " ms");
        System.out.println("Total OK = " + counter.intValue());
    }

    private static int getExpected(int a, int m, int n) {
        int x = a + 1;
        int y = x * 2 + m;
        int z = y * 3 - n;
        return z;
    }
}
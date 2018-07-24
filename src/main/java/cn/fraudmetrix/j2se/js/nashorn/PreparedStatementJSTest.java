/**
 *
 * 预编译的方式调用JS脚本
 *
 * @author xiaobin.hou
 * @create 2018-05-10 11:26
 **/
package cn.fraudmetrix.j2se.js.nashorn;


import javax.script.*;

public class PreparedStatementJSTest {

    public static void main(String[] args) throws Exception{
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("nashorn");

        StringBuffer scriptBf = new StringBuffer();

        scriptBf.append("function f(i) { return i + 1; };");
        scriptBf.append("f(w)");

        String script = scriptBf.toString();

        long compileStart = System.currentTimeMillis();
        final CompiledScript compiled = ((Compilable) scriptEngine).compile(script);
        System.out.println("compile cost " + (System.currentTimeMillis() - compileStart));

        Bindings bindings = new SimpleBindings();
        bindings.put("w", 10);

        Object result = null;
        try {
            long runStart = System.currentTimeMillis();
            result = compiled.eval(bindings);
            System.out.println("first run cost " + (System.currentTimeMillis() - runStart));
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 20; i++) {
            try {
                Bindings bindingsi = new SimpleBindings();
                bindingsi.put("w", i);
                long runStart = System.currentTimeMillis();
                result = compiled.eval(bindingsi);
                System.out.println(i + " run cost " + (System.currentTimeMillis() - runStart));
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }

        System.out.println(result);

    }
}
/**
 * @author xiaobin.hou
 * @create 2018-05-10 16:36
 **/
package cn.study.jdk.j2se.js.nashorn;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileWriter;
import java.io.IOException;

public class ScriptContextTest {


    public void attributeTest() throws ScriptException {

        ScriptEngine engine = BasicScript.getJavaScriptEngine();
        ScriptContext context = engine.getContext();

        context.setAttribute("name","alex",ScriptContext.GLOBAL_SCOPE);
        context.setAttribute("name","bob",ScriptContext.ENGINE_SCOPE);

        engine.eval("print('hello ' + name)",context);



        System.out.println(context.getAttribute("name"));

        System.out.println(context.getAttribute("name",ScriptContext.GLOBAL_SCOPE));

    }


    public void scriptContextBinding() throws ScriptException{
        ScriptEngine engine = BasicScript.getJavaScriptEngine();

        ScriptContext context = engine.getContext();
        Bindings bindings1 = engine.createBindings();
        System.out.println(bindings1.hashCode());
        Bindings bindings2 = engine.createBindings();
        System.out.println(bindings2.hashCode());

        bindings1.put("name","alex");
        bindings2.put("name","bob");

        engine.eval("print(name)",bindings1);
        engine.eval("print(name)",bindings2);


        context.setBindings(bindings1,ScriptContext.ENGINE_SCOPE);
        context.setBindings(bindings2,ScriptContext.GLOBAL_SCOPE);
        engine.eval("print(name)",context);
    }

    @Test
    public void invokeMethod() throws Exception{

        ScriptEngine engine = BasicScript.getJavaScriptEngine();

        engine.eval("var obj = {inner : function(name){return 'hello ' + name}};");

        Object obj = engine.get("obj");

        NashornScriptEngine nashorn =  (NashornScriptEngine)engine;

        Object result = nashorn.invokeMethod(obj, "inner", "alex");
        System.out.println(result);
    }


    public void scriptToFile() throws IOException,ScriptException {
        ScriptEngine engine = BasicScript.getJavaScriptEngine();

        ScriptContext context = engine.getContext();

        context.setWriter(new FileWriter("/Users/xiaobin.hou/Documents/test/test.txt"));

        engine.eval("print('hello script to file')");
    }
}
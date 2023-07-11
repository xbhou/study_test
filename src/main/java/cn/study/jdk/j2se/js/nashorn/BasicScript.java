/**
 * @author xiaobin.hou
 * @create 2018-05-10 15:16
 **/
package cn.study.jdk.j2se.js.nashorn;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

public class BasicScript {

    public static ScriptEngine getJavaScriptEngine(){
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engineByName = manager.getEngineByName("nashorn");

        return engineByName;
    }

    public static void greet() throws ScriptException,NoSuchMethodException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engineByName = engineManager.getEngineByName("javascript");
        ScriptEngine engineByExtension = engineManager.getEngineByExtension("js");
        ScriptEngine engineByMimeType = engineManager.getEngineByMimeType("text/javascript");

        engineByName.eval("function a(){return 1};var b = 1;");

        NashornScriptEngine nashornScriptEngine = (NashornScriptEngine)engineByName;

        Object a = nashornScriptEngine.invokeFunction("a");

        ScriptContext context = nashornScriptEngine.getContext();
        List<Integer> scopes = context.getScopes();

        nashornScriptEngine.put("","");

        ScriptContext context1 = nashornScriptEngine.getContext();
        context1.getAttribute("",ScriptContext.ENGINE_SCOPE);

        System.out.println(scopes);
        System.out.println(a);
    }

    public static void main(String[] args) {
        try {
            greet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
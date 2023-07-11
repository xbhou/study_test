/**
 * @author xiaobin.hou
 * @create 2018-05-10 15:54
 **/
package cn.study.jdk.j2se.js.nashorn;

import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * 脚本语言绑定对象测试
 */
public class ScriptBindingTest {


    @Test
    public void useCustomBinding() throws ScriptException{

        ScriptEngine scriptEngine = BasicScript.getJavaScriptEngine();

        Bindings bindings = new SimpleBindings();
        bindings.put("name" ,"bob");

        scriptEngine.eval("var message = 'Hello ' + name", bindings);
        Object message1 = scriptEngine.get("message");
        System.out.println(message1);

        scriptEngine.eval("print('hello ' + name);",bindings);

    }



    public void useDefaultBinding() throws ScriptException {

        ScriptEngine scriptEngine = BasicScript.getJavaScriptEngine();

        scriptEngine.put("name","Alex");
        scriptEngine.eval("var message = 'Hello ' + name");

        Object message = scriptEngine.get("message");

        System.out.println(message);
    }


}
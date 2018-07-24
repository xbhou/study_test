/**
 * @author xiaobin.hou
 * @create 2018-05-10 10:02
 **/
package cn.fraudmetrix.j2se.js.Rhino;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class RhinoAPITest{
    public static void main(String[] args) throws Exception{

        ScriptEngineManager manager = new ScriptEngineManager();

        ScriptEngine engine = manager.getEngineByName( "JavaScript" );
        System.out.println( engine.getClass().getName() );
        System.out.println( "Result:" + engine.eval( "function f() { return 1; }; f() + 1;" ) );
    }
}
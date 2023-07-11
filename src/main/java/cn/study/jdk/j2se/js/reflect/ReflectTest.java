/**
 * @author xiaobin.hou
 * @create 2018-05-11 11:01
 **/
package cn.study.jdk.j2se.js.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.*;

public class ReflectTest {

    public static void main(String[] args) {

        Class<? extends Class> aClass = ChinaDuck.class.getClass();

        Class<?>[] interfaces = aClass.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            System.out.println(interfaces[i].getName());
        }

        Class<?> superclass = aClass.getSuperclass();

        System.out.println(superclass.getName());

        System.out.println(ChinaDuck.class.getName());


        int modifiers = aClass.getModifiers();

    }
    
    @Test
    public void invokeMethod() throws NoSuchMethodException,InvocationTargetException,IllegalAccessException {
        Method getDuckColor = ChinaDuck.class.getMethod("getDuckColor");
        Object invoke = getDuckColor.invoke(new ChinaDuck());
        System.out.println(invoke);
    }


    //TODO 获取字段信息失败
    @Test
    public void getSetField() throws IllegalAccessException{

        Field[] declaredFields = ChinaDuck.class.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);

            Object o = declaredFields[i].get(new ChinaDuck()  );

            System.out.println(o);
        }

    }



    public void classMethod() throws ClassNotFoundException{
        Method[] methods = ChinaDuck.class.getMethods();
        for (int i = 0; i < methods.length; i++) {
            System.out.println(methods[i].getName());
            System.out.println(methods[i].getReturnType().getName());

            System.out.println(methods[i].getParameterCount());
        }


        Class<?> forName = Class.forName(ChinaDuck.class.getName());

    }



    public void classConstructor(){
        Constructor<?>[] constructors = ChinaDuck.class.getConstructors();

        for (int i = 0; i < constructors.length; i++) {
            int modifiers = constructors[i].getModifiers();
            if(Modifier.isPublic(modifiers)){
                System.out.println("public Constructor");
            }else{
                System.out.println("private Constructor");
            }

            Class<?>[] parameterTypes = constructors[i].getParameterTypes();

            if (parameterTypes.length > 0){
                StringBuffer bf = new StringBuffer();
                for (int j = 0; j < parameterTypes.length; j++) {
                    bf.append(parameterTypes[j].getName());
                    bf.append(" ");
                }
                System.out.println(bf.toString());
            }else{
                System.out.println("无参构造器");
            }
        }
        try{
            Constructor<ChinaDuck> constructor = ChinaDuck.class.getConstructor();
            ChinaDuck chinaDuck = constructor.newInstance();
            System.out.println(chinaDuck.name);

            Constructor<ChinaDuck> paramConstructor = ChinaDuck.class.getConstructor(String.class);
            ChinaDuck redDuck = paramConstructor.newInstance("red");
            System.out.println(redDuck.getColor());
        }catch (Exception e){
            System.out.println("没有午餐构造器");
        }


    }


    public void classField(){
        int modifiers = ChinaDuck.class.getModifiers();

        if (Modifier.isPrivate(modifiers)){
            System.out.println("private Class");
        }else if (Modifier.isPublic(modifiers)){
            System.out.println("public Class");
        }

        Field[] publicFields = ChinaDuck.class.getFields();
        for (int i = 0;i < publicFields.length; i++){
            String fieldName = publicFields[i].getName();
            Class<?> typeClass = publicFields[i].getType();

            System.out.println(fieldName);
            System.out.println(typeClass.getName());
        }

        Field[] declaredFields = ChinaDuck.class.getDeclaredFields();
        System.out.println("declared field count " + declaredFields.length);

        for (int i = 0; i < declaredFields.length; i++){
            String filedName = declaredFields[i].getName();
            Class<?> filedType = declaredFields[i].getType();
            System.out.println("file name is " + filedName + " and field type is " + filedType.getName());
        }
    }


    public void className(){

        Class<?>[] interfaces = ChinaDuck.class.getInterfaces();

        for(int i = 0;i < interfaces.length ; i++){
            System.out.println(interfaces[i].getName());
        }

        ChinaDuck.class.getSuperclass();
        System.out.println(ChinaDuck.class.getName());

    }


}
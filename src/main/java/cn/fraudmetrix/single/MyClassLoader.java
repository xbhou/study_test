/**
 * @author xiaobin.hou
 * @create 2018-07-31 14:41
 **/
package cn.fraudmetrix.single;

import java.io.*;

public class MyClassLoader extends ClassLoader {

    private String loadPath;

    MyClassLoader(ClassLoader cl){
        super(cl);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        FileInputStream fis = null;
        byte[] data = null;
        ByteArrayOutputStream baos = null;

        try {
            fis = new FileInputStream(new File(loadPath + className.replaceAll(".","/") + ".class"));
            baos = new ByteArrayOutputStream();
            int tempByte = 0;
            while ((tempByte = fis.read()) != -1){
                baos.write(tempByte);
            }
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }finally {
            try{
                if (fis != null){
                    fis.close();
                }
                if (baos != null){
                    baos.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }


        return defineClass(className,data,0,data.length);
    }

    public String getLoadPath() {
        return loadPath;
    }

    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
    }
}
package cn.study.algorithm.sort;

import com.alibaba.fastjson.JSON;

/**
 *  冒泡排序
 * @author xiaobin.hou
 * @date 2023/07/11
 */
public class BubbleSort {

    public static void main(String[] args) {
        int[] origin = new int[]{1,2,3,6,5,4};
        int[] sorted = sortV2(origin);
        System.out.println(JSON.toJSONString(sorted));
    }


    public static int[] sortV2(int[] origin){
        if (null == origin){
            return null;
        }
        int n = origin.length;
        for (int i = 0; i < n; i++){
            boolean flag = false;
            for (int j = 0; j < n - 1 - i;j++){
                if (origin[j] > origin[j+1]){
                    flag = true;
                    int temp = origin[j];
                    origin[j] = origin[j+1];
                    origin[j+1] = temp;
                }
            }
            if (!flag){
                break;
            }
        }
        return origin;
    }


    private static int[] sortV1(int[] a){
        if (null == a){
            return null;
        }

        int length = a.length;

        for (int i = 0; i < length; i++) {
            boolean flag = false;
            for (int j = 0; j < length - i - 1; j++) {
                if (a[j] > a[j+1]){
                    int temp = a[j];
                    a[j] = a[j+1];
                    a[j+1] = temp;
                    flag = true;
                }
            }
            if (!flag){
                break;
            }
        }
        return a;
    }


}

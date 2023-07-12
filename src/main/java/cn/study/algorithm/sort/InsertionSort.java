package cn.study.algorithm.sort;

import com.alibaba.fastjson.JSON;

/**
 * 选择排序
 * @author xiaobin.hou
 * @date 2023/07/12
 */
public class InsertionSort {

    public static void main(String[] args) {
        int[] origin = new int[]{1,2,3,6,5,4};
        int[] sorted = sortV2(origin);
        System.out.println(JSON.toJSONString(sorted));
    }


    private static int[] sortV2(int[] a){
        if (null == a){
            return null;
        }

        int length = a.length;

        for (int i = 1; i < length; i++){
            int value = a[i];
            int j = i - 1;
            for (; j>=0 ; j--){
                if (a[j] > value){
                    a[j+1] = a[j];
                }else {
                    break;
                }
            }
            a[j+1] = value;
        }

        return a;
    }

    private static int[] sortV1(int[] a){
        if (null == a){
            return null;
        }
        int length = a.length;
        for (int i = 1; i<length; i++){
            int temp = a[i];
            int j = i -1;
            for (;j>=0 ; j--){
                if (a[j] > temp){
                    //数据移动
                    a[j+1] = a[j];
                }else {
                    break;
                }
            }
            // 插入数据
            a[j+1] = temp;
        }
        return a;
    }
}

package com.qianqi.mylook.learning;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017/2/5.
 */

public class CookUtils {

    public static float mean(float[] array){
        if(array.length < 1)
            return 0;
        float sum = sum(array);
        return sum/array.length;
    }

    public static float sum(float[] array){
        if(array.length < 1)
            return 0;
        float sum = 0;
        for(float d:array){
            sum += d;
        }
        return sum;
    }

    public static int[] generate_subsets(int a, int size){
        int[] res = new int[size];
        Random random = new Random();
        for(int i = 0;i < size;i++){
            res[i] = random.nextInt(a);
        }
        return res;
    }

    public static int[] underRows(float[][] array, int column, float threshold, boolean reverse){
        float[] subColumn = subColumn(array,column);
        List<Integer> list = new ArrayList<>();
        for(int i = 0;i < subColumn.length;i++){
            float d = subColumn[i];
            if(!reverse && d < threshold){
                list.add(i);
            }
            if(reverse && d >= threshold){
                list.add(i);
            }
        }
        int[] res = new int[list.size()];
        for(int i = 0;i < res.length;i++){
            res[i] = list.get(i);
        }
        return res;
    }

    public static float[][] subRows(float[][] array,int[] rows){
        float[][] res = new float[rows.length][array[0].length];
        for(int i = 0;i < rows.length;i++){
            res[i] = array[rows[i]];
        }
        return res;
    }

    public static float[] subRows(float[] array,int[] rows){
        float[] res = new float[rows.length];
        for(int i = 0;i < rows.length;i++){
            res[i] = array[rows[i]];
        }
        return res;
    }

    public static float[][] subColumns(float[][] array,int[] columns){
        float[][] res = new float[array.length][columns.length];
        for(int i = 0;i < array.length;i++){
            for(int j = 0;j < columns.length;j++){
                res[i][j] = array[i][columns[j]];
            }
        }
        return res;
    }

    public static float[] subColumn(float[][] array,int column){
        float[] res = new float[array.length];
        for(int i = 0;i < res.length;i++){
            res[i] = array[i][column];
        }
        return res;
    }

    public static void sort(float[] x,float[] y) {
        if(x.length > 0 && x.length == y.length)   //查看数组是否为空
        {
            quickSort(x, 0, x.length-1,y);
        }
    }

    public static void quickSort(float[] x,int low,int high,float[] y) {
        if(low < high)
        {
            int middle = getMiddle(x,low,high,y); //将numbers数组进行一分为二
            quickSort(x, low, middle-1,y);   //对低字段表进行递归排序
            quickSort(x, middle+1, high,y); //对高字段表进行递归排序
        }
    }

    public static int getMiddle(float[] numbers, int low,int high,float[] y) {
        float temp = numbers[low]; //数组的第一个作为中轴
        float yTemp = y[low];
        while(low < high)
        {
            while(low < high && numbers[high] >= temp)
            {
                high--;
            }
            numbers[low] = numbers[high];//比中轴小的记录移到低端
            y[low] = y[high];
            while(low < high && numbers[low] <= temp)
            {
                low++;
            }
            numbers[high] = numbers[low] ; //比中轴大的记录移到高端
            y[high] = y[low];
        }
        numbers[low] = temp ; //中轴记录到尾
        y[low] = yTemp;
        return low ; // 返回中轴的位置
    }

    public static float SE(float yhat,float[] y){
        float res = 0;
        for(int i = 0;i < y.length;i++){
            res += Math.pow((y[i]-yhat),2);
        }
        return res;
    }
}

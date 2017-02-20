package com.qianqi.mylook.learning;

/**
 * Created by Administrator on 2017/1/18.
 */

public class RecordItem {
    private static final int INPUT_SIZE = 4;
    private static final int OUTPUT_SIZE = 1;
    private String packageName;
    private String date;
    private float[] input = null;
    private float output = -1;

    public RecordItem(String packageName, String date){
        this.packageName = packageName;
        this.date = date;
    }

    public RecordItem(String packageName, String date, float[] input, float output) {
        this.packageName = packageName;
        this.date = date;
        this.input = input;
        this.output = output;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for(float f:input){
            sb.append(f);
            sb.append(" ");
        }
        sb.append(output);
        return sb.toString();
    }

    public void decode(String inputString){
        String[] array = inputString.split(" ");
        if(array.length > 1){
            float[] tmp = new float[array.length-1];
            for(int i = 0;i < array.length - 1;i++){
                float f = Float.parseFloat(array[i]);
                tmp[i] = f;
            }
            this.input = tmp;
            this.output = Float.parseFloat(array[array.length-1]);
        }
    }

    public float[] getInput() {
        return input;
    }

    public void setInput(float[] input) {
        this.input = input;
    }

    public float getOutput() {
        return output;
    }

    public void setOutput(float output) {
        this.output = output;
    }
}

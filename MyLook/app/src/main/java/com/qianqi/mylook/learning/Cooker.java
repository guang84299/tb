package com.qianqi.mylook.learning;

import com.qianqi.mylook.utils.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Cooker {

    public static final int MIN_SAMPLES = 200;
    public static final float MIN_SAMPLES_Y = 0.005f;
    private static Cooker instance = new Cooker();
    private Map<String,RandomForestRegressor> forestMap = new HashMap<>();
    private int n_estimators = 10;
    private int min_samples_to_split = 20;
    private int max_depth = 6;
    private int max_features = -1;//未用到
    private float sub_sample_size = 0.8f;
    private int min_leaf_size = 10;
    private int bootstrap_size = -1;//未用到
    private int verbose = 0;

    public Cooker(){

    }

    public static Cooker getInstance(){
        return instance;
    }

    public boolean fit(String key,float[][] X,float[] y){
        if(X.length < MIN_SAMPLES){
//            L.d("sample too small,return");
            forestMap.remove(key);
            return false;
        }
        float mean = CookUtils.mean(y);
        if(mean < MIN_SAMPLES_Y){
//            L.d("y too small,return:"+mean+","+key);
            forestMap.remove(key);
            return false;
        }
        RandomForestRegressor forest = forestMap.get(key);
        if(forest == null){
            forest = new RandomForestRegressor(n_estimators,min_samples_to_split,max_depth,max_features,
                    (int) (sub_sample_size*X.length),min_leaf_size,bootstrap_size,verbose);
            forest.fit(X,y);
            forestMap.put(key,forest);
        }
        else{
            forest.config((int) (sub_sample_size*X.length));
            forest.fit(X,y);
        }
        return true;
    }

    public float[] predict(String key,float[][] X){
        RandomForestRegressor forest = forestMap.get(key);
        if(forest == null){
            return null;
        }
        return forest.predict(X);
    }

    class DecisionTreeRegressor{
        
        public String loss = "12";
        public String loss_function = "";
        public int min_samples_to_split = 5;
        public int max_depth = -1;
        public int max_features = -1;
        public int sub_sample_size = -1;
        public int min_leaf_size = 1;
        public Tree tree;
        
        public DecisionTreeRegressor(
                     int min_samples_to_split,
                     int max_depth,
                     int max_features,
                     int sub_sample_size,
                     int min_leaf_size){
            if (this.loss.equals("12")) {
                this.loss_function = "RMSE";
            }
            this.min_samples_to_split = min_samples_to_split;
            this.max_depth = max_depth;
            this.max_features = max_features;
            this.sub_sample_size = sub_sample_size;
            this.min_leaf_size = min_leaf_size;
            if (this.min_samples_to_split <= 2 * this.min_leaf_size) {
                this.min_samples_to_split = 2 * min_leaf_size + 1;
            }
            this.tree = new Tree();
        }

        public void fit(float[][] X, float[] y){
            RegressionTreeBuilder tree_builder = new RegressionTreeBuilder(this.min_samples_to_split,
                    this.max_depth,
                    this.max_features,
                    this.sub_sample_size,
                    this.min_leaf_size);
            this.tree = tree_builder.build_tree(X, y, this.max_depth);
        }

        public float[] predict(float[][] X){
            float[] res = new float[X.length];
            for(int i = 0;i < X.length;i++){
                res[i] = this.tree.get_prediction(X[i]);
            }
            return res;
        }

        public void config(int sub_sample_size) {
            this.sub_sample_size = sub_sample_size;
        }
    }

    class RegressionTreeBuilder{

        public int min_samples_to_split = 5;
        public int max_depth = -1;
        public int max_features = -1;
        public int sub_sample_size = -1;
        public int min_leaf_size = 1;
        public Tree tree;

        public RegressionTreeBuilder(
                     int min_samples_to_split,
                     int max_depth,
                     int max_features,
                     int sub_sample_size,
                     int min_leaf_size){
            this.tree = new Tree();
            this.min_samples_to_split = Math.max(min_samples_to_split,2);
            this.max_depth = max_depth;
            this.max_features = max_features;
            this.sub_sample_size = sub_sample_size;
            this.min_leaf_size = min_leaf_size;
        }

        public Tree build_tree(float[][] X,float[] y, int remaining_depth){
            float val = CookUtils.mean(y);
            if (y.length < this.min_samples_to_split || remaining_depth == 0)
                return new Tree(val);

            // Determine the subset of features (columns) to use for the split
//            int[] features = CookUtils.generate_subsets(X[0].length, this.max_features);

            // Determine the subset of observations (rows, samples) to use for the split
            int[] sub_sample = CookUtils.generate_subsets(X.length, this.sub_sample_size);

            float[] threshold = this.threshold_finder(CookUtils.subRows(X,sub_sample),CookUtils.subRows(y,sub_sample));
            int returned_feature = (int) threshold[0];
            float threshold_value = threshold[1];
            float MSE_estimate = threshold[2];
//            threshold_feature = features[returned_feature]
            int threshold_feature = returned_feature;

            Tree node = new Tree();
            int[] under_samples = CookUtils.underRows(X,threshold_feature,threshold_value,false);
            int[] up_samples = CookUtils.underRows(X,threshold_feature,threshold_value,true);
//            under_samples = X[:,threshold_feature] < threshold_value
            node.left = this.build_tree(CookUtils.subRows(X,under_samples), CookUtils.subRows(y,under_samples), remaining_depth - 1);
            node.right = this.build_tree(CookUtils.subRows(X,up_samples), CookUtils.subRows(y,up_samples), remaining_depth - 1);
            node.val = val;
            node.threshold_value = threshold_value;
            node.threshold_feature = threshold_feature;

            return node;
        }

        public float[] threshold_finder(float[][] X, float[] y){
            /**
            Finds the optimal threshold for the data X, y.

                    Returns the feature and value of the threshold, as well as the resulting mean squared error.
            **/
            int threshold_feature = 0;
            float threshold_value = 0;
            float full_SE = CookUtils.SE(CookUtils.mean(y),y);
            float best_SE = full_SE;
            int num_feature = X[0].length;
            for (int feature = 0;feature < num_feature;feature++){
                float[] column = CookUtils.subColumn(X,feature);

                float[] threshold = this.threshold_single_feature(column, y);
                float value = threshold[0];
                float split_SE = threshold[1];
                if (split_SE < best_SE){
                    threshold_feature = feature;
                    threshold_value = value;
                    best_SE = split_SE;
                }
            }
            float[] res = new float[3];
            res[0] = threshold_feature;
            res[1] = threshold_value;
            res[2] = best_SE/y.length;
            return res;
        }

        public float[] threshold_single_feature(float[] x,float[] y){
            /**Picks a threshold value for x to minimize the sum of square errors for y.

            Returns the optimal threshold point, and the resulting sum of squared errors
            **/
            CookUtils.sort(x,y);
            int n = y.length;
            float left_of_split_sum = 0;
            float right_of_split_sum = CookUtils.sum(y);

            float y_mean = CookUtils.mean(y);
            float full_SE = CookUtils.SE(y_mean,y);

            int best_k = this.min_leaf_size - 1;
            float best_SE = full_SE;

            for (int k = this.min_leaf_size - 1;k < n - this.min_leaf_size;k++){
                left_of_split_sum += y[k];
                right_of_split_sum -= y[k];
                float left_prediction = left_of_split_sum / (k+1f);
                float right_prediction = right_of_split_sum / (n-k-1f);

                float split_SE = (float) (full_SE - (k+1)*Math.pow(left_prediction - y_mean,2) - (n-k-1) * Math.pow(right_prediction - y_mean,2));

                if (split_SE < best_SE) {
                    best_k = k;
                    best_SE = split_SE;
                }
            }
            float threshold_point = (x[best_k]+x[best_k+1])/2f; // This could likely be impoved, but shouldn't matter for large data sets
            float[] res = new float[2];
            res[0] = threshold_point;
            res[1] = best_SE;
            return res;
        }
    }

    class Tree{

        public float val = 0;
        public Tree left;
        public Tree right;
        public int threshold_feature;
        public float threshold_value;

        public Tree(
                     float val,
                     Tree left,
                     Tree right,
                     int threshold_feature,
                     float threshold_value){
            this.val = val;
            this.left = left;
            this.right = right;
            this.threshold_value = threshold_value;
            this.threshold_feature = threshold_feature;
        }

        public Tree() {

        }

        public Tree(float val){
            this.val = val;
        }

        public float get_prediction(float[] x){
            if (this.left == null) {
                return this.val;
            }
            else{
                float x_feature_val = x[this.threshold_feature];
                if (x_feature_val >= this.threshold_value)
                    return this.right.get_prediction(x);
                else
                    return this.left.get_prediction(x);
            }

        }
    }

    class RandomForestRegressor{

        public int n_estimators = 5;
        public int min_samples_to_split = 5;
        public int max_depth = -1;
        public int max_features = -1;
        public int sub_sample_size = -1;
        public int min_leaf_size = 1;
        public int bootstrap_size = -1;
        public int verbose = 0;
        public List<DecisionTreeRegressor> tree_list;
        private int lastSampleNum = 0;

        public RandomForestRegressor(
                     int n_estimators,
                     int min_samples_to_split,
                     int max_depth,
                     int max_features,
                     int sub_sample_size,
                     int min_leaf_size,
                     int bootstrap_size,
                     int verbose){
            this.n_estimators = n_estimators;
            this.min_samples_to_split = min_samples_to_split;
            this.max_features = max_features;
            this.max_depth = max_depth;
            this.sub_sample_size = sub_sample_size;
            this.min_leaf_size = min_leaf_size;
            this.bootstrap_size = bootstrap_size;
            this.verbose = verbose;
            this.tree_list = new ArrayList<>(n_estimators);
            for(int i = 0;i < n_estimators;i++){
                this.tree_list.add(new DecisionTreeRegressor(
                        min_samples_to_split,
                        max_depth,
                        max_features,
                        sub_sample_size,
                        min_leaf_size));
            }
        }

        public void fit(float[][] X, float[] y){
            synchronized (this){
                if(X.length == lastSampleNum) {
//                    L.d("same sample,return");
                    return;
                }
                lastSampleNum = X.length;
                for(int k = 0;k < this.tree_list.size();k++){
                    DecisionTreeRegressor tree = this.tree_list.get(k);
                    if (this.verbose >= 1)
                        L.d("Training tree: "+k);
//                bootstrap_sample = generate_subsets(X.shape[0], this.bootstrap_size)
                    tree.fit(X,y);
                }
            }
        }

        public float[] predict(float[][] X){
            synchronized (this){
                float[] res = new float[X.length];
                for(int k = 0;k < this.tree_list.size();k++){
                    DecisionTreeRegressor tree = this.tree_list.get(k);
                    float[] p = tree.predict(X);
                    for(int i = 0;i < p.length;i++){
                        res[i] += p[i];
                    }
                }
                for(int i = 0;i < res.length;i++){
                    res[i] = res[i]/this.tree_list.size();
                }
                return res;
            }
        }

        public void config(int sub_sample_size) {
            this.sub_sample_size = sub_sample_size;
            for(int k = 0;k < this.tree_list.size();k++){
                DecisionTreeRegressor tree = this.tree_list.get(k);
                tree.config(sub_sample_size);
            }
        }
    }

}

package com.chuxinbuer.deyiguanjia.face;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MovingAverage {
    private Queue<Float> queue;
    private int maxSize;
    private float sum;

    //Initialize your data structure here.
    public MovingAverage(int Size) {
        queue = new ConcurrentLinkedQueue<Float>();
        maxSize = Size;
    }

    public double next(float value){
        sum = 0;
        if(queue.size()>=maxSize){
            queue.poll();
            queue.offer(value);
        }
        else
            queue.offer(value);
        for(float x:queue){
            sum+=x;
        }
        return (sum*1.0)/queue.size();
    }

    public void clear(){
        queue.clear();
    }

}

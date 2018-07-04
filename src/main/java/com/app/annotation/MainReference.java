package com.app.annotation;

import java.util.Comparator;

/**
 * @author yingyang
 * @date 2018/7/4.
 */
public class MainReference {
    
    public static void main(String[] args) {
        Comparator<Integer> com = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
        
        // Comparator<Integer> comparator = (x, y) -> Integer.compare(x, y);
        
        Comparator<Integer> comparator = Integer::compare;
        System.out.println(comparator.compare(1, 2));
    }
}

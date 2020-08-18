package com.myha.myflashcard;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

public class RandomColor {
    private Stack<Integer> recycle, colors;

    public RandomColor() {
        colors = new Stack<>();
        recycle =new Stack<>();
        recycle.addAll(Arrays.asList(
                0xffD4F1F4,0xffEAEAE0,0xffC8F4F9,0xffE8EEF1,
                0xffF4EBD0,0xffF4EBD0,0xffCFEED1,0xffFADCD9,
                0xffB5E5CF,0xffFFE9E4,0xffDBF5F0,0xffcddc39,
                0xffF7D6D0,0xffEAE7FA,0xffECE3F0,0xffE9D8E1,
                0xffE4F4F3,0xffEEEEEE,0xffF4EAE6,0xffDBE8D8
                )
        );
    }

    public int getColor() {
        if (colors.size()==0) {
            while(!recycle.isEmpty())
                colors.push(recycle.pop());
            Collections.shuffle(colors);
        }
        Integer c= colors.pop();
        recycle.push(c);
        return c;
    }
}

package com.myha.myflashcard;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SwipeCardLearnAdapter extends BaseAdapter
{
    public ArrayList<Boolean> answers;
    MyDatabaseHelper db;
    private Context context;
    private ArrayList<Word> words;
    private ArrayList<String> keys;

    public SwipeCardLearnAdapter(Context context, ArrayList<Word> words, ArrayList<String> keys)
    {
        this.context = context;
        this.words = words;
        this.keys = keys;
        db = new MyDatabaseHelper(context, keys);
        answers = new ArrayList<>();
    }

    @Override
    public int getCount()
    {
        return words.size();
    }

    @Override
    public Object getItem(int position)
    {
        return words.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        if (v == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            // normally use a viewholder
            v = inflater.inflate(R.layout.card_learn, parent, false);
        }
        //RandomColor randomColors = new RandomColor();
        //((CardView) v.findViewById(R.id.card_learn)).setBackgroundColor(randomColors.getColor());
        //((CardView) v.findViewById(R.id.card_learn)).setRadius((float) 10);

        ArrayList<Integer> rans = new ArrayList<>();
        for (int i = 0; i< words.size()*2;i++)
        {
            if(i<words.size())
                rans.add(i);
            else
                rans.add(position);
        }
        Collections.shuffle(rans);
        int randomPos = getRandom(rans);
        if (randomPos !=position)
        {
            answers.add(position, false);
        }
        else
        {
            answers.add(position, true);
        }
        ((TextView) v.findViewById(R.id.txt_content_1)).setText(words.get(position).getAttributeByKey(keys.get(0)));
        ((TextView) v.findViewById(R.id.txt_content_2)).setText(words.get(randomPos).getAttributeByKey(keys.get(1)));

        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String item = (String) getItem(position);
                Log.i("LearnCardActivity", item);
            }
        });

        return v;
    }
    private int getRandom(ArrayList<Integer> array) {
        int rnd = new Random().nextInt(array.size());
        return array.get(rnd);
    }
}

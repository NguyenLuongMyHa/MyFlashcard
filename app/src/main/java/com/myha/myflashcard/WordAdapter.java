package com.myha.myflashcard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.poi.hssf.util.HSSFColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder>
{
    private Context context;
    private ArrayList<Word> words;
    private ArrayList<String> keys;
    private Map<String, Integer> wordSizes;
    private MyDatabaseHelper db;

    public WordAdapter(Context context, ArrayList<Word> words, ArrayList<String> keys, Map<String, Integer> wordSizes)
    {
        this.context = context;
        this.words = words;
        this.keys = keys;
        this.wordSizes = wordSizes;
        db = new MyDatabaseHelper(context, keys);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_word, parent, false);
        return new ViewHolder(view, context, keys.size());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        final Word word = words.get(position);
        ArrayList<Integer> textViewsId = holder.myTextViewsId;
        Map<Integer, TextView> textViews = holder.myTextViews;
        for (int i = 0; i < textViews.size(); i++)
        {
            try
            {
                holder.myTextViews.get(textViewsId.get(i)).setText(Html.fromHtml("<b>" + keys.get(i) + ": </b>" + word.getAttributeByKey(keys.get(i))));
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
        }
        holder.ll_item_word.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                // Delete a record
                generateDialog(word);
                notifyDataSetChanged();
                return false;
            }
        });

    }
    private void generateDialog(final Word word)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        db.deleteWord(word.getId());
                        words.remove(word);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure to delete this item?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public int getItemCount()
    {
        return words.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private Map<Integer, TextView> myTextViews;
        private ArrayList<Integer> myTextViewsId;
        private LinearLayout ll_item_word;
        public ViewHolder(View view, final Context context, int numberOfTextView)
        {
            super(view);
            ll_item_word = view.findViewById(R.id.ll_item_word);
            myTextViews = new HashMap<>();
            myTextViewsId = new ArrayList<>();
            ArrayList<Integer> mycolors = new ArrayList<>();
            mycolors.add(0xE63700B3);
            mycolors.add(0xC93700B3);
            mycolors.add(0xA63700B3);
            for (int i = 0; i < numberOfTextView; i++)
            {
                try
                {
                    mycolors.get(i);
                }
                catch (IndexOutOfBoundsException e)
                {
                    mycolors.add(0xA63700B3);
                }
                TextView textView = new TextView(context);
                int tv_id = View.generateViewId();
                textView.setId(tv_id);
                myTextViewsId.add(tv_id);
                myTextViews.put(tv_id, textView);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.leftMargin = 16;
                textView.setTextSize((float) wordSizes.get(keys.get(i)));
                textView.setPadding(8, 8, 8, 8);
                textView.setTextColor(mycolors.get(i));
                textView.setLayoutParams(layoutParams);
                ll_item_word.addView(textView);
            }

        }
    }
}

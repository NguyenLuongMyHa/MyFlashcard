package com.myha.myflashcard;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.daprlabs.cardstack.SwipeDeck;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.katso.livebutton.LiveButton;

public class CardLearnActivity extends AppCompatActivity
{
    private final ArrayList<Word> wordList = new ArrayList<Word>();
    SwipeDeck cardStack;
    ArrayList<String> headers;
    ArrayList<String> displays;
    SharedPreferences sharedPreferences;
    LiveButton btn_yes;
    LiveButton btn_no;
    LiveButton btn_restart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_learn);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_cards);
        cardStack.setHardwareAccelerationEnabled(true);
        btn_yes = (LiveButton) findViewById(R.id.btn_yes);
        btn_no = (LiveButton) findViewById(R.id.btn_no);
        btn_restart = (LiveButton) findViewById(R.id.btn_replay);

        sharedPreferences = getSharedPreferences("HEADERS", MODE_PRIVATE);
        headers = new ArrayList<>();
        customDisplay();
        btn_restart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                customDisplay();
                btn_no.setVisibility(View.VISIBLE);
                btn_yes.setVisibility(View.VISIBLE);
            }
        });
    }

    private void customDisplay()
    {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Headers", "");
        if (json.isEmpty())
        {
        }
        else
        {
            Type type = new TypeToken<List<String>>()
            {
            }.getType();
            List<String> arrPackageData = gson.fromJson(json, type);
            headers.clear();
            headers.addAll(arrPackageData);

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_custom_display);
            displays = new ArrayList<>();
            final LinearLayout ll_custom_display = dialog.findViewById(R.id.ll_custom_display);
            for (int i = 0; i < headers.size(); i++)
            {
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                final CheckBox checkBox = new CheckBox(this);
                int cb_id = View.generateViewId();
                checkBox.setId(cb_id);
                LinearLayout.LayoutParams cbLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                cbLayoutParams.leftMargin = 16;
                checkBox.setTextSize((float) 16);
                checkBox.setPadding(8, 8, 8, 8);
                checkBox.setText(headers.get(i));
                checkBox.setLayoutParams(cbLayoutParams);
                checkBox.setGravity(View.TEXT_ALIGNMENT_CENTER);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {

                        if (buttonView.isChecked())
                        {
                            displays.add(String.valueOf(checkBox.getText()));
                        }
                        else
                        {
                            displays.remove(String.valueOf(checkBox.getText()));
                        }
                    }
                });
                linearLayout.addView(checkBox);
                ll_custom_display.addView(linearLayout);

            }

            final TextView textView = new TextView(this);
            textView.setText("Please select 2 types for learn");
            textView.setTextColor(Color.RED);
            Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (displays.size() == 2)
                    {
                        dialog.dismiss();
                        getWordList();
                    }
                    else
                    {
                        if (textView.getParent() != null)
                        {
                            ((ViewGroup) textView.getParent()).removeView(textView);
                        }
                        ll_custom_display.addView(textView);
                    }
                }
            });

            dialog.setCancelable(true);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void getWordList()
    {
        btn_no.setVisibility(View.VISIBLE);
        btn_yes.setVisibility(View.VISIBLE);
        btn_restart.setVisibility(View.VISIBLE);
        MyDatabaseHelper db = new MyDatabaseHelper(this, headers);
        List<Word> list = db.getAllWords();

        this.wordList.addAll(list);
        final SwipeCardLearnAdapter adapter = new SwipeCardLearnAdapter(this, wordList, displays);
        cardStack.setAdapter(adapter);
        cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback()
        {
            @Override
            public void cardSwipedLeft(int position)
            {
                Log.i("CardLearnActivity", "Swiped left (FALSE), position: " + position);
                if (adapter.answers.get(position))
                {
                    showWrongDialog(position);
                }
            }

            @Override
            public void cardSwipedRight(int position)
            {
                Log.i("CardLearnActivity", "Swiped right (TRUE), position: " + position);
                if (!adapter.answers.get(position))
                {
                    showWrongDialog(position);
                }


            }

            @Override
            public void cardsDepleted()
            {
                Log.i("CardLearnActivity", "no more cards");
                btn_no.setVisibility(View.GONE);
                btn_yes.setVisibility(View.GONE);
            }

            @Override
            public void cardActionDown()
            {
                Log.i("CardLearnActivity", "Swiped down");

            }

            @Override
            public void cardActionUp()
            {
                Log.i("CardLearnActivity", "Swiped up");

            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cardStack.swipeTopCardRight(100);
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cardStack.swipeTopCardLeft(100);
            }
        });

    }
    private void showWrongDialog(int pos)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_card_learn_wrong);
        TextView txt_key = (TextView) dialog.findViewById(R.id.txt_key);
        TextView txt_answer = (TextView) dialog.findViewById(R.id.txt_answer);
        txt_answer.setText(wordList.get(pos).getAttributeByKey(displays.get(1)));
        txt_key.setText(wordList.get(pos).getAttributeByKey(displays.get(0)));

        ConstraintLayout cl_wrong = (ConstraintLayout) dialog.findViewById(R.id.cl_wrong);
        // if button is clicked, close the custom dialog
        cl_wrong.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}

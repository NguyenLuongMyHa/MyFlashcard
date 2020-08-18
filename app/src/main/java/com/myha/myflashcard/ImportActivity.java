package com.myha.myflashcard;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class ImportActivity extends AppCompatActivity
{
    private final ArrayList<Word> wordList = new ArrayList<Word>();
    RecyclerView rec_data;
    WordAdapter wordAdapter;
    ArrayList<String> displays;
    ArrayList<String> headers;
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    ActionBar actionBar;
    Toolbar toolbar;
    SharedPreferences sharedPreferences;
    Map<String,Integer> displays_size;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        toolbar = findViewById(R.id.toolbar);
        initToolbar();
        rec_data = findViewById(R.id.rec_data);
        sharedPreferences = getSharedPreferences("HEADERS", MODE_PRIVATE);
        headers = new ArrayList<>();
        displays_size = new HashMap<>();
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
            for (int i = 0; i< headers.size(); i++)
            {
                displays_size.put(headers.get(i),16);
            }
            MyDatabaseHelper db = new MyDatabaseHelper(this, headers);
            List<Word> list = db.getAllWords();

            this.wordList.addAll(list);
            setRecyclerViewData(this.wordList, headers, displays_size);

        }
    }

    public void initToolbar()
    {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("My Flashcards");
        if (Build.VERSION.SDK_INT >= 21)
        {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private void browseDocuments()
    {
        String[] mimeTypes =
                {"application/vnd.ms-excel",
                 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                };

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0)
            {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        }
        else
        {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes)
            {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), 1001);

    }
    private void importDialogInstruction()
    {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_import_instruction);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                browseDocuments();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001)
        {
            if (resultCode == RESULT_OK)
            {
                String FileName = data.getData().getLastPathSegment();
                FileName = FileName.substring(FileName.lastIndexOf(":") + 1);
                //actionBar.setTitle(FileName);
                String FilePath = data.getData().getPath();
                if (FilePath.endsWith(".xlsx") || FilePath.endsWith(".xls"))
                {
                    try
                    {
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        workbook = new XSSFWorkbook(is);
                        sheet = workbook.getSheetAt(0);
                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                        Row row = sheet.getRow(0);//first row header
                        int cellsCount = row.getPhysicalNumberOfCells();
                        headers = new ArrayList<>();
                        for (int c = 0; c < cellsCount; c++)
                        {
                            String value = getCellAsString(row, c, formulaEvaluator);
                            headers.add(value);
                        }
                        Gson gson = new Gson();
                        String json = gson.toJson(headers);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Headers", json);
                        editor.apply();
                        generateDialog();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(this, "Sorry, we can not read this file", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }

    private void generateDialog()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        generateSQLiteDatabase();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        String data = "";
        for (int i = 0; i < headers.size(); i++)
        {
            String item = "\"" + headers.get(i) + "\"";
            data = data.concat(item);
            if (i != headers.size() - 1)
            {
                data = data.concat("; ");
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Is this your expected data? " + data).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void generateSQLiteDatabase()
    {
        try
        {
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            ArrayList<Word> data = new ArrayList<>();
            for (int r = 1; r < rowsCount; r++)
            {
                Word word = new Word();
                Row row = sheet.getRow(r);//first row header
                Map<String, String> m = new HashMap<>();
                for (int i = 0; i < headers.size(); i++)
                {
                    String value = getCellAsString(row, i, formulaEvaluator);
                    m.put(headers.get(i), value);
                }
                word.setAttributes(m);
                data.add(word);
            }
            MyDatabaseHelper db = new MyDatabaseHelper(this, headers);
            db.createDatabase();
            db.restoreDB();
            for (Word w : data)
            {
                db.addWord(w);
            }

            List<Word> list = db.getAllWords();
            this.wordList.clear();
            this.wordList.addAll(list);
            displays_size.clear();
            for (int i = 0; i< headers.size(); i++)
            {
                displays_size.put(headers.get(i), 16);
            }

            setRecyclerViewData(this.wordList, headers,displays_size);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator)
    {
        String value = "";
        try
        {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType())
            {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if (HSSFDateUtil.isCellDateFormatted(cell))
                    {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    }
                    else
                    {
                        value = "" + numericValue;
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
            }
        }
        catch (NullPointerException e)
        {
            //printlnToUser(e.toString());
        }
        return value;
    }

    private void setRecyclerViewData(ArrayList<Word> words, ArrayList<String> displays, Map<String, Integer> displays_size)
    {
        wordAdapter = new WordAdapter(this, words, displays, displays_size);
        wordAdapter.notifyDataSetChanged();
        rec_data.setAdapter(wordAdapter);
        rec_data.setLayoutManager(new LinearLayoutManager(this));
    }

    private void learnWithCard()
    {
        Intent i = new Intent(getApplicationContext(), CardLearnActivity.class);
        startActivity(i);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_my_flashcard, menu);
        MenuItem action_import = menu.findItem(R.id.action_import);
        MenuItem action_custom_display = menu.findItem(R.id.action_custom_display);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_import:
                importDialogInstruction();
                return true;
            case R.id.action_custom_display:
                customDisplay();
                return true;
            case R.id.action_learn_with_card:
                learnWithCard();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void customDisplay()
    {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_custom_display);
        displays = new ArrayList<>();
        displays_size.clear();
        LinearLayout ll_custom_display = dialog.findViewById(R.id.ll_custom_display);
        for (int i = 0; i < headers.size(); i++)
        {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            final CheckBox checkBox = new CheckBox(this);
            int cb_id = View.generateViewId();
            checkBox.setId(cb_id);
            LinearLayout.LayoutParams cbLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
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
            Spinner spinner = new Spinner(this);
            spinner.setLayoutParams(cbLayoutParams);
            int compareValue = 16;
            ArrayList<Integer> sizes = new ArrayList<>();
            for (int s = 10; s<30; s++)
            {
                sizes.add(s);
            }
            ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, sizes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            if (compareValue != 16) {
                int spinnerPosition = adapter.getPosition(compareValue);
                spinner.setSelection(spinnerPosition);
            }
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    displays_size.put(checkBox.getText().toString(),Integer.valueOf(parent.getItemAtPosition(position).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            TextView textView = new TextView(this);
            textView.setLayoutParams(cbLayoutParams);
            textView.setTextSize((float)16);
            textView.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            textView.setText("Size: ");

            linearLayout.addView(checkBox);
            linearLayout.addView(textView);
            linearLayout.addView(spinner);

            ll_custom_display.addView(linearLayout);

        }

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                if (displays.size() != 0)
                {
                    setRecyclerViewData(wordList, displays, displays_size);
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

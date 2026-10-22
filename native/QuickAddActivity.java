package uz.kunlikreja.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

/** Vidjetdagi "+" bosilganda chiqadigan kichik oyna: yangi vazifa nomini yozish. */
public class QuickAddActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = "Yangi vazifa";
        String hint = "Vazifa nomi";
        String addText = "Qo'shish";
        String cancelText = "Bekor qilish";
        try {
            SharedPreferences sp = getSharedPreferences("kunlik_widget", MODE_PRIVATE);
            JSONObject o = new JSONObject(sp.getString("data", "{}"));
            title = o.optString("addTitle", title);
            hint = o.optString("addHint", hint);
            addText = o.optString("addBtn", addText);
            cancelText = o.optString("cancelBtn", cancelText);
        } catch (Exception e) {
            // standart matnlar
        }

        int pad = (int) (20 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(18f);
        root.addView(tv);

        final EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        root.addView(input, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        Button cancel = new Button(this);
        cancel.setText(cancelText);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttons.addView(cancel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button add = new Button(this);
        add.setText(addText);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = input.getText().toString().trim();
                if (name.length() > 0) {
                    TaskWidgetProvider.addTask(QuickAddActivity.this, name);
                }
                finish();
            }
        });
        buttons.addView(add, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        root.addView(buttons, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        input.requestFocus();
    }
}

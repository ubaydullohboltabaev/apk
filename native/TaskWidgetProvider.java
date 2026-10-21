package uz.kunlikreja.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Bosh ekrandagi "Bugungi ishlar" vidjeti.
 * Ilova tayyor matnlarni (tanlangan tilda: lotin yoki kirill) SharedPreferences'ga yozadi,
 * vidjet ularni faqat ko'rsatadi.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    private static final int[] ROWS = {
        R.id.row1, R.id.row2, R.id.row3, R.id.row4,
        R.id.row5, R.id.row6, R.id.row7, R.id.row8
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            update(context, manager, id);
        }
    }

    public static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));
        for (int id : ids) {
            update(context, manager, id);
        }
    }

    private static void update(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        String raw = context.getSharedPreferences("kunlik_widget", Context.MODE_PRIVATE).getString("data", "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        String header = "Kunlik Reja";
        List<String> lines = new ArrayList<>();
        try {
            JSONObject o = new JSONObject(raw);
            header = o.optString("title", header);
            if (!today.equals(o.optString("date"))) {
                lines.add(o.optString("stale", "..."));
            } else {
                header = o.optString("header", header);
                JSONArray arr = o.optJSONArray("lines");
                if (arr != null) {
                    for (int i = 0; i < arr.length() && i < ROWS.length; i++) {
                        lines.add(arr.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            lines.add("Kunlik Reja");
        }

        views.setTextViewText(R.id.widget_header, header);
        for (int i = 0; i < ROWS.length; i++) {
            if (i < lines.size()) {
                views.setTextViewText(ROWS[i], lines.get(i));
                views.setViewVisibility(ROWS[i], View.VISIBLE);
            } else {
                views.setViewVisibility(ROWS[i], View.GONE);
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        manager.updateAppWidget(widgetId, views);
    }
}

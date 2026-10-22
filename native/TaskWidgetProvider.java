package uz.kunlikreja.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Bosh ekrandagi "Bugungi ishlar" vidjeti.
 * Ilova tayyor matnlarni SharedPreferences'ga yozadi. Vidjetda vazifani bosib bajarildi deb belgilash
 * va "+" bilan yangi vazifa qo'shish mumkin; bular ilova ochilganda unga o'tkaziladi.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TOGGLE = "uz.kunlikreja.app.WIDGET_TOGGLE";
    private static final String PREFS = "kunlik_widget";

    private static final int[] ROWS = {
        R.id.row1, R.id.row2, R.id.row3, R.id.row4,
        R.id.row5, R.id.row6, R.id.row7, R.id.row8
    };

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            update(context, manager, id);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && ACTION_TOGGLE.equals(intent.getAction())) {
            String id = intent.getStringExtra("id");
            if (id != null) {
                markDone(context, id);
            }
        }
    }

    public static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));
        for (int id : ids) {
            update(context, manager, id);
        }
    }

    private static JSONObject readPending(SharedPreferences sp) {
        try {
            String raw = sp.getString("pending", "");
            if (raw != null && raw.length() > 0) {
                return new JSONObject(raw);
            }
        } catch (Exception e) {
            // ignore
        }
        return new JSONObject();
    }

    private static void addPending(SharedPreferences sp, String key, JSONObject entry) {
        try {
            JSONObject p = readPending(sp);
            JSONArray arr = p.optJSONArray(key);
            if (arr == null) {
                arr = new JSONArray();
            }
            arr.put(entry);
            p.put(key, arr);
            sp.edit().putString("pending", p.toString()).apply();
        } catch (Exception e) {
            // ignore
        }
    }

    /** Vidjetda vazifa bosildi: ro'yxatdan olib tashlaymiz va ilova uchun belgilab qo'yamiz. */
    private static void markDone(Context context, String id) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            JSONObject o = new JSONObject(sp.getString("data", "{}"));
            JSONArray items = o.optJSONArray("items");
            JSONArray rest = new JSONArray();
            boolean found = false;
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject it = items.getJSONObject(i);
                    if (!found && id.equals(it.optString("id"))) {
                        found = true;
                    } else {
                        rest.put(it);
                    }
                }
            }
            if (found) {
                o.put("items", rest);
                o.put("done", o.optInt("done", 0) + 1);
                sp.edit().putString("data", o.toString()).apply();
                JSONObject e = new JSONObject();
                e.put("id", id);
                e.put("date", o.optString("date", today()));
                addPending(sp, "toggles", e);
            }
        } catch (Exception e) {
            // ignore
        }
        refreshAll(context);
    }

    /** Vidjet oynasidan yangi vazifa qo'shildi. */
    public static void addTask(Context context, String name) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String id = "w" + System.currentTimeMillis();
            String date = today();
            JSONObject e = new JSONObject();
            e.put("id", id);
            e.put("name", name);
            e.put("date", date);
            addPending(sp, "adds", e);

            JSONObject o = new JSONObject(sp.getString("data", "{}"));
            if (date.equals(o.optString("date"))) {
                JSONArray items = o.optJSONArray("items");
                if (items == null) {
                    items = new JSONArray();
                }
                JSONObject it = new JSONObject();
                it.put("id", id);
                it.put("t", "");
                it.put("n", name);
                items.put(it);
                o.put("items", items);
                o.put("total", o.optInt("total", 0) + 1);
                sp.edit().putString("data", o.toString()).apply();
            }
        } catch (Exception e) {
            // ignore
        }
        refreshAll(context);
    }

    private static PendingIntent openApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void update(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString("data", "");

        String header = "Rejam";
        String firstLine = null;
        String moreLine = null;
        String[] ids = new String[7];
        String[] texts = new String[7];
        int count = 0;
        int alpha = 90;
        float size = 13f;

        try {
            JSONObject o = new JSONObject(raw);
            header = o.optString("title", header);
            alpha = o.optInt("alpha", 90);
            size = (float) o.optInt("size", 13);
            if (!today().equals(o.optString("date"))) {
                firstLine = o.optString("stale", "...");
            } else {
                header = o.optString("hdr", header) + "  " + o.optInt("done", 0) + "/" + o.optInt("total", 0);
                JSONArray items = o.optJSONArray("items");
                if (items != null) {
                    for (int i = 0; i < items.length() && count < 7; i++) {
                        JSONObject it = items.getJSONObject(i);
                        String t = it.optString("t", "");
                        String n = it.optString("n", "");
                        ids[count] = it.optString("id", "");
                        texts[count] = "○  " + (t.length() > 0 ? t + "  " : "") + n;
                        count++;
                    }
                }
                if (count == 0) {
                    firstLine = o.optString("empty", "");
                }
                int more = o.optInt("more", 0);
                if (more > 0) {
                    moreLine = "+" + more + " " + o.optString("moreTxt", "");
                }
            }
        } catch (Exception e) {
            firstLine = "Rejam";
        }

        if (alpha < 0) alpha = 0;
        if (alpha > 100) alpha = 100;
        if (size < 8f) size = 8f;
        views.setInt(R.id.widget_bg_img, "setImageAlpha", (alpha * 255) / 100);
        views.setTextViewText(R.id.widget_header, header);

        String[] lines = new String[8];
        boolean[] clickable = new boolean[8];
        if (firstLine != null) {
            lines[0] = firstLine;
        } else {
            for (int i = 0; i < count; i++) {
                lines[i] = texts[i];
                clickable[i] = true;
            }
            if (moreLine != null) {
                lines[count] = moreLine;
            }
        }

        for (int i = 0; i < ROWS.length; i++) {
            int rid = ROWS[i];
            if (lines[i] == null) {
                views.setViewVisibility(rid, View.GONE);
                continue;
            }
            views.setTextViewText(rid, lines[i]);
            views.setTextViewTextSize(rid, TypedValue.COMPLEX_UNIT_SP, size);
            views.setViewVisibility(rid, View.VISIBLE);
            if (clickable[i]) {
                Intent t = new Intent(context, TaskWidgetProvider.class);
                t.setAction(ACTION_TOGGLE);
                t.putExtra("id", ids[i]);
                PendingIntent pi = PendingIntent.getBroadcast(
                    context, 100 + i, t, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(rid, pi);
            } else {
                views.setOnClickPendingIntent(rid, openApp(context));
            }
        }

        Intent add = new Intent(context, QuickAddActivity.class);
        add.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent addPi = PendingIntent.getActivity(
            context, 1, add, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_add, addPi);
        views.setOnClickPendingIntent(R.id.widget_header, openApp(context));

        manager.updateAppWidget(widgetId, views);
    }
}

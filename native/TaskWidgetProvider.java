package uz.kunlikreja.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

    protected int layoutId() { return R.layout.widget_tasks; }

    protected int rowCount() { return 8; }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            update(context, manager, id, layoutId(), rowCount());
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

    private static void refreshClass(Context context, Class<?> cls, int layout, int rows) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, cls));
        for (int id : ids) {
            update(context, manager, id, layout, rows);
        }
    }

    public static void refreshAll(Context context) {
        refreshClass(context, TaskWidgetProvider.class, R.layout.widget_tasks, 8);
        refreshClass(context, TaskWidgetMedium.class, R.layout.widget_medium, 4);
        refreshClass(context, TaskWidgetSmall.class, R.layout.widget_small, 3);
    }

    private static int parseColor(String s, int def) {
        try {
            return Color.parseColor(s);
        } catch (Exception e) {
            return def;
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

    private static final int[] BG_DARK = {
        R.drawable.widget_bg_dark_0, R.drawable.widget_bg_dark_1, R.drawable.widget_bg_dark_2,
        R.drawable.widget_bg_dark_3, R.drawable.widget_bg_dark_4
    };
    private static final int[] BG_LIGHT = {
        R.drawable.widget_bg_light_0, R.drawable.widget_bg_light_1, R.drawable.widget_bg_light_2,
        R.drawable.widget_bg_light_3, R.drawable.widget_bg_light_4
    };

    private static void update(Context context, AppWidgetManager manager, int widgetId, int layout, int maxRows) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layout);
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = sp.getString("data", "");

        String header = "Rejam";
        String firstLine = null;
        int count = 0;
        int moreCount = 0;
        String moreTxt = "";
        String[] ids = new String[8];
        String[] texts = new String[8];
        int alpha = 92;
        float size = 13f;
        boolean dark = true;
        int pal = 0;
        int ink = Color.parseColor("#E8EEFB");
        int accent = Color.parseColor("#4C8DFF");

        try {
            JSONObject o = new JSONObject(raw);
            header = o.optString("title", header);
            alpha = o.optInt("alpha", 92);
            double scale = o.optInt("scale", 100) / 100.0;
            size = (float) (13.0 * scale);
            dark = !"light".equals(o.optString("m", "dark"));
            pal = o.optInt("pal", 0);
            ink = parseColor(o.optString("ink", "#E8EEFB"), ink);
            accent = parseColor(o.optString("ok", "#4C8DFF"), accent);
            if (!today().equals(o.optString("date"))) {
                firstLine = o.optString("stale", "...");
            } else {
                header = o.optString("hdr", header) + "  " + o.optInt("done", 0) + "/" + o.optInt("total", 0);
                JSONArray items = o.optJSONArray("items");
                int n = items == null ? 0 : items.length();
                int more = o.optInt("more", 0);
                int total = n + more;
                moreTxt = o.optString("moreTxt", "");
                int show = total <= maxRows ? n : maxRows - 1;
                if (show > n) show = n;
                for (int i = 0; i < show && count < 8; i++) {
                    JSONObject it = items.getJSONObject(i);
                    String t = it.optString("t", "");
                    String nm = it.optString("n", "");
                    ids[count] = it.optString("id", "");
                    texts[count] = "\u25CB  " + (t.length() > 0 ? t + "  " : "") + nm;
                    count++;
                }
                moreCount = total - show;
                if (moreCount < 0) moreCount = 0;
                if (count == 0 && total == 0) {
                    firstLine = o.optString("empty", "");
                }
            }
        } catch (Exception e) {
            firstLine = "Rejam";
        }

        if (alpha < 0) alpha = 0;
        if (alpha > 100) alpha = 100;
        if (size < 8f) size = 8f;
        if (pal < 0 || pal > 4) pal = 0;

        views.setImageViewResource(R.id.widget_bg_img, dark ? BG_DARK[pal] : BG_LIGHT[pal]);
        views.setInt(R.id.widget_bg_img, "setImageAlpha", (alpha * 255) / 100);
        views.setTextViewText(R.id.widget_header, header);
        views.setTextColor(R.id.widget_header, accent);
        views.setTextColor(R.id.widget_add, accent);

        String[] lines = new String[8];
        boolean[] clickable = new boolean[8];
        if (firstLine != null) {
            lines[0] = firstLine;
        } else {
            for (int i = 0; i < count && i < maxRows; i++) {
                lines[i] = texts[i];
                clickable[i] = true;
            }
            if (moreCount > 0 && count < maxRows) {
                lines[count] = "+" + moreCount + " " + moreTxt;
            }
        }

        for (int i = 0; i < maxRows && i < ROWS.length; i++) {
            int rid = ROWS[i];
            if (lines[i] == null) {
                views.setViewVisibility(rid, View.GONE);
                continue;
            }
            views.setTextViewText(rid, lines[i]);
            views.setTextColor(rid, ink);
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
        views.setOnClickPendingIntent(R.id.widget_root, openApp(context));

        manager.updateAppWidget(widgetId, views);
    }
}

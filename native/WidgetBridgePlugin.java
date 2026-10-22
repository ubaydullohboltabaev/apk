package uz.kunlikreja.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

/** Ilovadagi bugungi vazifalar ro'yxatini vidjetga uzatadi va vidjetdagi amallarni qabul qiladi. */
@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridgePlugin extends Plugin {

    @PluginMethod
    public void update(PluginCall call) {
        String json = call.getString("json", "");
        Context context = getContext();
        context.getSharedPreferences("kunlik_widget", Context.MODE_PRIVATE)
            .edit()
            .putString("data", json)
            .apply();
        TaskWidgetProvider.refreshAll(context);
        call.resolve();
    }

    /** Vidjetda belgilangan va qo'shilgan vazifalarni qaytaradi va navbatni tozalaydi. */
    @PluginMethod
    public void consume(PluginCall call) {
        SharedPreferences sp = getContext().getSharedPreferences("kunlik_widget", Context.MODE_PRIVATE);
        String raw = sp.getString("pending", "");
        sp.edit().remove("pending").apply();
        JSONArray toggles = new JSONArray();
        JSONArray adds = new JSONArray();
        try {
            if (raw != null && raw.length() > 0) {
                JSONObject o = new JSONObject(raw);
                JSONArray t = o.optJSONArray("toggles");
                JSONArray a = o.optJSONArray("adds");
                if (t != null) toggles = t;
                if (a != null) adds = a;
            }
        } catch (Exception e) {
            // bo'sh qaytadi
        }
        JSObject ret = new JSObject();
        try {
            ret.put("toggles", toggles);
            ret.put("adds", adds);
        } catch (Exception e) {
            // ignore
        }
        call.resolve(ret);
    }
}

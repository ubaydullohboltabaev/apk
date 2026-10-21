package uz.kunlikreja.app;

import android.content.Context;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Ilovadagi bugungi vazifalar ro'yxatini vidjetga uzatadi. */
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
}

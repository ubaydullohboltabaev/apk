"""Android loyihasini tayyorlaydi: ruxsatlar, vidjet kodi va vidjet e'loni."""
import pathlib
import shutil
import sys

ROOT = pathlib.Path(".")
MAIN = ROOT / "android/app/src/main"
MANIFEST = MAIN / "AndroidManifest.xml"
NATIVE = ROOT / "native"

# ---- 1. Ruxsatlar ----
s = MANIFEST.read_text(encoding="utf-8")
if "</manifest>" not in s or "</application>" not in s:
    sys.exit("AndroidManifest.xml topilmadi yoki buzilgan")

perms = [
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.SCHEDULE_EXACT_ALARM",
    "android.permission.USE_EXACT_ALARM",
    "android.permission.VIBRATE",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.WAKE_LOCK",
]
add = ""
for q in perms:
    if q not in s:
        add += f'    <uses-permission android:name="{q}" />\n'
s = s.replace("</manifest>", add + "</manifest>")

# ---- 2. Vidjet e'loni ----
def make_receiver(cls, label, info):
    return f"""
        <receiver
            android:name="uz.kunlikreja.app.{cls}"
            android:exported="true"
            android:label="{label}">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/{info}" />
        </receiver>
"""
receiver = (make_receiver("TaskWidgetSmall", "Rejam: kichik", "widget_info_small")
            + make_receiver("TaskWidgetMedium", "Rejam: orta", "widget_info_medium")
            + make_receiver("TaskWidgetProvider", "Rejam: katta", "widget_info"))
quickadd = """
        <activity
            android:name="uz.kunlikreja.app.QuickAddActivity"
            android:exported="false"
            android:excludeFromRecents="true"
            android:taskAffinity=""
            android:theme="@android:style/Theme.DeviceDefault.Dialog"
            android:windowSoftInputMode="stateVisible" />
"""
if "TaskWidgetProvider" not in s:
    s = s.replace("</application>", receiver + quickadd + "    </application>")
MANIFEST.write_text(s, encoding="utf-8")
print("Manifest yangilandi")

# ---- 3. Vidjet va ko'prik kodini nusxalash ----
mains = list((MAIN / "java").rglob("MainActivity.java"))
if len(mains) != 1:
    sys.exit(f"MainActivity.java topilmadi: {mains}")
java_dir = mains[0].parent
old = mains[0].read_text(encoding="utf-8")
if "package uz.kunlikreja.app;" not in old:
    sys.exit("Paket nomi kutilgandan boshqacha: capacitor.config.json'dagi appId ni tekshiring")

targets = {
    "MainActivity.java": java_dir,
    "WidgetBridgePlugin.java": java_dir,
    "TaskWidgetProvider.java": java_dir,
    "TaskWidgetMedium.java": java_dir,
    "TaskWidgetSmall.java": java_dir,
    "QuickAddActivity.java": java_dir,
    "widget_tasks.xml": MAIN / "res/layout",
    "widget_medium.xml": MAIN / "res/layout",
    "widget_small.xml": MAIN / "res/layout",
    "widget_bg.xml": MAIN / "res/drawable",
    "widget_info.xml": MAIN / "res/xml",
    "widget_info_medium.xml": MAIN / "res/xml",
    "widget_info_small.xml": MAIN / "res/xml",
    "google-services.json": ROOT / "android/app",
    "debug.keystore": ROOT / "android/app",
}
for i in range(5):
    targets[f"widget_bg_dark_{i}.xml"] = MAIN / "res/drawable"
    targets[f"widget_bg_light_{i}.xml"] = MAIN / "res/drawable"
for name, dest in targets.items():
    src = NATIVE / name
    if not src.exists():
        sys.exit(f"native/{name} topilmadi")
    dest.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dest / name)
    print("Nusxalandi:", name)

# ---- 4. Google bilan kirish: Gradle sozlamalari ----
import re
var = ROOT / "android/variables.gradle"
v = var.read_text(encoding="utf-8")
if "rgcfaIncludeGoogle" not in v:
    v = v.replace("ext {", "ext {\n    rgcfaIncludeGoogle = true", 1)
    var.write_text(v, encoding="utf-8")
    print("variables.gradle: Google kirish yoqildi")

# ---- 5. Doimiy imzo (SHA-1 har safar bir xil bo'lishi uchun) ----
gr = ROOT / "android/app/build.gradle"
g = gr.read_text(encoding="utf-8")
if "debug.keystore" not in g:
    sign = """android {
    signingConfigs {
        debug {
            storeFile file('debug.keystore')
            storePassword 'android'
            keyAlias 'androiddebugkey'
            keyPassword 'android'
        }
    }"""
    g, n = re.subn(r"^android \{", sign, g, count=1, flags=re.M)
    if n != 1:
        sys.exit("app/build.gradle ichida 'android {' topilmadi")
    gr.write_text(g, encoding="utf-8")
    print("Doimiy imzo ulandi")

# ---- 6. Ikonka (Rejam) va fon rangi ----
res = MAIN / "res"
icons = NATIVE / "icons"
if icons.exists():
    for d in icons.iterdir():
        if d.is_dir():
            dest = res / d.name
            dest.mkdir(parents=True, exist_ok=True)
            for f in d.iterdir():
                shutil.copyfile(f, dest / f.name)
    (res / "values").mkdir(parents=True, exist_ok=True)
    (res / "values/ic_launcher_background.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="ic_launcher_background">#051335</color>\n</resources>\n',
        encoding="utf-8")
    print("Ikonka almashtirildi")

# ---- 7. Budilnik ovozlari (res/raw) ----
snd = NATIVE / "sounds"
if snd.exists():
    raw = res / "raw"
    raw.mkdir(parents=True, exist_ok=True)
    for f in snd.glob("*.wav"):
        shutil.copyfile(f, raw / f.name)
    print("Budilnik ovozlari nusxalandi")

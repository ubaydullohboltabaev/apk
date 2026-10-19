"""Android manifestga budilnik uchun kerakli ruxsatlarni qo'shadi."""
import pathlib
import sys

p = pathlib.Path("android/app/src/main/AndroidManifest.xml")
s = p.read_text(encoding="utf-8")

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

if "</manifest>" not in s:
    sys.exit("AndroidManifest.xml topilmadi yoki buzilgan")
s = s.replace("</manifest>", add + "</manifest>")
p.write_text(s, encoding="utf-8")
print("Ruxsatlar qo'shildi:", len(add.splitlines()), "ta")

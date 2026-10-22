# Kunlik Reja: APK yig'ish

Loyiha tarkibi:
- `www/index.html`: ilovaning o'zi (o'zbekcha lotin va kirill tillari)
- `native/`: Android vidjeti va uning ko'prigi (yig'ish paytida avtomatik qo'shiladi)
- `scripts/patch_manifest.py`: ruxsatlar va vidjetni Android loyihasiga qo'shadi
- `.github/workflows/build-apk.yml`: GitHub'da APK yig'adi

Yangilash: o'zgargan fayllarni GitHub'da shu yo'llar bilan yuklang (**Add file → Upload files**), keyin **Actions** bo'limida yig'ish tugashini kuting va `Kunlik-Reja-APK` ni yuklab oling.

Vidjetni qo'yish: telefon bosh ekranida bo'sh joyni uzoq bosing, **Vidjetlar** ni tanlang, «Kunlik Reja» ni toping va ekranga qo'ying.

Hisob (Google bilan kirish): `native/google-services.json` Firebase sozlamasi, `native/debug.keystore` doimiy imzo (SHA-1 har safar bir xil bo'lishi uchun). Firebase'da Authentication (Google) va Firestore yoqilgan bo'lishi kerak.

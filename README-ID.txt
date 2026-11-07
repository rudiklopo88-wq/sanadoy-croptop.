SANADOY CROPTOP — SCREEN CROP AI (SOURCE PROJECT)

NAMA APLIKASI
- Nama yang tampil di Android: Sanadoy croptop.
- applicationId tetap id.aibelajar.app agar versi baru dapat memperbarui instalasi lama tanpa mengubah identitas paket.

LOGO CUSTOM
- Logo custom belum dipasang karena file gambar logo belum diberikan.
- Setelah Anda mengunggah gambar logo, pasang sebagai ikon launcher Android (adaptive icon) lalu build ulang.

FITUR
- Meminta izin Android untuk mengambil layar.
- Mengambil satu frame layar setelah izin diberikan.
- Memilih area soal dengan sentuhan (crop manual).
- Mengirim gambar crop ke backend /solve.
- AI memberikan jawaban dan penjelasan Bahasa Indonesia.

PENTING
1. Ini source code Android + backend, bukan APK siap pasang.
2. Backend wajib di-hosting dengan HTTPS. Jangan menaruh API key OpenAI di APK.
3. Endpoint backend harus memiliki route POST /solve.
4. Aplikasi mengambil satu gambar layar setelah persetujuan sistem, bukan membaca layar terus-menerus.
5. Beberapa aplikasi yang memakai FLAG_SECURE memang tidak bisa ditangkap.
6. Uji pada perangkat Android nyata.

BUILD APK DENGAN HP ANDROID + GITHUB
1. Ekstrak ZIP ini di HP.
2. Buat repository baru di GitHub, misalnya sanadoy-croptop.
3. Unggah seluruh ISI folder proyek (termasuk folder .github) ke repository, bukan ZIP-nya.
4. Buka tab Actions di repository. Jika diminta, aktifkan GitHub Actions.
5. Pilih workflow “Build Sanadoy croptop APK”, lalu tekan Run workflow. Tunggu sampai status hijau (success).
6. Buka hasil run tersebut, cari bagian Artifacts, unduh “Sanadoy-croptop-APK”.
7. Ekstrak hasil unduhan, lalu buka app-debug.apk dan izinkan instalasi dari sumber tersebut jika Android memintanya.

CATATAN
- APK debug cocok untuk uji coba; distribusi luas sebaiknya memakai APK release yang ditandatangani.
- Jangan commit file .env atau API key ke GitHub.
- Masukkan URL backend HTTPS di aplikasi sebelum memakai fitur AI.


IKON APLIKASI
Ikon launcher Sanadoy croptop sudah dipasang pada folder app/src/main/res/mipmap-*.
Untuk menggantinya lagi, ganti file ic_launcher.png pada folder mipmap dan file ic_launcher_foreground.png untuk ikon adaptif.

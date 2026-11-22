# Stardew Life — Fabric 1.21.11 (v0.3 gameplay prototype)

Mod Minecraft Java Edition yang menambahkan loop gameplay cozy-farming terinspirasi game farming sim.

## Target
- Minecraft Java 1.21.11
- Fabric Loader 0.18+
- Fabric API 0.141.1+1.21.11
- Java 21
- Gradle / Fabric Loom

## Fitur yang tersedia di source v0.3
- Kalender 4 musim: Spring, Summer, Fall, Winter; 28 hari per musim.
- Status tanggal, tahun, jam dunia, dan musim.
- Dompet koin menggunakan scoreboard dunia.
- Katalog toko dan pembelian item; beberapa item dibatasi musim.
- Jual item dari tangan utama dengan daftar harga.
- Shipping Bin berbasis antrean: item disetorkan, saldo dicairkan setelah hari dunia berganti.
- Sistem energi (scoreboard awal 100; status dapat dicek).
- Farming XP dan level (command admin untuk simulasi XP).
- Sistem friendship untuk 12 nama villager, hadiah, dan sapaan.
- Data uang, XP, energi, friendship, dan shipping disimpan pada scoreboard dunia Minecraft.

## Perintah
- `/life help`
- `/life status`
- `/life calendar`
- `/life season`
- `/life clock`
- `/life wallet`
- `/life shop list`
- `/life shop buy <item> [jumlah]`
- `/life sellhand`
- `/life shiphand`
- `/life shipping status`
- `/life shipping collect`
- `/life energy`
- `/life farmlevel`
- `/life friendship <villager>`
- `/life gift <villager>`
- `/life hello <villager>`
- `/life farmxp <jumlah>` (butuh permission level 2)

Nama villager yang dikenali: Abigail, Alex, Elliott, Emily, Haley, Harvey, Leah, Maru, Penny, Sam, Sebastian, Shane.

## Cara build
1. Install JDK 21.
2. Buka folder project di terminal.
3. Jalankan `gradle build` (atau gunakan Gradle wrapper jika kamu menambahkannya).
4. File mod hasil build ada di `build/libs/`.
5. Salin JAR ke folder `mods` instance Fabric Minecraft 1.21.11.
6. Tes di world salinan terlebih dahulu.

## Penting: batas implementasi build ini
Ini adalah **gameplay prototype berbasis command**, bukan replika Stardew Valley penuh. Sistem berikut belum diimplementasikan sebagai fitur visual/otomatis:
- blok Shipping Bin yang bisa diletakkan dan GUI inventori;
- GUI toko dengan tombol Buy/Sell;
- crop block baru, kalender pertumbuhan tanaman, kualitas Bronze/Silver/Gold/Iridium;
- NPC villager custom dengan jadwal, dialog, hati/animasi, dan event;
- worldgen village/seed khusus, bangunan, festival, dan event musim;
- regenerasi energi otomatis saat tidur dan integrasi energi dengan aktivitas vanilla;
- menu jam/kalender berbentuk layar GUI.

Command friendship dan gift menyimpan progres per pemain pada scoreboard, tetapi belum mendeteksi interaksi fisik atau hadiah yang diberikan langsung kepada entity villager. Nama NPC adalah daftar relasi simulasi.

## Catatan teknis
- Belum ada JAR terkompilasi yang disertakan.
- Source belum dapat diverifikasi dengan build Gradle di lingkungan pembuatan file ini. Jalankan `gradle build` dan kirimkan error lengkap jika ada; jangan pasang ke world utama sebelum diuji.
- Gunakan salinan world untuk pengujian.

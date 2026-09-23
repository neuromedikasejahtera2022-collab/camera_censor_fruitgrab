# 🎨 Panduan Mengubah Objek Gambar di VSCode (Fruit Catcher Duo)

Seluruh objek permainan (apple, banana, orange, melon, golden fruit, zonk) kini menggunakan file drawable terpisah di folder `app/src/main/res/drawable/` sehingga Anda dapat dengan sangat mudah mengganti atau menyesuaikan gambarnya langsung di **VSCode**.

---

### 📁 Lokasi File Objek di Project

| Objek Permainan | Poin | File Resource Drawable | Ikon Saat Ini (Sesuai Upload) |
|---|---|---|---|
| **Apple** | +100 | `app/src/main/res/drawable/item_apple.xml` | **TikTok** (icon_164) |
| **Banana** | +200 | `app/src/main/res/drawable/item_banana.xml` | **Facebook** (icon_167) |
| **Orange** | +300 | `app/src/main/res/drawable/item_orange.xml` | **Telegram** (icon_171) |
| **Melon** | +400 | `app/src/main/res/drawable/item_melon.xml` | **Pinterest** (icon_173) |
| **Golden Fruit** | +500 | `app/src/main/res/drawable/item_golden.xml` | **LinkedIn** (icon_175) |
| **Zonk (Penalty)** | -150 | `app/src/main/res/drawable/item_zonk.xml` | **Reddit** (icon_181) |

---

### 🛠️ Cara Mengganti Gambar Objek di VSCode

#### Cara 1: Menggunakan Gambar PNG / JPG Sendiri (Paling Mudah)
1. Siapkan file gambar Anda (rekomendasi: format `.png` atau `.webp` transparan, ukuran rasio 1:1 seperti 128x128 atau 256x256 px).
2. Hapus file `.xml` yang ingin diganti (contoh: hapus `item_apple.xml` di `app/src/main/res/drawable/`).
3. Salin/tarik file gambar baru Anda ke dalam folder `app/src/main/res/drawable/` dengan nama yang sama persis:
   - `item_apple.png`
   - `item_banana.png`
   - `item_orange.png`
   - `item_melon.png`
   - `item_golden.png`
   - `item_zonk.png`
4. Android akan langsung menggunakan file gambar baru tersebut secara otomatis tanpa perlu mengubah baris kode Kotlin apa pun!

#### Cara 2: Mengedit Warna & Desain Vector XML di VSCode
1. Buka file XML objek di `app/src/main/res/drawable/` (misal: `item_apple.xml`).
2. Anda dapat mengubah nilai warna `android:fillColor="#..."` atau menambahkan elemen path SVG baru.

#### Cara 3: Mengubah Nilai Poin, Nama, atau Bobot Kemunculan
- **Nilai Poin & Label**: Buka `app/src/main/java/com/example/models/GameModels.kt` pada bagian `enum class FruitType`.
- **Frekuensi / Bobot Muncul**: Buka `app/src/main/java/com/example/config/GameConfig.kt` (misal bobot apple 50%, banana 25%, dll).

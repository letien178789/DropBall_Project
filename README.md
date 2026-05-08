# BALL FALL (Java Swing)

Game BALL FALL viết bằng Java Swing, chạy độc lập bằng `javac/java`.

## Sơ đồ liên kết file (Dependency Map)

```text
BallFallGame.java
 └─ tạo JFrame và gắn GamePanel

GamePanel.java
 ├─ dùng GameButton.java        (vẽ/click button)
 ├─ dùng Layer.java             (dữ liệu từng layer)
 ├─ dùng Scene.java             (trạng thái màn hình)
 ├─ dùng SettingsData.java      (âm lượng)
 ├─ dùng ProgressData.java      (level gần nhất)
 ├─ gọi SaveManager.java        (load/save save_state.properties)
 └─ gọi MusicManager.java       (phát MB1/MB2/MB3 nếu có JavaFX)

SaveManager.java
 ├─ đọc/ghi save_state.properties
 └─ map dữ liệu vào SettingsData + ProgressData
```

## Giải thích từng file

- `src/ballfall/BallFallGame.java`
  - Entry point của game.
  - Tạo cửa sổ game (`JFrame`) và gắn `GamePanel` vào content pane.

- `src/ballfall/GamePanel.java`
  - File quan trọng nhất: chứa game loop (`Timer`), render, input, chuyển scene.
  - Quản lý các màn: `MENU`, `LEVEL_MENU`, `SETTING`, `GAME`.
  - Vẽ background theo cơ chế **aspect-fill + center crop** để tự cắt ảnh sai tỉ lệ.
  - Gameplay chính:
    - Ball rơi từ trên xuống qua các lớp.
    - Layer dạng lát trụ đáy tròn xếp chồng gần như không khoảng cách.
    - D.B là cung nguy hiểm quay liên tục; P.B là phần còn lại.
    - Combo + invincible 5s khi đủ chuỗi phá layer.

- `src/ballfall/GameButton.java`
  - Component button đơn giản: lưu `rect`, text, hàm `draw(...)`, `clicked(...)`.

- `src/ballfall/Layer.java`
  - Model dữ liệu 1 layer: vị trí Y, góc nguy hiểm, màu P.B, góc quay hiện tại, tốc độ quay.

- `src/ballfall/Scene.java`
  - Enum scene để tránh hardcode string trạng thái màn hình.

- `src/ballfall/SettingsData.java`
  - Model lưu âm lượng `bgmVolume` và `sfxVolume`.

- `src/ballfall/ProgressData.java`
  - Model lưu `lastLevel`.

- `src/ballfall/SaveManager.java`
  - Đọc/ghi file `save_state.properties`.
  - Dùng để persist `last_level`, `bgm_volume`, `sfx_volume`.

- `src/ballfall/MusicManager.java`
  - Quản lý nhạc nền loop theo scene.
  - Dùng JavaFX qua reflection (nếu không có JavaFX thì game vẫn chạy, chỉ tắt nhạc).

## Luồng hoạt động nhanh

1. Chạy `main()` trong `BallFallGame`.
2. `GamePanel` khởi tạo, load background + load save + start timer.
3. Mỗi tick timer:
   - update physics (ball/layer rotation)
   - repaint scene.
4. Input chuột/phím đổi scene hoặc tương tác gameplay.
5. Khi đổi setting/progress -> `SaveManager.save(...)`.

## Chạy game

```bash
javac src/ballfall/*.java
java -cp src/ballfall BallFallGame
```

## Asset cần có

Đặt các file ở root project (hoặc `assets/`):
- Hình nền: `BG1.png`, `BG2.png`, `BG3.png`
- Nhạc nền: `MB1.mp3`, `MB2.mp3`, `MB3.mp3`

## Save file

Game tạo file `save_state.properties` với:
- `last_level`
- `bgm_volume`
- `sfx_volume`

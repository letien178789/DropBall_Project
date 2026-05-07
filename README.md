# BALL FALL (Java)

Game BALL FALL viết bằng Java Swing và đã chỉnh lại để tránh lỗi import/package khi compile.

## Cấu trúc mã nguồn

Tất cả file `.java` nằm trong `src/ballfall/`:
- `BallFallGame.java`: điểm vào chương trình.
- `GamePanel.java`: scene, render, input, gameplay.
- `GameButton.java`: nút UI dùng lại.
- `Scene.java`, `Layer.java`, `SettingsData.java`, `ProgressData.java`: model.
- `SaveManager.java`: đọc/ghi `save_state.properties`.

## Chạy game

> Nếu gặp lỗi `'javac' is not recognized...` thì cần cài JDK 17+ và mở terminal mới.

### Cách 1 (khuyến nghị)
```bash
javac src/ballfall/*.java
java -cp src/ballfall BallFallGame
```

### Cách 2 (khi đang đứng trong thư mục `src/ballfall`)
```bash
javac BallFallGame.java
java BallFallGame
```

## Lưu trạng thái

Game sẽ tạo `save_state.properties` để lưu:
- `last_level`
- `bgm_volume`
- `sfx_volume`

## Cập nhật mới

- Khung game ngang: **1920 x 1080**.
- Nhạc nền theo màn:
  - `MB1.mp3` cho Home
  - `MB2.mp3` cho Level/Setting
  - `MB3.mp3` cho Game

> Đặt các file `MB1.mp3`, `MB2.mp3`, `MB3.mp3` tại thư mục gốc project.
> Phát nhạc dùng JavaFX runtime (nếu máy có JavaFX). Nếu không có JavaFX, game vẫn chạy nhưng bỏ qua nhạc.

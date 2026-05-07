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

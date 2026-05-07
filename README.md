# BALL FALL (Java)

Game BALL FALL viết bằng Java Swing, được tách thành nhiều lớp nhỏ để dễ mở rộng.

## Cấu trúc mã nguồn

- `src/ballfall/BallFallGame.java`: điểm vào chương trình.
- `src/ballfall/ui/GamePanel.java`: xử lý scene, render, input, gameplay.
- `src/ballfall/ui/GameButton.java`: nút UI dùng lại.
- `src/ballfall/model/*`: model nhỏ (`Scene`, `Layer`, `SettingsData`, `ProgressData`).
- `src/ballfall/core/SaveManager.java`: đọc/ghi `save_state.properties`.

## Chạy game

### 1) Cài Java Development Kit (JDK)

Nếu gặp lỗi:

`'javac' is not recognized as an internal or external command, operable program or batch file.`

thì máy bạn chưa cài JDK hoặc chưa thêm JDK vào `PATH`.

- Windows: cài **JDK 17 hoặc mới hơn** (Temurin/Oracle/OpenJDK).
- Sau khi cài, mở terminal mới và kiểm tra:

```bash
java -version
javac -version
```

> Cả hai lệnh đều phải chạy được.

### 2) Compile và chạy

```bash
javac src/ballfall/model/*.java src/ballfall/core/*.java src/ballfall/ui/*.java src/ballfall/BallFallGame.java
java -cp src ballfall.BallFallGame
```

## Lưu trạng thái

Game sẽ tạo `save_state.properties` để lưu:
- `last_level`
- `bgm_volume`
- `sfx_volume`

# BALL FALL (Java)

Game BALL FALL viết bằng Java Swing, được tách thành nhiều lớp nhỏ để dễ mở rộng.

## Cấu trúc mã nguồn

- `src/ballfall/BallFallGame.java`: điểm vào chương trình.
- `src/ballfall/ui/GamePanel.java`: xử lý scene, render, input, gameplay.
- `src/ballfall/ui/GameButton.java`: nút UI dùng lại.
- `src/ballfall/model/*`: model nhỏ (`Scene`, `Layer`, `SettingsData`, `ProgressData`).
- `src/ballfall/core/SaveManager.java`: đọc/ghi `save_state.properties`.

## Chạy game

```bash
javac src/ballfall/model/*.java src/ballfall/core/*.java src/ballfall/ui/*.java src/ballfall/BallFallGame.java
java -cp src ballfall.BallFallGame
```

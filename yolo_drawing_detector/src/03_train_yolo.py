from ultralytics import YOLO


def train_yolo():
    model = YOLO("yolov8s.pt")

    model.train(
        data="data.yaml",
        imgsz=1280,
        epochs=100,
        batch=4,
        project="runs/detect",
        name="electrical_yolo",
        device=0
    )


if __name__ == "__main__":
    train_yolo()
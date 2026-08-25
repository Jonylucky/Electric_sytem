from pathlib import Path
from ultralytics import YOLO


ROOT = Path(__file__).resolve().parents[1]


def train_yolo():
    data_yaml = ROOT / "data.yaml"
    runs_dir = ROOT / "runs" / "detect"

    if not data_yaml.exists():
        raise FileNotFoundError(f"Cannot find data.yaml at: {data_yaml}")

    model = YOLO("yolov8s.pt")

    model.train(
        data=str(data_yaml),
        imgsz=1024,
        epochs=50,
        batch=1,
        project=str(runs_dir),
        name="electrical_yolo",
        device="cpu",
        workers=0
    )


if __name__ == "__main__":
    train_yolo()
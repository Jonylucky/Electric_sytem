from pathlib import Path
import random
import shutil


RAW_IMAGES = Path("data/raw_images")
RAW_LABELS = Path("data/raw_labels")
DATASET = Path("data/dataset")

TRAIN_RATIO = 0.8


def make_dirs():
    folders = [
        DATASET / "images/train",
        DATASET / "images/val",
        DATASET / "labels/train",
        DATASET / "labels/val",
    ]

    for folder in folders:
        folder.mkdir(parents=True, exist_ok=True)


def clear_old_dataset():
    target_dirs = [
        DATASET / "images/train",
        DATASET / "images/val",
        DATASET / "labels/train",
        DATASET / "labels/val",
    ]

    for target_dir in target_dirs:
        if target_dir.exists():
            for file in target_dir.glob("*"):
                if file.is_file():
                    file.unlink()


def copy_pair(image_path, image_target_dir, label_target_dir):
    label_path = RAW_LABELS / f"{image_path.stem}.txt"

    if not label_path.exists():
        print(f"Missing label for image: {image_path.name}")
        return False

    shutil.copy(image_path, image_target_dir / image_path.name)
    shutil.copy(label_path, label_target_dir / label_path.name)

    return True


def split_dataset():
    make_dirs()
    clear_old_dataset()

    images = list(RAW_IMAGES.glob("*.png"))

    if not images:
        print("No images found in data/raw_images")
        return

    random.shuffle(images)

    split_index = int(len(images) * TRAIN_RATIO)

    train_images = images[:split_index]
    val_images = images[split_index:]

    if len(val_images) == 0 and len(images) > 1:
        val_images = [train_images.pop()]

    train_count = 0
    val_count = 0

    for img in train_images:
        ok = copy_pair(
            img,
            DATASET / "images/train",
            DATASET / "labels/train"
        )
        if ok:
            train_count += 1

    for img in val_images:
        ok = copy_pair(
            img,
            DATASET / "images/val",
            DATASET / "labels/val"
        )
        if ok:
            val_count += 1

    print(f"Train images: {train_count}")
    print(f"Val images: {val_count}")


if __name__ == "__main__":
    split_dataset()
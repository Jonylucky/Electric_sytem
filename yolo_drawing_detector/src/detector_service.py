from pathlib import Path

import fitz
from PIL import Image, ImageDraw, ImageFont
from ultralytics import YOLO
from src.ocr_service import read_all_fcu_names_from_red_text

MODEL_PATH = Path("models/best.pt")

DPI = 300
IMG_SIZE = 1024
CONF = 0.4
CLASS_NAMES = {
    0: "FCU"
    # 1: "CB",
    # 2: "ATS",
    # 3: "Meter",
    # 4: "Transformer"
}

_model = None


def get_model():
    global _model

    if _model is None:
        if not MODEL_PATH.exists():
            raise FileNotFoundError(
                f"YOLO model not found: {MODEL_PATH}. "
                f"Please put best.pt into models/best.pt"
            )

        _model = YOLO(str(MODEL_PATH))

    return _model

def assign_fcu_names_to_devices(devices, labels, max_distance=500):
    pairs = []

    for device_index, device in enumerate(devices):
        x1, y1, x2, y2 = device["bbox_px"]
        device_center_x = (x1 + x2) / 2
        device_center_y = (y1 + y2) / 2

        for label_index, label in enumerate(labels):
            label_center_x, label_center_y = label["center"]

            distance = (
                (label_center_x - device_center_x) ** 2
                + (label_center_y - device_center_y) ** 2
            ) ** 0.5

            pairs.append({
                "device_index": device_index,
                "label_index": label_index,
                "distance": distance
            })

    pairs.sort(key=lambda item: item["distance"])

    used_devices = set()
    used_labels = set()

    for pair in pairs:
        if pair["distance"] > max_distance:
            continue

        device_index = pair["device_index"]
        label_index = pair["label_index"]

        if device_index in used_devices or label_index in used_labels:
            continue

        label = labels[label_index]

        devices[device_index]["fcu_name"] = label["text"]
        devices[device_index]["fcu_name_confidence"] = label["confidence"]
        devices[device_index]["fcu_name_distance"] = float(pair["distance"])

        used_devices.add(device_index)
        used_labels.add(label_index)

    for device in devices:
        device.setdefault("fcu_name", None)
        device.setdefault("fcu_name_confidence", None)
        device.setdefault("fcu_name_distance", None)

    return devices
def convert_bbox_to_normalized(bbox_px, image_width, image_height):
    x1, y1, x2, y2 = bbox_px

    return [
        x1 / image_width,
        y1 / image_height,
        x2 / image_width,
        y2 / image_height,
    ]


def image_bbox_to_pdf_bbox(bbox_px, pdf_height_pt, dpi):
    x1, y1, x2, y2 = bbox_px

    scale = dpi / 72

    x1_pt = x1 / scale
    x2_pt = x2 / scale

    y1_pt = pdf_height_pt - (y2 / scale)
    y2_pt = pdf_height_pt - (y1 / scale)

    return [x1_pt, y1_pt, x2_pt, y2_pt]


def detect_image(image_path: str):
    image_path = Path(image_path)

    if not image_path.exists():
        raise FileNotFoundError(f"Image not found: {image_path}")

    model = get_model()

    with Image.open(image_path) as image:
        image_width, image_height = image.size

    results = model.predict(
        source=str(image_path),
        imgsz=IMG_SIZE,
        conf=CONF,
        save=True,
        project="outputs",
        name=f"predict_{image_path.stem}",
        exist_ok=True,
        verbose=False
    )

    all_devices = []

    for result in results:
        for box in result.boxes:
            class_id = int(box.cls[0])
            confidence = float(box.conf[0])

            x1, y1, x2, y2 = box.xyxy[0].tolist()
            bbox_px = [float(x1), float(y1), float(x2), float(y2)]

            bbox_norm = convert_bbox_to_normalized(
                bbox_px,
                image_width,
                image_height
            )

         

            device = {
                "page_number": 1,
                "device_type": CLASS_NAMES.get(class_id, "unknown"),
                "class_id": class_id,
                "confidence": confidence,

                "image_width": image_width,
                "image_height": image_height,

                "bbox_px": bbox_px,
                "bbox_norm": bbox_norm,

             

                "image_path": str(image_path)
            }
            all_devices.append(device)

        labels = read_all_fcu_names_from_red_text(image_path)
    all_devices = assign_fcu_names_to_devices(all_devices, labels)

    annotated_image_path = render_detected_image(image_path, all_devices)

    for device in all_devices:
        device["original_image_path"] = str(image_path)
        device["original_image_url"] = f"/uploads/{image_path.name}"

        device["annotated_image_path"] = str(annotated_image_path)
        device["annotated_image_url"] = f"/outputs/annotated/{annotated_image_path.name}"

    return all_devices

    return all_devices

def render_detected_image(image_path: Path, devices):
    output_dir = Path("outputs/annotated")
    output_dir.mkdir(parents=True, exist_ok=True)

    annotated_image_path = output_dir / f"{image_path.stem}_detected.jpg"

    with Image.open(image_path).convert("RGB") as image:
        draw = ImageDraw.Draw(image)

        try:
            font = ImageFont.truetype("arial.ttf", 28)
        except OSError:
            font = ImageFont.load_default()

        for device in devices:
            x1, y1, x2, y2 = device["bbox_px"]

            label = device.get("fcu_name") or device.get("device_type", "device")
            confidence = device.get("confidence", 0)

            text = f"{label} {confidence:.2f}"

            # Vẽ box
            draw.rectangle(
                [(x1, y1), (x2, y2)],
                outline="red",
                width=5
            )

            # Vẽ nền text
            text_bbox = draw.textbbox((x1, y1), text, font=font)
            text_width = text_bbox[2] - text_bbox[0]
            text_height = text_bbox[3] - text_bbox[1]

            text_bg_top = max(0, y1 - text_height - 8)

            draw.rectangle(
                [
                    (x1, text_bg_top),
                    (x1 + text_width + 10, text_bg_top + text_height + 8)
                ],
                fill="red"
            )

            draw.text(
                (x1 + 5, text_bg_top + 4),
                text,
                fill="white",
                font=font
            )

        image.save(annotated_image_path, quality=95)

    return annotated_image_path

def detect_pdf(pdf_path: str):
    pdf_path = Path(pdf_path)

    if not pdf_path.exists():
        raise FileNotFoundError(f"PDF not found: {pdf_path}")

    output_image_dir = Path("outputs/rendered_pages") / pdf_path.stem
    output_image_dir.mkdir(parents=True, exist_ok=True)

    model = get_model()
    doc = fitz.open(str(pdf_path))

    all_devices = []

    for page_index, page in enumerate(doc):
        page_number = page_index + 1

        pdf_width_pt = page.rect.width
        pdf_height_pt = page.rect.height

        pix = page.get_pixmap(dpi=DPI)

        image_path = output_image_dir / f"page_{page_number}.png"
        pix.save(str(image_path))

        image_width = pix.width
        image_height = pix.height

        results = model.predict(
            source=str(image_path),
            imgsz=IMG_SIZE,
            conf=CONF,
            save=True,
            project="outputs",
            name=f"predict_{pdf_path.stem}",
            exist_ok=True,
            verbose=False
        )

        for result in results:
            for box in result.boxes:
                class_id = int(box.cls[0])
                confidence = float(box.conf[0])

                x1, y1, x2, y2 = box.xyxy[0].tolist()
                bbox_px = [float(x1), float(y1), float(x2), float(y2)]

                bbox_norm = convert_bbox_to_normalized(
                    bbox_px,
                    image_width,
                    image_height
                )

                bbox_pt = image_bbox_to_pdf_bbox(
                    bbox_px,
                    pdf_height_pt,
                    DPI
                )

                device = {
                    "page_number": page_number,
                    "device_type": CLASS_NAMES.get(class_id, "unknown"),
                    "class_id": class_id,
                    "confidence": confidence,

                    "dpi": DPI,
                    "image_width": image_width,
                    "image_height": image_height,

                    "pdf_width_pt": pdf_width_pt,
                    "pdf_height_pt": pdf_height_pt,

                    "bbox_px": bbox_px,
                    "bbox_norm": bbox_norm,
                    "bbox_pt": bbox_pt,

                    "image_path": str(image_path)
                }

                all_devices.append(device)

    doc.close()

    return all_devices
 
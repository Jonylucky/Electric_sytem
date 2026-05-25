import json
from pathlib import Path

import fitz
from ultralytics import YOLO


MODEL_PATH = "models/best.pt"
DPI = 200
IMG_SIZE = 1280
CONF = 0.25

CLASS_NAMES = {
    0: "ACB",
    1: "CB",
    2: "ATS",
    3: "Meter",
    4: "Transformer"
}


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


def detect_pdf(pdf_path: str):
    pdf_path = Path(pdf_path)

    output_image_dir = Path("outputs/rendered_pages") / pdf_path.stem
    output_image_dir.mkdir(parents=True, exist_ok=True)

    model = YOLO(MODEL_PATH)
    doc = fitz.open(pdf_path)

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
            exist_ok=True
        )

        for result in results:
            for box in result.boxes:
                class_id = int(box.cls[0])
                confidence = float(box.conf[0])

                x1, y1, x2, y2 = box.xyxy[0].tolist()
                bbox_px = [x1, y1, x2, y2]

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

                all_devices.append({
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
                    "bbox_pt": bbox_pt
                })

    return all_devices
import re
from pathlib import Path

import easyocr
import numpy as np
from PIL import Image


_reader = None


def get_ocr_reader():
    global _reader

    if _reader is None:
        _reader = easyocr.Reader(["en"], gpu=False)

    return _reader


def crop_around_bbox(image_path, bbox_px, padding=250):
    image_path = Path(image_path)

    with Image.open(image_path).convert("RGB") as image:
        width, height = image.size

        x1, y1, x2, y2 = bbox_px

        left = max(0, int(x1 - padding))
        top = max(0, int(y1 - padding))
        right = min(width, int(x2 + padding))
        bottom = min(height, int(y2 + padding))

        crop = image.crop((left, top, right, bottom))

    return crop, left, top

def keep_only_red_text(crop):
    arr = np.array(crop)

    r = arr[:, :, 0]
    g = arr[:, :, 1]
    b = arr[:, :, 2]

    red_mask = (
        (r > 120) &
        (r > g * 1.4) &
        (r > b * 1.4)
    )

    output = np.ones_like(arr) * 255
    output[red_mask] = [0, 0, 0]

    return output


def read_fcu_name_from_red_text(image_path, bbox_px, padding=250):
    reader = get_ocr_reader()

    crop, crop_left, crop_top = crop_around_bbox(
        image_path,
        bbox_px,
        padding=padding
    )

    red_text_image = keep_only_red_text(crop)

    results = reader.readtext(
        red_text_image,
        allowlist="Ff0123456789-",
        detail=1,
        paragraph=False
    )

    device_x1, device_y1, device_x2, device_y2 = bbox_px
    device_center_x = (device_x1 + device_x2) / 2
    device_center_y = (device_y1 + device_y2) / 2

    candidates = []

    for text_bbox, text, confidence in results:
        cleaned = text.upper().replace(" ", "")

        match = re.search(r"F\d{2}-\d{2}", cleaned)

        if not match:
            continue

        xs = [point[0] for point in text_bbox]
        ys = [point[1] for point in text_bbox]

        text_center_x = crop_left + (min(xs) + max(xs)) / 2
        text_center_y = crop_top + (min(ys) + max(ys)) / 2

        distance = (
            (text_center_x - device_center_x) ** 2
            + (text_center_y - device_center_y) ** 2
        ) ** 0.5

        candidates.append({
            "text": match.group(0),
            "confidence": float(confidence),
            "distance": float(distance),
            "text_center": [float(text_center_x), float(text_center_y)]
        })

    if not candidates:
        return None

    candidates.sort(key=lambda item: item["distance"])

    return candidates[0]
def read_all_fcu_names_from_red_text(image_path):
    reader = get_ocr_reader()

    with Image.open(image_path).convert("RGB") as image:
        red_text_image = keep_only_red_text(image)

    results = reader.readtext(
        red_text_image,
        allowlist="Ff0123456789-",
        detail=1,
        paragraph=False
    )

    labels = []

    for text_bbox, text, confidence in results:
        cleaned = text.upper().replace(" ", "")

        match = re.search(r"F\d{2}-\d{2}", cleaned)

        if not match:
            continue

        xs = [point[0] for point in text_bbox]
        ys = [point[1] for point in text_bbox]

        center_x = (min(xs) + max(xs)) / 2
        center_y = (min(ys) + max(ys)) / 2

        labels.append({
            "text": match.group(0),
            "confidence": float(confidence),
            "center": [float(center_x), float(center_y)],
            "bbox": text_bbox
        })

    return labels
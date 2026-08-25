from pathlib import Path
from uuid import uuid4
import traceback

from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from src.detector_service import detect_pdf, detect_image

from fastapi.staticfiles import StaticFiles

app = FastAPI(title="YOLO Drawing Detector API")
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")
app.mount("/outputs", StaticFiles(directory="outputs"), name="outputs")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # sau này đổi thành domain frontend thật
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


UPLOAD_DIR = Path("uploads")
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


@app.get("/")
def health_check():
    return {
        "status": "YOLO drawing detector is running"
    }


@app.post("/api/detect-file")
async def detect_uploaded_file(file: UploadFile = File(...)):
    allowed_extensions = (".pdf", ".png", ".jpg", ".jpeg")
    filename_lower = file.filename.lower()

    if not filename_lower.endswith(allowed_extensions):
        return JSONResponse(
            status_code=400,
            content={
                "success": False,
                "message": "Only PDF, PNG, JPG, JPEG files are allowed"
            }
        )

    unique_name = f"{uuid4()}_{file.filename}"
    saved_path = UPLOAD_DIR / unique_name

    content = await file.read()

    with open(saved_path, "wb") as f:
        f.write(content)

    try:
        if filename_lower.endswith(".pdf"):
            devices = detect_pdf(str(saved_path))
            file_type = "pdf"
        else:
            devices = detect_image(str(saved_path))
            file_type = "image"

        return {
            "success": True,
            "file_type": file_type,
            "file_name": file.filename,
            "saved_path": str(saved_path),
            "device_count": len(devices),
            "devices": devices
        }

    except Exception as e:
        traceback.print_exc()

        return JSONResponse(
            status_code=500,
            content={
                "success": False,
                "message": "Detection failed",
                "error": str(e)
            }
        )
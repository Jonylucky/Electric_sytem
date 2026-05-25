from pathlib import Path
from uuid import uuid4

from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware

from src.detector_service import detect_pdf


app = FastAPI()

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


@app.post("/api/detect-pdf")
async def detect_uploaded_pdf(file: UploadFile = File(...)):
    if not file.filename.lower().endswith(".pdf"):
        return {
            "success": False,
            "message": "Only PDF files are allowed"
        }

    unique_name = f"{uuid4()}_{file.filename}"
    saved_path = UPLOAD_DIR / unique_name

    content = await file.read()

    with open(saved_path, "wb") as f:
        f.write(content)

    devices = detect_pdf(str(saved_path))

    return {
        "success": True,
        "file_name": file.filename,
        "saved_path": str(saved_path),
        "device_count": len(devices),
        "devices": devices
    }
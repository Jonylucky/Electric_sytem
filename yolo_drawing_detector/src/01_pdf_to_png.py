from pathlib import Path
import fitz  # PyMuPDF


RAW_PDF_DIR = Path("data/raw_pdfs")
RAW_IMAGE_DIR = Path("data/raw_images")

DPI = 200


def convert_all_pdfs_to_png():
    RAW_IMAGE_DIR.mkdir(parents=True, exist_ok=True)

    pdf_files = list(RAW_PDF_DIR.glob("*.pdf"))

    if not pdf_files:
        print("No PDF files found in data/raw_pdfs")
        return

    for pdf_path in pdf_files:
        print(f"Processing PDF: {pdf_path}")

        doc = fitz.open(str(pdf_path))

        for page_index, page in enumerate(doc):
            pix = page.get_pixmap(dpi=DPI)

            image_name = f"{pdf_path.stem}_page_{page_index + 1}.png"
            image_path = RAW_IMAGE_DIR / image_name

            pix.save(str(image_path))

            print(f"Saved: {image_path}")
            print(f"Size: {pix.width} x {pix.height}")


if __name__ == "__main__":
    convert_all_pdfs_to_png()
"""
PDF 转换脚本 - 将天津大学校园生活/校务相关 PDF 转为 JSON，供向量库入库。
若未安装合适版本的 Java，会回退使用 PyPDF2。
"""

import json
import os
import shutil
from pathlib import Path
import PyPDF2
import re

def extract_text_from_pdf(pdf_path):
    """使用PyPDF2提取PDF文本"""
    text = ""
    try:
        with open(pdf_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            num_pages = len(pdf_reader.pages)
            
            for page_num in range(num_pages):
                page = pdf_reader.pages[page_num]
                text += page.extract_text()
                text += "\n\n"  # 页面之间添加分隔
                
    except Exception as e:
        print(f"Error reading {pdf_path}: {e}")
        return None
    
    return text

def clean_text(text):
    """清理提取的文本"""
    if not text:
        return ""
    
    # 移除多余的空白字符
    text = re.sub(r'\s+', ' ', text)
    
    # 修复常见的OCR错误
    text = text.replace('　', ' ')  # 替换全角空格
    
    # 按句子分割，便于后续处理
    sentences = re.split(r'[。！？]', text)
    sentences = [s.strip() for s in sentences if s.strip()]
    
    return sentences

def process_pdf_to_json(pdf_path, output_path):
    """处理单个PDF文件并保存为JSON"""
    print(f"Processing {pdf_path}...")
    
    # 提取文本
    text = extract_text_from_pdf(pdf_path)
    if not text:
        print(f"Failed to extract text from {pdf_path}")
        return False
    
    # 清理文本
    sentences = clean_text(text)
    
    # 构建JSON结构
    pdf_name = Path(pdf_path).stem
    json_data = {
        "source": pdf_name,
        "title": pdf_name,
        "content": text,
        "sentences": sentences,
        "metadata": {
            "type": "天津大学校园资料",
            "pages": len(sentences) // 20 + 1  # 估算页数
        }
    }
    
    # 保存为JSON
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(json_data, f, ensure_ascii=False, indent=2)
    
    print(f"Saved to {output_path}")
    return True

def _project_root() -> Path:
    """脚本所在目录（tian-agent）"""
    return Path(__file__).resolve().parent


def _pdf_sources() -> list[Path]:
    """data/origin 下全部 PDF，按文件名排序"""
    origin = _project_root() / "data" / "origin"
    if not origin.is_dir():
        return []
    return sorted(origin.glob("*.pdf"))


def _collect_opendataloader_text(node) -> list[str]:
    """递归收集 opendataloader 结构中的 content 文本（按树序）。"""
    out: list[str] = []
    if isinstance(node, dict):
        c = node.get("content")
        if isinstance(c, str) and c.strip():
            out.append(c.strip())
        for ch in node.get("kids") or []:
            out.extend(_collect_opendataloader_text(ch))
    elif isinstance(node, list):
        for x in node:
            out.extend(_collect_opendataloader_text(x))
    return out


def _normalize_json_file_to_bundle_item(raw, json_path: Path) -> dict | None:
    """
    将 processed 下单个 JSON 转为向量库使用的统一结构（顶层含 content）。
    支持：PyPDF2 生成的 dict；opendataloader 的 kids 结构。
    """
    if isinstance(raw, list):
        # 误把旧版 combined（嵌套列表）当单文件时可跳过
        return None
    if not isinstance(raw, dict):
        return None

    stem = json_path.stem
    if raw.get("kids"):
        texts = _collect_opendataloader_text(raw)
        body = "\n".join(texts) if texts else ""
        fn = raw.get("file name") or raw.get("file_name")
        src = Path(str(fn)).stem if fn else stem
        pages = raw.get("number of pages") or 1
        try:
            pages = int(pages)
        except (TypeError, ValueError):
            pages = 1
        return {
            "source": src,
            "title": stem,
            "content": body,
            "sentences": [],
            "metadata": {
                "type": "天津大学校园资料",
                "pages": pages,
            },
        }

    if "content" in raw and isinstance(raw["content"], str):
        return raw

    return None


def merge_processed_json_to_combined() -> Path | None:
    """
    将 data/processed 下各单文件 JSON 合并为扁平列表，写入 processed 与 data 根目录。
    排除 combined_documents.json，避免把旧合并结果再次嵌套造成 [[[[ 与乱码混入。
    """
    output_dir = _project_root() / "data" / "processed"
    if not output_dir.is_dir():
        return None

    all_documents: list[dict] = []
    for json_file in sorted(output_dir.glob("*.json")):
        if json_file.name == "combined_documents.json":
            continue
        try:
            with open(json_file, "r", encoding="utf-8") as f:
                raw = json.load(f)
        except (OSError, json.JSONDecodeError) as e:
            print(f"Warning: 无法读取 {json_file}: {e}")
            continue
        item = _normalize_json_file_to_bundle_item(raw, json_file)
        if item and item.get("content", "").strip():
            all_documents.append(item)
        else:
            print(f"Warning: 跳过无法识别的 JSON: {json_file}")

    if not all_documents:
        print("Warning: 没有可合并的文档条目。")
        return None

    combined_processed = output_dir / "combined_documents.json"
    data_dir = _project_root() / "data"
    data_combined = data_dir / "combined_documents.json"

    with open(combined_processed, "w", encoding="utf-8") as f:
        json.dump(all_documents, f, ensure_ascii=False, indent=2)

    data_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(combined_processed, data_combined)
    print(f"已合并 {len(all_documents)} 条文档 -> {combined_processed}")
    print(f"已复制至 {data_combined}")
    return data_combined


def main():
    """主函数"""
    pdf_files = _pdf_sources()
    if not pdf_files:
        print("未在 data/origin 下找到任何 .pdf 文件。")
        return

    # 创建输出目录
    output_dir = _project_root() / "data" / "processed"
    output_dir.mkdir(parents=True, exist_ok=True)

    # 处理每个PDF
    for pdf_path in pdf_files:
        output_file = output_dir / f"{pdf_path.stem}.json"
        process_pdf_to_json(pdf_path, output_file)
    
    print("\nConversion completed!")
    print(f"JSON files saved in {output_dir}")

    merge_processed_json_to_combined()
    print("（建库请运行 rag/vectore_store 的 load_document，并视情况清空 chroma_db 与 md5.text）")

if __name__ == "__main__":
    # 首先尝试使用opendataloader-pdf（如果Java已安装）
    try:
        import opendataloader_pdf

        print("Using opendataloader-pdf for conversion...")

        output_dir = _project_root() / "data" / "processed"
        output_dir.mkdir(parents=True, exist_ok=True)

        existing_files = [str(p) for p in _pdf_sources()]

        if existing_files:
            opendataloader_pdf.convert(
                input_path=existing_files,
                output_dir=str(output_dir),
                format="json,markdown"
            )
            print("Conversion completed using opendataloader-pdf!")
            merge_processed_json_to_combined()
        else:
            print("No PDF files in data/origin!")

    except Exception as e:
        print(f"opendataloader-pdf failed (likely due to missing Java): {e}")
        print("Falling back to PyPDF2...")
        
        # 尝试使用PyPDF2
        try:
            main()
        except ImportError:
            print("PyPDF2 not installed. Installing...")
            import subprocess
            subprocess.run(["pip", "install", "PyPDF2"])
            main()

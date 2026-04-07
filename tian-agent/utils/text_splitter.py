"""
轻量级 RecursiveCharacterTextSplitter 实现，不依赖 transformers/torch。
用于规避 langchain_text_splitters 在 Windows 上因 PyTorch DLL 加载失败导致的问题。
"""
from __future__ import annotations

import copy
import re
from typing import Any, Callable, Iterable, Literal

from langchain_core.documents import Document


def _split_text_with_regex(
    text: str, separator: str, *, keep_separator: bool | Literal["start", "end"] = False
) -> list[str]:
    """按分隔符分割文本。"""
    if separator:
        if keep_separator:
            splits_ = re.split(f"({separator})", text)
            splits = (
                ([splits_[i] + splits_[i + 1] for i in range(0, len(splits_) - 1, 2)])
                if keep_separator == "end"
                else ([splits_[i] + splits_[i + 1] for i in range(1, len(splits_), 2)])
            )
            if len(splits_) % 2 == 0:
                splits += splits_[-1:]
            splits = (
                ([*splits, splits_[-1]])
                if keep_separator == "end"
                else ([splits_[0], *splits])
            )
        else:
            splits = re.split(separator, text)
    else:
        splits = list(text)
    return [s for s in splits if s]


class RecursiveCharacterTextSplitter:
    """
    递归按字符分割文本，API 与 langchain_text_splitters.RecursiveCharacterTextSplitter 兼容。
    仅依赖 langchain_core，不依赖 transformers/torch。
    """

    def __init__(
        self,
        chunk_size: int = 4000,
        chunk_overlap: int = 200,
        separators: list[str] | None = None,
        length_function: Callable[[str], int] = len,
        keep_separator: bool | Literal["start", "end"] = False,
        strip_whitespace: bool = True,
        **kwargs: Any,
    ) -> None:
        if chunk_size <= 0:
            raise ValueError(f"chunk_size must be > 0, got {chunk_size}")
        if chunk_overlap < 0:
            raise ValueError(f"chunk_overlap must be >= 0, got {chunk_overlap}")
        if chunk_overlap > chunk_size:
            raise ValueError(
                f"chunk_overlap ({chunk_overlap}) must be <= chunk_size ({chunk_size})"
            )
        self._chunk_size = chunk_size
        self._chunk_overlap = chunk_overlap
        self._separators = separators or ["\n\n", "\n", " ", ""]
        self._length_function = length_function
        self._keep_separator = keep_separator
        self._strip_whitespace = strip_whitespace

    def _join_docs(self, docs: list[str], separator: str) -> str | None:
        text = separator.join(docs)
        if self._strip_whitespace:
            text = text.strip()
        return text or None

    def _merge_splits(self, splits: Iterable[str], separator: str) -> list[str]:
        separator_len = self._length_function(separator)
        docs = []
        current_doc: list[str] = []
        total = 0
        for d in splits:
            len_ = self._length_function(d)
            sep_len = separator_len if len(current_doc) > 0 else 0
            if total + len_ + sep_len > self._chunk_size:
                if len(current_doc) > 0:
                    doc = self._join_docs(current_doc, separator)
                    if doc is not None:
                        docs.append(doc)
                    while total > self._chunk_overlap or (
                        total + len_ + (separator_len if len(current_doc) > 0 else 0)
                        > self._chunk_size
                        and total > 0
                    ):
                        total -= self._length_function(current_doc[0]) + (
                            separator_len if len(current_doc) > 1 else 0
                        )
                        current_doc = current_doc[1:]
            current_doc.append(d)
            total += len_ + (separator_len if len(current_doc) > 1 else 0)
        doc = self._join_docs(current_doc, separator)
        if doc is not None:
            docs.append(doc)
        return docs

    def _split_text(self, text: str, separators: list[str]) -> list[str]:
        final_chunks: list[str] = []
        separator = separators[-1]
        new_separators: list[str] = []
        for i, _s in enumerate(separators):
            separator_ = re.escape(_s)
            if not _s:
                separator = _s
                break
            if re.search(separator_, text):
                separator = _s
                new_separators = separators[i + 1 :]
                break

        separator_ = re.escape(separator)
        splits = _split_text_with_regex(
            text, separator_, keep_separator=self._keep_separator
        )

        good_splits: list[str] = []
        merge_sep = "" if self._keep_separator else separator
        for s in splits:
            if self._length_function(s) < self._chunk_size:
                good_splits.append(s)
            else:
                if good_splits:
                    merged_text = self._merge_splits(good_splits, merge_sep)
                    final_chunks.extend(merged_text)
                    good_splits = []
                if not new_separators:
                    final_chunks.append(s)
                else:
                    other_info = self._split_text(s, new_separators)
                    final_chunks.extend(other_info)
        if good_splits:
            merged_text = self._merge_splits(good_splits, merge_sep)
            final_chunks.extend(merged_text)
        return final_chunks

    def split_text(self, text: str) -> list[str]:
        """分割文本为多个块。"""
        return list(self._split_text(text, self._separators))

    def split_documents(self, documents: Iterable[Document]) -> list[Document]:
        """分割文档列表。"""
        texts, metadatas = [], []
        for doc in documents:
            texts.append(doc.page_content)
            metadatas.append(doc.metadata)
        return self.create_documents(texts, metadatas=metadatas)

    def create_documents(
        self, texts: list[str], metadatas: list[dict[Any, Any]] | None = None
    ) -> list[Document]:
        """从文本列表创建 Document 列表。"""
        metadatas_ = metadatas or [{}] * len(texts)
        documents = []
        for i, text in enumerate(texts):
            for chunk in self.split_text(text):
                metadata = copy.deepcopy(metadatas_[i])
                documents.append(Document(page_content=chunk, metadata=metadata))
        return documents

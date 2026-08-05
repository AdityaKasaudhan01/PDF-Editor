import { EditableDocument } from "@/types/document";

export async function uploadDocument(file: File): Promise<{ id: string; fileName: string; status: string }> {
  const body = new FormData();
  body.append("file", file);

  const response = await fetch("/api/documents", {
    method: "POST",
    body
  });

  if (!response.ok) {
    throw new Error("Document upload failed");
  }

  return response.json();
}

export async function fetchDocumentById(id: string): Promise<EditableDocument> {
  const response = await fetch(`/api/documents/${id}`);

  if (!response.ok) {
    throw new Error("Failed to fetch document");
  }

  return response.json();
}

export async function exportDocument(id: string): Promise<Blob> {
  const response = await fetch(`/api/documents/${id}/export`);

  if (!response.ok) {
    throw new Error("Failed to export document");
  }

  return response.blob();
}

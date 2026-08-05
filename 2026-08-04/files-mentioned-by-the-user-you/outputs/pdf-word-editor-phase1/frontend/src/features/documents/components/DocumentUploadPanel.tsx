import { Upload } from "lucide-react";
import { useState } from "react";
import { uploadDocument } from "../api/documentApi";
import { useDocumentStore } from "@/features/editor/store/documentStore";

export function DocumentUploadPanel() {
  const [fileName, setFileName] = useState<string>("No document loaded");
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { fetchDocument } = useDocumentStore();

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFileName(file.name);
    setUploading(true);
    setError(null);

    try {
      const response = await uploadDocument(file);
      await fetchDocument(response.id);
      setFileName(response.fileName || file.name);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  };

  return (
    <aside className="border-r border-slate-200 bg-white p-4 flex flex-col gap-4">
      <div>
        <h2 className="text-xs font-semibold uppercase text-slate-500 mb-2">Upload</h2>
        <label className="flex h-32 cursor-pointer flex-col items-center justify-center gap-3 rounded border border-dashed border-slate-300 bg-slate-50 text-sm hover:border-accent">
          <Upload className="h-6 w-6 text-accent" aria-hidden="true" />
          <span className="font-medium">Upload PDF</span>
          <input
            className="sr-only"
            type="file"
            accept="application/pdf"
            onChange={handleFileChange}
            disabled={uploading}
          />
          {uploading && <span className="text-xs text-slate-500">Uploading...</span>}
        </label>
      </div>

      {error && (
        <p className="text-sm text-red-600">{error}</p>
      )}

      <section>
        <h2 className="text-xs font-semibold uppercase text-slate-500 mb-2">Document</h2>
        <p className="truncate text-sm">{fileName}</p>
      </section>
    </aside>
  );
}

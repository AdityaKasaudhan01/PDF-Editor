import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { fetchDocumentById } from "../../documents/api/documentApi";
import { EditableDocument, TextRun } from "@/types/document";
import { useHistoryStore } from "./historyStore";

interface DocumentState {
  document: EditableDocument | null;
  loading: boolean;
  error: string | null;
  currentPageIndex: number;
  setDocument: (doc: EditableDocument | null) => void;
  fetchDocument: (id: string) => Promise<void>;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setCurrentPageIndex: (index: number) => void;
  nextPage: () => void;
  previousPage: () => void;
  updateTextRun: (pageIndex: number, runId: string, updates: Partial<TextRun>, recordHistory?: boolean) => void;
  deleteTextRun: (pageIndex: number, runId: string) => void;
  addTextRun: (pageIndex: number, run: TextRun) => void;
  replaceDocument: (doc: EditableDocument) => void;
}

export const useDocumentStore = create<DocumentState>()(
  devtools((set, get) => ({
    document: null,
    loading: false,
    error: null,
    currentPageIndex: 0,
    setDocument: (doc) => set({ document: doc }),
    fetchDocument: async (id) => {
      set({ loading: true, error: null });
      try {
        const doc = await fetchDocumentById(id);
        set({ document: doc, loading: false, currentPageIndex: 0 });
      } catch (err) {
        set({
          loading: false,
          error: err instanceof Error ? err.message : "Failed to fetch document"
        });
      }
    },
    setLoading: (loading) => set({ loading }),
    setError: (error) => set({ error }),
    setCurrentPageIndex: (index) => set({ currentPageIndex: index }),
    nextPage: () => {
      const { document, currentPageIndex } = get();
      if (document && currentPageIndex < document.pages.length - 1) {
        set({ currentPageIndex: currentPageIndex + 1 });
      }
    },
    previousPage: () => {
      const { currentPageIndex } = get();
      if (currentPageIndex > 0) {
        set({ currentPageIndex: currentPageIndex - 1 });
      }
    },
    updateTextRun: (pageIndex, runId, updates, recordHistory = true) => {
      const { document } = get();
      if (!document) return;

      if (recordHistory) {
        const page = document.pages[pageIndex];
        const run = page?.textRuns.find((r) => r.id === runId);
        if (run) {
          useHistoryStore.getState().pushEntry({
            documentId: document.id,
            pageIndex,
            snapshot: { type: "updateTextRun", runId, previous: { ...run } }
          });
        }
      }

      const updatedPages = document.pages.map((page, idx) => {
        if (idx !== pageIndex) return page;
        return {
          ...page,
          textRuns: page.textRuns.map((run) =>
            run.id === runId ? { ...run, ...updates } : run
          )
        };
      });

      set({ document: { ...document, pages: updatedPages } });
    },
    deleteTextRun: (pageIndex, runId) => {
      const { document } = get();
      if (!document) return;

      const page = document.pages[pageIndex];
      const run = page?.textRuns.find((r) => r.id === runId);
      if (run) {
        useHistoryStore.getState().pushEntry({
          documentId: document.id,
          pageIndex,
          snapshot: { type: "deleteTextRun", runId, previous: { ...run } }
        });
      }

      const updatedPages = document.pages.map((page, idx) => {
        if (idx !== pageIndex) return page;
        return {
          ...page,
          textRuns: page.textRuns.filter((run) => run.id !== runId)
        };
      });

      set({ document: { ...document, pages: updatedPages } });
    },
    addTextRun: (pageIndex, newRun) => {
      const { document } = get();
      if (!document) return;

      useHistoryStore.getState().pushEntry({
        documentId: document.id,
        pageIndex,
        snapshot: { type: "addTextRun", runId: newRun.id }
      });

      const updatedPages = document.pages.map((page, idx) => {
        if (idx !== pageIndex) return page;
        return {
          ...page,
          textRuns: [...page.textRuns, newRun]
        };
      });

      set({ document: { ...document, pages: updatedPages } });
    },
    replaceDocument: (doc) => set({ document: doc })
  }))
);

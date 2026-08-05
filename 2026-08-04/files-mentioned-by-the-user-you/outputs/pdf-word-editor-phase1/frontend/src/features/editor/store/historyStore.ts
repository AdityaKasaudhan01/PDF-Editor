import { create } from "zustand";
import { devtools } from "zustand/middleware";

export interface HistoryEntry {
  id: string;
  documentId: string;
  pageIndex: number;
  snapshot: any; // We'll use JSON serializable snapshots
  timestamp: number;
}

interface HistoryState {
  entries: HistoryEntry[];
  currentIndex: number;
  maxEntries: number;
  pushEntry: (entry: Omit<HistoryEntry, "id" | "timestamp">) => void;
  undo: () => HistoryEntry | null;
  redo: () => HistoryEntry | null;
  canUndo: () => boolean;
  canRedo: () => boolean;
  clear: () => void;
}

export const useHistoryStore = create<HistoryState>()(
  devtools((set, get) => ({
    entries: [],
    currentIndex: -1,
    maxEntries: 50,

    pushEntry: (entry) => {
      const { entries, currentIndex, maxEntries } = get();
      const newEntry: HistoryEntry = {
        ...entry,
        id: crypto.randomUUID(),
        timestamp: Date.now()
      };

      // Trim any redo entries if we're not at the end
      const newEntries = entries.slice(0, currentIndex + 1);
      newEntries.push(newEntry);

      // Keep only the last maxEntries
      if (newEntries.length > maxEntries) {
        newEntries.shift();
      }

      set({
        entries: newEntries,
        currentIndex: newEntries.length - 1
      });
    },

    undo: () => {
      const { entries, currentIndex } = get();
      if (currentIndex <= 0) return null;

      const newIndex = currentIndex - 1;
      set({ currentIndex: newIndex });
      return entries[newIndex];
    },

    redo: () => {
      const { entries, currentIndex } = get();
      if (currentIndex >= entries.length - 1) return null;

      const newIndex = currentIndex + 1;
      set({ currentIndex: newIndex });
      return entries[newIndex];
    },

    canUndo: () => {
      const { currentIndex } = get();
      return currentIndex > 0;
    },

    canRedo: () => {
      const { entries, currentIndex } = get();
      return currentIndex < entries.length - 1;
    },

    clear: () => {
      set({ entries: [], currentIndex: -1 });
    }
  }))
);

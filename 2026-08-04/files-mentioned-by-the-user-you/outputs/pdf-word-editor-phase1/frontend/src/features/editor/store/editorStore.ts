import { create } from "zustand";

type EditorTool = "select" | "text" | "image";

type TextFormatting = {
  fontFamily: string;
  fontSize: number;
  fontWeight: string;
  fontStyle: string;
  color: string;
};

type EditorState = {
  activeTool: EditorTool;
  zoom: number;
  formatting: TextFormatting;
  setActiveTool: (tool: EditorTool) => void;
  setZoom: (zoom: number) => void;
  setFormatting: (formatting: Partial<TextFormatting>) => void;
  applyFormatting: (target: any) => void;
};

export const useEditorStore = create<EditorState>((set, get) => ({
  activeTool: "select",
  zoom: 1,
  formatting: {
    fontFamily: "Arial",
    fontSize: 24,
    fontWeight: "normal",
    fontStyle: "normal",
    color: "#000000"
  },
  setActiveTool: (activeTool) => set({ activeTool }),
  setZoom: (zoom) => set({ zoom }),
  setFormatting: (formatting) =>
    set((state) => ({
      formatting: { ...state.formatting, ...formatting }
    })),
  applyFormatting: (target) => {
    const { formatting } = get();
    if (!target) return;

    target.set({
      fontFamily: formatting.fontFamily,
      fontSize: formatting.fontSize,
      fontWeight: formatting.fontWeight,
      fontStyle: formatting.fontStyle,
      fill: formatting.color
    });

    const canvas = target.canvas;
    if (canvas) {
      canvas.requestRenderAll();
    }
  }
}));

import { create } from "zustand";
import type { Canvas } from "fabric";

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
  canvasInstance: Canvas | null;
  setActiveTool: (tool: EditorTool) => void;
  setZoom: (zoom: number) => void;
  setFormatting: (formatting: Partial<TextFormatting>) => void;
  applyFormatting: (target: any) => void;
  setCanvasInstance: (canvas: Canvas | null) => void;
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
  canvasInstance: null,
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
  },
  setCanvasInstance: (canvasInstance) => set({ canvasInstance })
}));

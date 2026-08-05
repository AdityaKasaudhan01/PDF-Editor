import { useState } from "react";
import { Image, MousePointer2, Type } from "lucide-react";
import { useEditorStore } from "../store/editorStore";
import { useDocumentStore } from "../store/documentStore";
import { EditableCanvas } from "./EditableCanvas";
import { FormattingToolbar } from "./FormattingToolbar";

const tools = [
  { id: "select", label: "Select", icon: MousePointer2 },
  { id: "text", label: "Text", icon: Type },
  { id: "image", label: "Image", icon: Image }
] as const;

export function EditorCanvasShell() {
  const activeTool = useEditorStore((state) => state.activeTool);
  const setActiveTool = useEditorStore((state) => state.setActiveTool);
  const zoom = useEditorStore((state) => state.zoom);
  const setZoom = useEditorStore((state) => state.setZoom);
  const { document, currentPageIndex, previousPage, nextPage } = useDocumentStore();
  const [findReplaceOpen, setFindReplaceOpen] = useState(false);

  return (
    <section className="flex flex-col h-full">
      <FormattingToolbar onFindReplace={() => setFindReplaceOpen(true)} />

      <div className="flex h-12 items-center gap-1 border-b border-slate-200 bg-white px-3">
        {tools.map((tool) => {
          const Icon = tool.icon;
          const isActive = activeTool === tool.id;

          return (
            <button
              className={`rounded p-2 ${isActive ? "bg-blue-50 text-accent" : "hover:bg-slate-100"}`}
              key={tool.id}
              title={tool.label}
              type="button"
              onClick={() => setActiveTool(tool.id)}
            >
              <Icon className="h-4 w-4" />
            </button>
          );
        })}

        <div className="ml-auto flex items-center gap-2">
          <button
            className="rounded p-2 hover:bg-slate-100 disabled:opacity-50"
            title="Zoom out"
            type="button"
            disabled={zoom <= 0.5}
            onClick={() => setZoom(Math.max(0.5, zoom - 0.1))}
          >
            <span className="text-xs font-mono">-</span>
          </button>
          <span className="text-xs w-12 text-center">{Math.round(zoom * 100)}%</span>
          <button
            className="rounded p-2 hover:bg-slate-100 disabled:opacity-50"
            title="Zoom in"
            type="button"
            disabled={zoom >= 3}
            onClick={() => setZoom(Math.min(3, zoom + 0.1))}
          >
            <span className="text-xs font-mono">+</span>
          </button>
        </div>
      </div>

      <div className="flex flex-1 items-start justify-center overflow-auto bg-canvas p-8">
        <div className="flex flex-col items-center gap-4">
          <div className="flex items-center gap-2">
            <button
              className="rounded px-3 py-1 hover:bg-slate-200 disabled:opacity-50"
              type="button"
              disabled={!document || currentPageIndex === 0}
              onClick={previousPage}
            >
              Previous
            </button>
            <span className="text-sm">
              Page {document ? currentPageIndex + 1 : 0} of {document?.pages.length || 0}
            </span>
            <button
              className="rounded px-3 py-1 hover:bg-slate-200 disabled:opacity-50"
              type="button"
              disabled={!document || currentPageIndex >= document.pages.length - 1}
              onClick={nextPage}
            >
              Next
            </button>
          </div>
          <EditableCanvas />
        </div>
      </div>
    </section>
  );
}

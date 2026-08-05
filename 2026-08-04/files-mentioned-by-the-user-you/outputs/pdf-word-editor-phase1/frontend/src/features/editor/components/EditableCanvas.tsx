import { useEffect, useRef, useCallback, useState } from "react";
import { Canvas, IText, Object, IEvent } from "fabric";
import { useDocumentStore } from "../store/documentStore";
import { useEditorStore } from "../store/editorStore";
import { useHistoryStore } from "../store/historyStore";
import { TextRun } from "@/types/document";

export function EditableCanvas() {
  const { document, currentPageIndex, updateTextRun, deleteTextRun, addTextRun } = useDocumentStore();
  const { zoom, activeTool, setActiveTool, formatting, applyFormatting } = useEditorStore();
  const { undo, redo, canUndo, canRedo } = useHistoryStore();
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const fabricRef = useRef<Canvas | null>(null);
  const documentRef = useRef(document);
  const pageIndexRef = useRef(currentPageIndex);
  const activeToolRef = useRef(activeTool);
  const formattingRef = useRef(formatting);
  const [renderTrigger, setRenderTrigger] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [findReplaceOpen, setFindReplaceOpen] = useState(false);
  const [findText, setFindText] = useState("");
  const [replaceText, setReplaceText] = useState("");

  documentRef.current = document;
  pageIndexRef.current = currentPageIndex;
  activeToolRef.current = activeTool;
  formattingRef.current = formatting;

  const ptToPx = (pt: number) => pt * (96 / 72);
  const pxToPt = (px: number) => px * (72 / 96);

  useEffect(() => {
    const canvasEl = canvasRef.current;
    if (!canvasEl) return;

    try {
      const canvas = new Canvas(canvasEl, {
        backgroundColor: "#ffffff",
        preserveObjectStacking: true,
        selection: true
      });

      fabricRef.current = canvas;

      return () => {
        canvas.dispose();
        fabricRef.current = null;
      };
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to initialize canvas");
    }
  }, []);

  useEffect(() => {
    if (!document) return;
    setRenderTrigger((prev) => prev + 1);
  }, [document?.id, currentPageIndex]);

  const renderDocument = useCallback(() => {
    const canvas = fabricRef.current;
    const doc = documentRef.current;
    const pageIndex = pageIndexRef.current;

    if (!canvas || !doc) return;

    try {
      canvas.clear();
      canvas.backgroundColor = "#ffffff";

      const page = doc.pages[pageIndex];
      if (!page) return;

      const pageWidthPx = ptToPx(page.width);
      const pageHeightPx = ptToPx(page.height);

      canvas.setWidth(pageWidthPx);
      canvas.setHeight(pageHeightPx);

      page.textRuns.forEach((run) => {
        const left = ptToPx(run.boundingBox.x);
        const top = ptToPx(run.boundingBox.y);
        const fontSize = ptToPx(run.fontSize);

        const text = new IText(run.text, {
          left,
          top,
          fontFamily: run.fontFamily,
          fontSize,
          fill: run.color,
          fontWeight: run.fontWeight === "bold" ? "bold" : "normal",
          fontStyle: run.fontStyle === "italic" ? "italic" : "normal",
          originX: "left",
          originY: "top",
          // @ts-ignore - custom property
          textRunId: run.id,
          selectable: true,
          evented: true,
          editable: true
        });

        canvas.add(text);
      });

      canvas.requestRenderAll();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to render document");
    }
  }, []);

  useEffect(() => {
    renderDocument();
  }, [renderTrigger, renderDocument]);

  useEffect(() => {
    const canvas = fabricRef.current;
    if (!canvas) return;

    const handleTextChanged = (e: { target: IText }) => {
      const textObj = e.target;
      // @ts-ignore
      const runId = textObj.textRunId;
      if (!runId) return;

      const doc = documentRef.current;
      const pageIndex = pageIndexRef.current;
      if (!doc) return;

      const page = doc.pages[pageIndex];
      if (!page) return;

      const run = page.textRuns.find((r) => r.id === runId);
      if (!run) return;

      updateTextRun(pageIndex, runId, {
        text: textObj.text || "",
        boundingBox: {
          ...run.boundingBox,
          x: pxToPt(textObj.left || 0),
          y: pxToPt(textObj.top || 0)
        }
      });
    };

    const handleObjectModified = (e: { target: Object }) => {
      const obj = e.target;
      if (!(obj instanceof IText)) return;

      // @ts-ignore
      const runId = obj.textRunId;
      if (!runId) return;

      const doc = documentRef.current;
      const pageIndex = pageIndexRef.current;
      if (!doc) return;

      const page = doc.pages[pageIndex];
      if (!page) return;

      const run = page.textRuns.find((r) => r.id === runId);
      if (!run) return;

      updateTextRun(pageIndex, runId, {
        boundingBox: {
          x: pxToPt(obj.left || 0),
          y: pxToPt(obj.top || 0),
          width: run.boundingBox.width,
          height: run.boundingBox.height
        }
      });
    };

    canvas.on("text:changed", handleTextChanged);
    canvas.on("object:modified", handleObjectModified);

    return () => {
      canvas.off("text:changed", handleTextChanged);
      canvas.off("object:modified", handleObjectModified);
    };
  }, [updateTextRun]);

  useEffect(() => {
    const canvas = fabricRef.current;
    if (!canvas) return;

    const handleMouseDown = (e: IEvent<MouseEvent>) => {
      if (activeToolRef.current !== "text") return;

      const pointer = canvas.getScenePoint(e.e);
      const doc = documentRef.current;
      const pageIndex = pageIndexRef.current;
      if (!doc) return;

      if (e.target && e.target instanceof IText) {
        (e.target as any).enterEditing();
        return;
      }

      const newRun: TextRun = {
        id: crypto.randomUUID(),
        text: "New Text",
        fontFamily: "Arial",
        fontSize: 24,
        fontWeight: "normal",
        fontStyle: "normal",
        color: "#000000",
        boundingBox: {
          x: pxToPt(pointer.x),
          y: pxToPt(pointer.y),
          width: 100,
          height: 28
        },
        rotation: 0
      };

      addTextRun(pageIndex, newRun);

      const text = new IText(newRun.text, {
        left: pointer.x,
        top: pointer.y,
        fontFamily: newRun.fontFamily,
        fontSize: ptToPx(newRun.fontSize),
        fill: newRun.color,
        originX: "left",
        originY: "top",
        // @ts-ignore
        textRunId: newRun.id,
        selectable: true,
        evented: true,
        editable: true
      });

      canvas.add(text);
      canvas.setActiveObject(text);
      (text as any).enterEditing();
      canvas.requestRenderAll();
    };

    canvas.on("mouse:down", handleMouseDown);
    return () => canvas.off("mouse:down", handleMouseDown);
  }, [addTextRun]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const canvas = fabricRef.current;
      if (!canvas) return;

      const isMac = navigator.platform.toUpperCase().indexOf("MAC") >= 0;
      const modKey = isMac ? e.metaKey : e.ctrlKey;

      if (modKey && e.key === "z" && !e.shiftKey) {
        e.preventDefault();
        const entry = undo();
        if (entry) applyHistoryEntry(entry);
        return;
      }

      if ((modKey && e.key === "y") || (modKey && e.shiftKey && e.key === "z") || (modKey && e.key === "Z")) {
        e.preventDefault();
        const entry = redo();
        if (entry) applyHistoryEntry(entry);
        return;
      }

      if (modKey && e.key === "c") {
        e.preventDefault();
        const activeObject = canvas.getActiveObject();
        if (activeObject) {
          activeObject.clone((cloned: any) => {
            (window as any).__clipboard = cloned;
          });
        }
        return;
      }

      if (modKey && e.key === "v") {
        e.preventDefault();
        const clipboard = (window as any).__clipboard;
        if (clipboard) {
          clipboard.clone((cloned: any) => {
            cloned.set({
              left: (cloned.left || 0) + 10,
              top: (cloned.top || 0) + 10
            });
            canvas.add(cloned);
            canvas.setActiveObject(cloned);
            canvas.requestRenderAll();
          });
        }
        return;
      }

      if ((e.key === "Delete" || e.key === "Backspace") && !canvas.getActiveObject()?.isEditing) {
        e.preventDefault();
        const activeObject = canvas.getActiveObject();
        if (activeObject && activeObject instanceof IText) {
          // @ts-ignore
          const runId = activeObject.textRunId;
          if (runId) {
            const doc = documentRef.current;
            const pageIndex = pageIndexRef.current;
            if (doc) {
              deleteTextRun(pageIndex, runId);
              canvas.remove(activeObject);
              canvas.requestRenderAll();
            }
          }
        }
        return;
      }

      if (modKey && e.key === "f") {
        e.preventDefault();
        setFindReplaceOpen(true);
        return;
      }

      if (modKey && e.key === "h") {
        e.preventDefault();
        setFindReplaceOpen(true);
        return;
      }

      if (e.key === "Escape") {
        setActiveTool("select");
        canvas.discardActiveObject();
        canvas.requestRenderAll();
        return;
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [undo, redo, deleteTextRun, setActiveTool]);

  const applyHistoryEntry = (entry: any) => {
    console.log("Applying history entry:", entry);
    setRenderTrigger((prev) => prev + 1);
  };

  const handleFind = () => {
    if (!findText || !document) return;

    const canvas = fabricRef.current;
    if (!canvas) return;

    const pageIndex = pageIndexRef.current;
    const page = document.pages[pageIndex];
    if (!page) return;

    canvas.getObjects().forEach((obj) => {
      if (obj instanceof IText) {
        // @ts-ignore
        const runId = obj.textRunId;
        const run = page.textRuns.find((r) => r.id === runId);
        if (run && run.text.toLowerCase().includes(findText.toLowerCase())) {
          (obj as any).set("stroke", "#ff0000");
          (obj as any).set("strokeWidth", 2);
        } else {
          (obj as any).set("stroke", null);
          (obj as any).set("strokeWidth", 0);
        }
      }
    });

    canvas.requestRenderAll();
  };

  const handleReplace = () => {
    if (!findText || !replaceText || !document) return;

    const pageIndex = pageIndexRef.current;
    const page = document.pages[pageIndex];

    page?.textRuns.forEach((run) => {
      if (run.text.toLowerCase().includes(findText.toLowerCase())) {
        const newText = run.text.replace(new RegExp(findText, "gi"), replaceText);
        updateTextRun(pageIndex, run.id, { text: newText }, true);
      }
    });

    setFindText("");
    setReplaceText("");
    setFindReplaceOpen(false);
    setRenderTrigger((prev) => prev + 1);
  };

  const handleReplaceAll = () => {
    if (!findText || !replaceText || !document) return;

    document.pages.forEach((page, pageIndex) => {
      page.textRuns.forEach((run) => {
        if (run.text.toLowerCase().includes(findText.toLowerCase())) {
          const newText = run.text.replace(new RegExp(findText, "gi"), replaceText);
          updateTextRun(pageIndex, run.id, { text: newText }, true);
        }
      });
    });

    setFindText("");
    setReplaceText("");
    setFindReplaceOpen(false);
    setRenderTrigger((prev) => prev + 1);
  };

  useEffect(() => {
    const canvas = fabricRef.current;
    if (!canvas) return;

    canvas.setZoom(zoom);
    canvas.requestRenderAll();
  }, [zoom]);

  if (error) {
    return (
      <div className="p-4 text-red-600">
        <p className="font-semibold">Editor error</p>
        <pre className="text-sm">{error}</pre>
      </div>
    );
  }

  return (
    <div className="relative">
      <canvas ref={canvasRef} className="border border-slate-200 shadow-sm" />

      {findReplaceOpen && (
        <div className="absolute top-4 right-4 bg-white border border-slate-200 rounded-lg shadow-lg p-4 w-80 z-50">
          <h3 className="text-sm font-semibold mb-3">Find & Replace</h3>
          <div className="flex flex-col gap-2">
            <input
              type="text"
              placeholder="Find..."
              value={findText}
              onChange={(e) => setFindText(e.target.value)}
              className="px-3 py-2 border border-slate-200 rounded text-sm"
            />
            <input
              type="text"
              placeholder="Replace..."
              value={replaceText}
              onChange={(e) => setReplaceText(e.target.value)}
              className="px-3 py-2 border border-slate-200 rounded text-sm"
            />
            <div className="flex gap-2">
              <button
                onClick={handleFind}
                className="flex-1 px-3 py-2 bg-accent text-white rounded text-sm hover:bg-blue-600"
              >
                Find
              </button>
              <button
                onClick={handleReplace}
                className="flex-1 px-3 py-2 bg-slate-100 rounded text-sm hover:bg-slate-200"
              >
                Replace
              </button>
            </div>
            <button
              onClick={handleReplaceAll}
              className="w-full px-3 py-2 bg-slate-100 rounded text-sm hover:bg-slate-200"
            >
              Replace All
            </button>
            <button
              onClick={() => setFindReplaceOpen(false)}
              className="w-full px-3 py-2 text-slate-500 rounded text-sm hover:bg-slate-50"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

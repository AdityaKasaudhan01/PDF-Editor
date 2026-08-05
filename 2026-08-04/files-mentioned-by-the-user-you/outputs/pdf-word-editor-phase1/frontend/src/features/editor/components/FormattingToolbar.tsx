import { useEditorStore } from "../store/editorStore";
import { fabric } from "fabric";

interface FormattingToolbarProps {
  onFindReplace: () => void;
}

export function FormattingToolbar({ onFindReplace }: FormattingToolbarProps) {
  const { formatting, setFormatting, applyFormatting, activeTool } = useEditorStore();

  const handleBold = () => {
    const newWeight = formatting.fontWeight === "bold" ? "normal" : "bold";
    setFormatting({ fontWeight: newWeight });
  };

  const handleItalic = () => {
    const newStyle = formatting.fontStyle === "italic" ? "normal" : "italic";
    setFormatting({ fontStyle: newStyle });
  };

  const handleUnderline = () => {
    // Fabric.js underline implementation
    const canvas = document.querySelector("canvas") as any;
    if (canvas && canvas.fabric) {
      const activeObj = canvas.fabric.getActiveObject();
      if (activeObj && activeObj instanceof fabric.Text) {
        const currentUnderline = (activeObj as any).underline || false;
        (activeObj as any).set("underline", !currentUnderline);
        canvas.fabric.requestRenderAll();
      }
    }
  };

  const handleFontChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setFormatting({ fontFamily: e.target.value });
  };

  const handleSizeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormatting({ fontSize: parseInt(e.target.value) || 24 });
  };

  const handleColorChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormatting({ color: e.target.value });
  };

  const applyToSelection = () => {
    const canvas = document.querySelector("canvas") as any;
    if (canvas && canvas.fabric) {
      const activeObj = canvas.fabric.getActiveObject();
      applyFormatting(activeObj);
    }
  };

  return (
    <div className="flex items-center gap-2 border-b border-slate-200 bg-white px-3 py-1">
      <select
        value={formatting.fontFamily}
        onChange={handleFontChange}
        className="px-2 py-1 border border-slate-200 rounded text-sm"
        title="Font Family"
      >
        <option value="Arial">Arial</option>
        <option value="Times New Roman">Times New Roman</option>
        <option value="Courier New">Courier New</option>
        <option value="Georgia">Georgia</option>
        <option value="Verdana">Verdana</option>
      </select>

      <input
        type="number"
        value={formatting.fontSize}
        onChange={handleSizeChange}
        min="8"
        max="200"
        className="w-16 px-2 py-1 border border-slate-200 rounded text-sm"
        title="Font Size"
      />

      <input
        type="color"
        value={formatting.color}
        onChange={handleColorChange}
        className="w-8 h-8 border border-slate-200 rounded cursor-pointer"
        title="Text Color"
      />

      <div className="w-px h-6 bg-slate-200 mx-1" />

      <button
        onClick={handleBold}
        className={`px-3 py-1 rounded text-sm font-bold ${
          formatting.fontWeight === "bold" ? "bg-blue-50 text-accent" : "hover:bg-slate-100"
        }`}
        title="Bold (Ctrl+B)"
      >
        B
      </button>

      <button
        onClick={handleItalic}
        className={`px-3 py-1 rounded text-sm italic ${
          formatting.fontStyle === "italic" ? "bg-blue-50 text-accent" : "hover:bg-slate-100"
        }`}
        title="Italic (Ctrl+I)"
      >
        I
      </button>

      <button
        onClick={handleUnderline}
        className="px-3 py-1 rounded text-sm underline hover:bg-slate-100"
        title="Underline (Ctrl+U)"
      >
        U
      </button>

      <div className="w-px h-6 bg-slate-200 mx-1" />

      <button
        onClick={applyToSelection}
        className="px-3 py-1 bg-accent text-white rounded text-sm hover:bg-blue-600"
        title="Apply formatting to selection"
      >
        Apply
      </button>

      <div className="ml-auto">
        <button
          onClick={onFindReplace}
          className="px-3 py-1 text-sm text-slate-600 hover:bg-slate-100 rounded"
          title="Find & Replace (Ctrl+F)"
        >
          Find & Replace
        </button>
      </div>
    </div>
  );
}

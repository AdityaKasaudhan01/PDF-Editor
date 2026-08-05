import { FileText, Redo2, Save, Undo2 } from "lucide-react";
import type { PropsWithChildren } from "react";

export function AppShell({ children }: PropsWithChildren) {
  return (
    <div className="min-h-screen">
      <header className="flex h-14 items-center justify-between border-b border-slate-200 bg-white px-4">
        <div className="flex items-center gap-2 font-semibold">
          <FileText className="h-5 w-5 text-accent" aria-hidden="true" />
          <span>PDF Word Editor</span>
        </div>
        <nav className="flex items-center gap-1" aria-label="Document actions">
          <button className="rounded p-2 hover:bg-slate-100" title="Undo" type="button">
            <Undo2 className="h-4 w-4" />
          </button>
          <button className="rounded p-2 hover:bg-slate-100" title="Redo" type="button">
            <Redo2 className="h-4 w-4" />
          </button>
          <button className="rounded p-2 hover:bg-slate-100" title="Save" type="button">
            <Save className="h-4 w-4" />
          </button>
        </nav>
      </header>
      <main className="grid min-h-[calc(100vh-3.5rem)] grid-cols-[320px_1fr]">{children}</main>
    </div>
  );
}

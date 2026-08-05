import { AppShell } from "../components/layout/AppShell";
import { DocumentUploadPanel } from "../features/documents/components/DocumentUploadPanel";
import { EditorCanvasShell } from "../features/editor/components/EditorCanvasShell";

export function App() {
  return (
    <AppShell>
      <DocumentUploadPanel />
      <EditorCanvasShell />
    </AppShell>
  );
}

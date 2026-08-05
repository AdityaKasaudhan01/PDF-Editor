export interface BoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface TextRun {
  id: string;
  text: string;
  fontFamily: string;
  fontSize: number;
  fontWeight: string;
  fontStyle: string;
  color: string;
  boundingBox: BoundingBox;
  rotation: number;
}

export interface Page {
  pageNumber: number;
  width: number;
  height: number;
  textRuns: TextRun[];
}

export interface DocumentMetadata {
  title?: string;
  author?: string;
  pageCount: number;
}

export interface EditableDocument {
  id: string;
  metadata: DocumentMetadata;
  pages: Page[];
}

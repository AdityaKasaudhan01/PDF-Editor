# Phase 1 Architecture

## Goal

Phase 1 establishes the monorepo, build systems, module boundaries, and shared contracts for a true PDF editing engine. It does not yet implement authentication, parsing, editing, or PDF generation.

## Approach

The backend is organized around ports for the core engines:

- `pdfparser`: converts uploaded PDFs into the editable document model.
- `documentmodel`: owns the canonical JSON-friendly model.
- `layoutengine`: groups text and recalculates reflow.
- `renderengine`: prepares page previews for the editor.
- `pdfgenerator`: exports a new valid PDF from the document model.
- `fontmanager`: resolves embedded and fallback fonts.
- `imagemanager`: extracts, stores, and replaces image assets.
- `historymanager`: tracks undo, autosave, and version snapshots.

The frontend is feature-oriented:

- `documents`: upload and document lifecycle UI.
- `editor`: canvas shell, toolbar state, and future PDF.js/Fabric.js integration.
- `components/layout`: app-level chrome shared by features.
- `lib`: cross-feature infrastructure such as React Query.

## Why This Structure

PDF editing needs strict separation between the raw PDF, the editable document model, and exported output. This keeps the parser, layout logic, editor UI, and generator independently testable. The ports also make it possible to introduce heavier implementations later without rewriting controllers or UI contracts.

## Testing Strategy

Phase 1 includes:

- Spring Boot context test to verify backend wiring.
- Frontend scripts for TypeScript build and Vitest.
- Shared JSON schema for validating document model compatibility.

Phase 2 should add authentication tests, security configuration tests, and frontend auth flow tests.

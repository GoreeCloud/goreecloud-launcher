# Home Page Switcher Accessibility Context — Development

GoreeCloud Launcher now exposes the authoritative Home page context as one coherent accessibility semantic for each page-selector item.

## Behavior

- Accessibility services receive the page number, app count, unsupported workspace-item count when present, and current selected state.
- The semantic label is derived from the same `WorkspaceHomePageContext` used for visible page and move-target context.
- The selector's visual labels, lazy scrolling, page mutations, and Room source of truth are unchanged.
- Unsupported workspace items remain visible rather than being silently omitted from page context.

## Boundary

Development only. This is read-only presentation metadata; it adds no workspace mutation, synchronization authority, folder/widget rendering, Room cutover qualification, or Stable status.

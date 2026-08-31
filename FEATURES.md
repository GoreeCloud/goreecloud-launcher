# GoreeCloud Launcher Features

## Implemented in Development source

- Native Android launcher application foundations using Jetpack Compose.
- Android launchable-application discovery and stable workspace-key mapping.
- Room-backed authoritative workspace cutover/read model.
- Multi-page Home projection from authoritative Room state.
- Home page selection, creation, guarded reordering, and guarded deletion of eligible empty secondary pages.
- Scrollable/lazy Home page selector with authoritative app/unsupported-item context beneath each page label.
- Automatic focus/scroll of the selected Home page after selection, reorder, or page-count changes.
- App launch from rendered Home pages.
- App movement between authoritative Home pages.
- Within-page nearest-free-cell earlier/later movement.
- Guarded exact one-cell movement left/right/up/down that fails closed on occupied or out-of-bounds targets.
- Development presentation for unsupported workspace item counts rather than silently dropping their existence from page context.

## Development / acceptance work still required

- Complete folder, shortcut, and widget presentation/workflows as applicable.
- Mature page editing and drag/drop interactions.
- Complete profile/work-profile and large installed-app-set acceptance.
- Glaze UI rendered accessibility/responsiveness acceptance across supported phone/tablet/foldable configurations.
- Accepted Privacy Shield/Wardveil/Everkeep integrations where future launcher behavior requires them.
- Signed packaging, upgrade/recovery acceptance, representative physical-device testing, and Stable qualification.

## Planned product capabilities — not current implementation claims

- Richer launcher personalization with explicit privacy boundaries.
- Search and app discovery enhancements.
- Widgets/folders/shortcuts and additional workspace organization.
- Cross-device workspace continuity where Everkeep/Sync authority is explicitly implemented and accepted.
- Additional GoreeCloud application/service surfaces where they preserve Android and user authority.

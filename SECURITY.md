# GoreeCloud Launcher Security

GoreeCloud Launcher is a Development-stage native Android HOME application. Security, privacy, recovery, and release claims in this repository are evidence-gated; successful source or CI validation does not establish production or Stable qualification.

## Current security boundary

The current Launcher is intentionally local-first and offline-capable.

- The application requests no Android runtime or install-time permissions in its authored manifest.
- The application does not request `INTERNET` and does not use `QUERY_ALL_PACKAGES`.
- Android package visibility is bounded to the `MAIN`/`LAUNCHER` discovery contract plus explicitly declared first-party GoreeCloud Index entry points.
- Cleartext application traffic is disabled at the manifest level.
- Advertising, sponsorship, attribution, behavioral-tracking, and remote-analytics SDKs are prohibited by repository policy and CI.
- Workspace state, preferences, Home layout, app organization, appearance choices, and usage-derived Launcher state remain device-local unless a separately reviewed export, restore, synchronization, or platform integration explicitly authorizes transfer.
- Android platform automatic backup is disabled. Current Launcher recovery authority is the separately reviewed, versioned, validated portability/restore path rather than an opaque automatic copy of private Room or DataStore state.
- External intents, imported state, portable snapshots, package/profile identities, shortcuts, and future theme or widget payloads must be treated as untrusted input.

The current machine-readable Platform Contract remains `Development` and `nonconformant`. Manager, Privacy Shield, Wardveil Security, Everkeep, Glaze UI, Mesh, and Identity integrations remain independently evidence-gated according to `goreecloud.platform.yaml`; repository presence or UI labeling is not accepted integration evidence.

## Data and recovery protections

Launcher durable state currently includes Android DataStore preferences and Room-backed workspace state. These stores are implementation state, not a portable backup format.

Approved recovery work must preserve the following rules:

1. Validate the complete imported format before mutation.
2. Reject malformed, tampered, noncanonical, unsupported, or out-of-range data rather than silently normalizing it.
3. Keep package/profile and Android runtime identifiers out of portable formats unless their rebinding semantics are explicitly designed and accepted.
4. Never copy stale `AppWidget` identifiers as if they were portable identities.
5. Preserve one authoritative workspace source and fail closed when authority or persisted state is ambiguous.
6. Keep recovery evidence categorical and privacy-bounded; do not log installed-app inventories, private workspace identifiers, credentials, or raw imported payloads.
7. Treat current partial portability codecs and restore coordinators as Development evidence only until complete product-wide recovery and representative-device acceptance exist.

## Android platform exposure

Launcher is exported as the Android HOME/LAUNCHER activity because that is its product role. This exported surface must not be treated as general external mutation authority. HOME/launcher intents, activity re-entry, shortcuts, future deep links, and other external inputs require bounded parsing and state transitions.

The application must not gain privileged or broad Android access merely to imitate system components. In particular, ordinary Launcher features must not depend on root, device-administrator authority, broad filesystem access, unrestricted package visibility, or a privileged accessibility service.

## Dependency and source-control security

- Keep signing keys, keystores, passwords, reusable tokens, recovery secrets, private keys, and production credentials outside Git, issues, pull requests, CI logs, screenshots, and ordinary documentation.
- Use exact-revision validation for release-sensitive work.
- Preserve generated Room schema history and fail closed on unexplained schema drift.
- Keep dependency changes reviewable and bounded to the capability that requires them.
- Do not introduce remote code, remote fonts, remote icon runtimes, analytics libraries, ad SDKs, or tracking libraries as convenience dependencies.

## Platform-system integration

A GoreeCloud platform-system integration is accepted only when the repository has substantive implementation plus the applicable authorization, privacy, security, resilience, failure, runtime, accessibility, and evidence requirements. Decorative status cards, icons, labels, or manifest declarations do not satisfy that boundary.

Any future networked integration must document its destination, authority, data minimization, authentication/authorization, failure behavior, offline behavior, logging boundary, and user controls before production acceptance.

## Vulnerability handling

Do not place reusable secrets, private user data, installed-application inventories, private device details, or exploit payloads containing sensitive data in public issue text. Use the repository's available private GitHub security-reporting channel when configured. If no private reporting channel is available, report the minimum non-sensitive information needed to establish the problem and request an appropriate private handoff before sharing sensitive reproduction material.

Security fixes must preserve GoreeCloud review, exact-head validation, and lifecycle gates. A security patch is not considered production-integrated until the authoritative repository and applicable project records reflect the verified final state.

## Release boundary

Launcher remains Development. Production or Stable qualification additionally requires the applicable current Glaze UI acceptance, Wardveil Security and Privacy Shield acceptance, Everkeep/recovery acceptance, Identity/Mesh/Manager integration where required, representative physical-device/default-HOME testing, accessibility acceptance, performance validation, protected signing/provenance, release verification, and closure of the governing stabilization gates.

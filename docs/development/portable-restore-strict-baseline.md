# Portable restore strict persisted baseline

Status: **Development recovery-hardening evidence**

## Purpose

The current bounded Launcher restore writer coordinates Room workspace state with seven portable Preferences DataStore values through a durable local recovery journal. Recovery decisions must therefore use the exact persisted portable preference state, not the sanitized view used by ordinary UI reads.

## Implemented boundary

Before a new restore plans or mutates Room state, `LauncherTransactionalPortableRestoreWriter` now reads the existing portable preference baseline through `readPortablePreferencesForRecovery()`. If the persisted subset is outside the canonical portable recovery domain, the writer throws `LauncherPortableRestoreRecoveryRequiredException` before Room planning, journal creation, or restore mutation.

Failed-apply preference compensation now uses the same strict raw decoder. Rollback is permitted only when the canonical persisted subset still equals either the exact previous value or the exact just-applied value. Invalid persisted data or any third canonical state is left untouched, the rollback is treated as unverified, and the durable recovery journal is preserved.

This prevents recovery from silently clamping, replacing, or normalizing corrupt preference bytes while constructing or reconciling recovery evidence.

## Validation

JVM regression coverage verifies that an exact canonical recovery read returns the original portable preferences and that a noncanonical recovery read produces the recovery-required failure path.

Exact-head repository CI remains the validation authority for this Development candidate.

## Authority and non-claims

This hardening does not make Room and DataStore one physical transaction. It does not add clean-target reconstruction, package/profile/folder/shortcut/widget rebinding, cross-device portability, artifact provenance, Everkeep recovery acceptance, Privacy Shield acceptance, Wardveil Security acceptance, production restore authorization, Release Candidate qualification, or Stable qualification.

Those remain independently gated by the applicable GoreeCloud authorities and target-environment evidence.

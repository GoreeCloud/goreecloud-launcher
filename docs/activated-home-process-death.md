# Activated Home process-death acceptance

This acceptance slice extends the merged activity-recreation coverage into a host-driven Android process-death and cold-start boundary for the activated HOME-role workspace path.

## Acceptance model

The test deliberately separates persistence setup from post-death verification. `ActivatedHomeProcessDeathRuntimeTest.seedTerminalRoomPlacement` runs first against the installed application, drives the production `MainActivity` and `WorkspaceProductionRuntimeCoordinator` to terminal `ROOM`, and verifies the exact ordered Favorite/Dock snapshot through the authoritative Room observer.

The Android 16 emulator job then starts GoreeCloud Launcher through the HOME intent, records a live application PID, calls `am force-stop com.goreecloud.launcher`, and requires `pidof` to become empty. The job relaunches the HOME intent without reinstalling or clearing application data and requires a live GoreeCloud Launcher process again.

`ActivatedHomeProcessDeathRuntimeTest.verifyColdStartRoomPlacementAndReactivity` then runs in the post-death environment. It requires the durable workspace authority marker to remain `ROOM`, requires the authoritative Room placement snapshot to equal the pre-death ordered snapshot, launches the real `MainActivity`, verifies the persisted Favorite is rendered, performs a fresh Room-authoritative Favorite mutation through the production runtime coordinator, and requires the Compose Home UI to react to the new Room state.

## Why the process boundary is host-driven

An instrumentation test cannot prove its own target process was killed while continuing to execute inside that process. The CI host therefore owns the `force-stop`, PID absence check, and HOME cold-start sequence. The two instrumentation phases run around that host-controlled boundary without reinstalling the application or clearing its data.

After the process-death acceptance phases complete, CI removes the test HOME-role assignment, clears application data, and executes the existing unfiltered `connectedDebugAndroidTest` suite so the established runtime regression suite remains independent from the seeded process-death state.

## Fail-closed requirements

The process-death gate fails if the launcher is not the HOME role holder, if no live launcher process exists before force-stop, if a launcher process remains after force-stop, if HOME cannot cold-start the launcher again, if terminal Room authority is lost, if Room placement differs from the seeded snapshot, if the persisted Favorite is not rendered, or if a post-death authoritative mutation does not propagate reactively to Home.

## Privacy and security boundary

The process-death acceptance adds no Android requested permission, INTERNET permission, network dependency, cloud dependency, advertising, sponsorship, analytics, attribution, or tracking SDK. The HOME-role change and process-control commands exist only in the Android CI test environment.

## Remaining acceptance boundaries

This slice is intended to establish Android OS process termination plus cold-start persistence on the API 36 emulator. It does not establish schema-version upgrade recovery, representative physical-device/default-HOME acceptance, multi-page live cell/span placement, signing, or release acceptance.

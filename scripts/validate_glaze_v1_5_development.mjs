import assert from 'node:assert/strict';
import {execFileSync} from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import {pathToFileURL} from 'node:url';

const root = path.resolve(import.meta.dirname, '..');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const json = relative => JSON.parse(read(relative));
const clone = value => JSON.parse(JSON.stringify(value));

const expected = Object.freeze({
  repository: 'GoreeCloud/goreecloud-launcher',
  stableVersion: '1.4.1',
  stableRevision: '4fab9da0fad2e5c974e0e66ec88632c61745751c',
  developmentVersion: '1.5.0-dev.1',
  developmentRevision: 'e7c397837908e4644d6230f17d0f73e84e3d1558',
  sharedProfileId: 'goreecloud-launcher-handheld'
});

const contract = json('contracts/glaze-ui/launcher-glaze-v1.5-development.json');
const platform = read('goreecloud.platform.yaml');
const adoption = read('docs/glaze-ui-adoption.md');
const metrics = read('app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeMetrics.kt');
const optical = read('app/src/main/java/com/goreecloud/launcher/ui/theme/GlazeOpticalV14.kt');
const glazeRoot = String(process.env.GLAZE_V15_ROOT || '').trim();

assert.ok(glazeRoot, 'GLAZE_V15_ROOT must point to the exact Glaze UI V1.5 Development checkout');
assert.ok(fs.existsSync(glazeRoot), `GLAZE_V15_ROOT does not exist: ${glazeRoot}`);
const upstreamRevision = execFileSync('git', ['-C', glazeRoot, 'rev-parse', 'HEAD'], {encoding: 'utf8'}).trim();
assert.equal(upstreamRevision, expected.developmentRevision, 'Glaze V1.5 checkout must match the exact governed Development revision');

assert.equal(contract.schemaVersion, 1);
assert.equal(contract.documentVersion, '1.0');
assert.equal(contract.recordType, 'goreecloud-launcher-glaze-v1.5-development-integration');
assert.equal(contract.consumer?.repository, expected.repository);
assert.equal(contract.consumer?.lifecycle, 'development');
assert.equal(contract.glazeUi?.implementedStableTarget, expected.stableVersion);
assert.equal(contract.glazeUi?.implementedStableRevision, expected.stableRevision);
assert.equal(contract.glazeUi?.developmentVersion, expected.developmentVersion);
assert.equal(contract.glazeUi?.developmentRevision, expected.developmentRevision);
assert.equal(contract.glazeUi?.sharedRepresentativeProfile, expected.sharedProfileId);
assert.equal(contract.integrationBoundary?.developmentOnly, true);
assert.equal(contract.integrationBoundary?.testOnly, true);
assert.equal(contract.integrationBoundary?.runtimeDependencyAdded, false);
assert.equal(contract.integrationBoundary?.platformManifestStableTargetChanged, false);
assert.equal(contract.integrationBoundary?.stable141SourceMappingPreserved, true);
assert.equal(contract.integrationBoundary?.consumerAcceptanceEstablished, false);
assert.equal(contract.integrationBoundary?.releaseCandidateQualified, false);
assert.equal(contract.integrationBoundary?.stableQualified, false);
assert.equal(contract.integrationBoundary?.productionEligible, false);
assert.equal(contract.authorityBoundary?.glazeAuthority, 'presentation-only');
assert.equal(contract.authorityBoundary?.workspaceAuthority, 'Room');
assert.equal(contract.authorityBoundary?.applicationInventoryAuthority, 'Android LauncherApps');
assert.equal(contract.authorityBoundary?.universalSearchAuthority, 'GoreeCloud Index');
assert.equal(contract.authorityBoundary?.authorizationMayBeInferredByGlaze, false);
assert.equal(contract.authorityBoundary?.providerPrecedenceMayBeInferredByGlaze, false);
assert.equal(contract.authorityBoundary?.permissionMayBeGrantedByGlaze, false);
assert.equal(contract.authorityBoundary?.automaticNavigationAllowed, false);
assert.equal(contract.authorityBoundary?.consequentialExecutionMayBeAutomatic, false);
assert.equal(contract.authorityBoundary?.fallbackExecutionMayBeAutomatic, false);
assert.equal(contract.acceptance?.repositoryLocalConsumerAcceptance, false);
assert.equal(contract.acceptance?.productionAcceptance, false);
assert.equal(contract.validation?.scenarios?.length, 4);

// Preserve the actual current-Stable Launcher mapping and fail closed if V1.5 leaks into production declarations.
assert.ok(metrics.includes(`const val targetVersion = "${expected.stableVersion}"`));
assert.ok(metrics.includes(`const val sourceRevision = "${expected.stableRevision}"`));
assert.ok(optical.includes(`const val targetVersion = "${expected.stableVersion}"`));
assert.ok(optical.includes(`const val stableSourceRevision = "${expected.stableRevision}"`));
assert.ok(adoption.includes('Official target: **GLAZE UI V1.4.1 (`1.4.1`)**'));
assert.ok(adoption.includes(`Exact Stable merged source authority: \`${expected.stableRevision}\``));
assert.match(platform, /lifecycle:\s*development/);
assert.match(platform, /result:\s*applicable-migration-required/);
assert.ok(platform.includes('version: "1.4.1"'));
assert.ok(platform.includes('glaze_ui_required: "1.4.1"'));
assert.ok(platform.includes('glaze-ui==1.4.1'));
assert.ok(platform.includes('conformance:\n  status: nonconformant'));
assert.ok(!platform.includes('glaze-ui==1.5.0-dev.1'));
assert.ok(!platform.includes('glaze_ui_required: "1.5.0-dev.1"'));

const upstreamRegistry = JSON.parse(fs.readFileSync(path.join(glazeRoot, 'registry/development/glaze-v1.5.0-dev.1.json'), 'utf8'));
const profiles = JSON.parse(fs.readFileSync(path.join(glazeRoot, 'contracts/v1.5/representative-consumers.dev.json'), 'utf8'));
assert.equal(upstreamRegistry.version, expected.developmentVersion);
assert.equal(upstreamRegistry.lifecycle, 'development');
assert.equal(upstreamRegistry.consumerEligible, false);
assert.equal(upstreamRegistry.stableBaseline, expected.stableVersion);
assert.equal(upstreamRegistry.representativeConsumerAcceptanceEstablished, false);
assert.equal(upstreamRegistry.representativeConsumerIntegrationChangesStableTarget, false);
assert.equal(profiles.version, expected.developmentVersion);
assert.equal(profiles.stableConsumerTarget, expected.stableVersion);
assert.equal(profiles.developmentOnly, true);
assert.equal(profiles.consumerAcceptanceEstablished, false);
assert.equal(profiles.repositoryLocalAcceptanceRequired, true);

const {resolveGlazeInterface} = await import(pathToFileURL(path.join(glazeRoot, 'js/glaze-v1.5-resolution.dev.mjs')).href);
const {glazeProviderDevelopmentContract} = await import(pathToFileURL(path.join(glazeRoot, 'js/glaze-v1.5-provider-registry.dev.mjs')).href);
assert.equal(glazeProviderDevelopmentContract.providerPrecedenceInferred, false);
assert.equal(glazeProviderDevelopmentContract.authorizationInferred, false);

const launcherProfile = profiles.profiles.find(profile => profile.id === expected.sharedProfileId);
assert.ok(launcherProfile, `Missing upstream representative Launcher profile: ${expected.sharedProfileId}`);
assert.equal(launcherProfile.repository, expected.repository);

function assertGlobalBoundaries(result) {
  assert.equal(result.version, expected.developmentVersion);
  assert.equal(result.lifecycle, 'development');
  assert.equal(result.stableBaseline, expected.stableVersion);
  assert.equal(result.authority.glazeAuthority, 'presentation-only');
  assert.equal(result.authority.authorizationInferred, false);
  assert.equal(result.authority.permissionGranted, false);
  assert.equal(result.authority.providerPrecedenceInferred, false);
  assert.equal(result.authority.operationalAuthorityGranted, false);
  assert.equal(result.authority.automaticNavigationAllowed, false);
  assert.equal(result.authority.automaticPermissionRequestAllowed, false);
  assert.equal(result.authority.automaticConsequentialExecutionAllowed, false);
  assert.equal(result.authority.automaticFallbackExecutionAllowed, false);
  assert.equal(result.continuity.taskStateReset, false);
  assert.equal(result.continuity.pageReloadRequired, false);
  assert.equal(result.privacy.localFirst, true);
  assert.equal(result.privacy.telemetryRequired, false);
  assert.equal(result.privacy.remoteAnalysisRequired, false);
  assert.equal(result.diagnostics.authority.operationalAuthorityGranted, false);
  assert.equal(result.diagnostics.authority.securityStateManufactured, false);
  assert.equal(result.diagnostics.authority.privacyStateManufactured, false);
  assert.equal(result.diagnostics.privacy.rawContextIncluded, false);
  assert.equal(result.diagnostics.privacy.providerIdentityIncluded, false);
}

// Scenario 1: canonical shared handheld profile preserves local Launcher and Index authority while offline.
const handheld = resolveGlazeInterface(clone(launcherProfile.input));
assertGlobalBoundaries(handheld);
assert.equal(handheld.composition.paneMode, 'single-pane');
assert.equal(handheld.composition.controlDensity, 'comfortable');
assert.equal(handheld.composition.commandSurface, 'direct-controls');
assert.equal(handheld.composition.connectivityPresentation, 'offline');
assert.equal(handheld.navigation.acceptedCurrentId, 'home');
assert.equal(handheld.navigation.currentDestinationChanged, false);
const launchApp = handheld.actions.actions.find(action => action.id === 'launch-app');
const searchGoreeCloud = handheld.actions.actions.find(action => action.id === 'search-goreecloud');
assert.equal(launchApp.enabled, true);
assert.equal(searchGoreeCloud.enabled, true);
assert.equal(handheld.capabilities.byId['application.launch'].provenance.authority, 'application');
assert.equal(handheld.capabilities.byId['service.index-search'].provenance.authority, 'service');
assert.equal(handheld.diagnostics.capabilities.find(item => item.id === 'service.index-search').authority, 'service');
assert.equal(JSON.stringify(handheld.diagnostics).includes('goreecloud-index'), false);

// Scenario 2: Index unavailability disables only universal search presentation, not local application launch.
const unavailableInput = clone(launcherProfile.input);
unavailableInput.providers.find(provider => provider.id === 'goreecloud-index').capabilities[0].state = 'temporarily-unavailable';
const unavailable = resolveGlazeInterface(unavailableInput);
assertGlobalBoundaries(unavailable);
const unavailableLaunch = unavailable.actions.actions.find(action => action.id === 'launch-app');
const unavailableSearch = unavailable.actions.actions.find(action => action.id === 'search-goreecloud');
assert.equal(unavailableLaunch.enabled, true);
assert.equal(unavailableSearch.enabled, false);
assert.equal(unavailableSearch.state, 'temporarily-unavailable');
assert.ok(unavailableSearch.reasonCodes.includes('temporarily-unavailable'));
assert.equal(unavailable.navigation.acceptedCurrentId, 'home');

// Scenario 3: duplicate Index capability ownership fails closed instead of inventing provider precedence.
const conflictInput = clone(launcherProfile.input);
conflictInput.providers.push({
  id: 'duplicate-index-service',
  authority: 'service',
  capabilities: [{id: 'service.index-search', domain: 'service', state: 'available'}]
});
const conflict = resolveGlazeInterface(conflictInput);
assertGlobalBoundaries(conflict);
assert.ok(conflict.conflicts.capabilityIds.includes('service.index-search'));
assert.equal(conflict.capabilities.byId['service.index-search'], undefined);
const conflictLaunch = conflict.actions.actions.find(action => action.id === 'launch-app');
const conflictSearch = conflict.actions.actions.find(action => action.id === 'search-goreecloud');
assert.equal(conflictLaunch.enabled, true);
assert.equal(conflictSearch.enabled, false);
assert.equal(conflictSearch.state, 'unknown');
assert.ok(conflictSearch.reasonCodes.includes('capability-unknown'));
assert.equal(conflict.authority.providerPrecedenceInferred, false);

// Scenario 4: Launcher-owned workspace editing policy may restrict presentation, but Glaze cannot bypass or execute it.
const restrictedInput = clone(launcherProfile.input);
const launcherRuntime = restrictedInput.providers.find(provider => provider.id === 'goreecloud-launcher-runtime');
launcherRuntime.capabilities.push({id: 'authorization.workspace-edit', domain: 'authorization', state: 'restricted'});
restrictedInput.actions.push({
  id: 'edit-home',
  label: 'Edit Home',
  requiredCapabilities: ['authorization.workspace-edit'],
  consequential: true
});
const restricted = resolveGlazeInterface(restrictedInput);
assertGlobalBoundaries(restricted);
assert.equal(restricted.capabilities.byId['authorization.workspace-edit'].provenance.authority, 'application');
const editHome = restricted.actions.actions.find(action => action.id === 'edit-home');
assert.equal(editHome.enabled, false);
assert.equal(editHome.state, 'restricted');
assert.equal(editHome.consequential, true);
assert.equal(editHome.automaticExecutionAllowed, false);
assert.ok(editHome.reasonCodes.includes('restricted-by-authority'));
assert.equal(restricted.navigation.acceptedCurrentId, 'home');

console.log('GoreeCloud Launcher / GLAZE UI 1.5.0-dev.1 repository-local Development integration: PASS');
console.log(`Launcher exact upstream Glaze revision: ${expected.developmentRevision}`);
console.log('Launcher Development scenarios: 4');
console.log(`Current Stable Launcher Glaze target remains: ${expected.stableVersion}`);
console.log('Launcher V1.5 runtime dependency added: false');
console.log('Launcher V1.5 consumer acceptance established: false');
console.log('Launcher Release Candidate / Stable / production acceptance established: false');

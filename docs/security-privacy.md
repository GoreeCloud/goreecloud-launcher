# Security and privacy

The launcher is high-trust software because it becomes the HOME surface and can enumerate launchable apps. Its default architecture therefore keeps app inventory, layout, folders, local search data and usage-derived suggestions on-device.

No advertising, attribution, sponsorship or engagement SDK is included. No `INTERNET` permission is requested in Milestone 0. Future optional network integrations must be explicit, separately disableable, documented by destination/purpose, and unnecessary for core launcher operation.

Do not add Accessibility Service or device-admin privileges merely to imitate privileged launcher behavior. Imported themes/backups are untrusted input. Release signing secrets remain outside source control.

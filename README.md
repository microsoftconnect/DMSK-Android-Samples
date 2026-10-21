# Dragon Medical SpeechKit Android SDK Samples

This repository contains only Android SDK sample applications. The repository that contains the Android SDK package is [DMSK-Android](https://github.com/microsoftconnect/DMSK-Android).

## SDK version and compatibility

The sample sources are synchronized with the Android SDK package **7.0.16.1**.

All samples use the shared [SDK download script](samples/download-speechkit-aar.gradle) to download `NuanceSpeechAnywhere.aar` from the `main` branch of [DMSK-Android](https://github.com/microsoftconnect/DMSK-Android/tree/main). They use the latest Android SDK build available on that branch when first downloaded, rather than a pinned release or commit.

The AAR is cached in each sample project's `.gradle/downloaded-sdk/NuanceSpeechAnywhere-main.aar` file. To pick up a newer SDK build from `main`, delete that cached file in the relevant sample project and rebuild. A normal rebuild reuses the cached SDK.

The new [Compose UI sample](samples/src/dm_speechkitcomposeuisample) requires **Release 2026.4 (7.0 R1) or higher**. It is not compatible with earlier SDK releases.

## Credentials

All samples use `ENTER_YOUR_ORGANIZATION_TOKEN` and `ENTER_YOUR_PARTNER_GUID` placeholders and the SDK's default server configuration. Supply your own credentials before opening a session. Retrieve the organization token through user settings or your backend rather than hard-coding it, and do not commit real credentials.

## Documentation

See the following pages for more information:

[Dragon Medical SpeechKit documentation](https://learn.microsoft.com/en-us/industry/healthcare/speechkit/)

[Android SDK help on Microsoft Learn](https://learn.microsoft.com/en-us/industry/healthcare/speechkit/android-sdk/)

[Set up Dragon Medical SpeechKit](https://learn.microsoft.com/en-us/industry/healthcare/speechkit/android-sdk/setup)

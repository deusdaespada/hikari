# Hikari

Manga reader for Android based on Mihon featuring offline downloads and security features.

[![Downloads](https://img.shields.io/github/downloads/LeverTeam/hikari/total?style=for-the-badge)](https://github.com/LeverTeam/hikari/releases)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

## Features

- Extension management across multiple repositories with automatic Keiyoushi sync
- Material You UI with dynamic palette-aware skins using AGSL shaders
- Library organization and chapter downloads for offline viewing
- Biometric security, automatic updates, and background backups
- High-performance native image upscaling using AMD FSR 1.0 (EASU) in the reader
- Shader-animated liquid progress bar with dynamic Hikari skin integration
- Asynchronous color extraction for smoother transitions when opening manga details

## Installation

### Downloads

Latest APK releases are available on [GitHub](https://github.com/LeverTeam/hikari/releases). Downloads are tracked automatically via GitHub Assets.

### Prerequisites

- JDK 21
- Android SDK

### Build

```bash
git clone https://github.com/LeverTeam/hikari.git
cd hikari
chmod +x gradlew
./gradlew assembleDebug
```

## Usage

1. Locate the built APK in `app/build/outputs/apk/debug/`
2. Install the APK on an Android device
3. Navigate to the Browse panel to initialize the default extension repository

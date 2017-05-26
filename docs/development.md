## Development environment

### Getting started

### Prerequisites

The following dependencies must be installed to successfully compile, test and run the project:

  - Java 8
  - Android SDK API level 21

### Download source code

Clone the GIT repository

    git clone https://github.com/suomenriistakeskus/oma-riista-android.git

### Setup project

Create a "riista.properties" which should define keystore settings used (key* prefixes) with some other configuration values (like staging server url). See the .gradle files for more details.

Use Firebase to create two projects with appropriate package names matching your build variants and flavors. Place both "google-services.json"-files into a place where the build system can find them (for example "riistaandroid/production" and "riistaandroid/staging").

Open project with Android Studio

If running app in emulator Google APIs image is required

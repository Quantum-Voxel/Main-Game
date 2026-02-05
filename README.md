# Quantum Voxel: Next Generation
Quantum Voxel is a block-based game currently in development.

## ⚠️ Development has paused for now. The owner has paused development but I will be continuing from where it was left off. Thanks for understanding and moving over here (we may move back to gitlab at a later point.)

## Licensing
Most of the project is licensed under [Apache 2.0](LICENSE.md).  
However the 2 files ([DerivativeTunnelClosingCaveCarver.java](server/src/main/java/dev/ultreon/qvoxel/world/gen/noise/DerivativeTunnelClosingCaveCarver.java) and [DerivativeTunnelClosingCaveCarver.java](server/src/main/java/dev/ultreon/qvoxel/world/gen/noise/OpenSimplex2CachedColumnNoise.java)) are licensed under MIT.

## Art Licensing
All art is licensed under All Rights Reserved, unless explicitly stated otherwise.
The art provided in this repository is owned by their original creators. 

## Build Process
* To build the project, run the following command:
  ```bash
  ./gradlew package
  ```
  or on Windows:
  ```bash
  gradlew.bat package
  ```
  And then get the compiled files from `client/dist/QuantumVoxel-{PLATFORM}.zip` where `{PLATFORM}` is the platform you use.

* To run the project, run the following command:
  ```bash
  ./gradlew runClient
  ```
  or on Windows:
  ```bash
  gradlew.bat runClient
  ```

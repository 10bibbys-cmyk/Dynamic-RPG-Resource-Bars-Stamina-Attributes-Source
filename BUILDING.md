# Build and verify 1.0.2.1

**Targets:** Minecraft 1.20.1; Fabric Loader 0.16.14+; Fabric API; Java 17; Dynamic RPG Resource Bars 0.8.1; Stamina Attributes 1.6.0.

Install JDK 17 and Gradle 8.8 and run from this repository:

```sh
gradle --no-daemon clean build
```

The project resolves the dependency JARs from the Maven repositories declared in `build.gradle`:
- CurseMaven: Dynamic RPG Resource Bars 0.8.1
- Modrinth Maven: Stamina Attributes 1.6.0

The resulting remapped JAR is created in `build/libs`. Verify `fabric.mod.json` reports version `1.0.2.1`.

The public source build checks compilation, not in-game compatibility beyond what was tested by the modpack maintainer. The separate addon code is source-available for private verification; the upstream GPLv3 and artwork terms are retained. Read `LICENSE` and `NOTICE.md` before reuse.

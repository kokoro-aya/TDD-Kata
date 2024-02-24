plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "TDD-Banking-Kata"

include("frontend")
include("banking")
include("infrastructure")

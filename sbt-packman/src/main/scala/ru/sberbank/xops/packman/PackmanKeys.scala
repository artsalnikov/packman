package ru.sberbank.xops.packman

import sbt._

import java.io.File

trait PackmanKeys {

  lazy val productionPackage = taskKey[Unit]("Package distribution for production.")
  lazy val ctlDescriptionsPath = settingKey[File]("Target directory for CTL descriptions.")

}

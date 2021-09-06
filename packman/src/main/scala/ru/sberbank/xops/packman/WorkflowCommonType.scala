package ru.sberbank.xops.packman

abstract class WorkflowCommonType {

  lazy val name: String = getClass.getName.toLowerCase.replaceAll("[^a-z0-9]", "-")

  def getDescription: String

}

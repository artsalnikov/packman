package ru.sberbank.xops.packman

abstract class WorkflowType1 extends WorkflowCommonType {

  val type1SpecificParameter: String = "some text"

  final val wfType: String = "type1"

  override def getDescription: String = s"$name has type $wfType"

}

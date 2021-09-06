package ru.sberbank.xops.packman

abstract class WorkflowType2 extends WorkflowCommonType {

  val type2SpecificParameter: String = "some other text"

  final val wfType: String = "type2"

  override def getDescription: String = s"$name has type $wfType"

}

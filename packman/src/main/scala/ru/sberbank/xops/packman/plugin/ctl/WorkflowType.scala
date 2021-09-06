package ru.sberbank.xops.packman.plugin.ctl

import com.google.common.reflect.ClassPath

import scala.collection.convert.ImplicitConversions._
import scala.language.reflectiveCalls

class WorkflowType(clazz: Class[_]) {

  def getDescriptionsFromProject(loader: ClassLoader): List[String] = {
    val workflowTypeClass = Class.forName(clazz.getName, true, loader)
    val allClasses = ClassPath.from(loader).getTopLevelClasses.toSet
    val workflowTypeClasses = allClasses.filter { cls =>
      try {
        clazz.getName != cls.getName &&
          workflowTypeClass.isAssignableFrom(cls.load())
      } catch {
        case e: LinkageError => println(e.getMessage); false
      }
    }
    workflowTypeClasses.toList.map { cls =>
      println(s"Instance for class ${cls.getName}...")
      val classInstance = Class.forName(cls.getName, true, loader).newInstance
      classInstance.asInstanceOf[{def getDescription: String}].getDescription
    }
  }

}

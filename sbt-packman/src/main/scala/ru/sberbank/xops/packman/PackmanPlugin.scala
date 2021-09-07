package ru.sberbank.xops.packman

import com.google.common.reflect.ClassPath
import org.reflections.Reflections
import ru.sberbank.xops.packman.plugin.ctl.WorkflowType
import sbt.Keys.{scalaInstance, _}
import sbt.internal.inc.classpath.ClasspathUtil
import sbt._
import sbt.internal.inc.ScalaInstance
import sbt.internal.util.ManagedLogger

import java.io.{File, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import scala.collection.convert.ImplicitConversions._
import scala.language.reflectiveCalls
import scala.util.Properties

object PackmanPlugin extends AutoPlugin {

  object autoImport extends PackmanKeys
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    ctlDescriptionsPath := target.value / "ctl",
    productionPackage := productionPackageTask.value
  )

  private def productionPackageTask = Def.task {
    val logger = streams.value.log
    val pythonDirectory = target.value / "python"
    logger.info(s"Production package in ${baseDirectory.value}")
    createPythonParties(pythonDirectory, baseDirectory.value)
    createCtlDescriptions((Compile / fullClasspath).value.map(_.data.toPath), scalaInstance.value,
      ctlDescriptionsPath.value / "ctl.yml", logger)
    compose(target.value / "composed", ctlDescriptionsPath.value, pythonDirectory, logger)
    zip(target.value, logger)
  }

  private def createCtlDescriptions(classpath: Seq[Path], scalaInstance: ScalaInstance,
                                    targetDescriptionPath: File,
                                    logger: ManagedLogger): Unit = {
    logger.info(s"Classpath is initialized as $classpath")
    val loader = ClasspathUtil.makeLoader(classpath, scalaInstance)
    logger.info(s"Loader $loader is initialized")
    val workflowDescriptions = List(classOf[WorkflowType1], classOf[WorkflowType2]).map(new WorkflowType(_))
      .map(_.getDescriptionsFromProject(loader)).reduce(_ ++ _)
    logger.info(s"${workflowDescriptions.length} description gotten")
    IO.createDirectory(targetDescriptionPath.getParentFile)
    new PrintWriter(targetDescriptionPath, "UTF-8") {
      write(workflowDescriptions.mkString(Properties.lineSeparator))
      close()
    }
  }

  private def createPythonParties(pythonDirectory: File, projectPath: File): Unit = {
    val modules = getModules(projectPath)
    modules.map(getPythonRoots).reduce(_ ++ _).foreach { file =>
      println(s"TEEST: ${projectPath.toPath.relativize(file.toPath)}")
      val archiveName = projectPath.toPath.relativize(file.toPath).toString.replaceAll("[^A-Za-z0-9]", "-")
      val archiveDirectory = pythonDirectory / archiveName
      IO.createDirectory(archiveDirectory)
      val archiveFile = archiveDirectory / (archiveName + ".zip")
      IO.zip(sbt.io.Path.allSubpaths(file), archiveFile, None)
    }
  }

  private def getModules(projectPath: File): Array[File] = {
    if (isModule(projectPath)) Array(projectPath)
    else projectPath.listFiles.filter(isModule)
  }

  private def isModule(path: File): Boolean = path.isDirectory && (path / "src" / "main").exists

  private def getPythonRoots(pythonSourcePath: File): Array[File] = {
    val (directories, files) = pythonSourcePath.listFiles.partition(_.isDirectory)
    files.filter(_.getName == "Main.py") ++ directories.map(getPythonRoots).reduceOption(_ ++ _).getOrElse(Array())
  }

  private def compose(targetDirectory: File,
                      ctlDescriptionFile: File,
                      pythonDirectory: File,
                      logger: ManagedLogger): Unit = {
    IO.createDirectory(targetDirectory)
    logger.info(s"Target directory for compose $targetDirectory created")
    IO.copyDirectory(ctlDescriptionFile, targetDirectory / "ctl", CopyOptions().withOverwrite(true))
    IO.copyDirectory(pythonDirectory, targetDirectory, CopyOptions().withOverwrite(true))
    logger.info("Compose finished")
  }

  private def zip(targetDirectory: File, logger: ManagedLogger): Unit = {
    val zip = targetDirectory / "distribution.zip"
    logger.info(s"Zip file from ${targetDirectory / "composed"} to $zip...")
    IO.zip(sbt.io.Path.allSubpaths(targetDirectory / "composed"), zip, None)
    logger.info("Zip finished")
  }


/* it's work
    val classpath = (Compile / fullClasspath).value.map(_.data.toPath)
    log.info(s"Classpath is initialized as $classpath")
    val loader = ClasspathUtil.makeLoader(classpath, scalaInstance.value)
    log.info(s"Loader $loader is initialized")
    val guavaClassPath = ClassPath.from(loader)
    log.info(s"guavaClassPath is defined")
    val allClasses = guavaClassPath.getTopLevelClasses.toSet
    log.info(s"${allClasses.size} classes found")
    val workFlowType2Class = Class.forName(classOf[WorkflowType2].getName, true, loader)
    log.info(s"Basic class ${classOf[WorkflowType2].getName} initialized")
    val acceptedClasses = allClasses.filter(_.getName.startsWith("artsalnikov")).filter { cls =>
      try {
        log.info(s"Try to load ${cls.getName}")
        val result = workFlowType2Class.isAssignableFrom(cls.load())
        log.info(result.toString)
        result
      } catch {
        //case e: ClassNotFoundException => log.info(e.getMessage); false
        case e: LinkageError => log.info(e.getMessage); false
      }
    }
    log.info(s"${acceptedClasses.size} accepted classes found")
    log.info("Try to instantiate classes")
    acceptedClasses.foreach { cls =>
      log.info(s"Instantiating class ${cls.getName}")
      val myClass = Class.forName(cls.getName, true, loader).newInstance
      println("Message is below")
      println(myClass.asInstanceOf[ {def getDescription: String}].getDescription)
    }
*/


/*
    // we need to make sure the class we want to use is compiled
    // create custom class loader from the output of compile plus other deps
    val classpath = (Compile / fullClasspath).value.map(_.data.toPath)
    log.info(s"Classpath is initialized as $classpath")
    val loader = ClasspathUtil.makeLoader(classpath, scalaInstance.value)
    log.info(s"Loader ${loader} is initialized")
    val packages = loader.getDefinedPackages
    log.info(s"${packages.length} packages is defined")
    val classes = packages.map { pkg =>
      log.info(s"Try to read package ${pkg.getName}")
      val reflections = new Reflections(pkg.getName)
      log.info(s"Reflection is created")
      val classes = reflections.getSubTypesOf(classOf[Object])
      log.info(s"${classes.toSet.size} classes found")
      val acceptedClasses = classes.toSet.filter(_.isAssignableFrom(classOf[WorkflowType2]))
      log.info(s"${acceptedClasses.size} accepted classes found")
      acceptedClasses
    }.reduce(_ union _)
    log.info("Try to instantiate classes")
    classes.foreach { cls =>
      log.info(s"Instantiating class ${cls.getName}")
      val myClass = Class.forName(cls.getName, true, loader).newInstance
      println("Message is below")
      println(myClass.asInstanceOf[ {def getDescription: String}].getDescription)
    }
*/


    //val myClass = Class.forName("artsalnikov.ProjectTest", true, loader).newInstance
    //println("Message is below")
    //println(myClass.asInstanceOf[ {def getHello: String}].getHello)

}

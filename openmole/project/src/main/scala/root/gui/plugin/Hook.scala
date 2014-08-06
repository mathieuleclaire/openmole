package root.gui.plugin

import sbt._
import root.gui._
import root.base

object Hook extends PluginDefaults {
  implicit val artifactPrefix = Some("org.openmole.gui.plugin.hook")

  lazy val display = OsgiProject("display") dependsOn (Ext.dataui, base.plugin.Hook.display, base.Misc.replication % "test")

  lazy val file = OsgiProject("file") dependsOn (Ext.dataui, base.plugin.Hook.file, base.Misc.replication % "test")
}

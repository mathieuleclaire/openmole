/*
 * Copyright (C) 2011 <mathieu.leclaire at openmole.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.ide.plugin.task.exploration

import java.awt.Color
import org.openmole.core.implementation.task.ExplorationTask
import org.openmole.ide.core.properties.ISamplingDataUI
import org.openmole.ide.core.exception.GUIUserBadDataError
import org.openmole.ide.core.palette.SamplingDataProxyUI
import org.openmole.ide.core.properties.TaskDataUI

class ExplorationTaskDataUI(val name: String,s: Option[SamplingDataProxyUI] = None) extends TaskDataUI{
  def this(n:String) = this(n,None)
  sampling = s
   
  override def coreObject = {
    if (sampling.isDefined) new ExplorationTask(name,sampling.get.dataUI.coreObject)
    else throw new GUIUserBadDataError("Sampling missing to instanciate the exploration task " + name)
  } 
  
  override def coreClass= classOf[ExplorationTask]
  
  override def imagePath = "img/thumb/explorationTaskSmall.png"
  
  override def buildPanelUI = new ExplorationTaskPanelUI(this)
  
  override def borderColor = new Color(255,102,0)
  
  override def backgroundColor = new Color(255,102,0,128)
}

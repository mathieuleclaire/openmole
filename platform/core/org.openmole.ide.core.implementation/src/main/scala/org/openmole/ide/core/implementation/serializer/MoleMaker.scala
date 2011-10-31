/*
 * Copyright (C) 2011 Mathieu leclaire <mathieu.leclaire at openmole.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.ide.core.implementation.serializer

import org.openmole.core.model.data.IPrototype
import org.openmole.core.model.execution.IEnvironment
import org.openmole.core.model.mole.ICapsule
import org.openmole.ide.core.model.commons.TransitionType._
import org.openmole.core.model.mole.IMole
import org.openmole.ide.core.model.dataproxy.IPrototypeDataProxyUI
import org.openmole.ide.core.model.workflow.ICapsuleUI
import org.openmole.ide.core.model.commons.CapsuleType._
import org.openmole.core.implementation.task._
import org.openmole.core.implementation.mole._
import org.openmole.core.implementation.transition._
import org.openmole.ide.misc.exception.GUIUserBadDataError
import org.openmole.ide.core.model.workflow.IMoleSceneManager
import org.openmole.core.model.mole.IMoleExecution
import org.openmole.core.model.task.ITask
import org.openmole.ide.core.implementation.dataproxy.Proxys
import org.openmole.ide.core.implementation.workflow.MoleSceneManager
import org.openmole.ide.core.implementation.workflow.TransitionUI
import org.openmole.ide.core.model.workflow.ICapsuleUI
import org.openmole.ide.core.model.workflow.ITransitionUI
import scala.collection.JavaConversions._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

object MoleMaker {
                                            
  def buildMoleExecution(mole: IMole,manager: IMoleSceneManager, capsuleMap: Map[ICapsuleUI,ICapsule]): (IMoleExecution,Set[(IEnvironment,String)]) = {
    var envs = new HashSet[(IEnvironment,String)]
    val strat = new FixedEnvironmentSelection
    println("capsules :: " + manager.capsules.size)
    manager.capsules.foreach(c=>println("nn :: " + c._1))
    manager.capsules.values.foreach{c=> 
      println("env ??" + c.dataProxy.get.dataUI.environment.isDefined)
      if (c.dataProxy.get.dataUI.environment.isDefined){
        val env= c.dataProxy.get.dataUI.environment.get.dataUI.coreObject
        envs+= new Tuple2(env,c.dataProxy.get.dataUI.environment.get.dataUI.name)
        //   println(":: " + env + " for " + c.dataProxy.get.dataUI.name + ", " + doneCapsules(c))
        strat.select(capsuleMap(c),env)
      }}
   
    println("return env ??" + envs.size)
    (new MoleExecution(mole,strat),envs.toSet)
  }
  
  def buildMole(manager: IMoleSceneManager) = {
    if (manager.startingCapsule.isDefined){
      val prototypeMap: Map[IPrototypeDataProxyUI,IPrototype[_]] = Proxys.prototypes.values.map{p=> p->p.dataUI.coreObject}.toMap
      val capsuleMap= manager.capsules.map{c=> c._2->new Capsule(buildTask(c._2))}.toMap
      capsuleMap.foreach{case (cui,ccore)=> manager.capsuleConnections(cui).foreach(t=>buildTransition(ccore, capsuleMap(t.target.capsule),t))}
    
      (new Mole(capsuleMap(manager.startingCapsule.get)),capsuleMap,prototypeMap)
    }
    else throw new GUIUserBadDataError("No starting capsule is defined. The mole construction is not possible. Please define a capsule as a starting capsule.")  
  }
  
  def buildTask(capsuleUI: ICapsuleUI) = {
    capsuleUI.capsuleType match {
      case EXPLORATION_TASK=> addPrototypes(capsuleUI,capsuleUI.dataProxy.get.dataUI.coreObject.asInstanceOf[ExplorationTask])
      case BASIC_TASK=> addPrototypes(capsuleUI,capsuleUI.dataProxy.get.dataUI.coreObject)
      case CAPSULE=> throw new GUIUserBadDataError("A capsule without any task can not be run")  
    }
  }
  
  def addPrototypes(capsuleUI: ICapsuleUI, task: ITask): ITask = {
    capsuleUI.dataProxy.get.dataUI.prototypesIn.foreach(pui=> {task.addInput( pui.dataUI.coreObject)})
    capsuleUI.dataProxy.get.dataUI.prototypesOut.foreach(pui=> {task.addOutput(pui.dataUI.coreObject)})
    task
  }
  
  def buildTransition(sourceCapsule: ICapsule, targetCapsule: ICapsule,t: ITransitionUI){
    t.transitionType match {
      case BASIC_TRANSITION=> new Transition(sourceCapsule,targetCapsule) 
      case AGGREGATION_TRANSITION=> new AggregationTransition(sourceCapsule,targetCapsule)
      case EXPLORATION_TRANSITION=> new ExplorationTransition(sourceCapsule,targetCapsule)
      case _=> throw new GUIUserBadDataError("No matching type between capsule " + sourceCapsule +" and " + targetCapsule +". The transition can not be built")
    }
  }
}

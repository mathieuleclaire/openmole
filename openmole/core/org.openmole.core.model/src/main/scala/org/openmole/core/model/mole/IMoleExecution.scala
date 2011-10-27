/*
 * Copyright (C) 2010 reuillon
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.core.model.mole


import org.openmole.core.model.job.State.State
import org.openmole.core.model.tools.IRegistryWithTicket
import org.openmole.core.model.transition.IMasterTransition
import org.openmole.misc.eventdispatcher.Event
import org.openmole.misc.exception.InternalProcessingError
import org.openmole.misc.exception.MultipleException
import org.openmole.misc.exception.UserBadDataError
import org.openmole.core.model.data.IContext
import org.openmole.core.model.data.IDataChannel
import org.openmole.core.model.data.IVariable
import org.openmole.core.model.job.IJob
import org.openmole.core.model.job.IMoleJob
import org.openmole.core.model.job.MoleJobId
import scala.collection.mutable.Buffer

object IMoleExecution {
   
  case class Starting extends Event[IMoleExecution]
  case class Finished extends Event[IMoleExecution]
  case class OneJobStatusChanged(val moleJob: IMoleJob, val newState: State, val oldState: State) extends Event[IMoleExecution]
  case class OneJobSubmitted(val moleJob: IMoleJob) extends Event[IMoleExecution]
  case class JobInCapsuleFinished(val moleJob: IMoleJob, val capsule: ICapsule) extends Event[IMoleExecution]
  case class JobInCapsuleStarting(val moleJob: IMoleJob, val capsule: ICapsule) extends Event[IMoleExecution]
  
}

trait IMoleExecution {

  @throws(classOf[InternalProcessingError])
  @throws(classOf[UserBadDataError])
  def start: this.type
    
  @throws(classOf[InternalProcessingError])
  @throws(classOf[UserBadDataError])
  def cancel: this.type
    
  @throws(classOf[InterruptedException])
  @throws(classOf[MultipleException])
  def waitUntilEnded: this.type
      
  def exceptions: Iterable[Throwable]
  
  def isFinished: Boolean

  def submit(moleJob: IMoleJob, capsule: ICapsule, subMole: ISubMoleExecution, ticket: ITicket)
  def submitToEnvironment(job: IJob, capsule: ICapsule)

  def mole: IMole

  def rootTicket: ITicket
  def nextTicket(parent: ITicket): ITicket
  
  def nextJobId: MoleJobId
  
  def dataChannelRegistry: IRegistryWithTicket[IDataChannel, Buffer[IVariable[_]]]
  def subMoleExecution(job: IMoleJob): Option[ISubMoleExecution]
      
  def ticket(job: IMoleJob): Option[ITicket]
    
  def moleJobs: Iterable[IMoleJob]
  def id: String
}

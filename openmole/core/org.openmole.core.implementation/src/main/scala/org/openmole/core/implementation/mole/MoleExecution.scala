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

package org.openmole.core.implementation.mole

import java.util.UUID
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import org.openmole.core.implementation.data.Context
import org.openmole.core.model.mole.ICapsule
import org.openmole.core.model.data.IContext
import org.openmole.core.model.data.IVariable
import org.openmole.core.model.job.IJob
import org.openmole.core.model.job.IMoleJob
import org.openmole.core.model.job.IMoleJob.moleJobOrdering
import org.openmole.core.model.job.MoleJobId
import org.openmole.core.model.mole.IEnvironmentSelection
import org.openmole.core.model.mole.IMole
import org.openmole.core.model.mole.IMoleExecution
import org.openmole.misc.tools.service.Logger
import org.openmole.misc.exception.MultipleException
import org.openmole.misc.eventdispatcher.EventDispatcher
import org.openmole.misc.eventdispatcher.Event
import org.openmole.misc.eventdispatcher.EventListener
import org.openmole.core.model.mole.ITicket
import org.openmole.core.model.mole.IMoleJobGrouping
import org.openmole.core.model.mole.ISubMoleExecution
import org.openmole.core.model.data.IDataChannel
import org.openmole.misc.tools.service.Priority
import org.openmole.core.implementation.execution.local.LocalExecutionEnvironment
import org.openmole.core.implementation.tools.RegistryWithTicket
import org.openmole.core.model.mole.IInstantRerun
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

object MoleExecution extends Logger

class MoleExecution(val mole: IMole, environmentSelection: IEnvironmentSelection, moleJobGrouping: IMoleJobGrouping, instantRerun: IInstantRerun) extends IMoleExecution {

  def this(mole: IMole) = this(mole, FixedEnvironmentSelection.Empty, MoleJobGrouping.Empty, IInstantRerun.empty)
    
  def this(mole: IMole, environmentSelection: IEnvironmentSelection) = this(mole, environmentSelection, MoleJobGrouping.Empty, IInstantRerun.empty)
      
  def this(mole: IMole, environmentSelection: IEnvironmentSelection, moleJobGrouping: IMoleJobGrouping) = this(mole, environmentSelection, moleJobGrouping, IInstantRerun.empty)
  
  def this(mole: IMole, instantRerun: IInstantRerun) = this(mole, FixedEnvironmentSelection.Empty, MoleJobGrouping.Empty, instantRerun)
    
  def this(mole: IMole, environmentSelection: IEnvironmentSelection, instantRerun: IInstantRerun) = this(mole, environmentSelection, MoleJobGrouping.Empty, instantRerun)

  import IMoleExecution._
  import MoleExecution._
 
  private val _started = new AtomicBoolean(false)
  private val canceled = new AtomicBoolean(false)
  private val _finished = new Semaphore(0)

  override val id = UUID.randomUUID.toString  
  private val ticketNumber = new AtomicLong
  private val currentJobId = new AtomicLong

  
  val rootSubMoleExecution = new SubMoleExecution(None, this)
  val rootTicket = Ticket(id, ticketNumber.getAndIncrement)  
  val dataChannelRegistry = new RegistryWithTicket[IDataChannel, Buffer[IVariable[_]]]

  val exceptions = new ListBuffer[Throwable]
  

  def submit(moleJob: IMoleJob, capsule: ICapsule, subMole: ISubMoleExecution, ticket: ITicket): Unit =
    if(!canceled.get) {
      val instant = synchronized {
        EventDispatcher.trigger(this, new JobInCapsuleStarting(moleJob, capsule))
        EventDispatcher.trigger(this, new IMoleExecution.OneJobSubmitted(moleJob))
        instantRerun.rerun(moleJob, capsule)
      }
        
      if(!instant) subMole.group(moleJob, capsule, moleJobGrouping(capsule))
    }
  

  def submitToEnvironment(job: IJob, capsule: ICapsule): Unit = {
    (environmentSelection.select(capsule) match {
        case Some(environment) => environment
        case None => LocalExecutionEnvironment
      }).submit(job)
  }
 
  def start(context: IContext): this.type = {
    rootSubMoleExecution.newChild.submit(mole.root, context, nextTicket(rootTicket))
    this
  }
  
  override def start = {
    if(!_started.getAndSet(true)) start(Context.empty) 
    this
  }
  
  override def cancel: this.type = {
    if(!canceled.getAndSet(true)) {
      rootSubMoleExecution.cancel
      EventDispatcher.trigger(this, new IMoleExecution.Finished)
    }
    this
  }

  override def moleJobs = rootSubMoleExecution.jobs

  override def waitUntilEnded = {
    _finished.acquire
    _finished.release
    if(!exceptions.isEmpty) throw new MultipleException(exceptions)
    this
  }
    
  def jobFailedOrCanceled(moleJob: IMoleJob, capsule: ICapsule) = synchronized {
    moleJob.exception match {
      case None =>
      case Some(e) => exceptions += e
    }
    jobOutputTransitionsPerformed(moleJob, capsule)
  }

  def jobOutputTransitionsPerformed(job: IMoleJob, capsule: ICapsule) = synchronized {
    if(!canceled.get)  {
      instantRerun.jobFinished(job, capsule)
      if (finished) {  
        _finished.release
        EventDispatcher.trigger(this, new IMoleExecution.Finished)
      }
    }
  }
  
  override def finished: Boolean = rootSubMoleExecution.nbJobInProgress == 0
  
  override def started: Boolean = _started.get

  override def nextTicket(parent: ITicket): ITicket = Ticket(parent, ticketNumber.getAndIncrement)

  def nextJobId = new MoleJobId(id, currentJobId.getAndIncrement)
 
  
}

/*
 * Copyright (C) 15/01/14 Romain Reuillon
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

package org.openmole.plugin.method.abc

import fr.iscpif.scalabc.algorithm._
import org.openmole.core.context.{ Val, Variable }
import org.openmole.core.expansion.FromContext
import org.openmole.core.workflow.sampling._

object LenormandSampling {

  def apply(
    lenormand: Lenormand with ABC,
    state:     Val[Lenormand#STATE]
  ) = {
    val (_lenormand, _state) = (lenormand, state)
    new LenormandSampling {
      val lenormand = _lenormand
      def state = _state
    }
  }

}

abstract class LenormandSampling extends Sampling {

  val lenormand: Lenormand with ABC
  def state: Val[Lenormand#STATE]

  def prototypes = lenormand.priorPrototypes
  override def inputs = Seq(state)

  override def apply() = FromContext.apply { p ⇒
    import p._
    lenormand.sample(context(state))(random()).map {
      sampled ⇒
        (lenormand.priorPrototypes zip sampled).map {
          case (v, s) ⇒ Variable(v, s)
        }
    }.toIterator
  }

}

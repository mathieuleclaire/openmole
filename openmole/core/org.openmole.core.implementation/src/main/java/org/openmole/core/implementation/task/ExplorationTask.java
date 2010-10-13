/*
 *  Copyright (C) 2010 reuillon
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmole.core.implementation.task;

import org.openmole.core.implementation.data.Data;
import org.openmole.core.model.data.IData;
import org.openmole.commons.exception.InternalProcessingError;
import org.openmole.commons.exception.UserBadDataError;
import org.openmole.core.model.execution.IProgress;
import org.openmole.core.model.job.IContext;
import org.openmole.core.model.sampler.ISample;
import org.openmole.core.model.sampler.ISampler;
import org.openmole.core.model.task.IExplorationTask;
import org.openmole.core.model.task.annotations.Output;
import org.openmole.commons.aspect.caching.ChangeState;

public class ExplorationTask extends GenericTask implements IExplorationTask {

    @Output
    final static public IData<ISample> ExploredPlan = new Data<ISample>("ExploredPlan", ISample.class);

    private ISampler plan;

    public ExplorationTask(String name) throws UserBadDataError, InternalProcessingError {
        super(name);
    }

    public ExplorationTask(String name, ISampler plan) throws UserBadDataError, InternalProcessingError {
        super(name);
        setPlan(plan);
    }

    //If input prototype as the same name as the output it is erased
    @Override
    protected void process(IContext global, IContext context, IProgress progress) throws UserBadDataError, InternalProcessingError, InterruptedException {
        context.putVariable(ExploredPlan.getPrototype(), plan.build(global, context));
    }

    /* (non-Javadoc)
     * @see org.openmole.methods.task.IExploration#setDesign(org.openmole.core.task.ExperimentalDesign)
     */
    @ChangeState
    public void setPlan(ISampler plan) {
        this.plan = plan;
    }

    /* (non-Javadoc)
     * @see org.openmole.methods.task.IExploration#getDesign()
     */
    @Override
    public ISampler getPlan() {
        return plan;
    }

}

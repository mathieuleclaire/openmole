/*
 *  Copyright (c) 2008, Cemagref
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation; either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
 *  MA  02110-1301  USA
 */

package org.simexplorer.ui.ide.is;

import fr.cemagref.simexplorer.is.entities.metadata.MetaData;
import org.openmole.commons.exception.InternalProcessingError;
import org.simexplorer.ui.ide.workflow.model.ExplorationApplication;

public interface ExplorationApplicationSaver {

    public MetaData saveExplorationApplication(ExplorationApplication explorationApplication) throws InternalProcessingError;
}

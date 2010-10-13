/*
 *  Copyright © 2008, Cemagref
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
package org.simexplorer.ide.ui.dataexplorer.factors;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows FactorsExplorer component.
 */
public class FactorsExplorerAction extends AbstractAction {

    public FactorsExplorerAction() {
        super(NbBundle.getMessage(FactorsExplorerAction.class, "CTL_FactorsExplorerAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(FactorsExplorerTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = FactorsExplorerTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}

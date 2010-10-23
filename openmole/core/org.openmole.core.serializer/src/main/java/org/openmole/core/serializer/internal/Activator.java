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
package org.openmole.core.serializer.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.openmole.misc.pluginmanager.IPluginManager;
import org.openmole.core.serializer.ISerializer;
import org.openmole.misc.hashservice.IHashService;
import org.openmole.misc.workspace.IWorkspace;

public class Activator implements BundleActivator {

    static BundleContext context;
    private static IPluginManager pluginManager;
    private static IHashService hashService;
    ServiceRegistration msgSerial;
    private static IWorkspace workspace;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        msgSerial = context.registerService(ISerializer.class.getName(), new Serializer(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.context = null;
        msgSerial.unregister();
    }

    public static BundleContext getContext() {
        return context;
    }

    public static IWorkspace getWorkspace() {
        if (workspace != null) {
            return workspace;
        }

        synchronized (Activator.class) {
            if (workspace == null) {
                ServiceReference ref = getContext().getServiceReference(IWorkspace.class.getName());
                workspace = (IWorkspace) getContext().getService(ref);
            }
            return workspace;
        }
    }

    public synchronized static IPluginManager getPluginManager() {
        if (pluginManager == null) {
            ServiceReference ref = getContext().getServiceReference(IPluginManager.class.getName());
            pluginManager = (IPluginManager) getContext().getService(ref);
        }
        return pluginManager;
    }

    public synchronized static IHashService getHashService() {
        if (hashService == null) {
            ServiceReference ref = getContext().getServiceReference(IHashService.class.getName());
            hashService = (IHashService) getContext().getService(ref);
        }
        return hashService;
    }
    
}

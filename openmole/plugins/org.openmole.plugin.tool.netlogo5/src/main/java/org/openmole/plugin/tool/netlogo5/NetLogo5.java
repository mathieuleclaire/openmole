/*
 * Copyright (C) 2011 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmole.plugin.tool.netlogo5;

import org.nlogo.agent.Observer;
import org.nlogo.agent.World;
import org.nlogo.api.LogoException;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.nvm.Procedure;
import org.openmole.plugin.tool.netlogo.NetLogo;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author Romain Reuillon
 */
public class NetLogo5 implements NetLogo {

    protected HeadlessWorkspace workspace = createWorkspace();

    private HeadlessWorkspace createWorkspace() {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getNetLogoClassLoader());
        try {
            return HeadlessWorkspace.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(threadClassLoader);
        }
    }

    @Override
    public void open(String script) throws Exception {
        workspace.open(script);
    }

    @Override
    public void command(String cmd) throws Exception {
        workspace.command(cmd);
    }

    @Override
    public boolean isNetLogoException(Throwable e) {
        return LogoException.class.isAssignableFrom(e.getClass());
    }

    @Override
    public Object report(String variable) throws Exception {
        return workspace.report(variable);
    }

    @Override
    public void dispose() throws Exception {
        workspace.dispose();
    }

    @Override
    public String[] globals() {
        World world = workspace.world();
        Observer observer = world.observer();
        String nlGlobalList[] = new String[world.getVariablesArraySize(observer)];
        for (int i = 0; i < nlGlobalList.length; i++) {
            nlGlobalList[i] = world.observerOwnsNameAt(i);
        }
        return nlGlobalList;
    }

    public String[] reporters() {
        LinkedList<String> reporters = new LinkedList<String>();
        for (Map.Entry<String, Procedure> e : workspace.getProcedures().entrySet()) {
            if (e.getValue().tyype == Procedure.Type.REPORTER) reporters.add(e.getKey());
        }
        return reporters.toArray(new String[0]);
    }

    @Override
    public ClassLoader getNetLogoClassLoader() {
        return HeadlessWorkspace.class.getClassLoader();
    }
}

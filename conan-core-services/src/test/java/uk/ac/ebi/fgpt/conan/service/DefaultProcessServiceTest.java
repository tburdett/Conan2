/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
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
 **/
package uk.ac.ebi.fgpt.conan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessAdapter;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessListener;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 26/02/13
 * Time: 13:53
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultProcessServiceTest {

    @Mock
    ExecutionContext ec;

    @Mock
    Locality locality;

    @Mock
    Scheduler scheduler;

    @Mock
    ConanProcess conanProcess;

    private ConanProcessService conanProcessService;

    @Before
    public void setup() throws InterruptedException, ProcessExecutionException {
        this.conanProcessService = new DefaultProcessService();

        when(locality.establishConnection()).thenReturn(true);
        when(locality.disconnect()).thenReturn(true);
        when(locality.execute(anyString())).thenReturn(0);
        when(locality.monitoredExecute(anyString(), (ProcessAdapter)anyObject(), (ProcessListener)anyObject())).thenReturn(0);

        when(scheduler.createCommand(anyString())).thenReturn("bsub \"sleep 10\"");
        when(scheduler.createProcessAdapter()).thenReturn(null);

        when(ec.getLocality()).thenReturn(locality);
        when(ec.getScheduler()).thenReturn(scheduler);
    }

    @Test
    public void executeLocalUnscheduledTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(false);
        when(ec.isForegroundJob()).thenReturn(true);

        int exitCode = this.conanProcessService.execute("sleep 10", ec);

        assertTrue(exitCode == 0);
    }

    @Test
    public void executeLocalMonitoredTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(true);
        when(ec.isForegroundJob()).thenReturn(true);

        int exitCode = this.conanProcessService.execute("sleep 10", ec);

        assertTrue(exitCode == 0);
    }

    @Test
    public void executeLocalScheduledBackgroundTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(true);
        when(ec.isForegroundJob()).thenReturn(false);

        int exitCode = this.conanProcessService.execute("sleep 10", ec);

        assertTrue(exitCode == 0);
    }


    @Test
    public void executeProcessTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(false);
        when(ec.isForegroundJob()).thenReturn(true);
        when(ec.getExternalProcessConfiguration()).thenReturn(null);

        when(conanProcess.getFullCommand()).thenReturn("sleep 10");

        int exitCode = this.conanProcessService.execute(conanProcess, ec);

        assertTrue(exitCode == 0);
    }
}

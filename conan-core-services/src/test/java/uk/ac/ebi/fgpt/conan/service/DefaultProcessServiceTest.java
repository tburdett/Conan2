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
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionResult;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 26/02/13
 * Time: 13:53
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultProcessServiceTest {

    @Mock
    private ExecutionContext ec;

    @Mock
    private Locality locality;

    @Mock
    private Scheduler scheduler;

    @Mock
    private ConanProcess conanProcess;

    private ConanProcessService conanProcessService;

    @Before
    public void setup() throws InterruptedException, ProcessExecutionException {
        this.conanProcessService = new DefaultProcessService();

        when(scheduler.createCommand(anyString(), anyBoolean())).thenReturn("bsub \"sleep 10\"");
        when(scheduler.createProcessAdapter()).thenReturn(null);

        when(locality.establishConnection()).thenReturn(true);
        when(locality.disconnect()).thenReturn(true);
        when(locality.execute(anyString(), (Scheduler)anyObject())).thenReturn(new DefaultExecutionResult(0, null, null, -1));
        when(locality.monitoredExecute(anyString(), (Scheduler)anyObject())).thenReturn(new DefaultExecutionResult(0, null, null, -1));
        when(locality.dispatch(anyString(), (Scheduler)anyObject())).thenReturn(new DefaultExecutionResult(0, null, null, -1));

        when(ec.getLocality()).thenReturn(locality);
        when(ec.getScheduler()).thenReturn(scheduler);
    }

    @Test
    public void executeLocalUnscheduledTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(false);
        when(ec.isForegroundJob()).thenReturn(true);

        ExecutionResult result = this.conanProcessService.execute("sleep 10", ec);

        assertTrue(result.getExitCode() == 0);
    }

    @Test
    public void executeLocalMonitoredTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(true);
        when(ec.isForegroundJob()).thenReturn(true);

        ExecutionResult result = this.conanProcessService.execute("sleep 10", ec);

        assertTrue(result.getExitCode() == 0);
    }

    @Test
    public void executeLocalScheduledBackgroundTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(true);
        when(ec.isForegroundJob()).thenReturn(false);

        ExecutionResult result = this.conanProcessService.execute("sleep 10", ec);

        assertTrue(result.getExitCode() == 0);
    }


    @Test
    public void executeProcessTest() throws ProcessExecutionException, InterruptedException {

        when(ec.usingScheduler()).thenReturn(false);
        when(ec.isForegroundJob()).thenReturn(true);
        when(ec.getExternalProcessConfiguration()).thenReturn(null);

        when(conanProcess.getFullCommand()).thenReturn("sleep 10");

        ExecutionResult result = this.conanProcessService.execute(conanProcess, ec);

        assertTrue(result.getExitCode() == 0);
    }
}

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
package uk.ac.ebi.fgpt.conan.core.context.locality;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 13/02/13
 * Time: 12:02
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalTest {

    private Local local;

    private static final String CMD = "sleep 0";

    @Mock
    private Scheduler scheduler;


    @Before
    public void setup() {

        this.local = new Local();
    }

    @Test
    public void executeTest() throws InterruptedException, ProcessExecutionException, CommandExecutionException, IOException {

        ExecutionResult executionResult = this.local.execute(CMD, scheduler);

        assertTrue(executionResult.getExitCode() == 0);
    }

    @Test
    public void monitoredExecuteTest() throws InterruptedException, ProcessExecutionException, CommandExecutionException, IOException {

        ExecutionResult executionResult = this.local.monitoredExecute(CMD, scheduler);

        assertTrue(executionResult.getExitCode() == 0);
    }

    @Test
    public void dispatchTest() throws InterruptedException, ProcessExecutionException, CommandExecutionException, IOException {

        this.local.dispatch(CMD, scheduler);
    }

}

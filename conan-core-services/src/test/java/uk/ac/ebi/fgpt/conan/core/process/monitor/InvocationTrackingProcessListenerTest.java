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
package uk.ac.ebi.fgpt.conan.core.process.monitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessEvent;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessListener;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * User: maplesod
 * Date: 26/02/13
 * Time: 14:58
 */
@RunWith(MockitoJUnitRunner.class)
public class InvocationTrackingProcessListenerTest {

    private ProcessListener processListener;

    private static boolean success = false;

    @Mock
    ProcessEvent processEvent;

    @Before
    public void setup() {
        this.processListener = new InvocationTrackingProcessListener();
    }


    @Test
    public void testWaitFor() throws Exception {

        success = false;

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    processListener.waitFor();
                    success = true;
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        when(processEvent.getExitValue()).thenReturn(0);

        this.processListener.processComplete(processEvent);

        // Give the main thread a little time to register the update of the success flag
        Thread.sleep(1);

        assertTrue(success);
    }
}

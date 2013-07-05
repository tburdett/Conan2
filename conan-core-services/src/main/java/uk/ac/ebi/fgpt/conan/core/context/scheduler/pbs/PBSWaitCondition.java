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
package uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs;

import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.WaitCondition;

/**
 * User: maplesod
 * Date: 27/03/13
 * Time: 10:39
 */
public class PBSWaitCondition implements WaitCondition {

    private PBSExitStatusType exitStatus;
    private String condition;

    public PBSWaitCondition(PBSExitStatusType exitStatus, String condition) {
        super();
        this.exitStatus = exitStatus;
        this.condition = condition;
    }

    /*public LSFWaitCondition(ExitStatusType exitStatusType, String condition) {
        super();
        this.exitStatus = (LSFExitStatusType) new LSFExitStatus().create(exitStatusType).getExitStatus();
        this.condition = condition;
    }      */

    @Override
    public ExitStatus createExitStatus(ExitStatus.Type exitStatusType) {

        /*if (exitStatusType == ExitStatusType.COMPLETED_SUCCESS)
            return LSFExitStatusType.DONE;
        else if (exitStatusType == ExitStatusType.COMPLETED_FAILED)
            return LSFExitStatusType.ENDED;  */

        //return exitStatusType.;  //To change body of implemented methods use File | Settings | File Templates.
        return null;
    }

    @Override
    public ExitStatus getExitStatus() {
        return new PBSExitStatus(exitStatus);
    }

    @Override
    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = (PBSExitStatusType) exitStatus;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String getCommand() {
        return this.toString() + " \"sleep 10 2>&1\"";
    }

    @Override
    public String toString() {
        return "-W depend=" + this.exitStatus.getCommand() + ":" + this.condition + "";
    }
}

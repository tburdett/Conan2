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

import uk.ac.ebi.fgpt.conan.core.context.scheduler.lsf.LSFExitStatusType;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;

/**
 * User: maplesod
 * Date: 27/03/13
 * Time: 10:41
 */
public class PBSExitStatus implements ExitStatus {

    private PBSExitStatusType command;

    public PBSExitStatus() {
        this(PBSExitStatusType.AFTER_ANY);
    }

    public PBSExitStatus(PBSExitStatusType command) {
        this.command = command;
    }

    @Override
    public ExitStatus.Type getExitStatus() {
        return this.command.getExitStatus();
    }

    @Override
    public String getCommand() {
        return this.command.getCommand();
    }

    @Override
    public ExitStatus create(ExitStatus.Type exitStatusType) {

        for (PBSExitStatusType type : PBSExitStatusType.values()) {
            if (type.getExitStatus() == exitStatusType) {
                return new PBSExitStatus(type);
            }
        }

        return null;
    }
}

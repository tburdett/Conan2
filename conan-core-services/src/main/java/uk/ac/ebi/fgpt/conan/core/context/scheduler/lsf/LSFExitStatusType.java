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
package uk.ac.ebi.fgpt.conan.core.context.scheduler.lsf;

import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;

/**
 * User: maplesod
 * Date: 10/01/13
 * Time: 17:41
 */
public enum LSFExitStatusType {

    ENDED {
        @Override
        public ExitStatus.Type getExitStatus() {
            return ExitStatus.Type.COMPLETED_FAILED;
        }

        @Override
        public String getCommand() {
            return "ended";
        }


    },
    DONE {
        @Override
        public ExitStatus.Type getExitStatus() {
            return ExitStatus.Type.COMPLETED_SUCCESS;
        }

        public String getCommand() {
            return "done";
        }
    };

    public abstract ExitStatus.Type getExitStatus();

    public abstract String getCommand();

    public static LSFExitStatusType select(ExitStatus.Type type) {
        if (type == ExitStatus.Type.COMPLETED_SUCCESS)
            return DONE;
        else if (type == ExitStatus.Type.COMPLETED_FAILED) {
            return ENDED;
        }

        return ENDED;
    }
}


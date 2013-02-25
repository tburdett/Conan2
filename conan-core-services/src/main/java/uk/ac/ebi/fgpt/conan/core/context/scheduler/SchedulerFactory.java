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
package uk.ac.ebi.fgpt.conan.core.context.scheduler;

import uk.ac.ebi.fgpt.conan.core.context.scheduler.lsf.LSFArgs;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.lsf.LSFScheduler;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs.PBSArgs;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs.PBSScheduler;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;

public enum SchedulerFactory {

    LSF {
        @Override
        public AbstractScheduler create() {
            return new LSFScheduler();
        }

        @Override
        public AbstractSchedulerArgs createArgs() {
            return new LSFArgs();
        }
    },
    PBS {
        @Override
        public AbstractScheduler create() {
            return new PBSScheduler();
        }

        @Override
        public AbstractSchedulerArgs createArgs() {
            return new PBSArgs();
        }
    };

    public abstract AbstractScheduler create();

    public abstract AbstractSchedulerArgs createArgs();

    public static Scheduler createScheduler() {
        return LSF.create();
    }

    public static Scheduler createScheduler(String scheduler) {
        return SchedulerFactory.valueOf(scheduler).create();
    }
}

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

import uk.ac.ebi.fgpt.conan.model.context.Locality;

public enum LocalityFactory {

    LOCAL {
        @Override
        public Locality create() {
            return new Local();
        }
    },
    REMOTE {
        @Override
        public Locality create() {

            throw new UnsupportedOperationException("Haven't finished implementing remote execution yet.");
            //return new Remote();
        }
    };

    public abstract Locality create();

    public static Locality createLocality() {
        return LOCAL.create();
    }

    public static Locality createLocality(String locality) {
        return LocalityFactory.valueOf(locality).create();
    }
}

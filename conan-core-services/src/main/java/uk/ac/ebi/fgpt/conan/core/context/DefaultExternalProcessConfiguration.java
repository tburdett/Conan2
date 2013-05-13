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
package uk.ac.ebi.fgpt.conan.core.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.context.ExternalProcessConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * User: maplesod
 * Date: 13/02/13
 * Time: 14:29
 */
public class DefaultExternalProcessConfiguration implements ExternalProcessConfiguration {

    private static Logger log = LoggerFactory.getLogger(DefaultExternalProcessConfiguration.class);

    private String processConfigFilePath;

    private Properties properties = new Properties();

    @Override
    public String getProcessConfigFilePath() {
        return this.processConfigFilePath;
    }

    @Override
    public void setProcessConfigFilePath(String processConfigFilePath) {
        this.processConfigFilePath = processConfigFilePath;
    }

    @Override
    public void load() throws IOException {

        File processConfigFile = new File(this.processConfigFilePath);

        if (processConfigFile.exists()) {
            properties.load(new InputStreamReader(new FileInputStream(processConfigFile)));
        }
        else {
            log.warn("Specified external process configuration file does not exist: " + this.processConfigFilePath);
        }
    }

    @Override
    public String getCommand(String key) {
        return properties.getProperty(key);
    }
}

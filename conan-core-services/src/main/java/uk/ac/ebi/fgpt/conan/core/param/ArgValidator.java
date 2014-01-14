package uk.ac.ebi.fgpt.conan.core.param;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 13/01/14
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public enum ArgValidator {

    DEFAULT {
        @Override
        public boolean validate(String arg) {
            return !StringUtils.containsAny(arg, new char[]{' ', '\t', '\r', '\n', '/', File.separatorChar});
        }
    },
    DIGITS {
        @Override
        public boolean validate(String arg) {
            return StringUtils.isNumeric(arg);
        }
    },
    INTEGER {
        @Override
        public boolean validate(String arg) {
            try {
                Integer.parseInt(arg);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    },
    FLOAT {
        @Override
        public boolean validate(String arg) {
            try {
                Float.parseFloat(arg);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    },
    PATH {
        @Override
        public boolean validate(String arg) {
            return !StringUtils.containsAny(arg, new char[] {'\t', '\r', '\n'});
        }
    },
    OFF {
        @Override
        public boolean validate(String arg) {
            return true;
        }
    };

    public abstract boolean validate(String arg);
}

package uk.ac.ebi.fgpt.conan.model.param;

import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

public enum CommandLineFormat {

    POSIX {
        @Override
        public String buildOption(ParamMapEntry entry) throws ConanParameterException {

            String longName = entry.getKey().getLongName();
            String shortName = entry.getKey().getShortName();

            StringBuilder sb = new StringBuilder();

            boolean longNamePresent = longName != null && !longName.isEmpty();
            boolean shortNamePresent = shortName != null && !shortName.isEmpty();

            if (longNamePresent) {
                sb.append("--").append(longName);
            }
            else if (shortNamePresent) {
                sb.append("-").append(shortName);
            }
            else {
                throw new ConanParameterException("Neither short name nor long name were specified for this parameter");
            }

            if (!entry.getKey().isBoolean()) {
                if (longNamePresent) {
                    sb.append("=");
                }
                else {
                    sb.append(" ");
                }

                sb.append(entry.getValue());
            }

            return sb.toString().trim();
        }
    },
    KEY_VALUE_PAIR {
        @Override
        public String buildOption(ParamMapEntry entry) throws ConanParameterException {

            String longName = entry.getKey().getLongName();
            String shortName = entry.getKey().getShortName();

            StringBuilder sb = new StringBuilder();

            boolean longNamePresent = longName != null && !longName.isEmpty();
            boolean shortNamePresent = shortName != null && !shortName.isEmpty();

            if (longNamePresent) {
                sb.append(longName);
            }
            else if (shortNamePresent) {
                sb.append(shortName);
            }
            else {
                throw new ConanParameterException("Neither short name nor long name were specified for this parameter");
            }

            if (!entry.getKey().isBoolean()) {
                sb.append("=").append(entry.getValue());
            }

            return sb.toString().trim();
        }
    };

    public abstract String buildOption(ParamMapEntry entry) throws ConanParameterException;
}

package uk.ac.ebi.fgpt.conan.process.atlas;

/**
 * Enum for Atlas Eligibility failure reasons and appropriate messages for Submission Tracking database
 *
 * @author Natalja Kurbatova
 * @date 15/02/11
 */


public enum FailureReasons {
        DATA_FILES_MISSING(1), ARRAY_DESIGN_NOT_IN_ATLAS(2), TYPE_OF_EXPERIMENT(3), TWO_CHANNELS(4), NO_FACTOR_VALUES(5),
        REPLICATES(6), NOT_IN_CONTROLLED_VOCABULARY(7), REPEATED(8);

        private int code;

        private FailureReasons(int c) {
            code = c;
        }

        public int getCode() {
            return code;
        }
}

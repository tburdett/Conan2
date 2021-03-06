CREATE SEQUENCE "SEQ_CONAN" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 15321 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "CONAN_USERS"
  (
    "ID"          NUMBER NOT NULL ENABLE,
    "FIRST_NAME"  VARCHAR2(200 BYTE),
    "LAST_NAME"   VARCHAR2(200 BYTE) NOT NULL ENABLE,
    "EMAIL"       VARCHAR2(200 BYTE) NOT NULL ENABLE,
    "RESTAPIKEY"  VARCHAR2(200 BYTE),
    "USER_NAME"   VARCHAR2(200 BYTE) NOT NULL ENABLE,
    "PERMISSIONS" VARCHAR2(200 BYTE),
    CONSTRAINT "CONAN_USERS_PK" PRIMARY KEY ("ID") ENABLE
  )
 ;
CREATE OR REPLACE TRIGGER "USERS_ID_FROM_SEQ" before
  INSERT ON "CONAN_USERS" FOR EACH row BEGIN IF inserting THEN IF :NEW."ID" IS NULL THEN
  SELECT SEQ_CONAN.nextval INTO :NEW."ID" FROM dual;
END IF;
END IF;
END;
/
ALTER TRIGGER "USERS_ID_FROM_SEQ" ENABLE;

CREATE TABLE "CONAN_TASKS"
  (
    "ID"   NUMBER NOT NULL ENABLE,
    "NAME" VARCHAR2(200 BYTE),
    "START_DATE" TIMESTAMP (6) DEFAULT NULL,
    "END_DATE" TIMESTAMP (6),
    "USER_ID"                NUMBER NOT NULL ENABLE,
    "PIPELINE_NAME"          VARCHAR2(200 BYTE) NOT NULL ENABLE,
    "PRIORITY"               VARCHAR2(200 BYTE) DEFAULT NULL,
    "FIRST_PROCESS_INDEX"    NUMBER DEFAULT NULL NOT NULL ENABLE,
    "STATE"                  VARCHAR2(200 BYTE) DEFAULT NULL,
    "STATUS_MESSAGE"         VARCHAR2(1000 BYTE) DEFAULT NULL,
    "CURRENT_EXECUTED_INDEX" NUMBER DEFAULT NULL NOT NULL ENABLE,
    "CREATION_DATE" TIMESTAMP (6),
    CONSTRAINT "CONAN_TASKS_PK" PRIMARY KEY ("ID") ENABLE,
    CONSTRAINT "CONAN_TASKS_USERS_ID_FK" FOREIGN KEY ("USER_ID") REFERENCES "CONAN_USERS" ("ID") ENABLE
  )
 ;
CREATE OR REPLACE TRIGGER "TASKS_ID_FROM_SEQ" before
  INSERT ON "CONAN_TASKS" FOR EACH row BEGIN IF inserting THEN IF :NEW."ID" IS NULL THEN
  SELECT SEQ_CONAN.nextval INTO :NEW."ID" FROM dual;
END IF;
END IF;
END;
/
ALTER TRIGGER "TASKS_ID_FROM_SEQ" ENABLE;

CREATE INDEX CONAN_TASKS_NAME_STATE ON CONAN_TASKS (NAME, STATE);

CREATE TABLE "CONAN_PROCESSES"
  (
    "ID"   NUMBER NOT NULL ENABLE,
    "NAME" VARCHAR2(200 BYTE) NOT NULL ENABLE,
    "START_DATE" TIMESTAMP (6) DEFAULT SYSDATE NOT NULL ENABLE,
    "END_DATE" TIMESTAMP (6),
    "USER_ID"   NUMBER NOT NULL ENABLE,
    "TASK_ID"   NUMBER NOT NULL ENABLE,
    "EXIT_CODE" NUMBER,
    "ERROR_MESSAGE" VARCHAR2(200 BYTE),
    CONSTRAINT "CONAN_PROCESSES_PK" PRIMARY KEY ("ID") ENABLE,
    CONSTRAINT "CONAN_PROCESSES_USER_ID_FK" FOREIGN KEY ("USER_ID") REFERENCES "CONAN_USERS" ("ID") ENABLE,
    CONSTRAINT "CONAN_PROCESSES_TASK_ID_FK" FOREIGN KEY ("TASK_ID") REFERENCES "CONAN_TASKS" ("ID") ENABLE
  )
 ;
CREATE OR REPLACE TRIGGER "PROCESSES_ID_FROM_SEQ" before
  INSERT ON "CONAN_PROCESSES" FOR EACH row BEGIN IF inserting THEN IF :NEW."ID" IS NULL THEN
  SELECT SEQ_CONAN.nextval INTO :NEW."ID" FROM dual;
END IF;
END IF;
END;
/
ALTER TRIGGER "PROCESSES_ID_FROM_SEQ" ENABLE;

CREATE TABLE "CONAN_PARAMETERS"
  (
    "ID"              NUMBER NOT NULL ENABLE,
    "PARAMETER_NAME"  VARCHAR2(200 BYTE) NOT NULL ENABLE,
    "PARAMETER_VALUE" VARCHAR2(1000 BYTE) NOT NULL ENABLE,
    "TASK_ID"         NUMBER NOT NULL ENABLE,
    CONSTRAINT "CONAN_PAREMETERS_PK" PRIMARY KEY ("ID") ENABLE,
    CONSTRAINT "CONAN_PAREMETERS_TASK_ID_FK" FOREIGN KEY ("TASK_ID") REFERENCES "CONAN_TASKS" ("ID") ENABLE
  )
 ;
CREATE OR REPLACE TRIGGER "CONAN_PARAMETERS_PK" before
  INSERT ON "CONAN_PARAMETERS" FOR EACH row BEGIN IF inserting THEN IF :NEW."ID" IS NULL THEN
  SELECT SEQ_CONAN.nextval INTO :NEW."ID" FROM dual;
END IF;
END IF;
END;
/
ALTER TRIGGER "CONAN_PARAMETERS_PK" ENABLE;
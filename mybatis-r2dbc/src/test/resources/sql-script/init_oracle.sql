-- @DELIMITER /
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE emp';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE dept';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- @DELIMITER ;
commit ;
--  DEMO *** or dept

CREATE TABLE dept
(
    dept_no     NUMBER(19) check (dept_no > 0) NOT NULL,
    dept_name   varchar2(64)         NOT NULL,
    location    varchar2(100)        NOT NULL,
    create_time timestamp(0)            NOT NULL,
    PRIMARY KEY (dept_no)
);

commit ;

-- Generate ID using sequence and trigger
CREATE SEQUENCE dept_seq START WITH 1 INCREMENT BY 1;

-- @DELIMITER /

CREATE OR REPLACE TRIGGER dept_seq_tr
BEFORE INSERT ON dept
FOR EACH ROW
WHEN (NEW.dept_no IS NULL)
BEGIN
SELECT dept_seq.NEXTVAL INTO :NEW.dept_no FROM DUAL;
END;
/

-- @DELIMITER ;

commit ;

-- Records of dept


INSERT INTO dept VALUES ('1', 'ACCOUNTING', 'NEW YORK', SYSTIMESTAMP);

INSERT INTO dept VALUES ('2', 'RESEARCH', 'DALLAS', SYSTIMESTAMP);

INSERT INTO dept VALUES ('3', 'SALES', 'CHICAGO', SYSTIMESTAMP);

INSERT INTO dept VALUES ('4', 'OPERATIONS', 'BOSTON', SYSTIMESTAMP);

commit ;
--  DEMO *** or emp


CREATE TABLE emp
(
    emp_no      NUMBER(19) check (emp_no > 0) NOT NULL,
    emp_name    varchar2(64)         NOT NULL,
    job         varchar2(100)        NOT NULL,
    manager     varchar2(100) DEFAULT NULL        NULL,
    hire_date   date                NOT NULL,
    salary      NUMBER(10)      NOT NULL,
    kpi      NUMBER(3,2)      NOT NULL,
    dept_no     NUMBER(19) check (dept_no > 0) NOT NULL,
    create_time timestamp(0)            NOT NULL,
    PRIMARY KEY (emp_no)
    ,
    CONSTRAINT FK_DEPTNO FOREIGN KEY (dept_no) REFERENCES dept (dept_no)
);

commit ;

-- Generate ID using sequence and trigger
CREATE SEQUENCE emp_seq START WITH 1 INCREMENT BY 1;

commit ;

-- @DELIMITER /

CREATE OR REPLACE TRIGGER emp_seq_tr
BEFORE INSERT ON emp
FOR EACH ROW
WHEN (NEW.emp_no IS NULL)
BEGIN
SELECT emp_seq.NEXTVAL INTO :NEW.emp_no FROM DUAL;
END;
/

-- @DELIMITER ;

commit ;

CREATE INDEX FK_DEPTNO ON emp (dept_no);

commit ;

-- Records of emp

INSERT INTO emp VALUES ('1', 'SMITH', 'CLERK', '13', TO_DATE('1980-12-17','YYYY-MM-DD'), '800', '0.82','2', SYSTIMESTAMP);

INSERT INTO emp VALUES ('2', 'ALLEN', 'SALESMAN', '6', TO_DATE('1981-02-20','YYYY-MM-DD'), '1600', '0.57', '3', SYSTIMESTAMP);

INSERT INTO emp VALUES ('3', 'WARD', 'SALESMAN', '6', TO_DATE('1981-02-22','YYYY-MM-DD'), '1250', '0.73', '3', SYSTIMESTAMP);

INSERT INTO emp VALUES ('4', 'JONES', 'MANAGER', '9', TO_DATE('1981-04-02','YYYY-MM-DD'), '2975', '0.94', '2', SYSTIMESTAMP);

INSERT INTO emp VALUES ('5', 'MARTIN', 'SALESMAN', '6', TO_DATE('1981-09-28','YYYY-MM-DD'), '1250', '0.83', '3', SYSTIMESTAMP);

INSERT INTO emp VALUES ('6', 'BLAKE', 'MANAGER', '9', TO_DATE('1981-05-01','YYYY-MM-DD'), '2850', '0.50', '3', SYSTIMESTAMP);

INSERT INTO emp VALUES ('7', 'CLARK', 'MANAGER', '9', TO_DATE('1981-06-09','YYYY-MM-DD'), '2450', '0.69', '1', SYSTIMESTAMP);

INSERT INTO emp VALUES ('8', 'SCOTT', 'ANALYST', '4', TO_DATE('1987-04-19','YYYY-MM-DD'), '3000', '0.47', '2', SYSTIMESTAMP);

INSERT INTO emp VALUES ('9', 'KING', 'PRESIDENT', null, TO_DATE('1981-11-17','YYYY-MM-DD'), '5000', '1.00', '1', SYSTIMESTAMP);

INSERT INTO emp VALUES ('10', 'TURNER', 'SALESMAN', '6', TO_DATE('1981-09-08','YYYY-MM-DD'), '1500', '0.52', '3', SYSTIMESTAMP);

INSERT INTO emp VALUES ('11', 'ADAMS', 'CLERK', '8', TO_DATE('1987-05-23','YYYY-MM-DD'), '1100', '0.74', '2', SYSTIMESTAMP);

INSERT INTO emp VALUES ('12', 'JAMES', 'CLERK', '6', TO_DATE('1981-12-03','YYYY-MM-DD'), '950', '0.91', '3', SYSTIMESTAMP);

INSERT INTO emp VALUES ('13', 'FORD', 'ANALYST', '4', TO_DATE('1981-12-03','YYYY-MM-DD'), '3000', '1.00', '2', SYSTIMESTAMP);

INSERT INTO emp VALUES ('14', 'MILLER', 'CLERK', '7', TO_DATE('1982-01-23','YYYY-MM-DD'), '1300', '0.99', '1', SYSTIMESTAMP);

commit ;

-- @DELIMITER /

BEGIN
EXECUTE IMMEDIATE 'DROP TABLE subject';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/


BEGIN
EXECUTE IMMEDIATE 'DROP TABLE subject_data';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- @DELIMITER ;

commit ;

CREATE TABLE subject
(
    id     NUMBER(10) NOT NULL,
    name   VARCHAR2(20),
    age    NUMBER(10) NOT NULL,
    height NUMBER(10),
    weight NUMBER(10),
    active RAW(1),
    dt     TIMESTAMP(0),
    length NUMBER(19)
);

commit ;

CREATE TABLE subject_data
(
    aByte      NUMBER(3),
    aShort     NUMBER(5),
    aChar      CHAR,
    anInt      NUMBER(10),
    aLong      NUMBER(19),
    aFloat     BINARY_DOUBLE,
    aDouble    BINARY_DOUBLE,
    aBoolean   RAW(1),
    aString    VARCHAR2(255),
    anEnum     VARCHAR2(50),
    aDecimal     NUMBER(4, 2),
    aTimestamp TIMESTAMP(0),
    aDate DATE,
    aDateTime TIMESTAMP(0)
);

commit ;

INSERT INTO subject
SELECT 1, 'a', 10, 100, 45, HEXTORAW('01'), TO_TIMESTAMP('2023-01-01 01:01:01.000', 'YYYY-MM-DD HH24:MI:SS.FF3'), 22222222222 FROM dual
UNION ALL

SELECT 2, 'b', 20, NULL, 45, HEXTORAW('01'), SYSTIMESTAMP, 22222222222 FROM dual
UNION ALL

SELECT 3, 'c', 30, 33, NULL, HEXTORAW('00'), NULL, 22222222222 FROM dual
UNION ALL

SELECT 4, 'd', 40, NULL, NULL, HEXTORAW('00'), SYSTIMESTAMP, 22222222222 FROM dual;


INSERT INTO subject_data
SELECT 1, 1, 'a', 1, 1, TO_BINARY_DOUBLE(1), TO_BINARY_DOUBLE(1.0), HEXTORAW('01'), 'a', 'A', 10.23, SYSTIMESTAMP, SYSDATE, SYSDATE  FROM dual
UNION ALL
SELECT 2, 2, 'b', 2, 2, TO_BINARY_DOUBLE(2), TO_BINARY_DOUBLE(2.0), HEXTORAW('00'), 'b', 'B', 10.23, TO_TIMESTAMP('2023-01-01 01:01:01.000', 'YYYY-MM-DD HH24:MI:SS.FF3'), TO_DATE('2023-01-01', 'YYYY-MM-DD'), SYSDATE  FROM dual
UNION ALL
SELECT 3, 3, 'c', 3, 3, TO_BINARY_DOUBLE(3), TO_BINARY_DOUBLE(3.0), HEXTORAW('01'), 'c', 'C', 10.23, SYSTIMESTAMP, NULL, SYSDATE  FROM dual;


commit ;

CREATE TABLE subject_content
(
    id  NUMBER(10),
    blob_content BLOB,
    clob_content CLOB
);

commit ;

INSERT INTO subject_content
SELECT 1, UTL_RAW.CAST_TO_RAW('This is a blob content1'), 'This is a clob content1' FROM dual;

INSERT INTO subject_content
SELECT 2, UTL_RAW.CAST_TO_RAW('This is a blob content2'), 'This is a clob content2' FROM dual;

commit ;
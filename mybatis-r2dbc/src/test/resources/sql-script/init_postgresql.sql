DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS emp;
DROP TABLE IF EXISTS dept;

CREATE TABLE IF NOT EXISTS dept
(
    dept_no     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dept_name   varchar(64)  NOT NULL,
    location    varchar(100) NOT NULL,
    create_time timestamp without time zone NOT NULL
)
;

INSERT INTO dept (dept_name, location, create_time) VALUES ('ACCOUNTING', 'NEW YORK', NOW());
INSERT INTO dept (dept_name, location, create_time) VALUES ('RESEARCH', 'DALLAS', NOW());
INSERT INTO dept (dept_name, location, create_time) VALUES ('SALES', 'CHICAGO', NOW());
INSERT INTO dept (dept_name, location, create_time) VALUES ('OPERATIONS', 'BOSTON', NOW());


CREATE TABLE IF NOT EXISTS emp
(
    emp_no      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    emp_name    varchar(64)     NOT NULL,
    job         varchar(100)    NOT NULL,
    manager     varchar(100)    NULL DEFAULT NULL,
    hire_date   date            NOT NULL,
    salary      INT             NOT NULL,
    kpi         DECIMAL(3, 2)   NOT NULL,
    dept_no     INT NOT NULL,
    create_time timestamp without time zone    NOT NULL,
    CONSTRAINT FK_DEPTNO FOREIGN KEY (dept_no) REFERENCES dept (dept_no)
)
;

INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'SMITH', 'CLERK', '13', '1980-12-17', '800', '0.82','2',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'ALLEN', 'SALESMAN', '6', '1981-02-20', '1600', '0.57', '3',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'WARD', 'SALESMAN', '6', '1981-02-22', '1250', '0.73', '3',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'JONES', 'MANAGER', '9', '1981-04-02', '2975', '0.94', '2',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'MARTIN', 'SALESMAN', '6', '1981-09-28', '1250', '0.83', '3',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'BLAKE', 'MANAGER', '9', '1981-05-01', '2850', '0.50', '3',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'CLARK', 'MANAGER', '9', '1981-06-09', '2450', '0.69', '1',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'SCOTT', 'ANALYST', '4', '1987-04-19', '3000', '0.47', '2',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'KING', 'PRESIDENT', null, '1981-11-17', '5000', '1.00', '1',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'TURNER', 'SALESMAN', '6', '1981-09-08', '1500', '0.52', '3',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'ADAMS', 'CLERK', '8', '1987-05-23', '1100', '0.74', '2',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'JAMES', 'CLERK', '6', '1981-12-03', '950', '0.91', '3',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'FORD', 'ANALYST', '4', '1981-12-03', '3000', '1.00', '2',NOW());
INSERT INTO emp (emp_name, job, manager, hire_date, salary, kpi, dept_no, create_time) VALUES ( 'MILLER', 'CLERK', '7', '1982-01-23', '1300', '0.99', '1',NOW());

-- Records of project

CREATE TABLE IF NOT EXISTS project
(
    project_id BIGINT NOT NULL,
    emp_no     BIGINT NOT NULL,
    start_date date   NOT NULL,
    end_date   date   NOT NULL,
    PRIMARY KEY (project_id),
    CONSTRAINT fk_EMPNO FOREIGN KEY (emp_no) REFERENCES emp (emp_no)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
);

INSERT INTO project VALUES (1, 7, '2005-06-16', '2005-06-18');
INSERT INTO project VALUES (4, 7, '2005-06-19', '2005-06-24');
INSERT INTO project VALUES (7, 7, '2005-06-22', '2005-06-25');
INSERT INTO project VALUES (10, 7, '2005-06-25', '2005-06-28');
INSERT INTO project VALUES (13, 7, '2005-06-28', '2005-07-02');
INSERT INTO project VALUES (2, 9, '2005-06-17', '2005-06-21');
INSERT INTO project VALUES (8, 9, '2005-06-23', '2005-06-25');
INSERT INTO project VALUES (14, 9, '2005-06-29', '2005-06-30');
INSERT INTO project VALUES (11, 9, '2005-06-26', '2005-06-27');
INSERT INTO project VALUES (5, 9, '2005-06-20', '2005-06-24');
INSERT INTO project VALUES (3, 14, '2005-06-18', '2005-06-22');
INSERT INTO project VALUES (12, 14, '2005-06-27', '2005-06-28');
INSERT INTO project VALUES (15, 14, '2005-06-30', '2005-07-03');
INSERT INTO project VALUES (9, 14, '2005-06-24', '2005-06-27');
INSERT INTO project VALUES (6, 14, '2005-06-21', '2005-06-23');


DROP TABLE IF EXISTS subject;
DROP TABLE IF EXISTS subject_data;

CREATE TABLE subject
(
    id     INT NOT NULL,
    name   VARCHAR(20),
    age    INT NOT NULL,
    height INT,
    weight INT,
    active BOOLEAN,
    dt     TIMESTAMP without time zone,
    length BIGINT
);

CREATE TABLE subject_data
(
    aByte      SMALLINT,
    aShort     SMALLINT,
    aChar      CHAR,
    anInt      INT,
    aLong      BIGINT,
    aFloat     FLOAT,
    aDouble    DOUBLE PRECISION,
    aBoolean   BOOLEAN,
    aString    VARCHAR(255),
    anEnum     VARCHAR(50),
    aDecimal   DECIMAL(4, 2),
    aTimestamp TIMESTAMP without time zone,
    aDate DATE,
    aDateTime TIMESTAMP without time zone
);

INSERT INTO subject
VALUES (1, 'a', 10, 100, 45, true, '2023-01-01 01:01:01.000', 22222222222),
       (2, 'b', 20, NULL, 45, true, CURRENT_TIMESTAMP, 22222222222),
       (3, 'c', 30, 33, NULL, false, NULL, 22222222222),
       (4, 'd', 40, NULL, NULL, false, CURRENT_TIMESTAMP, 22222222222);

INSERT INTO subject_data
VALUES (1, 1, 'a', 1, 1, 1, 1.0, true, 'a', 'A', 10.23, CURRENT_TIMESTAMP, DATE(NOW()), NOW()),
       (2, 2, 'b', 2, 2, 2, 2.0, false, 'b', 'B', 10.23, NULL, '2023-01-01', '2023-01-01 01:01:01.000'),
       (3, 3, 'c', 3, 3, 3, 3.0, true, 'c', 'C', 10.23, CURRENT_TIMESTAMP, NULL, NOW());

CREATE TABLE subject_content
(
    id  INT,
    blob_content bytea,
    clob_content TEXT
);


INSERT INTO subject_content VALUES (1,'This is a blob content1'::bytea,'This is a clob content1');
INSERT INTO subject_content VALUES (2,'This is a blob content2'::bytea,'This is a clob content2');
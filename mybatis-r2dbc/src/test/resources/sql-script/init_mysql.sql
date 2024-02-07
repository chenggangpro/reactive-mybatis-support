DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `emp`;
DROP TABLE IF EXISTS `dept`;

-- Table structure for dept
CREATE TABLE IF NOT EXISTS `dept`
(
    `dept_no`     BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'dept no',
    `dept_name`   varchar(64)         NOT NULL COMMENT 'dept name',
    `location`    varchar(100)        NOT NULL COMMENT 'location',
    `create_time` datetime            NOT NULL COMMENT 'create time',
    PRIMARY KEY (`dept_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='dept';


-- Records of dept

INSERT INTO dept VALUES ('1', 'ACCOUNTING', 'NEW YORK', NOW());
INSERT INTO dept VALUES ('2', 'RESEARCH', 'DALLAS', NOW());
INSERT INTO dept VALUES ('3', 'SALES', 'CHICAGO', NOW());
INSERT INTO dept VALUES ('4', 'OPERATIONS', 'BOSTON', NOW());

-- Table structure for emp

CREATE TABLE IF NOT EXISTS `emp`
(
    `emp_no`      BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'emp no',
    `emp_name`    varchar(64)         NOT NULL COMMENT 'emp name',
    `job`         varchar(100)        NOT NULL COMMENT 'job',
    `manager`     varchar(100)        NULL DEFAULT NULL COMMENT 'manager',
    `hire_date`   date                NOT NULL COMMENT 'hire date',
    `salary`      INT(11)      NOT NULL COMMENT 'salary',
    `kpi`      DECIMAL(3,2)      NOT NULL COMMENT 'kpi',
    `dept_no`     BIGINT(20) unsigned NOT NULL COMMENT 'dept no',
    `create_time` datetime            NOT NULL COMMENT 'create time',
    PRIMARY KEY (`emp_no`),
    KEY FK_DEPTNO (dept_no),
    CONSTRAINT FK_DEPTNO FOREIGN KEY (dept_no) REFERENCES dept (dept_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='emp';

-- Records of emp
INSERT INTO emp VALUES ('1', 'SMITH', 'CLERK', '13', '1980-12-17', '800', '0.82','2', NOW());
INSERT INTO emp VALUES ('2', 'ALLEN', 'SALESMAN', '6', '1981-02-20', '1600', '0.57', '3', NOW());
INSERT INTO emp VALUES ('3', 'WARD', 'SALESMAN', '6', '1981-02-22', '1250', '0.73', '3', NOW());
INSERT INTO emp VALUES ('4', 'JONES', 'MANAGER', '9', '1981-04-02', '2975', '0.94', '2', NOW());
INSERT INTO emp VALUES ('5', 'MARTIN', 'SALESMAN', '6', '1981-09-28', '1250', '0.83', '3', NOW());
INSERT INTO emp VALUES ('6', 'BLAKE', 'MANAGER', '9', '1981-05-01', '2850', '0.50', '3', NOW());
INSERT INTO emp VALUES ('7', 'CLARK', 'MANAGER', '9', '1981-06-09', '2450', '0.69', '1', NOW());
INSERT INTO emp VALUES ('8', 'SCOTT', 'ANALYST', '4', '1987-04-19', '3000', '0.47', '2', NOW());
INSERT INTO emp VALUES ('9', 'KING', 'PRESIDENT', null, '1981-11-17', '5000', '1.00', '1', NOW());
INSERT INTO emp VALUES ('10', 'TURNER', 'SALESMAN', '6', '1981-09-08', '1500', '0.52', '3', NOW());
INSERT INTO emp VALUES ('11', 'ADAMS', 'CLERK', '8', '1987-05-23', '1100', '0.74', '2', NOW());
INSERT INTO emp VALUES ('12', 'JAMES', 'CLERK', '6', '1981-12-03', '950', '0.91', '3', NOW());
INSERT INTO emp VALUES ('13', 'FORD', 'ANALYST', '4', '1981-12-03', '3000', '1.00', '2', NOW());
INSERT INTO emp VALUES ('14', 'MILLER', 'CLERK', '7', '1982-01-23', '1300', '0.99', '1', NOW());

-- Records of project

CREATE TABLE IF NOT EXISTS `project`
(
    `project_id` BIGINT(20) unsigned NOT NULL COMMENT 'project no',
    `emp_no`     BIGINT(20) unsigned NOT NULL COMMENT 'emp no',
    `start_date` date                NOT NULL COMMENT 'start date',
    `end_date`   date                NOT NULL COMMENT 'end date',
    PRIMARY KEY (`project_id`),
    KEY FK_EMPNO (emp_no),
    CONSTRAINT FK_EMPNO FOREIGN KEY (emp_no) REFERENCES emp (emp_no)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
);

INSERT INTO `project` VALUES (1, 7, '2005-06-16', '2005-06-18');
INSERT INTO `project` VALUES (4, 7, '2005-06-19', '2005-06-24');
INSERT INTO `project` VALUES (7, 7, '2005-06-22', '2005-06-25');
INSERT INTO `project` VALUES (10, 7, '2005-06-25', '2005-06-28');
INSERT INTO `project` VALUES (13, 7, '2005-06-28', '2005-07-02');
INSERT INTO `project` VALUES (2, 9, '2005-06-17', '2005-06-21');
INSERT INTO `project` VALUES (8, 9, '2005-06-23', '2005-06-25');
INSERT INTO `project` VALUES (14, 9, '2005-06-29', '2005-06-30');
INSERT INTO `project` VALUES (11, 9, '2005-06-26', '2005-06-27');
INSERT INTO `project` VALUES (5, 9, '2005-06-20', '2005-06-24');
INSERT INTO `project` VALUES (3, 14, '2005-06-18', '2005-06-22');
INSERT INTO `project` VALUES (12, 14, '2005-06-27', '2005-06-28');
INSERT INTO `project` VALUES (15, 14, '2005-06-30', '2005-07-03');
INSERT INTO `project` VALUES (9, 14, '2005-06-24', '2005-06-27');
INSERT INTO `project` VALUES (6, 14, '2005-06-21', '2005-06-23');


DROP TABLE IF EXISTS `subject`;
DROP TABLE IF EXISTS `subject_data`;

CREATE TABLE `subject`
(
    `id`     INT NOT NULL,
    `name`   VARCHAR(20),
    `age`    INT NOT NULL,
    `height` INT,
    `weight` INT,
    `active` BIT,
    `dt`     TIMESTAMP,
    `length` BIGINT
);

CREATE TABLE `subject_data`
(
    `aByte`      TINYINT,
    `aShort`     SMALLINT,
    `aChar`      CHAR,
    `anInt`      INT,
    `aLong`      BIGINT,
    `aFloat`     FLOAT,
    `aDouble`    DOUBLE,
    `aBoolean`   BIT,
    `aString`    VARCHAR(255),
    `anEnum`     VARCHAR(50),
    `aDecimal`     DECIMAL(4, 2),
    `aTimestamp` TIMESTAMP,
    `aDate` DATE,
    `aDateTime` DATETIME
);

INSERT INTO `subject`
VALUES (1, 'a', 10, 100, 45, 1, '2023-01-01 01:01:01.000', 22222222222),
       (2, 'b', 20, NULL, 45, 1, CURRENT_TIMESTAMP, 22222222222),
       (3, 'c', 30, 33, NULL, 0, NULL, 22222222222),
       (4, 'd', 40, NULL, NULL, 0, CURRENT_TIMESTAMP, 22222222222);

INSERT INTO `subject_data`
VALUES (1, 1, 'a', 1, 1, 1, 1.0, 1, 'a', 'A', 10.23, CURRENT_TIMESTAMP, DATE(NOW()), NOW()),
       (2, 2, 'b', 2, 2, 2, 2.0, 0, 'b', 'B', 10.23, NULL, '2023-01-01', '2023-01-01 01:01:01.000'),
       (3, 3, 'c', 3, 3, 3, 3.0, 1, 'c', 'C', 10.23, CURRENT_TIMESTAMP, NULL, NOW());

CREATE TABLE `subject_content`
(
  `id`  INT,
  `blob_content` BLOB,
  `clob_content` TEXT
);

INSERT INTO `subject_content` VALUES (1, BINARY 'This is a blob content1','This is a clob content1');
INSERT INTO `subject_content` VALUES (2, BINARY 'This is a blob content2','This is a clob content2');
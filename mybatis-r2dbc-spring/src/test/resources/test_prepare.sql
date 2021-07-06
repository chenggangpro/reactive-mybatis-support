DROP TABLE IF EXISTS people;
CREATE TABLE people (
                      id  INT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'auto_increment_id',
                      nick VARCHAR(32) NULL DEFAULT NULL COMMENT 'nickname',
                      created_at DATETIME NULL DEFAULT NULL COMMENT 'create_time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='people';

INSERT people (nick, created_at) VALUES ('mybatis',NOW());
INSERT people (nick, created_at) VALUES ('reactive_mybatis',NOW());
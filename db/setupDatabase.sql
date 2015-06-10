CREATE USER 'ema'@'%' IDENTIFIED BY 'password';

create database ema

grant all on ema.* to 'ema'@'%';

flush privileges;

CREATE TABLE `ESB_MESSAGE`
(
    `id` bigint(20) NOT NULL,  `
    'error_component` varchar(255) DEFAULT NULL,
    `error_details` text,
    `error_message` text,
    `error_queue` varchar(255) DEFAULT NULL,  `
    'error_system` varchar(255) DEFAULT NULL,
    `error_type` varchar(255) DEFAULT NULL,
    `message_guid` varchar(255) DEFAULT NULL,
    `jms_message_id` varchar(255) DEFAULT NULL,
    `message_type` varchar(255) DEFAULT NULL,
    `occurrence_count` int(11) DEFAULT NULL,
    `payload` text,
    `service_name` varchar(255) DEFAULT NULL,
    `source_location` varchar(255) DEFAULT NULL,
    `source_queue` varchar(255) DEFAULT NULL,
    `source_system` varchar(255) DEFAULT NULL,
    `jms_message_timestamp` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `ESB_MESSAGE_HEADER`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL,
    `type` varchar(255) DEFAULT NULL,
    `value` varchar(255) DEFAULT NULL,
    `message_id` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_rrpyp9ig9g991m2ktamrcvs6b` (`message_id`),
    CONSTRAINT `FK_rrpyp9ig9g991m2ktamrcvs6b`
    FOREIGN KEY (`message_id`) REFERENCES `ESB_MESSAGE` (`id`)
);

CREATE TABLE `ESB_MESSAGE_SENSITIVE_INFO`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `value` varchar(255) DEFAULT NULL,
    `message_id` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_8p928yu1xeh4h3qkuao1wgt9h` (`message_id`),
    CONSTRAINT `FK_8p928yu1xeh4h3qkuao1wgt9h`
    FOREIGN KEY (`message_id`) REFERENCES `ESB_MESSAGE` (`id`)
);

CREATE TABLE `AUDIT_EVENT`
(
    `event_id` bigint(20) NOT NULL,
    `action` varchar(255) NOT NULL,
    `key_type` varchar(255) DEFAULT NULL,
    `timestamp` datetime NOT NULL,
    `message` varchar(255) DEFAULT NULL,
    `message_key` varchar(255) NOT NULL,
    `message_type` varchar(255) NOT NULL,
    `principal` varchar(255) NOT NULL,  PRIMARY KEY (`event_id`)
);

CREATE TABLE `METADATA`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL,
    `parent_id` bigint(20) DEFAULT NULL,
    `type` varchar(255) DEFAULT NULL,
    `value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO METADATA
    values (1,'SearchKeys',-1,'SearchKeys','SearchKeys');

INSERT INTO METADATA
    values (2,'Entities',-1,'Entities','Entities');

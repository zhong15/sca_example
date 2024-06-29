CREATE TABLE `address` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `address` varchar(50) NOT NULL COMMENT '城市',
  `weather` varchar(50) NOT NULL COMMENT '天气',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市表';

CREATE TABLE `weather` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `address_id` bigint(20) unsigned NOT NULL COMMENT '城市ID',
  `address` varchar(50) NOT NULL COMMENT '城市',
  `the_day` date NOT NULL COMMENT '日期',
  `weather` varchar(50) NOT NULL COMMENT '天气',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_address_id_the_day` (`address_id`,`the_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='天气表';

#!/bin/bash

export HOME=/root

cat << EOF |  mysql CRX
CREATE TABLE IF NOT EXISTS TaskResponses (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        owner_id   BIGINT UNSIGNED DEFAULT NULL,
        parent_id  BIGINT UNSIGNED DEFAULT NULL,
	rating     VARCHAR(8192),
        text       MEDIUMTEXT,
        FOREIGN KEY(owner_id)  REFERENCES Users(id),
        FOREIGN KEY(parent_id) REFERENCES Announcements(id),
        PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
EOF


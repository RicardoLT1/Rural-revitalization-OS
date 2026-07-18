INSERT IGNORE INTO auth_user(id,username,display_name,role_code,village_id,password_hash,enabled) VALUES
(1,'user_demo','小程序用户','USER','1','$2b$10$h1mNJCQKQgwEm/OPogUvSuGcenYKrapWm2V/8Av.G49rqgQF0cQ/S',1),
(2,'staff_demo','业务工作人员','STAFF','1','$2b$10$4PirSOgGPakqJlaMrBjVIuzAWTYKhh/daS7qTzqGphiW86ATkedSm',1),
(3,'admin','系统管理员','ADMIN','1','$2b$10$tGRHcthZBWEDA0StPqjIIeo2DsCyjJzX2Ug1Ld3otIZMS9xIXNsUO',1),
(4,'operator','兼容运营账号','STAFF','1','$2b$10$NE/.qKNHvvcZIegU4TvH7u2EDJ6jmIXLWdZ0REVYMhVllveCaQa5y',1),
(5,'approver','兼容审批账号','STAFF','1','$2b$10$wX7QOZHyktoeaJfpQqHD/Od6PuWGy.5PGYINoE6vbUxRSVUivZKoi',1),
(6,'viewer','兼容查看账号','STAFF','1','$2b$10$3JD6vncKDMq6r3WMxvuneecoZIaYGVP01rt0w3uz5pLTwz7JCPQc.',1),
(7,'disabled','停用账号','USER','1','$2b$10$jdMbTO9sK4hk3/QhrDPqq.RRGWEmEBf/Mzu6ajBSXnHa4JXAw1Iqm',0);

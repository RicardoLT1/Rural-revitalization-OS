INSERT IGNORE INTO resource(id, village_id, name, category, lat, lng, address, area, annual_estimate, investment_status, intro, owner, contact, related_projects, occupancy_rate, expected_roi, status) VALUES
(201, 1, '溪畔共享民宿组团', '乡村民宿', 30.638211, 119.684912, '青耘村溪畔片区 12 号', 860.00, 128.60, '可招商', '临溪院落和旧宅连片改造，适合精品民宿、团建接待和周末微度假运营。', '青耘村运营公司', '0572-8001201', '精品民宿,团建接待,周末微度假', 68, 22, 'active'),
(202, 1, '稻田研学课堂', '研学基地', 30.642016, 119.692145, '青耘村东侧稻田研学区', 5200.00, 86.40, '可招商', '连片水稻田与农事课堂，可承接亲子研学、劳动教育和季节活动。', '青耘村合作社', '0572-8001202', '亲子研学,劳动教育,农事课堂', 54, 18, 'active'),
(203, 1, '老粮仓文创展厅', '文旅空间', 30.640522, 119.681337, '青耘村老粮仓片区', 1180.00, 152.80, '洽谈中', '老粮仓改造空间，适合文创市集、非遗展陈、品牌发布和小型展览。', '青耘村运营公司', '0572-8001203', '文创市集,非遗展陈,品牌发布', 73, 24, 'active'),
(204, 1, '竹林露营营地', '文旅空间', 30.646832, 119.689728, '青耘村北侧竹林入口', 3600.00, 96.80, '可招商', '靠近竹林步道和溪流，适合轻露营、自然教育和周末市集。', '青耘村运营公司', '0572-8001204', '轻露营,自然教育,周末市集', 61, 19, 'active'),
(205, 1, '共富工坊展销点', '非遗工坊', 30.636506, 119.682917, '青耘村游客中心旁', 430.00, 58.20, '已签约', '面向农产品展销和手作体验的小型商业空间，适合共富工坊长期运营。', '青耘村集体经济合作社', '0572-8001205', '农产品展销,手作体验,共富工坊', 79, 15, 'active'),
(206, 1, '山谷共享菜园', '农田', 30.645332, 119.677861, '青耘村西侧山谷', 4800.00, 44.60, '可招商', '适合认养农业、社区团建、季节采摘和农事体验。', '青耘村合作社', '0572-8001206', '认养农业,社区团建,季节采摘', 42, 12, 'active'),
(207, 1, '村口农产品加工间', '农产品加工空间', 30.635921, 119.686512, '青耘村村口共富工坊西侧', 520.00, 67.50, '可招商', '具备初加工和冷链周转条件，可承接竹笋、茶点和农产品轻加工。', '青耘村集体经济合作社', '0572-8001207', '农产品加工,冷链周转,品牌包装', 48, 16, 'active'),
(208, 1, '村集体闲置仓储空间', '村集体资产', 30.637018, 119.679735, '青耘村西入口仓储区', 980.00, 39.80, '已下架', '原村集体仓储空间，后续计划纳入统一改造，暂不对外招商。', '青耘村村委会', '0572-8001208', '仓储改造,集体资产', 20, 8, 'offline');

INSERT IGNORE INTO resource_tag(id, name, sort_no) VALUES
(21, '可招商', 21),
(22, '洽谈中', 22),
(23, '已签约', 23),
(24, '高收益', 24),
(25, '研学友好', 25),
(26, '文旅引流', 26);

INSERT IGNORE INTO resource_tag_rel(resource_id, tag_id) VALUES
(201, 21), (201, 24), (201, 26),
(202, 21), (202, 25),
(203, 22), (203, 24), (203, 26),
(204, 21), (204, 26),
(205, 23), (205, 25),
(206, 21), (206, 25),
(207, 21), (207, 24),
(208, 26);

INSERT IGNORE INTO workflow(id, village_id, title, category, resource_id, status, current_node_id, applicant, applicant_user_id, request_id, applicant_name, approver_id, approver_name, version, created_at) VALUES
(2201, 1, '溪畔共享民宿组团合作申请', 'COOPERATION_APPLICATION', 201, 'PENDING', 'approve', 'user_demo', '1', 'visual-seed-2201', '游客小程序用户', NULL, NULL, 0, '2026-07-03 09:18:00'),
(2202, 1, '稻田研学课堂暑期研学合作申请', 'COOPERATION_APPLICATION', 202, 'PENDING', 'approve', 'user_demo', '1', 'visual-seed-2202', '亲子研学机构', NULL, NULL, 0, '2026-07-03 10:05:00'),
(2203, 1, '竹林露营营地周末市集合作申请', 'COOPERATION_APPLICATION', 204, 'APPROVED', 'archive', 'user_demo', '1', 'visual-seed-2203', '山野生活运营团队', '2', 'staff_demo', 1, '2026-07-02 15:30:00'),
(2204, 1, '老粮仓文创展厅品牌发布合作申请', 'COOPERATION_APPLICATION', 203, 'APPROVED', 'archive', 'user_demo', '1', 'visual-seed-2204', '湖州文创品牌方', '2', 'staff_demo', 1, '2026-07-01 16:20:00'),
(2205, 1, '山谷共享菜园认养活动合作申请', 'COOPERATION_APPLICATION', 206, 'REJECTED', 'archive', 'user_demo', '1', 'visual-seed-2205', '社区农业团队', '2', 'staff_demo', 1, '2026-06-30 11:42:00'),
(2206, 1, '农产品加工间品牌包装合作申请', 'COOPERATION_APPLICATION', 207, 'APPROVED', 'archive', 'user_demo', '1', 'visual-seed-2206', '青耘农品合作方', '2', 'staff_demo', 1, '2026-06-29 14:12:00');

INSERT IGNORE INTO workflow_node(id, workflow_id, node_key, title, status, sort_no, assignee, remark) VALUES
(2301, 2201, 'submit', '提交合作申请', 'done', 1, 'user_demo', '用户提交民宿组团合作意向'),
(2302, 2201, 'approve', '工作人员审批', 'doing', 2, 'staff_demo', '待核对院落档期与改造边界'),
(2303, 2202, 'submit', '提交合作申请', 'done', 1, 'user_demo', '用户提交暑期研学合作意向'),
(2304, 2202, 'approve', '工作人员审批', 'doing', 2, 'staff_demo', '待确认接待容量与安全方案'),
(2305, 2203, 'submit', '提交合作申请', 'done', 1, 'user_demo', '用户提交周末市集合作意向'),
(2306, 2203, 'approve', '工作人员审批', 'done', 2, 'staff_demo', '符合竹林营地当前运营方向'),
(2307, 2204, 'submit', '提交合作申请', 'done', 1, 'user_demo', '用户提交品牌发布活动合作意向'),
(2308, 2204, 'approve', '工作人员审批', 'done', 2, 'staff_demo', '展陈内容与粮仓文创定位匹配'),
(2309, 2205, 'submit', '提交合作申请', 'done', 1, 'user_demo', '用户提交共享菜园认养活动'),
(2310, 2205, 'approve', '工作人员审批', 'done', 2, 'staff_demo', '活动周期与当前排期冲突'),
(2311, 2206, 'submit', '提交合作申请', 'done', 1, 'user_demo', '用户提交农产品包装合作'),
(2312, 2206, 'approve', '工作人员审批', 'done', 2, 'staff_demo', '可与共富工坊展销联动');

INSERT IGNORE INTO todo_item(id, workflow_id, title, category, status, due_date, assignee, assignee_id) VALUES
(2401, 2201, '审批溪畔共享民宿组团合作申请', 'COOPERATION_APPLICATION', 'PENDING', '2026-07-04 18:00:00', 'staff_demo', '2'),
(2402, 2202, '审批稻田研学课堂暑期研学合作申请', 'COOPERATION_APPLICATION', 'PENDING', '2026-07-04 18:00:00', 'staff_demo', '2'),
(2403, 2203, '竹林露营营地周末市集合作申请', 'COOPERATION_APPLICATION', 'APPROVED', '2026-07-03 18:00:00', 'staff_demo', '2'),
(2404, 2204, '老粮仓文创展厅品牌发布合作申请', 'COOPERATION_APPLICATION', 'APPROVED', '2026-07-02 18:00:00', 'staff_demo', '2'),
(2405, 2205, '山谷共享菜园认养活动合作申请', 'COOPERATION_APPLICATION', 'REJECTED', '2026-07-01 18:00:00', 'staff_demo', '2'),
(2406, 2206, '农产品加工间品牌包装合作申请', 'COOPERATION_APPLICATION', 'APPROVED', '2026-06-30 18:00:00', 'staff_demo', '2');

INSERT IGNORE INTO approval_record(id, workflow_id, node_id, title, applicant, amount, action, status, remark, handled_at) VALUES
(2503, 2203, 'approve', '竹林露营营地周末市集合作申请', '2', 0.00, 'APPROVED', 'APPROVED', '活动周期与营地运营计划匹配，同意进入对接。', '2026-07-03 09:35:00'),
(2504, 2204, 'approve', '老粮仓文创展厅品牌发布合作申请', '2', 0.00, 'APPROVED', 'APPROVED', '展陈内容清晰，预计带动文创消费和游客停留。', '2026-07-02 10:12:00'),
(2505, 2205, 'approve', '山谷共享菜园认养活动合作申请', '2', 0.00, 'REJECTED', 'REJECTED', '当前档期与已有采摘活动冲突，建议调整时间后再提交。', '2026-07-01 15:48:00'),
(2506, 2206, 'approve', '农产品加工间品牌包装合作申请', '2', 0.00, 'APPROVED', 'APPROVED', '可与农产品展销、共富工坊形成联动，同意推进。', '2026-06-30 10:28:00');

INSERT IGNORE INTO operation_log(id, workflow_id, resource_id, action, operator_id, operator_name, remark, created_at) VALUES
(2601, 2201, 201, 'SUBMIT_APPLICATION', '1', '游客小程序用户', '提交合作申请', '2026-07-03 09:18:00'),
(2602, 2202, 202, 'SUBMIT_APPLICATION', '1', '亲子研学机构', '提交合作申请', '2026-07-03 10:05:00'),
(2603, 2203, 204, 'APPROVE_WORKFLOW', '2', 'staff_demo', '审批通过', '2026-07-03 09:35:00'),
(2604, 2204, 203, 'APPROVE_WORKFLOW', '2', 'staff_demo', '审批通过', '2026-07-02 10:12:00'),
(2605, 2205, 206, 'APPROVE_WORKFLOW', '2', 'staff_demo', '审批驳回', '2026-07-01 15:48:00'),
(2606, 2206, 207, 'APPROVE_WORKFLOW', '2', 'staff_demo', '审批通过', '2026-06-30 10:28:00');

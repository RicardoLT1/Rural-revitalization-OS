UPDATE village
SET name='青耘村',
    region='浙江湖州安吉',
    address='青耘村乡村运营示范片区'
WHERE id=1;

UPDATE resource
SET name='溪畔共创民宿院',
    category='闲置农房',
    address='青耘村溪畔组 2 号',
    investment_status='可招商',
    intro='临溪院落，适合民宿和团建接待。',
    owner='青耘村运营公司',
    related_projects='民宿改造,青年主理人计划'
WHERE id=101;

UPDATE resource
SET name='稻田研学基地',
    category='土地',
    address='青耘村东侧稻田片区',
    investment_status='可招商',
    intro='连片稻田景观，适合研学和亲子活动。',
    owner='青耘村合作社',
    related_projects='农事课堂,稻田音乐会'
WHERE id=102;

UPDATE resource
SET name='老粮仓文创空间',
    category='文旅空间',
    address='青耘村老粮仓片区',
    investment_status='可招商',
    intro='老粮仓改造空间，适合集市、展陈和发布会。',
    owner='青耘村运营公司',
    related_projects='乡创市集,非遗工坊'
WHERE id=103;

INSERT IGNORE INTO resource(id, village_id, name, category, lat, lng, address, area, annual_estimate, investment_status, intro, owner, contact, related_projects, occupancy_rate, expected_roi, status) VALUES
(104, 1, '竹林露营营地', '文旅空间', 30.642815, 119.690228, '青耘村北侧竹林入口', 3200, 96.8, '洽谈中', '靠近竹林步道，可承接轻露营、自然教育和周末市集。', '青耘村运营公司', '0572-8001004', '竹林徒步,自然教育', 61, 19, 'active'),
(105, 1, '共富工坊展销点', '闲置农房', 30.636506, 119.682917, '青耘村游客中心旁', 430, 58.2, '已签约', '面向农产品展销和手作体验的小型商业空间。', '青耘村集体经济合作社', '0572-8001005', '农产品展销,手作体验', 74, 15, 'active'),
(106, 1, '山谷共享菜园', '土地', 30.645332, 119.677861, '青耘村西侧山谷', 4800, 44.6, '可招商', '适合认养农业、社区团建和季节性采摘活动。', '青耘村合作社', '0572-8001006', '认养农业,社区团建', 38, 12, 'active');

INSERT IGNORE INTO resource_tag(id, name, sort_no) VALUES
(6, '洽谈中', 5),
(7, '已签约', 6);

INSERT IGNORE INTO resource_tag_rel(resource_id, tag_id) VALUES
(104, 4), (104, 6),
(105, 2), (105, 7),
(106, 3), (106, 5);

INSERT IGNORE INTO workflow(id, village_id, title, category, resource_id, status, current_node_id, applicant, applicant_user_id, request_id, applicant_name, approver_id, approver_name, version, created_at) VALUES
(1201, 1, '溪畔共创民宿院合作申请', 'COOPERATION_APPLICATION', 101, 'PENDING', 'approve', 'user_demo', '1', 'seed-application-1201', '小程序用户', NULL, NULL, 0, '2026-07-01 09:10:00'),
(1202, 1, '山谷共享菜园认养合作申请', 'COOPERATION_APPLICATION', 106, 'APPROVED', 'archive', 'user_demo', '1', 'seed-application-1202', '小程序用户', '2', 'staff_demo', 1, '2026-06-30 14:25:00'),
(1203, 1, '竹林露营营地活动合作申请', 'COOPERATION_APPLICATION', 104, 'REJECTED', 'archive', 'user_demo', '1', 'seed-application-1203', '小程序用户', '2', 'staff_demo', 1, '2026-06-29 16:40:00');

INSERT IGNORE INTO workflow_node(id, workflow_id, node_key, title, status, sort_no, assignee, remark) VALUES
(1301, 1201, 'submit', '提交申请', 'done', 1, 'user_demo', '小程序端提交合作意向'),
(1302, 1201, 'approve', '工作人员审批', 'doing', 2, 'staff_demo', '等待工作人员确认资源排期'),
(1303, 1202, 'submit', '提交申请', 'done', 1, 'user_demo', '认养农业合作意向'),
(1304, 1202, 'approve', '工作人员审批', 'done', 2, 'staff_demo', '符合当前资源运营方向'),
(1305, 1203, 'submit', '提交申请', 'done', 1, 'user_demo', '活动档期合作意向'),
(1306, 1203, 'approve', '工作人员审批', 'done', 2, 'staff_demo', '当前资源仍在洽谈期');

INSERT IGNORE INTO todo_item(id, workflow_id, title, category, status, due_date, assignee, assignee_id) VALUES
(1401, 1201, '审批溪畔共创民宿院合作申请', 'COOPERATION_APPLICATION', 'PENDING', '2026-07-03 18:00:00', 'staff_demo', '2'),
(1402, 1202, '山谷共享菜园认养合作申请', 'COOPERATION_APPLICATION', 'APPROVED', '2026-07-01 18:00:00', 'staff_demo', '2'),
(1403, 1203, '竹林露营营地活动合作申请', 'COOPERATION_APPLICATION', 'REJECTED', '2026-06-30 18:00:00', 'staff_demo', '2');

INSERT IGNORE INTO approval_record(id, workflow_id, node_id, title, applicant, amount, action, status, remark, handled_at) VALUES
(1502, 1202, 'approve', '山谷共享菜园认养合作申请', '2', 0.00, 'APPROVED', 'APPROVED', '合作方向明确，同意进入对接。', '2026-07-01 10:20:00'),
(1503, 1203, 'approve', '竹林露营营地活动合作申请', '2', 0.00, 'REJECTED', 'REJECTED', '资源仍在洽谈期，暂缓新增合作。', '2026-06-30 11:35:00');

INSERT IGNORE INTO operation_log(id, workflow_id, resource_id, action, operator_id, operator_name, remark, created_at) VALUES
(1601, 1201, 101, 'SUBMIT_APPLICATION', '1', '小程序用户', '提交合作申请', '2026-07-01 09:10:00'),
(1602, 1202, 106, 'APPROVE_WORKFLOW', '2', 'staff_demo', '审批通过', '2026-07-01 10:20:00'),
(1603, 1203, 104, 'APPROVE_WORKFLOW', '2', 'staff_demo', '审批驳回', '2026-06-30 11:35:00');

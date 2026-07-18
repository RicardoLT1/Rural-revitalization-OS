INSERT IGNORE INTO village(id, name, region, address, status) VALUES
(1, '青耘村', '浙江湖州安吉', '青耘村乡村运营示范片区', 'active');

INSERT IGNORE INTO resource(id, village_id, name, category, lat, lng, address, area, annual_estimate, investment_status, intro, owner, contact, related_projects, occupancy_rate, expected_roi, status) VALUES
(101, 1, '溪畔共创民宿院', '闲置农房', 30.638211, 119.684912, '青耘村溪畔组12号', 680, 86.5, '可招商', '临溪院落，适合民宿和团建接待。', '青耘村运营公司', '0572-8001001', '民宿改造,青年主理人计划', 42, 18, 'active'),
(102, 1, '稻田研学基地', '土地', 30.641122, 119.681533, '青耘村东侧稻田片区', 5600, 72.3, '可招商', '连片稻田景观，适合研学和亲子活动。', '青耘村合作社', '0572-8001002', '农事课堂,稻田音乐会', 55, 16, 'active'),
(103, 1, '老粮仓文创空间', '文旅空间', 30.637821, 119.688016, '青耘村老粮仓片区', 920, 104.8, '可招商', '老粮仓改造空间，适合集市、展陈和发布会。', '青耘村运营公司', '0572-8001003', '乡创市集,非遗工坊', 68, 22, 'active');

INSERT IGNORE INTO resource_tag(id, name, sort_no) VALUES
(1, '全部', 0), (2, '闲置农房', 1), (3, '土地', 2), (4, '文旅空间', 3), (5, '可招商', 4);

INSERT IGNORE INTO resource_tag_rel(resource_id, tag_id) VALUES
(101, 2), (101, 5), (102, 3), (102, 5), (103, 4), (103, 5);

INSERT IGNORE INTO workflow(id, village_id, title, category, status, current_node_id, applicant) VALUES
(201, 1, '老粮仓文创空间招商立项', '项目申报', '进行中', 'review', '2'),
(202, 1, '溪畔民宿院资产流转', '资产流转', '待审批', 'approve', '2');

INSERT IGNORE INTO workflow_node(id, workflow_id, node_key, title, status, sort_no, assignee, remark) VALUES
(301, 201, 'submit', '材料提交', 'done', 1, 'operator', '材料已提交'),
(302, 201, 'review', '部门复核', 'doing', 2, 'approver', '等待预算附件'),
(303, 201, 'archive', '归档', 'pending', 3, 'admin', NULL),
(304, 202, 'submit', '流转申请', 'done', 1, 'operator', NULL),
(305, 202, 'approve', '合同审批', 'doing', 2, 'approver', NULL);

INSERT IGNORE INTO todo_item(id, workflow_id, title, category, status, due_date, assignee) VALUES
(401, 201, '补充老粮仓消防改造预算', '项目申报', '待处理', '2026-07-01 18:00:00', 'operator'),
(402, 202, '确认溪畔民宿院流转合同', '资产流转', '进行中', '2026-07-02 12:00:00', 'approver'),
(403, 201, '整理招商合作初稿', '项目申报', '已逾期', '2026-06-25 18:00:00', 'operator');

INSERT IGNORE INTO approval_record(id, workflow_id, node_id, title, applicant, amount, action, status, remark, handled_at) VALUES
(501, 201, 'review', '老粮仓文创空间招商立项', 'operator', 18.00, 'supplement', '待补充材料', '请补充消防预算。', '2026-06-26 09:20:00'),
(502, 202, 'approve', '溪畔民宿院资产流转', 'operator', 32.00, 'pending', '待审批', '合同条款待审批。', '2026-06-26 10:15:00');

INSERT IGNORE INTO report_snapshot(id, village_id, stat_date, visitor_count, revenue, project_progress, risk_count, investment_conversion_rate, culture_revenue, product_revenue, service_revenue) VALUES
(701, 1, '2026-06-20', 1180, 12.60, 62.00, 2, 21.50, 6.60, 3.50, 2.50),
(702, 1, '2026-06-21', 1260, 13.20, 63.00, 2, 22.10, 6.90, 3.70, 2.60),
(703, 1, '2026-06-22', 1390, 15.10, 65.00, 1, 22.80, 7.90, 4.20, 3.00),
(704, 1, '2026-06-23', 1510, 16.40, 67.00, 1, 23.60, 8.40, 4.80, 3.20),
(705, 1, '2026-06-24', 1680, 18.20, 69.00, 2, 24.20, 9.60, 5.10, 3.50),
(706, 1, '2026-06-25', 2120, 24.30, 72.00, 3, 25.10, 13.10, 6.80, 4.40),
(707, 1, '2026-06-26', 2360, 26.80, 74.00, 3, 26.30, 14.20, 7.60, 5.00);

INSERT IGNORE INTO forecast_result(id, village_id, forecast_date, actual_value, predict_value, upper_value, lower_value, risk_level, strategy) VALUES
(801, 1, '2026-06-27', NULL, 2500, 2850, 2200, 'medium', '提前准备周末接待和停车分流。'),
(802, 1, '2026-06-28', NULL, 3020, 3360, 2700, 'high', '开启志愿者和安全巡检机制。');

INSERT IGNORE INTO investment_match_record(id, resource_id, investor, score, reason, priority, direction, status) VALUES
(901, 103, '湖州山野文创运营有限公司', 94, '具备文创集市和展陈运营经验。', '高优先', '文创市集运营', 'active'),
(902, 101, '青旅乡宿联合体', 91, '具备民宿主理人资源。', '高优先', '精品民宿运营', 'active');

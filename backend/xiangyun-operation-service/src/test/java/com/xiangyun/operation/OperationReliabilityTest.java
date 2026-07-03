package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(OperationReliabilityTest.Config.class)
class OperationReliabilityTest {

    private final OperationService operationService;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    OperationReliabilityTest(OperationService operationService,
                             JdbcTemplate jdbcTemplate,
                             PlatformTransactionManager transactionManager) {
        this.operationService = operationService;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    void resetSchema() {
        jdbcTemplate.execute("drop all objects");
        jdbcTemplate.execute("""
                create table resource(
                  id bigint primary key,
                  village_id bigint,
                  name varchar(160),
                  category varchar(64),
                  lat decimal(10,6),
                  lng decimal(10,6),
                  address varchar(255),
                  area decimal(12,2),
                  annual_estimate decimal(12,2),
                  investment_status varchar(64),
                  intro varchar(255),
                  owner varchar(128),
                  contact varchar(64),
                  related_projects varchar(255),
                  occupancy_rate int default 0,
                  expected_roi int default 0,
                  deleted tinyint default 0
                )
                """);
        jdbcTemplate.execute("""
                create table resource_tag(
                  id bigint primary key,
                  name varchar(64),
                  sort_no int default 0,
                  deleted tinyint default 0
                )
                """);
        jdbcTemplate.execute("""
                create table resource_tag_rel(
                  resource_id bigint,
                  tag_id bigint
                )
                """);
        jdbcTemplate.execute("""
                create table workflow(
                  id bigint primary key,
                  village_id bigint not null,
                  title varchar(160) not null,
                  category varchar(64) not null,
                  resource_id bigint,
                  status varchar(64) not null,
                  current_node_id varchar(64),
                  applicant varchar(64),
                  applicant_user_id varchar(64),
                  request_id varchar(64) unique,
                  applicant_name varchar(128),
                  approver_id varchar(64),
                  approver_name varchar(128),
                  version int not null default 0,
                  created_at timestamp default current_timestamp,
                  updated_at timestamp default current_timestamp,
                  deleted tinyint default 0
                )
                """);
        jdbcTemplate.execute("""
                create table todo_item(
                  id bigint primary key,
                  workflow_id bigint,
                  title varchar(160),
                  category varchar(64),
                  status varchar(64),
                  due_date timestamp,
                  assignee varchar(64),
                  assignee_id varchar(64),
                  deleted tinyint default 0
                )
                """);
        jdbcTemplate.execute("""
                create table approval_record(
                  id bigint primary key,
                  workflow_id bigint,
                  node_id varchar(64),
                  title varchar(160),
                  applicant varchar(64),
                  action varchar(64),
                  status varchar(64),
                  remark varchar(255),
                  handled_at timestamp,
                  deleted tinyint default 0
                )
                """);
        jdbcTemplate.execute("""
                create table operation_log(
                  id bigint primary key,
                  workflow_id bigint,
                  resource_id bigint,
                  action varchar(64) not null,
                  operator_id varchar(64),
                  operator_name varchar(128),
                  remark varchar(255),
                  created_at timestamp default current_timestamp,
                  deleted tinyint default 0
                )
                """);
        jdbcTemplate.update("""
                insert into resource(id, village_id, name, category, address, area, annual_estimate, investment_status, intro, owner, contact)
                values(101, 1, '溪畔共创民宿院', '闲置农房', '青耘村', 680, 86.5, '可招商', '临溪院落', '青耘村运营公司', '0572')
                """);
    }

    @Test
    void concurrentApprovalOnlyAllowsOneSuccess() throws Exception {
        seedPendingWorkflow(201L);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Object>> futures = new ArrayList<>();
        for (String approverId : List.of("staff-a", "staff-b")) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    return operationService.approve("201", "approve", approverId, approverId, Map.of("remark", "agree"));
                } catch (BusinessException ex) {
                    return ex;
                }
            }));
        }

        ready.await();
        start.countDown();
        List<Object> results = List.of(futures.get(0).get(), futures.get(1).get());
        executor.shutdownNow();

        long successCount = results.stream().filter(Map.class::isInstance).count();
        long conflictCount = results.stream()
                .filter(BusinessException.class::isInstance)
                .map(BusinessException.class::cast)
                .filter(ex -> ex.getCode() == 40901 || ex.getCode() == 40902)
                .count();
        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(1);
        assertThat(count("approval_record")).isEqualTo(1);
        assertThat(count("operation_log")).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select status from workflow where id=201", String.class)).isEqualTo("APPROVED");
        assertThat(jdbcTemplate.queryForObject("select version from workflow where id=201", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select status from todo_item where workflow_id=201", String.class)).isEqualTo("APPROVED");
    }

    @Test
    void submitApplicationRollsBackWhenTodoInsertFails() {
        jdbcTemplate.execute("alter table todo_item alter column title varchar(1)");

        assertThatThrownBy(() -> operationService.submitCooperationApplication(
                "1",
                "user_demo",
                "rollback-submit",
                Map.of("resourceId", "101", "title", "合作申请回滚测试")))
                .isInstanceOf(RuntimeException.class);

        assertThat(count("workflow")).isZero();
        assertThat(count("todo_item")).isZero();
        assertThat(count("approval_record")).isZero();
        assertThat(count("operation_log")).isZero();
    }

    @Test
    void approveRollsBackWhenOperationLogInsertFails() {
        seedPendingWorkflow(202L);
        jdbcTemplate.execute("alter table operation_log alter column remark varchar(1)");

        assertThatThrownBy(() -> operationService.approve(
                "202",
                "approve",
                "staff_demo",
                "staff_demo",
                Map.of("remark", "同意合作申请")))
                .isInstanceOf(RuntimeException.class);

        assertThat(count("approval_record")).isZero();
        assertThat(count("operation_log")).isZero();
        assertThat(jdbcTemplate.queryForObject("select status from workflow where id=202", String.class)).isEqualTo("PENDING");
        assertThat(jdbcTemplate.queryForObject("select version from workflow where id=202", Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("select status from todo_item where workflow_id=202", String.class)).isEqualTo("PENDING");
    }

    private void seedPendingWorkflow(long workflowId) {
        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    insert into workflow(id, village_id, title, category, resource_id, status, current_node_id, applicant, applicant_user_id, request_id, applicant_name, version)
                    values(?, 1, '合作申请', 'COOPERATION_APPLICATION', 101, 'PENDING', 'approve', '1', '1', ?, '小程序用户', 0)
                    """, workflowId, "seed-" + workflowId);
            jdbcTemplate.update("""
                    insert into todo_item(id, workflow_id, title, category, status, due_date, assignee, assignee_id)
                    values(?, ?, '合作申请', 'COOPERATION_APPLICATION', 'PENDING', current_timestamp, 'staff_demo', '2')
                    """, workflowId + 1000, workflowId);
        });
    }

    private int count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean
        DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:operation_reliability;MODE=MySQL;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource);
        }

        @Bean
        OperationService operationService(JdbcTemplate jdbcTemplate,
                                          StringRedisTemplate redisTemplate,
                                          AuthClient authClient) {
            return new OperationService(jdbcTemplate, redisTemplate, authClient, new ObjectMapper(), 600);
        }

        @Bean
        StringRedisTemplate redisTemplate() {
            StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
            ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            return redisTemplate;
        }

        @Bean
        AuthClient authClient() {
            return mock(AuthClient.class);
        }
    }
}

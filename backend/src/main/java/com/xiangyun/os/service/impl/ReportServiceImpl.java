package com.xiangyun.os.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyun.os.entity.ReportSnapshot;
import com.xiangyun.os.mapper.ReportSnapshotMapper;
import com.xiangyun.os.service.ReportService;
import com.xiangyun.os.vo.CommonVO;
import com.xiangyun.os.vo.ReportDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final ReportSnapshotMapper reportSnapshotMapper;

    @Override
    public ReportDashboardVO getReportDashboard(String period) {
        String activePeriod = "30d".equals(period) ? "30d" : "7d";
        int size = "30d".equals(activePeriod) ? 30 : 7;
        List<ReportSnapshot> snapshots = reportSnapshotMapper.selectList(new LambdaQueryWrapper<ReportSnapshot>()
                .eq(ReportSnapshot::getDeleted, 0)
                .orderByAsc(ReportSnapshot::getStatDate));
        List<ReportSnapshot> scoped = tail(snapshots, size);
        ReportSnapshot latest = scoped.isEmpty() ? null : scoped.get(scoped.size() - 1);

        return new ReportDashboardVO(
                buildSummary(scoped, latest),
                List.of(new CommonVO.OptionItem("7d", "近7天"), new CommonVO.OptionItem("30d", "近30天")),
                activePeriod,
                buildFlowPoints(scoped),
                buildRevenueBar(scoped),
                buildRatioRing(scoped),
                buildAutoSummary(scoped, latest),
                buildTips()
        );
    }

    private List<ReportDashboardVO.ReportSummary> buildSummary(List<ReportSnapshot> snapshots, ReportSnapshot latest) {
        int visitors = snapshots.stream().mapToInt(ReportSnapshot::getVisitorCount).sum();
        BigDecimal revenue = snapshots.stream().map(ReportSnapshot::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal conversion = latest == null ? BigDecimal.ZERO : latest.getInvestmentConversionRate();
        return List.of(
                new ReportDashboardVO.ReportSummary("visitor", "本月累计客流", String.valueOf(visitors), "+8.6%"),
                new ReportDashboardVO.ReportSummary("revenue", "本月累计营收", revenue.setScale(1, RoundingMode.HALF_UP) + "万元", "+12.3%"),
                new ReportDashboardVO.ReportSummary("conversion", "招商转化率", conversion.setScale(1, RoundingMode.HALF_UP) + "%", "+3.1%")
        );
    }

    private List<CommonVO.TrendPoint> buildFlowPoints(List<ReportSnapshot> snapshots) {
        return snapshots.stream()
                .sorted(Comparator.comparing(ReportSnapshot::getStatDate))
                .map(item -> new CommonVO.TrendPoint(item.getStatDate().format(DAY_FORMATTER), item.getVisitorCount()))
                .toList();
    }

    private ReportDashboardVO.RevenueBar buildRevenueBar(List<ReportSnapshot> snapshots) {
        List<String> labels = snapshots.stream().map(item -> item.getStatDate().format(DAY_FORMATTER)).toList();
        List<BigDecimal> values = snapshots.stream().map(ReportSnapshot::getRevenue).toList();
        return new ReportDashboardVO.RevenueBar(labels, List.of(new CommonVO.ChartSeries("营收", values)));
    }

    private ReportDashboardVO.RevenueRatio buildRatioRing(List<ReportSnapshot> snapshots) {
        BigDecimal culture = snapshots.stream().map(ReportSnapshot::getCultureRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal product = snapshots.stream().map(ReportSnapshot::getProductRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal service = snapshots.stream().map(ReportSnapshot::getServiceRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = culture.add(product).add(service);
        List<BigDecimal> ratios = total.compareTo(BigDecimal.ZERO) == 0
                ? List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
                : List.of(percent(culture, total), percent(product, total), percent(service, total));
        return new ReportDashboardVO.RevenueRatio(
                List.of("文旅收入", "农产品销售", "活动服务"),
                ratios,
                List.of("#2F7D32", "#6FAF5E", "#D58A2A")
        );
    }

    private String buildAutoSummary(List<ReportSnapshot> snapshots, ReportSnapshot latest) {
        BigDecimal revenue = snapshots.stream().map(ReportSnapshot::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        int visitors = snapshots.stream().mapToInt(ReportSnapshot::getVisitorCount).sum();
        int risk = latest == null ? 0 : latest.getRiskCount();
        return "系统自动汇总：当前周期累计客流 " + visitors + " 人次，营收 " + revenue.setScale(1, RoundingMode.HALF_UP)
                + " 万元，风险事项 " + risk + " 项，建议继续强化周末活动承接与重点资源招商。";
    }

    private List<CommonVO.AiSuggestion> buildTips() {
        return List.of(
                new CommonVO.AiSuggestion("report-ai-1", "提升文旅转化", "文旅收入占比最高，可将热门资源与活动套餐联动招商。", "P1", "查看招商推荐", "match", "运营建议"),
                new CommonVO.AiSuggestion("report-ai-2", "关注客流峰值", "周末客流上行明显，建议提前准备分流与志愿者排班。", "P2", "查看趋势预测", "forecast", "风险规避")
        );
    }

    private BigDecimal percent(BigDecimal value, BigDecimal total) {
        return value.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    }

    private List<ReportSnapshot> tail(List<ReportSnapshot> snapshots, int size) {
        if (snapshots.size() <= size) {
            return snapshots;
        }
        return snapshots.subList(snapshots.size() - size, snapshots.size());
    }
}

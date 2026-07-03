package com.xiangyun.os.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiangyun.os.entity.ReportSnapshot;
import com.xiangyun.os.entity.Village;
import com.xiangyun.os.mapper.ReportSnapshotMapper;
import com.xiangyun.os.mapper.VillageMapper;
import com.xiangyun.os.service.DashboardService;
import com.xiangyun.os.vo.CommonVO;
import com.xiangyun.os.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final VillageMapper villageMapper;
    private final ReportSnapshotMapper reportSnapshotMapper;

    @Override
    public DashboardVO getDashboard() {
        Village village = villageMapper.selectList(new LambdaQueryWrapper<Village>()
                        .eq(Village::getDeleted, 0)
                        .orderByAsc(Village::getId))
                .stream()
                .findFirst()
                .orElse(null);
        List<ReportSnapshot> snapshots = reportSnapshotMapper.selectList(new LambdaQueryWrapper<ReportSnapshot>()
                .eq(ReportSnapshot::getDeleted, 0)
                .orderByAsc(ReportSnapshot::getStatDate));
        ReportSnapshot latest = snapshots.isEmpty() ? null : snapshots.get(snapshots.size() - 1);

        DashboardVO.TrendSeries trends = new DashboardVO.TrendSeries(toTrend(tail(snapshots, 7)), toTrend(tail(snapshots, 30)));
        return new DashboardVO(
                village == null ? "青禾村" : village.getName(),
                "乡村CEO",
                buildStats(latest, snapshots),
                trends,
                buildRisks(latest),
                buildSuggestions(latest)
        );
    }

    private List<DashboardVO.StatItem> buildStats(ReportSnapshot latest, List<ReportSnapshot> snapshots) {
        BigDecimal monthRevenue = snapshots.stream()
                .map(ReportSnapshot::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int visitorCount = latest == null ? 0 : latest.getVisitorCount();
        BigDecimal progress = latest == null ? BigDecimal.ZERO : latest.getProjectProgress();
        int riskCount = latest == null ? 0 : latest.getRiskCount();
        List<DashboardVO.StatItem> stats = new ArrayList<>();
        stats.add(new DashboardVO.StatItem("flow", "今日客流", visitorCount, "人次", 8, "up", "success", "leaf"));
        stats.add(new DashboardVO.StatItem("revenue", "本月营收", monthRevenue.setScale(1, RoundingMode.HALF_UP), "万元", 12, "up", "success", "wallet"));
        stats.add(new DashboardVO.StatItem("progress", "项目进度", progress, "%", 5, "up", "info", "flag"));
        stats.add(new DashboardVO.StatItem("risk", "风险预警", riskCount, "项", riskCount > 2 ? 2 : 0, riskCount > 2 ? "up" : "flat", riskCount > 2 ? "warning" : "success", "alert"));
        return stats;
    }

    private List<CommonVO.TrendPoint> toTrend(List<ReportSnapshot> snapshots) {
        return snapshots.stream()
                .sorted(Comparator.comparing(ReportSnapshot::getStatDate))
                .map(item -> new CommonVO.TrendPoint(item.getStatDate().format(DAY_FORMATTER), item.getVisitorCount()))
                .toList();
    }

    private List<ReportSnapshot> tail(List<ReportSnapshot> snapshots, int size) {
        if (snapshots.size() <= size) {
            return snapshots;
        }
        return snapshots.subList(snapshots.size() - size, snapshots.size());
    }

    private List<CommonVO.RiskAlert> buildRisks(ReportSnapshot latest) {
        int riskCount = latest == null ? 0 : latest.getRiskCount();
        if (riskCount <= 0) {
            return Collections.singletonList(new CommonVO.RiskAlert("risk-0", "当前暂无高风险事项", "low", "核心运营指标保持稳定。", "运营组"));
        }
        return List.of(
                new CommonVO.RiskAlert("risk-1", "周末客流承载压力上升", "medium", "预测客流高于工作日均值，建议补充志愿者与接驳指引。", "运营组"),
                new CommonVO.RiskAlert("risk-2", "活动审批节点临近超时", "high", "有活动筹备流程仍停留在材料确认节点。", "协同组")
        );
    }

    private List<CommonVO.AiSuggestion> buildSuggestions(ReportSnapshot latest) {
        String conversion = latest == null ? "0" : latest.getInvestmentConversionRate().setScale(1, RoundingMode.HALF_UP).toPlainString();
        return List.of(
                new CommonVO.AiSuggestion("ai-1", "优先盘活高潜资源", "建议优先推进文旅空间招商，当前招商转化率约 " + conversion + "%。", "P1", "查看招商推荐", "match", "招商匹配"),
                new CommonVO.AiSuggestion("ai-2", "补强周末运营排班", "未来 7 天客流预计持续上行，建议提前安排摊位、停车与安全巡检。", "P2", "查看趋势预测", "forecast", "趋势预测")
        );
    }
}

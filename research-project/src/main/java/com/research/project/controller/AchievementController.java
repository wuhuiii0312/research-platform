package com.research.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.common.core.util.SecurityUtils;
import com.research.project.entity.ProjectMember;
import com.research.project.mapper.ProjectMemberMapper;
import com.research.project.mapper.ProjectResultMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

@RestController
@RequestMapping("/achievement")
@Api(tags = "成果管理-统计")
public class AchievementController {

    @Autowired
    private ProjectResultMapper projectResultMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @GetMapping("/statistic")
    @ApiOperation("成果统计")
    public CommonResult<?> getAchievementStatistic(@RequestParam(required = false) Long projectId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "请先登录");
        }

        // 查询当前用户参与的项目（有效成员 status=1）
        java.util.List<ProjectMember> members = projectMemberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getUserId, userId)
                        .eq(ProjectMember::getStatus, 1)
        );
        java.util.Set<Long> joinedProjectIds = new java.util.HashSet<>();
        if (members != null) {
            for (ProjectMember m : members) {
                if (m.getProjectId() != null) {
                    joinedProjectIds.add(m.getProjectId());
                }
            }
        }
        // 没有参与任何项目，直接返回全 0
        if (joinedProjectIds.isEmpty()) {
            return CommonResult.success(emptyStat());
        }

        // 若传了 projectId，但不是本人参与的项目，则返回 0
        if (projectId != null && !joinedProjectIds.contains(projectId)) {
            return CommonResult.success(emptyStat());
        }

        java.util.Map<String, Integer> total = initStat();
        if (projectId != null) {
            mergeStat(total, projectResultMapper.statisticResult(projectId));
        } else {
            for (Long pid : joinedProjectIds) {
                mergeStat(total, projectResultMapper.statisticResult(pid));
            }
        }
        return CommonResult.success(total);
    }

    @GetMapping("/export")
    @ApiOperation("导出成果统计Excel")
    public void export(@RequestParam(required = false) Long projectId, HttpServletResponse response) throws Exception {
        CommonResult<?> statResult = getAchievementStatistic(projectId);
        @SuppressWarnings("unchecked")
        Map<String, Integer> statMap = (Map<String, Integer>) statResult.getData();
        if (statMap == null) statMap = java.util.Collections.emptyMap();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成果统计");
        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("成果类型");
        row0.createCell(1).setCellValue("数量");
        int rowNum = 1;
        rowNum = addRow(sheet, rowNum, "总成果数", statMap.getOrDefault("total", 0));
        rowNum = addRow(sheet, rowNum, "论文", statMap.getOrDefault("paperCount", 0));
        rowNum = addRow(sheet, rowNum, "专利", statMap.getOrDefault("patentCount", 0));
        rowNum = addRow(sheet, rowNum, "软件著作权", statMap.getOrDefault("softCount", 0));
        addRow(sheet, rowNum, "报告", statMap.getOrDefault("reportCount", 0));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=achievement-statistic.xlsx");
        OutputStream os = response.getOutputStream();
        workbook.write(os);
        os.flush();
        workbook.close();
    }

    private static int addRow(Sheet sheet, int rowNum, String label, int value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        return rowNum + 1;
    }

    private java.util.Map<String, Integer> initStat() {
        java.util.Map<String, Integer> m = new java.util.HashMap<>();
        m.put("total", 0);
        m.put("paperCount", 0);
        m.put("patentCount", 0);
        m.put("softCount", 0);
        m.put("reportCount", 0);
        return m;
    }

    private java.util.Map<String, Integer> emptyStat() {
        return initStat();
    }

    private void mergeStat(java.util.Map<String, Integer> target, java.util.Map<String, ?> src) {
        if (src == null) return;
        addField(target, "total", src.get("total"));
        addField(target, "paperCount", src.get("paperCount"));
        addField(target, "patentCount", src.get("patentCount"));
        addField(target, "softCount", src.get("softCount"));
        addField(target, "reportCount", src.get("reportCount"));
    }

    private void addField(java.util.Map<String, Integer> map, String key, Object v) {
        if (v == null) return;
        int cur = map.getOrDefault(key, 0);
        int inc = 0;
        if (v instanceof Number) {
            inc = ((Number) v).intValue();
        } else {
            try {
                inc = Integer.parseInt(v.toString());
            } catch (NumberFormatException ignored) {
                inc = 0;
            }
        }
        map.put(key, cur + inc);
    }
}

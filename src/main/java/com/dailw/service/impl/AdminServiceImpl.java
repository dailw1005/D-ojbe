package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.common.ErrorCode;
import com.dailw.constant.CommonConstant;
import com.dailw.constant.UserConstant;
import com.dailw.exception.BusinessException;
import com.dailw.mapper.QuestionSubmitMapper;
import com.dailw.mapper.UserMapper;
import com.dailw.model.dto.admin.AdminUserQueryRequest;
import com.dailw.model.dto.admin.AdminUserUpdateRoleRequest;
import com.dailw.model.dto.admin.AdminUserUpdateStatusRequest;
import com.dailw.model.entity.User;
import com.dailw.model.enums.QuestionSubmitStatusEnum;
import com.dailw.model.vo.UserVO;
import com.dailw.model.vo.admin.DashboardVO;
import com.dailw.service.interfaces.AdminService;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.service.interfaces.QuestionSolutionService;
import com.dailw.service.interfaces.QuestionSubmitService;
import com.dailw.service.interfaces.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionSolutionService questionSolutionService;

    private static final Set<String> VALID_ROLES = Set.of(
            UserConstant.ADMIN_ROLE, UserConstant.DEFAULT_ROLE, UserConstant.BAN_ROLE);

    @Override
    public Page<UserVO> queryUserPage(AdminUserQueryRequest request) {
        long current = request.getCurrent();
        long pageSize = request.getPageSize();

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();

        String username = request.getUsername();
        String role = request.getRole();
        Integer status = request.getStatus();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();

        queryWrapper.like(StringUtils.isNotBlank(username), User::getUsername, username);
        queryWrapper.eq(StringUtils.isNotBlank(role), User::getRole, role);
        queryWrapper.eq(status != null, User::getStatus, status);
        queryWrapper.ge(startTime != null, User::getCreateTime, startTime);
        queryWrapper.le(endTime != null, User::getCreateTime, endTime);

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            switch (sortField) {
                case "id":
                    queryWrapper.orderBy(true, isAsc, User::getId);
                    break;
                case "username":
                    queryWrapper.orderBy(true, isAsc, User::getUsername);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, User::getCreateTime);
                    break;
                case "lastLoginTime":
                    queryWrapper.orderBy(true, isAsc, User::getLastLoginTime);
                    break;
                case "loginCount":
                    queryWrapper.orderBy(true, isAsc, User::getLoginCount);
                    break;
                default:
                    queryWrapper.orderByDesc(User::getId);
            }
        } else {
            queryWrapper.orderByDesc(User::getId);
        }

        Page<User> page = userService.page(new Page<>(current, pageSize), queryWrapper);
        List<UserVO> records = page.getRecords().stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());

        Page<UserVO> result = new Page<>(current, pageSize, page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public Boolean updateUserRole(Long adminUserId, AdminUserUpdateRoleRequest request) {
        if (request.getUserId() == null || StringUtils.isBlank(request.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!VALID_ROLES.contains(request.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的角色");
        }
        if (adminUserId.equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能修改自己的角色");
        }

        User user = userService.getById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        user.setRole(request.getRole());
        return userService.updateById(user);
    }

    @Override
    public Boolean updateUserStatus(Long adminUserId, AdminUserUpdateStatusRequest request) {
        if (request.getUserId() == null || request.getStatus() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getStatus() < 0 || request.getStatus() > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的状态值");
        }
        if (adminUserId.equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能修改自己的账户状态");
        }

        User user = userService.getById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        user.setStatus(request.getStatus());
        return userService.updateById(user);
    }

    @Override
    @Cacheable(value = "dashboard", key = "'full'")
    public DashboardVO getDashboard() {
        DashboardVO dashboard = new DashboardVO();

        // Overview
        DashboardVO.Overview overview = new DashboardVO.Overview();
        overview.setTotalUsers(userService.count());
        overview.setTotalQuestions(questionService.count());
        overview.setTotalSubmissions(questionSubmitService.count());
        overview.setTotalSolutions(questionSolutionService.count());

        Date todayStart = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        overview.setTodayNewUsers(userService.count(
                Wrappers.<User>lambdaQuery().ge(User::getCreateTime, todayStart)));
        overview.setTodaySubmissions(questionSubmitService.count(
                Wrappers.lambdaQuery(com.dailw.model.entity.QuestionSubmit.class)
                        .ge(com.dailw.model.entity.QuestionSubmit::getCreateTime, todayStart)));
        dashboard.setOverview(overview);

        // Submission trend (last 7 days)
        List<Map<String, Object>> submitRows = questionSubmitMapper.selectDailySubmitCount(7);
        dashboard.setSubmissionTrend(submitRows.stream()
                .map(r -> new DashboardVO.TrendPoint(
                        r.get("date").toString(), ((Number) r.get("count")).longValue()))
                .collect(Collectors.toList()));

        // Submission status distribution
        List<Map<String, Object>> statusRows = questionSubmitMapper.selectStatusDistribution();
        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (Map<String, Object> row : statusRows) {
            Integer statusCode = ((Number) row.get("status")).intValue();
            QuestionSubmitStatusEnum statusEnum = QuestionSubmitStatusEnum.getEnumByValue(statusCode);
            String label = statusEnum != null ? statusEnum.getText() : "未知(" + statusCode + ")";
            statusDist.put(label, ((Number) row.get("count")).longValue());
        }
        dashboard.setSubmissionStatusDist(statusDist);

        // User trend (last 7 days)
        List<Map<String, Object>> userRows = userMapper.selectDailyNewUsers(7);
        dashboard.setUserTrend(userRows.stream()
                .map(r -> new DashboardVO.TrendPoint(
                        r.get("date").toString(), ((Number) r.get("count")).longValue()))
                .collect(Collectors.toList()));

        // User role distribution
        List<Map<String, Object>> roleRows = userMapper.selectRoleDistribution();
        Map<String, Long> roleDist = new LinkedHashMap<>();
        for (Map<String, Object> row : roleRows) {
            roleDist.put(row.get("role").toString(), ((Number) row.get("count")).longValue());
        }
        dashboard.setUserRoleDist(roleDist);

        return dashboard;
    }

    private UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setGender(user.getGender());
        userVO.setBirthday(user.getBirthday());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setLastLoginIp(user.getLastLoginIp());
        userVO.setLoginCount(user.getLoginCount());
        userVO.setRole(user.getRole());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }
}

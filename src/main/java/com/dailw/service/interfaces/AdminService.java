package com.dailw.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.model.dto.admin.AdminUserQueryRequest;
import com.dailw.model.dto.admin.AdminUserUpdateRoleRequest;
import com.dailw.model.dto.admin.AdminUserUpdateStatusRequest;
import com.dailw.model.vo.UserVO;
import com.dailw.model.vo.admin.DashboardVO;

public interface AdminService {

    Page<UserVO> queryUserPage(AdminUserQueryRequest request);

    Boolean updateUserRole(Long adminUserId, AdminUserUpdateRoleRequest request);

    Boolean updateUserStatus(Long adminUserId, AdminUserUpdateStatusRequest request);

    DashboardVO getDashboard();
}

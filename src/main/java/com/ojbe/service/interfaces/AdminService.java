package com.ojbe.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.model.dto.admin.AdminUserQueryRequest;
import com.ojbe.model.dto.admin.AdminUserUpdateRoleRequest;
import com.ojbe.model.dto.admin.AdminUserUpdateStatusRequest;
import com.ojbe.model.vo.UserVO;
import com.ojbe.model.vo.admin.DashboardVO;

public interface AdminService {

    Page<UserVO> queryUserPage(AdminUserQueryRequest request);

    Boolean updateUserRole(Long adminUserId, AdminUserUpdateRoleRequest request);

    Boolean updateUserStatus(Long adminUserId, AdminUserUpdateStatusRequest request);

    DashboardVO getDashboard();
}

package com.vts.ProjectScheduler.repository.ibas;

import com.vts.ProjectScheduler.entity.ibas.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>{
	Role  findByRoleId(String roleId);
	Role  findByRoleName(String roleName);

}

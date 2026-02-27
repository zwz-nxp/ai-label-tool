package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.user.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, String> {}

package com.example.banhcanh.repository;

import com.example.banhcanh.model.UserRole;
import com.example.banhcanh.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(Long userId);
    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(Long userId);
}

package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Sysuser;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<Sysuser, Integer> {

    Optional<Sysuser> findByUserName(String userName);

    boolean existsByUserName(String userName);

    @Query("select u from Sysuser u where lower(u.userName) = lower(:userName)")
    Optional<Sysuser> findByUserNameIgnoreCase(@Param("userName") String userName);

    @Query("select case when count(u) > 0 then true else false end from Sysuser u where lower(u.userName) = lower(:userName)")
    boolean existsByUserNameIgnoreCase(@Param("userName") String userName);
}

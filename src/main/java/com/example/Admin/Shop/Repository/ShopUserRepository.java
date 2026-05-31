package com.example.Admin.Shop.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Admin.Shop.Model.ShopRole;
import com.example.Admin.Shop.Model.ShopUser;

public interface ShopUserRepository extends JpaRepository<ShopUser, Long> {
    Optional<ShopUser> findByUsernameIgnoreCase(String username);

    Optional<ShopUser> findByEmailIgnoreCase(String email);

    Optional<ShopUser> findFirstByPhoneOrderByIdAsc(String phone);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    long countByRole(ShopRole role);

    List<ShopUser> findByRole(ShopRole role);

    List<ShopUser> findByRoleIn(List<ShopRole> roles);
}

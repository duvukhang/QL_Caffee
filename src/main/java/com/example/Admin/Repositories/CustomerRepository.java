package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByUserName(String userName);

    boolean existsByUserName(String userName);

    @Query("select c from Customer c where lower(c.userName) = lower(:userName)")
    Optional<Customer> findByUserNameIgnoreCase(@Param("userName") String userName);

    @Query("select case when count(c) > 0 then true else false end from Customer c where lower(c.userName) = lower(:userName)")
    boolean existsByUserNameIgnoreCase(@Param("userName") String userName);
}

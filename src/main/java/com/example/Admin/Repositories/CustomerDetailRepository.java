package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.CustomerDetail;

import java.util.Optional;

@Repository
public interface CustomerDetailRepository extends JpaRepository<CustomerDetail, String> {

    Optional<CustomerDetail> findByEmailOrCustomerId(String email, String customerId);

    Optional<CustomerDetail> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNum(String phoneNum);

    boolean existsByIdNumber(String idNumber);

    boolean existsByEmailAndCustomerIdNot(String email, String customerId);

    boolean existsByPhoneNumAndCustomerIdNot(String phoneNum, String customerId);

    boolean existsByIdNumberAndCustomerIdNot(String idNumber, String customerId);
}

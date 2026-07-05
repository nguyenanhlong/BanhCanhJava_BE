package com.example.banhcanh.repository;

import com.example.banhcanh.model.MembershipVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MembershipVoucherRepository extends JpaRepository<MembershipVoucher, Long> {
    List<MembershipVoucher> findByUserIdOrderByIssuedAtDesc(Long userId);
    List<MembershipVoucher> findByUserIdAndStatus(Long userId, String status);
    List<MembershipVoucher> findByTierIdOrderByIssuedAtDesc(Long tierId);
}

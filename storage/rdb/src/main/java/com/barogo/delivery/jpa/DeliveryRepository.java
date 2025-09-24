package com.barogo.delivery.jpa;

import com.barogo.delivery.domain.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Page<Delivery> findByMemberIdAndRequestedAtBetween(Long memberId,
                                                       LocalDateTime from,
                                                       LocalDateTime to,
                                                       Pageable pageable);

}

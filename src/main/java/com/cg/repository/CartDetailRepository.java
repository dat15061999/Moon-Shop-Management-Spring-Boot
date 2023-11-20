package com.cg.repository;

import com.cg.model.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail,Long> {
    Optional<CartDetail> findCartDetailByProductId(Long idProduct);
}
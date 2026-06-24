package com.example.banhcanh.repository;

import com.example.banhcanh.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findByStatus(String status);
}

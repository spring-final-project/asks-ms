package com.springcloud.demo.asksmicroservice.asks.repository;

import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AskRepository extends JpaRepository<Ask, String>, JpaSpecificationExecutor<Ask> {
}

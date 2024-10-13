package com.springcloud.demo.asksmicroservice.asks.repository;

import com.springcloud.demo.asksmicroservice.asks.dto.FilterAskDTO;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AskSpecification {
    public Specification<Ask> withFilters(FilterAskDTO filters) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getUserId() != null) {
                predicates.add(builder.equal(root.get("userId"), filters.getUserId()));
            }
            if(filters.getRoomId() != null){
                predicates.add(builder.equal(root.get("roomId"), filters.getRoomId()));
            }

            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);

            return builder.and(predicatesArray);
        };
    }
}

package com.springcloud.demo.asksmicroservice.asks.repository;

import com.springcloud.demo.asksmicroservice.asks.dto.FilterAskDTO;
import com.springcloud.demo.asksmicroservice.asks.model.Ask;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AskSpecificationTest {

    @InjectMocks
    private AskSpecification askSpecification;

    private Root<Ask> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder builder;
    private FilterAskDTO filters;

    @BeforeEach
    void setup() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        builder = mock(CriteriaBuilder.class);
        filters = new FilterAskDTO();
    }

    @Test
    void withNoFilters() {
        Predicate predicate = askSpecification.withFilters(filters).toPredicate(root, query, builder);

        assertThat(predicate).isNull();
    }

    @Test
    void withUserIdFilter() {
        filters.setUserId(UUID.randomUUID().toString());

        askSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("userId"), filters.getUserId());
    }

    @Test
    void withRoomIdFilter() {
        filters.setRoomId(UUID.randomUUID().toString());

        askSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("roomId"), filters.getRoomId());
    }

}
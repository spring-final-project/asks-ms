package com.springcloud.demo.asksmicroservice.client.rooms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.asksmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.asksmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.asksmicroservice.exceptions.InheritedException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoomClientImpl implements RoomClient {

    private final RoomClient roomClient;

    @Override
    @CircuitBreaker(name = "rooms-service", fallbackMethod = "findRoomByIdFallback")
    public RoomDTO findById(String id) {
        return roomClient.findById(id);
    }

    RoomDTO findRoomByIdFallback(String id, Throwable e) throws Exception {
        if(!(e instanceof FeignException.FeignClientException feignClientException)){
            throw new ForbiddenException("Rooms service not available. Try later");
        }

        Map body = new ObjectMapper().readValue(feignClientException.contentUTF8(), Map.class);

        throw new InheritedException(
                feignClientException.status(),
                (String) body.get("message")
        );
    }
}

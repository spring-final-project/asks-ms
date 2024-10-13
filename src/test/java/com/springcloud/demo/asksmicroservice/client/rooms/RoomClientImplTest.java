package com.springcloud.demo.asksmicroservice.client.rooms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.asksmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.asksmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.asksmicroservice.exceptions.InheritedException;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class RoomClientImplTest {

    @Mock
    private RoomClient roomClient;

    @InjectMocks
    private RoomClientImpl roomClientImpl;

    @Nested
    class FindUserById {
        @Test
        void findUserById() {
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .build();

            given(roomClient.findById(anyString())).willReturn(roomDTO);

            RoomDTO result = roomClientImpl.findById(roomDTO.getId());

            assertThat(result).isEqualTo(roomDTO);
        }
    }

    @Nested
    class Fallback {
        @Test
        void whenCannotConnectToUsersService() {

            ForbiddenException response = Assertions.assertThrows(ForbiddenException.class, () -> {
                roomClientImpl.findRoomByIdFallback(UUID.randomUUID().toString(), new RuntimeException());
            });

            assertThat(response.getMessage()).isEqualTo("Rooms service not available. Try later");
        }

        @Test
        void whenReceiveClientExceptionFromUsersService() throws JsonProcessingException {
            Map body = Map.of("message", "Not found room with id");
            String bodyString = new ObjectMapper().writeValueAsString(body);

            InheritedException response = Assertions.assertThrows(InheritedException.class, () -> {
                roomClientImpl.findRoomByIdFallback(
                        UUID.randomUUID().toString(),
                        new FeignException.FeignClientException(400, null, mock(Request.class), bodyString.getBytes(), null)
                );
            });

            assertThat(response.getMessage()).isEqualTo(body.get("message"));
        }
    }
}
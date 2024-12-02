package com.springcloud.demo.asksmicroservice.asks.dto;

import com.springcloud.demo.asksmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.asksmicroservice.client.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishAskEventDTO {
    private String id;
    private String createdAt;
    private String respondedAt;
    private String question;
    private String answer;
    private RoomDTO room;
    private UserDTO user;
}

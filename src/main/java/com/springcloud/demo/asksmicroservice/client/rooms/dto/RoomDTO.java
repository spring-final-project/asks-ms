package com.springcloud.demo.asksmicroservice.client.rooms.dto;

import com.springcloud.demo.asksmicroservice.client.users.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private String id;
    private Integer num;
    private String name;
    private String description;
    private String ownerId;
    private UserDTO owner;
}

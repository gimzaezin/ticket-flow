package com.gimzazin.tiketflow.users.dto;

import com.gimzazin.tiketflow.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequestDto {
    private String name;
    private String email;
    private String phone;

    public User toEntity() {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .phone(this.phone)
                .build();
    }
}

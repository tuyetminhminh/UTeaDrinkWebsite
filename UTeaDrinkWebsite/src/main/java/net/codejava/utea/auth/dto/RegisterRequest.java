package net.codejava.utea.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address; // 🏡 thêm địa chỉ
}
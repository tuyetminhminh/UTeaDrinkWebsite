package net.codejava.utea.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordChangeForm {

    @NotBlank(message = "Mật khẩu hiện tại không được trống")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được trống")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được trống")
    private String confirmPassword;
}

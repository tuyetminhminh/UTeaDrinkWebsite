package net.codejava.utea.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserForm {
    private Long id;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email quá dài (tối đa 150 ký tự)")
    private String email;

    @Size(max = 100, message = "Username quá dài (tối đa 100 ký tự)")
    private String username;

    @Size(max = 200, message = "Họ tên quá dài (tối đa 200 ký tự)")
    private String fullName;

    @NotBlank(message = "Trạng thái không hợp lệ")
    private String status = "ACTIVE";

    // ❗ KHÔNG đặt @Size ở đây: để trống khi sửa = giữ nguyên
    private String password;

    @NotBlank(message = "Vui lòng chọn vai trò")
    private String roleCode = "CUSTOMER";
}

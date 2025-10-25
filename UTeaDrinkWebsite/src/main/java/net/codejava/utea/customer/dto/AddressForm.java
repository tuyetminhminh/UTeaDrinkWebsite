package net.codejava.utea.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddressForm {
    @NotBlank(message = "Họ tên người nhận không được trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được trống")
    @Pattern(regexp = "^[0-9+]{8,15}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Số nhà/đường không được trống")
    private String line;

    @NotBlank(message = "Phường/Xã không được trống")
    private String ward;

    @NotBlank(message = "Quận/Huyện không được trống")
    private String district;

    @NotBlank(message = "Tỉnh/Thành phố không được trống")
    private String province;

    private boolean isDefault;
}

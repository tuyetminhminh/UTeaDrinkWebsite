package net.codejava.utea.service;

public interface OtpService {
    void sendResetOtpToEmail(String email);           // tạo + gửi OTP
    void resetPasswordByEmail(String email, String otpCode, String newPassword); // xác minh + đổi mật khẩu
}

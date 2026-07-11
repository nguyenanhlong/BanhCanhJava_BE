package com.example.banhcanh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(to);
        msg.setSubject("Mã đặt lại mật khẩu - Bánh Canh Cá Lóc Miền Trung");
        msg.setText("Xin chào,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình tại Bánh Canh Cá Lóc Miền Trung.\n\n"
                + "Mã xác thực của bạn là: " + otp + "\n\n"
                + "Mã có hiệu lực trong 15 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\n"
                + "Bánh Canh Cá Lóc Miền Trung");
        mailSender.send(msg);
    }

    public void sendOrderNotification(String to, String orderId, String customerName, String status, Long totalAmount) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(to);
        msg.setSubject("Cập nhật đơn hàng #" + orderId + " - Bánh Canh Cá Lóc Miền Trung");

        String statusText;
        switch (status) {
            case "pending": statusText = "đang chờ xác nhận"; break;
            case "preparing": statusText = "đang được chế biến"; break;
            case "shipping": statusText = "đang được giao đi"; break;
            case "completed": statusText = "đã giao thành công"; break;
            case "cancelled": statusText = "đã bị hủy"; break;
            default: statusText = status;
        }

        msg.setText("Xin chào " + customerName + ",\n\n"
                + "Đơn hàng #" + orderId + " của bạn đã được cập nhật sang trạng thái: " + statusText + ".\n\n"
                + "Tổng tiền: " + (totalAmount != null ? totalAmount + "đ" : "—") + "\n\n"
                + "Cảm ơn bạn đã mua hàng tại Bánh Canh Cá Lóc Miền Trung!\n\n"
                + "Trân trọng,\n"
                + "Bánh Canh Cá Lóc Miền Trung");
        mailSender.send(msg);
    }
}

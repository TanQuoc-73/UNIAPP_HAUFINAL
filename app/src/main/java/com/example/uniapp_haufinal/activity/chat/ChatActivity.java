package com.example.uniapp_haufinal.activity.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {
    // Thông tin server TCP của bạn (Bắt buộc phải có Server riêng chạy mã này)
    private static final String SERVER_IP = "192.168.1.100"; // Đổi thành IP máy chủ của bạn
    private static final int SERVER_PORT = 9999;

    private String partnerId, currentUserId;
    private FirebaseAuth auth;

    // Các biến cho TCP Socket
    private Socket tcpSocket;
    private PrintWriter out;
    private BufferedReader in;

    private LinearLayout chatContainer;
    private EditText edtMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById(R.id.chatContainer);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        // 1. DÙNG FIREBASE: Lấy ID người dùng một cách bảo mật
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Nhận ID người nhận từ màn hình trước
        partnerId = getIntent().getStringExtra("partner_id");

        // 2. DÙNG TCP SOCKET: Mở kết nối đến máy chủ
        connectToTcpServer();

        // 3. Xử lý nút gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendTcpMessage(message);
            }
        });
    }

    private void connectToTcpServer() {
        // Mở một luồng chạy ngầm (Background Thread) để không làm đơ ứng dụng
        new Thread(() -> {
            try {
                // Khởi tạo kết nối Socket tới Server của bạn
                tcpSocket = new Socket(SERVER_IP, SERVER_PORT);

                // Khởi tạo ống truyền dữ liệu (Gửi đi)
                out = new PrintWriter(tcpSocket.getOutputStream(), true);
                // Khởi tạo ống nhận dữ liệu (Đọc về)
                in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

                // Ngay khi kết nối, gửi Firebase UID của mình lên Server để Server biết mình là ai
                out.println("AUTH|" + currentUserId);

                // Liên tục lắng nghe tin nhắn mới từ Server gửi về
                String incomingMessage;
                while ((incomingMessage = in.readLine()) != null) {
                    final String msg = incomingMessage;

                    // Android quy định: Giao diện (UI) chỉ được cập nhật trên luồng chính
                    runOnUiThread(() -> {
                        displayMessage(msg, partnerId); // Hiển thị tin nhắn của đối tác
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi kết nối Socket!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendTcpMessage(String text) {
        // Gửi tin nhắn cũng phải dùng luồng ngầm
        new Thread(() -> {
            if (out != null) {
                // Gửi tin nhắn theo cú pháp: "ID_NGUOI_NHAN|NOI_DUNG_TIN_NHAN"
                // Server sẽ đọc dòng này và tìm đúng Socket của partnerId để chuyển tiếp
                out.println(partnerId + "|" + text);

                // Cập nhật giao diện bên mình (đã gửi thành công)
                runOnUiThread(() -> {
                    displayMessage(text, currentUserId);
                    edtMessage.setText("");
                });
            }
        }).start();
    }

    private void displayMessage(String text, String senderId) {
        // ... (Giữ nguyên logic vẽ TextView động như ở đoạn code Firebase trước)
        TextView tvMessage = new TextView(this);
        tvMessage.setText(text);
        tvMessage.setTextColor(Color.BLACK);
        tvMessage.setPadding(32, 24, 32, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);

        if (senderId.equals(currentUserId)) {
            params.gravity = Gravity.END;
            tvMessage.setBackgroundColor(Color.parseColor("#E3F2FD"));
        } else {
            params.gravity = Gravity.START;
            tvMessage.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }

        tvMessage.setLayoutParams(params);
        chatContainer.addView(tvMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nhớ đóng Socket khi thoát màn hình Chat để giải phóng bộ nhớ
        try {
            if (tcpSocket != null) tcpSocket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
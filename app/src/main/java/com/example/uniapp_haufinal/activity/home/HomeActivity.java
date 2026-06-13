package com.example.uniapp_haufinal.activity.home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.uniapp_haufinal.R;
import com.bumptech.glide.Glide;

//cac activity nav chuyen trang
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;

//firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    TextView navMap, navMarket, navProfile, navPost, navChat;

    LinearLayout postContainer;
    FirebaseFirestore db;

    // Hàm giả lập AI đề xuất bài viết dựa trên tương tác
    private List<DocumentSnapshot> rankPostsByAI(List<DocumentSnapshot> posts) {
        List<DocumentSnapshot> sortedPosts = new ArrayList<>(posts);
        
        // AI Logic: Tính toán điểm số dựa trên likeCount và thời gian (giả lập)
        // Công thức: Score = (Likes * 10) + (Comments * 5) - (Giờ từ lúc đăng)
        Collections.sort(sortedPosts, (p1, p2) -> {
            long likes1 = p1.getLong("likeCount") != null ? p1.getLong("likeCount") : 0;
            long likes2 = p2.getLong("likeCount") != null ? p2.getLong("likeCount") : 0;
            
            long comments1 = p1.getLong("commentCount") != null ? p1.getLong("commentCount") : 0;
            long comments2 = p2.getLong("commentCount") != null ? p2.getLong("commentCount") : 0;

            // Điểm số tương tác
            long score1 = (likes1 * 2) + comments1;
            long score2 = (likes2 * 2) + comments2;

            return Long.compare(score2, score1); // Sắp xếp giảm dần theo điểm AI
        });

        return sortedPosts;
    }

    //ham hien thi bai viet
    private void loadPost(){
        postContainer.removeAllViews();

        db.collection("posts")
                .limit(50) // Lấy danh sách thô để xử lý sắp xếp theo tương tác
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> allPosts = queryDocumentSnapshots.getDocuments();
                    List<DocumentSnapshot> recommendedPosts = rankPostsByAI(allPosts);

                    if (recommendedPosts.isEmpty()) {
                        showEmptyState("Chưa có bài viết nào. Hãy là người đầu tiên đăng bài!");
                        return;
                    }

                    for (DocumentSnapshot document : recommendedPosts){
                        String postId = document.getId();
                        final String authorName = document.getString("authorName") != null ? document.getString("authorName") : "An danh";
                        final String content = document.getString("content") != null ? document.getString("content") : "";
                        Long likeCount = document.getLong("likeCount");
                        final String imageUrl = document.getString("imageUrl");

                        if(likeCount == null){
                            likeCount = 0L;
                        }

                        LinearLayout postLayout = new LinearLayout(this);
                        postLayout.setOrientation(LinearLayout.VERTICAL);
                        postLayout.setPadding(32, 24, 32, 24);
                        postLayout.setBackgroundColor(Color.WHITE);
                        
                        // User info
                        TextView txtAuthor = new TextView(this);
                        txtAuthor.setText(authorName);
                        txtAuthor.setTextColor(Color.BLACK);
                        txtAuthor.setTextSize(16);
                        txtAuthor.setTypeface(null, android.graphics.Typeface.BOLD);
                        txtAuthor.setPadding(0, 0, 0, 8);

                        // Content text
                        TextView txtContent = new TextView(this);
                        txtContent.setText(content);
                        txtContent.setTextColor(Color.BLACK);
                        txtContent.setTextSize(15);
                        txtContent.setPadding(0, 0, 0, 12);

                        postLayout.addView(txtAuthor);
                        postLayout.addView(txtContent);

                        // Hiển thị ảnh nếu có
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            ImageView imgPost = new ImageView(this);
                            imgPost.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imgPost.setBackgroundColor(Color.LTGRAY);
                            
                            if (!isDestroyed()) {
                                Glide.with(this).load(imageUrl).into(imgPost);
                            }
                            
                            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 600);
                            imgParams.setMargins(0, 8, 0, 8);
                            postLayout.addView(imgPost, imgParams);
                        }

                        // Stats
                        TextView txtLikeStats = new TextView(this);
                        txtLikeStats.setText("👍 " + likeCount + " lượt thích");
                        txtLikeStats.setTextColor(Color.GRAY);
                        txtLikeStats.setTextSize(13);
                        txtLikeStats.setPadding(0, 12, 0, 12);
                        postLayout.addView(txtLikeStats);

                        View line = new View(this);
                        line.setBackgroundColor(Color.parseColor("#E4E6EB"));
                        postLayout.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

                        // Actions Layout
                        LinearLayout actionsLayout = new LinearLayout(this);
                        actionsLayout.setOrientation(LinearLayout.HORIZONTAL);
                        actionsLayout.setPadding(0, 8, 0, 0);

                        TextView btnLike = createActionButton("Thích");
                        TextView btnComment = createActionButton("Bình luận");
                        TextView btnShare = createActionButton("Chia sẻ");

                        btnLike.setOnClickListener(v -> {
                            db.collection("posts").document(postId)
                                    .update("likeCount", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> loadPost());
                        });

                        btnComment.setOnClickListener(v -> showCommentDialog(postId));

                        btnShare.setOnClickListener(v -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, authorName + " đã đăng: " + content + (imageUrl != null ? "\n" + imageUrl : ""));
                            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết qua"));
                        });

                        actionsLayout.addView(btnLike);
                        actionsLayout.addView(btnComment);
                        actionsLayout.addView(btnShare);
                        postLayout.addView(actionsLayout);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 16);
                        postContainer.addView(postLayout, params);
                    }
                }).addOnFailureListener(e -> {
                    if (postContainer.getChildCount() == 0) {
                        showEmptyState("Không có bài viết nào để hiển thị");
                    } else {
                        Toast.makeText(this, "Không tải được bài viết", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmptyState(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 100, 0, 0);
        tv.setTextColor(Color.GRAY);
        postContainer.addView(tv);
    }

    private TextView createActionButton(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 16, 0, 16);
        tv.setTextColor(Color.parseColor("#65676B"));
        tv.setTextSize(14);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void showCommentDialog(String postId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Viết bình luận");

        final EditText input = new EditText(this);
        input.setHint("Nhập nội dung bình luận...");
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String commentText = input.getText().toString().trim();
            if (!commentText.isEmpty()) {
                String uid = FirebaseAuth.getInstance().getUid();
                Map<String, Object> comment = new HashMap<>();
                comment.put("userId", uid);
                comment.put("text", commentText);
                comment.put("timestamp", FieldValue.serverTimestamp());

                db.collection("posts").document(postId).collection("comments").add(comment)
                        .addOnSuccessListener(doc -> {
                            db.collection("posts").document(postId).update("commentCount", FieldValue.increment(1));
                            Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    //reload bai viet
    protected void onResume(){
        super.onResume();
        if(postContainer !=null){
            loadPost();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        postContainer = findViewById(R.id.postContainer);

        //Nav
        //Market
        navMarket = findViewById(R.id.navMarket);
        navMarket.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, MarketActivity.class);
            startActivity(intent);
        });

        //Map
        navMap = findViewById(R.id.navMap);
        navMap.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, MapActivity.class);
            startActivity(intent);
        });

        //profile
        navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(view->{
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        //post
        navPost = findViewById(R.id.navPost);
        navPost.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        // Chat
        navChat = findViewById(R.id.navChat);
        navChat.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.example.uniapp_haufinal.activity.chat.ChatListActivity.class);
            startActivity(intent);
        });

    }
}

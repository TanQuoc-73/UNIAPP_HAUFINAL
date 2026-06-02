package com.example.uniapp_haufinal.activity.home;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomePost {
    Activity activity;
    LinearLayout postContainer;
    FirebaseFirestore db;

    List<QueryDocumentSnapshot> danhSachPost = new ArrayList<>();
    int soBaiDangHienThi = 5;
    final int SO_BAI_MOI_LAN = 5;

    public HomePost(Activity activity, LinearLayout postContainer, FirebaseFirestore db){
        this.activity = activity;
        this.postContainer = postContainer;
        this.db = db;
    }

    //ham load bai viet tu firestore
    public void loadPost(){
        postContainer.removeAllViews();
        soBaiDangHienThi = SO_BAI_MOI_LAN;

        db.collection("posts")
                //loc bai viet
//                .whereEqualTo("status", "approved")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    danhSachPost.clear();
                    for (var document : queryDocumentSnapshots){
                        danhSachPost.add(document);
                    }

                    hienThiPost();

                }).addOnFailureListener(e -> {
                    Toast.makeText(activity, "Khong tai dc bai viet", Toast.LENGTH_SHORT).show();
                });
    }

    private void hienThiPost(){
        postContainer.removeAllViews();

        int soBaiCanHienThi = Math.min(soBaiDangHienThi, danhSachPost.size());

        for(int i = 0; i < soBaiCanHienThi; i++){
            QueryDocumentSnapshot document = danhSachPost.get(i);
            themPostVaoManHinh(document);
        }

        if(soBaiCanHienThi < danhSachPost.size()){
            TextView btnTaiThem = new TextView(activity);
            btnTaiThem.setText("Tai them 5 bai viet");
            btnTaiThem.setTextColor(Color.WHITE);
            btnTaiThem.setTextSize(16);
            btnTaiThem.setGravity(android.view.Gravity.CENTER);
            btnTaiThem.setBackgroundColor(Color.BLACK);
            btnTaiThem.setPadding(16, 16, 16, 16);

            btnTaiThem.setOnClickListener(view -> {
                soBaiDangHienThi = soBaiDangHienThi + SO_BAI_MOI_LAN;
                hienThiPost();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 0, 16, 24);
            postContainer.addView(btnTaiThem, params);
        }
    }

    private void themPostVaoManHinh(QueryDocumentSnapshot document){
        String authorName = document.getString("authorName");
        String content = document.getString("content");
        Long likeCount = document.getLong("likeCount");

        if(authorName == null || authorName.isEmpty()){
            authorName = "An danh";
        }
        if(content == null){
            content = "";
        }
        if(likeCount == null){
            likeCount = 0L;
        }

        LinearLayout postLayout = new LinearLayout(activity);
        postLayout.setOrientation(LinearLayout.VERTICAL);
        postLayout.setPadding(16, 16, 16, 16);
        postLayout.setBackgroundColor(Color.WHITE);

        TextView txtAuthor = new TextView(activity);
        txtAuthor.setText(authorName);
        txtAuthor.setTextColor(Color.BLACK);
        txtAuthor.setTextSize(16);
        txtAuthor.setTypeface(null, android.graphics.Typeface.BOLD);
        txtAuthor.setPadding(0, 0, 0, 12);

        TextView txtImage = new TextView(activity);
        txtImage.setText("Anh bai viet");
        txtImage.setTextColor(Color.DKGRAY);
        txtImage.setTextSize(18);
        txtImage.setGravity(android.view.Gravity.CENTER);
        txtImage.setBackgroundColor(Color.rgb(230, 230, 230));

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                520
        );

        TextView txtLike = new TextView(activity);
        txtLike.setText(likeCount + " luot thich");
        txtLike.setTextColor(Color.BLACK);
        txtLike.setTextSize(14);
        txtLike.setTypeface(null, android.graphics.Typeface.BOLD);
        txtLike.setPadding(0, 12, 0, 4);

        TextView txtAction = new TextView(activity);
        txtAction.setText("Tim   Binh luan   Chia se");
        txtAction.setTextColor(Color.DKGRAY);
        txtAction.setTextSize(14);
        txtAction.setPadding(0, 10, 0, 0);

        TextView txtContent = new TextView(activity);
        txtContent.setText(authorName + " " + content);
        txtContent.setTextColor(Color.BLACK);
        txtContent.setTextSize(15);
        txtContent.setPadding(0, 0, 0, 8);

        postLayout.addView(txtAuthor);
        postLayout.addView(txtImage, imageParams);
        postLayout.addView(txtAction);
        postLayout.addView(txtLike);
        postLayout.addView(txtContent);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);

        postContainer.addView(postLayout, params);
    }
}

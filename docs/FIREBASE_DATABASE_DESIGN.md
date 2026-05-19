# Firebase database design

Nguoi lam: Duong Quoc Tan

Trang thai: Dang lam - Duong Quoc Tan

Muc tieu: thiet ke database don gian, de code voi Firebase Auth + Cloud Firestore. Khong tach qua nhieu bang neu chua can.

## Dich vu Firebase dung trong app

- Firebase Auth: dang ky, dang nhap, dang xuat.
- Cloud Firestore: luu user, profile, bai viet, market, chat, map.
- Firebase Storage: luu anh bai viet, anh san pham, avatar. Firestore chi luu URL anh.

Can them dependency vao `app/build.gradle.kts` khi code Firestore:

```kotlin
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-storage")
```

## Collections chinh

```text
users
profiles
posts
postComments
postLikes
marketItems
transactions
chats
mapPlaces
reports
```

## 1. users

Luu thong tin tai khoan co ban. Document id nen dung `uid` cua Firebase Auth.

Path:

```text
users/{uid}
```

Fields:

```js
{
  uid: "firebase_auth_uid",
  email: "user@gmail.com",
  displayName: "Nguyen Van A",
  phone: "0987654321",
  avatarUrl: "",
  role: "user", // user | admin
  status: "active", // active | locked
  createdAt: serverTimestamp(),
  updatedAt: serverTimestamp()
}
```

Query database:

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();
String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

db.collection("users").document(uid).get()
    .addOnSuccessListener(document -> {
        String name = document.getString("displayName");
        String phone = document.getString("phone");
    });
```

## 2. profiles

Luu thong tin profile mo rong. Neu muon don gian hon co the gop vao `users`, nhung tach rieng se dung dung yeu cau co bang `profile`.

Path:

```text
profiles/{uid}
```

Fields:

```js
{
  uid: "firebase_auth_uid",
  bio: "Sinh vien HAU",
  faculty: "Cong nghe thong tin",
  className: "CNTT1",
  address: "Ha Noi",
  friendIds: ["uid_1", "uid_2"],
  updatedAt: serverTimestamp()
}
```

Query database:

```java
db.collection("profiles").document(uid).get();
```

## 3. posts

Luu bai dang cua user. Anh upload len Firebase Storage, sau do luu link vao `imageUrls`.

Path:

```text
posts/{postId}
```

Fields:

```js
{
  postId: "auto_id",
  authorId: "uid_nguoi_dang",
  authorName: "Nguyen Van A",
  authorAvatarUrl: "",
  content: "Noi dung bai viet",
  imageUrls: ["https://..."],
  visibility: "public", // public | friends
  status: "pending", // pending | approved | rejected | deleted
  likeCount: 0,
  commentCount: 0,
  shareCount: 0,
  aiScore: 0,
  aiStatus: "not_checked", // not_checked | safe | warning | blocked
  createdAt: serverTimestamp(),
  updatedAt: serverTimestamp()
}
```

Query database:

```java
db.collection("posts")
    .whereEqualTo("status", "approved")
    .whereEqualTo("visibility", "public")
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .limit(20)
    .get();
```

Admin duyet bai:

```java
db.collection("posts").document(postId)
    .update("status", "approved", "updatedAt", FieldValue.serverTimestamp());
```

## 4. postComments

Luu binh luan bai viet. De collection rieng cho de query va khong lam document `posts` qua lon.

Path:

```text
postComments/{commentId}
```

Fields:

```js
{
  commentId: "auto_id",
  postId: "post_id",
  userId: "uid_nguoi_comment",
  userName: "Nguyen Van B",
  content: "Binh luan",
  status: "active", // active | deleted
  createdAt: serverTimestamp()
}
```

Query database:

```java
db.collection("postComments")
    .whereEqualTo("postId", postId)
    .whereEqualTo("status", "active")
    .orderBy("createdAt", Query.Direction.ASCENDING)
    .get();
```

## 5. postLikes

Moi user chi duoc tim 1 lan tren 1 bai viet. Document id nen dung `{postId}_{uid}`.

Path:

```text
postLikes/{postId_uid}
```

Fields:

```js
{
  postId: "post_id",
  userId: "uid_nguoi_tim",
  createdAt: serverTimestamp()
}
```

Query database:

```java
String likeId = postId + "_" + uid;
db.collection("postLikes").document(likeId).set(likeData);
```

Khi tha tim nen tang `likeCount` trong `posts`:

```java
db.collection("posts").document(postId)
    .update("likeCount", FieldValue.increment(1));
```

## 6. marketItems

Luu vat pham mua ban. Day la collection chinh cua module Market.

Path:

```text
marketItems/{itemId}
```

Fields:

```js
{
  itemId: "auto_id",
  sellerId: "uid_nguoi_ban",
  sellerName: "Nguyen Van A",
  title: "Sach giao trinh",
  description: "Sach con moi",
  quantity: 1,
  price: 50000,
  pickupLocation: "Cong truong DH Kien truc",
  contactPhone: "0987654321",
  imageUrls: ["https://..."],
  category: "book",
  status: "available", // available | locked | sold | hidden
  lockedBy: null,
  lockedUntil: null,
  createdAt: serverTimestamp(),
  updatedAt: serverTimestamp()
}
```

Query database:

```java
db.collection("marketItems")
    .whereEqualTo("status", "available")
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get();
```

Lock vat pham 3 phut khi bam mua:

```java
long lockedUntil = System.currentTimeMillis() + 3 * 60 * 1000;

db.collection("marketItems").document(itemId)
    .update(
        "status", "locked",
        "lockedBy", uid,
        "lockedUntil", lockedUntil,
        "updatedAt", FieldValue.serverTimestamp()
    );
```

Khi hien thi danh sach, neu item `locked` nhung `lockedUntil` da qua thoi gian hien tai thi co the cap nhat lai thanh `available`.

## 7. transactions

Luu lich su mua ban. Tao khi user bam mua hoac khi chat giao dich bat dau.

Path:

```text
transactions/{transactionId}
```

Fields:

```js
{
  transactionId: "auto_id",
  itemId: "item_id",
  buyerId: "uid_nguoi_mua",
  sellerId: "uid_nguoi_ban",
  status: "chatting", // chatting | completed | canceled
  price: 50000,
  createdAt: serverTimestamp(),
  updatedAt: serverTimestamp()
}
```

Query database:

```java
db.collection("transactions")
    .whereEqualTo("buyerId", uid)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get();
```

## 8. chats

Luu phong chat giua nguoi mua va nguoi ban. Tin nhan de trong subcollection `messages`.

Path:

```text
chats/{chatId}
chats/{chatId}/messages/{messageId}
```

Fields cua `chats/{chatId}`:

```js
{
  chatId: "auto_id",
  itemId: "item_id",
  transactionId: "transaction_id",
  buyerId: "uid_nguoi_mua",
  sellerId: "uid_nguoi_ban",
  memberIds: ["uid_nguoi_mua", "uid_nguoi_ban"],
  lastMessage: "Minh muon mua",
  lastMessageAt: serverTimestamp(),
  createdAt: serverTimestamp()
}
```

Fields cua `messages/{messageId}`:

```js
{
  messageId: "auto_id",
  senderId: "uid_nguoi_gui",
  text: "Noi dung tin nhan",
  imageUrl: "",
  createdAt: serverTimestamp(),
  readBy: ["uid_nguoi_gui"]
}
```

Query database:

```java
db.collection("chats")
    .whereArrayContains("memberIds", uid)
    .orderBy("lastMessageAt", Query.Direction.DESCENDING)
    .get();
```

```java
db.collection("chats").document(chatId)
    .collection("messages")
    .orderBy("createdAt", Query.Direction.ASCENDING)
    .addSnapshotListener((snapshots, error) -> {
        // render messages realtime
    });
```

## 9. mapPlaces

Luu dia diem user danh dau tren map.

Path:

```text
mapPlaces/{placeId}
```

Fields:

```js
{
  placeId: "auto_id",
  ownerId: "uid_nguoi_tao",
  title: "Quan cafe",
  description: "Dia diem hoc nhom",
  latitude: 21.0278,
  longitude: 105.8342,
  address: "Ha Noi",
  visibility: "public", // public | friends | private
  imageUrl: "",
  likeCount: 0,
  createdAt: serverTimestamp(),
  updatedAt: serverTimestamp()
}
```

Query database:

```java
db.collection("mapPlaces")
    .whereEqualTo("visibility", "public")
    .get();
```

## 10. reports

Dung cho AI hoac admin quan ly noi dung vi pham.

Path:

```text
reports/{reportId}
```

Fields:

```js
{
  reportId: "auto_id",
  targetType: "post", // post | marketItem | comment
  targetId: "id_bi_report",
  reporterId: "uid_nguoi_report",
  reason: "spam",
  status: "pending", // pending | reviewed | ignored
  createdAt: serverTimestamp()
}
```

## Query can lam truoc

Auth + Profile:

```text
users/{uid}
profiles/{uid}
```

- Sau khi dang ky Firebase Auth thanh cong, tao document trong `users/{uid}` va `profiles/{uid}`.
- Trang Profile doc `users/{uid}` va `profiles/{uid}`.

Market:

```text
marketItems where status == "available" order by createdAt desc
marketItems/{itemId}
transactions where buyerId == uid
transactions where sellerId == uid
```

Post:

```text
posts where status == "approved" and visibility == "public" order by createdAt desc
postComments where postId == postId order by createdAt asc
postLikes/{postId_uid}
```

Chat:

```text
chats where memberIds array-contains uid order by lastMessageAt desc
chats/{chatId}/messages order by createdAt asc
```

Map:

```text
mapPlaces where visibility == "public"
mapPlaces where ownerId == uid
```

Admin:

```text
posts where status == "pending" order by createdAt asc
reports where status == "pending" order by createdAt asc
```

## Index Firestore nen tao

Firestore se bao link tao index khi query loi. Cac index nen co:

```text
posts: status ASC, visibility ASC, createdAt DESC
postComments: postId ASC, status ASC, createdAt ASC
marketItems: status ASC, createdAt DESC
transactions: buyerId ASC, createdAt DESC
transactions: sellerId ASC, createdAt DESC
chats: memberIds ARRAY, lastMessageAt DESC
posts: status ASC, aiScore DESC, createdAt DESC
```

## Security rules goi y

Ban don gian de demo:

```js
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return signedIn() && request.auth.uid == userId;
    }

    function isAdmin() {
      return signedIn()
        && exists(/databases/$(database)/documents/users/$(request.auth.uid))
        && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    match /users/{userId} {
      allow read: if signedIn();
      allow create, update: if isOwner(userId) || isAdmin();
      allow delete: if isAdmin();
    }

    match /profiles/{userId} {
      allow read: if signedIn();
      allow create, update: if isOwner(userId) || isAdmin();
      allow delete: if isAdmin();
    }

    match /posts/{postId} {
      allow read: if signedIn();
      allow create: if signedIn();
      allow update, delete: if signedIn() && (resource.data.authorId == request.auth.uid || isAdmin());
    }

    match /postComments/{commentId} {
      allow read, create: if signedIn();
      allow update, delete: if signedIn() && (resource.data.userId == request.auth.uid || isAdmin());
    }

    match /postLikes/{likeId} {
      allow read, create, delete: if signedIn();
    }

    match /marketItems/{itemId} {
      allow read: if signedIn();
      allow create: if signedIn();
      allow update, delete: if signedIn() && (resource.data.sellerId == request.auth.uid || isAdmin() || request.resource.data.lockedBy == request.auth.uid);
    }

    match /transactions/{transactionId} {
      allow read, create, update: if signedIn()
        && (request.resource.data.buyerId == request.auth.uid
          || request.resource.data.sellerId == request.auth.uid
          || isAdmin());
    }

    match /chats/{chatId} {
      allow read, create, update: if signedIn() && request.auth.uid in request.resource.data.memberIds;

      match /messages/{messageId} {
        allow read, create: if signedIn()
          && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.memberIds;
      }
    }

    match /mapPlaces/{placeId} {
      allow read: if signedIn();
      allow create: if signedIn();
      allow update, delete: if signedIn() && (resource.data.ownerId == request.auth.uid || isAdmin());
    }

    match /reports/{reportId} {
      allow read: if isAdmin();
      allow create: if signedIn();
      allow update, delete: if isAdmin();
    }
  }
}
```

## Note qua trinh lam

- Da chon Cloud Firestore vi phu hop app mobile, CRUD nhanh, chat realtime de lam bang `addSnapshotListener`.
- Da tach database thanh cac collection toi thieu: `users`, `profiles`, `posts`, `postComments`, `postLikes`, `marketItems`, `transactions`, `chats`, `mapPlaces`, `reports`.
- Market co trang thai `available`, `locked`, `sold`, `hidden` de lam chuc nang lock 3 phut khi user bam mua.
- Chat dung `chats/{chatId}/messages/{messageId}` de lay tin nhan realtime.
- Post co `status` de admin duyet bai va co `aiScore`, `aiStatus` de sau nay them AI neu kip.
- Map dung `visibility` de hien thi cong dong, ban be hoac rieng tu.

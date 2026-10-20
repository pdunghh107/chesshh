---
name: writing-javadoc
description: "Quy chuẩn và hướng dẫn cách viết Javadoc tối ưu, chuyên nghiệp cho dự án Testify. Bắt buộc tham khảo khi viết comment cho code Java."
---

# Hướng dẫn viết Javadoc (Quy chuẩn)

Javadoc là một công cụ tích hợp sẵn trong bộ công cụ phát triển Java (JDK), được sử dụng để tự động tạo tài liệu API dưới dạng trang web (HTML) trực tiếp từ các dòng chú thích (comments) trong mã nguồn.

Các đoạn chú thích Javadoc luôn được đặt trong cặp dấu `/** ... */` và nằm ngay phía trên khai báo của lớp (class), giao diện (interface), phương thức (method) hoặc biến (field).

## Cách tối ưu hóa việc sử dụng Javadoc
Nhiều lập trình viên viết Javadoc chỉ để "cho có", khiến tài liệu trở nên rườm rà nhưng thiếu thông tin. Để Javadoc thực sự mang lại giá trị cho đội ngũ của bạn, hãy áp dụng các nguyên tắc sau:

1. **Tóm tắt súc tích ở câu đầu tiên:** Javadoc sẽ trích xuất câu đầu tiên của bạn để đưa vào bảng mục lục tổng hợp. Câu này phải là một mô tả ngắn gọn, đầy đủ nhất về chức năng chính của đoạn mã.

2. **Giải thích "Tại sao" và "Như thế nào", không chỉ "Cái gì":** Đừng lặp lại tên hàm. Với phương thức `getUserId()`, đừng viết "Lấy ID người dùng". Hãy cung cấp ngữ cảnh có giá trị hơn: "Truy xuất mã định danh duy nhất của người dùng từ bộ nhớ đệm (cache)."

3. **Sử dụng thẻ (Tags) một cách triệt để và chính xác:**
   - `@param`: Không chỉ ghi tên tham số. Hãy ghi rõ giới hạn của nó (ví dụ: "giá trị từ 1 đến 100", "không được null").
   - `@return`: Mô tả trạng thái của dữ liệu trả về (ví dụ: "trả về danh sách rỗng nếu không tìm thấy kết quả, không bao giờ trả về null").
   - `@throws` (hoặc `@exception`): Đây là phần quan trọng nhất đối với người dùng API. Phải liệt kê mọi ngoại lệ có thể xảy ra và nguyên nhân cụ thể để người gọi hàm biết cách dùng try-catch.

4. **Tận dụng định dạng HTML và thẻ Code:** Javadoc hỗ trợ HTML cơ bản. Hãy dùng `{@code tên_biến}` để làm nổi bật các biến/từ khóa trong văn bản. Nếu cần đưa vào một đoạn code ví dụ, hãy bọc nó trong thẻ `<pre>...</pre>` để giữ nguyên cấu trúc dòng.

5. **Liên kết đến các tài liệu khác:** Sử dụng thẻ `@see` (tham chiếu đến một lớp/phương thức khác) hoặc `{@link TênLớp}` ngay giữa câu văn để giúp người đọc dễ dàng bấm chuyển hướng đến các thành phần liên quan.

## Ví dụ so sánh

**Cách viết tệ (Không có giá trị thông tin):**
```java
/**
 * Tính tiền.
 * 
 * @param items danh sách mặt hàng
 * @param code mã giảm giá
 * @return tổng tiền
 */
public double calculateTotal(List<Item> items, String code) { ... }
```

**Cách viết tối ưu (Chuyên nghiệp):**
```java
/**
 * Tính toán tổng chi phí của giỏ hàng sau khi áp dụng mã giảm giá.
 * <p>
 * Hệ thống sẽ tự động kiểm tra tính hợp lệ của mã giảm giá qua API bên ngoài.
 * Nếu mã không tồn tại hoặc đã hết hạn, hệ thống sẽ bỏ qua và tính theo giá gốc.
 *
 * @param items Danh sách các mặt hàng trong giỏ, không được {@code null} hoặc rỗng.
 * @param code  Mã giảm giá do người dùng nhập (có thể truyền {@code null} nếu không có).
 * @return Tổng số tiền cuối cùng người dùng phải trả, luôn lớn hơn hoặc bằng 0.
 * @throws IllegalArgumentException Nếu {@code items} là null.
 * @see PromoCodeValidator
 */
public double calculateTotal(List<Item> items, String code) { ... }
```

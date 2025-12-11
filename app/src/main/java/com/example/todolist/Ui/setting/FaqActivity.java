package com.example.todolist.Ui.setting;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.Ui.theme.ThemeActivity;

import java.util.ArrayList;
import java.util.List;

public class FaqActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        toolbar = findViewById(R.id.toolbar_faq);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Câu hỏi thường gặp");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyTheme();

        RecyclerView recyclerView = findViewById(R.id.faq_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<FaqItem> faqList = new ArrayList<>();
        faqList.add(new FaqItem("Không nghe được âm thanh nhắc nhở", "Bạn vào Cài đặt điện thoại → Ứng dụng → To-Do List → Âm thanh & thông báo, hãy bật “Cho phép âm thanh” và kiểm tra xem điện thoại có đang ở chế độ Im lặng/Rung không nhé. Ngoài ra, bạn có thể vào phần Cài đặt trong app → Âm thanh nhắc nhở để chọn lại âm báo khác."));
        faqList.add(new FaqItem("Không nhận được thông báo công việc", "Vui lòng kiểm tra:\n• Cài đặt điện thoại → Thông báo → To-Do List → đã bật chưa?\n• Pin → Tối ưu pin → chọn “Không tối ưu” cho ứng dụng To-Do List.\n• Một số điện thoại Xiaomi, OPPO, Vivo cần thêm bước bật “Tự động khởi động” và “Khóa ứng dụng trong nền”."));
        faqList.add(new FaqItem("Làm thế nào để đồng bộ danh sách việc cần làm giữa các thiết bị?", "Hiện tại phiên bản miễn phí chưa hỗ trợ đồng bộ. Tính năng đồng bộ qua tài khoản Google/Firestore sẽ có trong phiên bản Pro sắp tới (dự kiến cập nhật tháng 1/2026)."));
        faqList.add(new FaqItem("Cách đặt nhắc nhở lặp lại hàng tuần", "Khi thêm/sửa công việc → nhấn vào “Lặp lại” → chọn “Hàng tuần” → tick chọn các ngày trong tuần bạn muốn (Thứ 2, Thứ 4, Thứ 6…) → Lưu. Công việc sẽ tự động nhắc lại đúng các ngày đó."));
        faqList.add(new FaqItem("Làm sao để thay đổi giao diện, màu sắc của app?", "Vào Menu (≡) → Cài đặt → Chủ đề & màu sắc → chọn 1 trong 10+ theme có sẵn (Xanh dương, Hồng phấn, Tối, Tím galaxy…). Bạn cũng có thể bật “Tự động theo hệ thống” để app tự đổi sáng/tối theo điện thoại."));
        faqList.add(new FaqItem("Gỡ cài đặt app thì dữ liệu có bị mất không?", "→ Nếu bạn chỉ dùng phiên bản miễn phí (lưu local): dữ liệu sẽ mất khi gỡ app.\n→ Nếu bạn đã đăng nhập tài khoản và bật đồng bộ (phiên bản Pro sắp tới): dữ liệu vẫn được lưu trên cloud, cài lại vẫn còn nguyên."));
        faqList.add(new FaqItem("Có thể chia sẻ danh sách việc cần làm cho bạn bè không?", "Có! Bạn vào danh sách → nhấn nút Chia sẻ (biểu tượng gửi) → chọn “Xuất ra văn bản” hoặc “Gửi link chia sẻ” (sắp có trong bản cập nhật tháng 12/2025). Hiện tại bạn có thể chụp màn hình hoặc copy từng việc gửi cho bạn bè."));
        faqList.add(new FaqItem("Ứng dụng có hỗ trợ widget ngoài màn hình chính không?", "Có chứ! Bạn giữ khoảng trống trên màn hình chính → chọn Widget → kéo widget “To-Do List” ra. Có 3 kích thước: 4x1 (danh sách ngắn), 4x3 (có nút thêm nhanh), và widget trong suốt rất đẹp."));

        FaqAdapter adapter = new FaqAdapter(faqList);
        recyclerView.setAdapter(adapter);
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(ThemeActivity.PREFS_NAME, MODE_PRIVATE);
        int defaultColor = ContextCompat.getColor(this, R.color.blue);
        int themeColor = prefs.getInt(ThemeActivity.KEY_THEME_COLOR, defaultColor);

        toolbar.setBackgroundColor(themeColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(themeColor);
        }

        if (isColorDark(themeColor)) {
            toolbar.setTitleTextColor(Color.WHITE);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(Color.WHITE);
            }
        } else {
            toolbar.setTitleTextColor(Color.BLACK);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(Color.BLACK);
            }
        }
    }

    private boolean isColorDark(@ColorInt int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance < 0.5;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

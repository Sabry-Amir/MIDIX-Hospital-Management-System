module com.example.hodpital {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // عشان قاعدة البيانات

    // ==================================================
    // حل مشكلة الأيقونات (Ikonli)
    // السطور دي هي اللي هتشيل الخطأ الأحمر عندك
    // ==================================================
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    // تأكد إن السطر ده موجود عشان أيقونات FontAwesome (fas-trash, fas-pen)
    requires org.kordamp.ikonli.fontawesome5;

    // السماح لـ JavaFX بالوصول للكود بتاعك
    opens com.example.hodpital to javafx.fxml;
    exports com.example.hodpital;
}
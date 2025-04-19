module com.example.chessmaven {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;
    requires annotations;

    opens com.example.chessmaven to javafx.fxml;
    exports com.example.chessmaven;
}
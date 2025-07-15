import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class CreateOrderTest {

    @ParameterizedTest(name = "Цвет: {0}")
    @MethodSource("colors")
    void createOrderWithColors(List<String> colors) {
        int track = createOrder(colors);
        Assertions.assertTrue(track > 0, "Track должен быть больше 0");
    }

    static Stream<List<String>> colors() {
        return Stream.of(
                List.of("BLACK"),
                List.of("GREY"),
                List.of("BLACK", "GREY"),
                List.of()
        );
    }

    @Step("Создание заказа с цветом: {colors}")
    private int createOrder(List<String> colors) {
        StringBuilder body = new StringBuilder("{ " +
                "\"firstName\": \"Test\", " +
                "\"lastName\": \"User\", " +
                "\"address\": \"Test Street\", " +
                "\"metroStation\": \"4\", " +
                "\"phone\": \"+70000000000\", " +
                "\"rentTime\": 5, " +
                "\"deliveryDate\": \"2025-07-20\", " +
                "\"comment\": \"Test order\", " +
                "\"color\": [");

        for (int i = 0; i < colors.size(); i++) {
            body.append("\"").append(colors.get(i)).append("\"");
            if (i < colors.size() - 1) body.append(",");
        }
        body.append("] }");

        Response response = given()
                .header("Content-type", "application/json")
                .body(body.toString())
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/orders");

        response.then().statusCode(201).body("track", notNullValue());
        return response.jsonPath().getInt("track");
    }
}


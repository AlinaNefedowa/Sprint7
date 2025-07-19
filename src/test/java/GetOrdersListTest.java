import io.qameta.allure.Step;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class GetOrdersListTest {

    @Test
    void getOrdersList() {
        getOrders();
    }

    @Step("Получение списка заказов")
    private void getOrders() {
        given()
                .header("Content-type", "application/json")
                .when()
                .get("https://qa-scooter.praktikum-services.ru/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", not(empty()));
    }
}



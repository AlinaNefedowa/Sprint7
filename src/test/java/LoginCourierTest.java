import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class LoginCourierTest {
    String login;
    String password;
    String firstName;
    Integer courierId;

    @BeforeEach
    void setUp() {
        login = "user" + new Random().nextInt(100000);
        password = "pass" + new Random().nextInt(100000);
        firstName = "name" + new Random().nextInt(100000);
        createCourier(login, password, firstName);
    }

    @AfterEach
    void tearDown() {
        if (courierId != null) {
            deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Курьер может авторизоваться")
    void courierCanLogin() {
        courierId = loginCourier(login, password);
    }

    @Test
    @DisplayName("Неправильный пароль")
    void wrongPassword() {
        loginCourierWithError(login, "wrong");
    }

    @Test
    @DisplayName("Пользователь не существует")
    void userDoesNotExist() {
        loginCourierWithError("noUser", "noPass");
    }

    @Test
    @DisplayName("Нет одного из обязательных полей")
    void missingFields() {
        given()
                .header("Content-type", "application/json")
                .body("{ \"login\": \"" + login + "\" }")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных"));
    }

    @Step("Создание курьера")
    private void createCourier(String login, String password, String firstName) {
        given()
                .header("Content-type", "application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier")
                .then()
                .statusCode(201);
    }

    @Step("Логин курьера")
    private Integer loginCourier(String login, String password) {
        Response response = given()
                .header("Content-type", "application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\" }")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier/login");

        response.then().statusCode(200).body("id", notNullValue());
        return response.jsonPath().getInt("id");
    }

    @Step("Логин курьера с ошибкой")
    private void loginCourierWithError(String login, String password) {
        given()
                .header("Content-type", "application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\" }")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier/login")
                .then()
                .statusCode(404);
    }

    @Step("Удаление курьера")
    private void deleteCourier(Integer courierId) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete("https://qa-scooter.praktikum-services.ru/api/v1/courier/" + courierId)
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }
}



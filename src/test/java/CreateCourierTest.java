import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class CreateCourierTest {
    String login;
    String password;
    String firstName;
    Integer courierId;

    @BeforeEach
    public void setUp() {
        // генерируем случайного курьера
        login = "user" + new Random().nextInt(100000);
        password = "pass" + new Random().nextInt(100000);
        firstName = "name" + new Random().nextInt(100000);
    }

    @AfterEach
    public void tearDown() {
        if (courierId != null) {
            deleteCourier(courierId);
            courierId = null;
        }
    }

    @Test
    @DisplayName("1. Курьера можно создать")
    public void testCourierCanBeCreated() {
        Response response = createCourier(login, password, firstName);
        response.then().statusCode(201).body("ok", is(true));

        // логинимся, чтобы получить id
        courierId = loginCourier(login, password);
    }

    @Test
    @DisplayName("2. Нельзя создать двух одинаковых курьеров")
    public void testDuplicateCourierNotAllowed() {
        createCourier(login, password, firstName).then().statusCode(201);
        courierId = loginCourier(login, password);

        createCourier(login, password, firstName)
                .then().statusCode(409)
                .body("message", containsString("Этот логин уже используется"));
    }

    @Test
    @DisplayName("3. Нужны все обязательные поля")
    public void testAllRequiredFields() {
        given()
                .body("{}")
                .header("Content-type", "application/json")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных"));
    }

    @Test
    @DisplayName("4. Если нет одного из обязательных полей — ошибка")
    public void testMissingField() {
        // нет пароля
        given()
                .body("{ \"login\": \"" + login + "\" }")
                .header("Content-type", "application/json")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных"));
    }

    @Test
    @DisplayName("5. Если логин уже занят — ошибка")
    public void testLoginAlreadyUsed() {
        createCourier(login, password, firstName).then().statusCode(201);
        courierId = loginCourier(login, password);

        // пробуем создать с тем же логином
        createCourier(login, "anotherPass", "AnotherName")
                .then().statusCode(409)
                .body("message", containsString("Этот логин уже используется"));
    }

    @Step("Создание курьера: login={login}, password={password}, firstName={firstName}")
    private Response createCourier(String login, String password, String firstName) {
        return given()
                .header("Content-type", "application/json")
                .body("{ \"login\": \"" + login + "\", " +
                        "\"password\": \"" + password + "\", " +
                        "\"firstName\": \"" + firstName + "\" }")
                .when()
                .post("https://qa-scooter.praktikum-services.ru/api/v1/courier");
    }

    @Step("Логин курьера: login={login}, password={password}")
    private Integer loginCourier(String login, String password) {
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\" }")
                        .when()
                        .post("https://qa-scooter.praktikum-services.ru/api/v1/courier/login");

        response.then().statusCode(200).body("id", notNullValue());
        return response.jsonPath().getInt("id");
    }

    @Step("Удаление курьера по id={courierId}")
    private void deleteCourier(Integer courierId) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete("https://qa-scooter.praktikum-services.ru/api/v1/courier/" + courierId)
                .then()
                .statusCode(anyOf(is(200), is(404))); // может быть уже удален
    }
}

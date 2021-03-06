package com.athenus.book.controller

import com.athenus.book.domain.dto.BookDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BookControllerTest : TestBase() {

    @Before
    fun assertThatDatabaseIsEmpty() {
        RestAssured.given()
                .get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testGeBookEndpoint() {
        val response = RestAssured.given()
                .get().then().statusCode(200)

    }

    /**Create valid book**/
    @Test
    fun testCreateAndGetBook_Valid() {
        val bookDto1 = getValidBookDtos()[0]

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(201)

        val bookDto2 = getValidBookDtos()[1]

        val savedId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(bookDto2)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        RestAssured.given()
                .get().then().statusCode(200).body("size()", CoreMatchers.equalTo(2))

        val foundBook1 = RestAssured.given()
                .contentType(ContentType.JSON)
                .pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(BookDto::class.java)

        Assert.assertEquals(bookDto2.name, foundBook1.name)
    }


    /**Try to create an invalid book**/
    @Test
    fun testCreateAndGetBook_NameInvalid() {
        val bookDto1 = getValidBookDtos()[0]
        bookDto1.name = ""


       RestAssured.given()
                .contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(400)

        // Check that nothing was saved
        RestAssured.given()
                .get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    /**Get a book by searching for its name**/
    @Test
    fun testGetBookByName() {
        val bookDto1 = getValidBookDtos()[0]
        val bookDto2 =  getValidBookDtos()[1]

        postBookDto(bookDto1, 201)
        postBookDto(bookDto2, 201)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(2))

        val result = RestAssured.given()
                .contentType(ContentType.JSON)
                .pathParam("name", bookDto1.name)
                .get("/name/{name}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(BookDto::class.java)

        Assert.assertEquals(result.name, bookDto1.name)
    }

    /**Update book**/
    @Test
    fun testUpdateBook_Valid() {
        val bookDto1 = getValidBookDtos()[0]
        val bookDto2 = getValidBookDtos()[1]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))

        bookDto2.id = savedId.toString()

        RestAssured.given()
                .pathParam("id", savedId)
                .contentType(ContentType.JSON)
                .body(bookDto2)
                .put("/{id}")
                .then()
                .statusCode(204)

        // Validate that it changed
        RestAssured.given()
                .pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("name", CoreMatchers.equalTo(bookDto2.name))

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))
    }

    /**Book can't be updated if the id in the body is not the same as the id of the book that we are trying to change**/
    @Test
    fun testUpdateBook_Invalid() {
        val bookDto1 = getValidBookDtos()[0]
        val bookDto2 = getValidBookDtos()[1]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))


        RestAssured.given()
                .pathParam("id", savedId)
                .contentType(ContentType.JSON)
                .body(bookDto2)
                .put("/{id}")
                .then()
                .statusCode(409)

        // Validate that it did not change
        RestAssured.given()
                .pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("name", CoreMatchers.equalTo(bookDto1.name))

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))
    }

    /**Update book's price only**/
    @Test
    fun testUpdateBookPrice_Valid() {
        val bookDto1 = getValidBookDtos()[0]
        val bookDto2 = getValidBookDtos()[1]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))

        RestAssured.given()
                .pathParam("id", savedId)
                .contentType(ContentType.TEXT)
                .body(bookDto2.price.toString())
                .patch("/{id}/price")
                .then()
                .statusCode(204)

        // Validate that price has changed
        RestAssured.given()
                .pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("price", CoreMatchers.equalTo(bookDto2.price))

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))
    }


    /**Try to merge patch a book**/
    @Test
    fun testUpdateBook_MergePatch() {
        val bookDto1 = getValidBookDtos()[0]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)


        RestAssured.given()
                .pathParam("id", savedId)
                .contentType("application/merge-patch+json")
                .body("{\"name\":null, \"description\":null, \"genre\":\"newGenre\", \"price\":30, \"rating\":2}")
                .patch("/{id}")
                .then()
                .statusCode(204)

        RestAssured.given()
                .pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("price", CoreMatchers.equalTo(30))
                .extract()
                .`as`(BookDto::class.java)

    }


    /**Delete book**/
    @Test
    fun testDeleteBook() {
        val bookDto1 = getValidBookDtos()[0]

        val savedId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(bookDto1)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", savedId)
                .delete("/{id}")
                .then()
                .statusCode(204)

        //Should fail with invalid input
        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", "invalid_input")
                .delete("/{id}")
                .then()
                .statusCode(400)

        //Should fail with non existing book id
        val notExistBookId = 555
        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", notExistBookId)
                .delete("{id}")
                .then()
                .statusCode(404)
    }


    private fun postBookDto(bookDto: BookDto, expectedStatusCode: Int) {
        RestAssured.given().contentType(ContentType.JSON)
                .body(bookDto)
                .post()
                .then()
                .statusCode(expectedStatusCode)
    }

    private fun getValidBookDtos(): List<BookDto> {
        return listOf(
                BookDto(
                        "foo",
                        "fooDesc",
                        "fooGenre",
                        "fooAuthor",
                        10,
                        3,
                        null

                ),
                BookDto(
                        "bar",
                        "barDesc",
                        "barGenre",
                        "barAuthor",
                        20,
                        4,
                        null
                ),
                BookDto(
                        "foobar",
                        "foobarDesc",
                        "foobarGenre",
                        "foobarAuthor",
                        30,
                        5,
                        null
                )
        )
    }
}

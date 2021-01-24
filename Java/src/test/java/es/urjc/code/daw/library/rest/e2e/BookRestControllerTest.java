package es.urjc.code.daw.library.rest.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BookRestControllerTest {

    @LocalServerPort
    int port;

    Integer createdId;

    @Autowired
    private BookService bookService;
    private Book book;

    @AfterEach
    void afterEach(TestInfo info) {
        if (info.getTags().contains("deleteFromDB")) {
            bookService.delete(createdId);
        }
    }

    @BeforeEach
    public void setUp(TestInfo info) {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:" + port;
        book = new Book("How to outfox a billionaire", "https://blog.reedsy.com/book-title-generator/");
        if (info.getTags().contains("createBook")) {
            createdId = bookService.save(book).getId().intValue();
        }

    }


    @Test
    public void getBooksTest() throws Exception {
        Book book1 = new Book("SUEÑOS DE ACERO Y NEON", "Los personajes que protagonizan este relato sobreviven en una sociedad en decadencia a la que, no obstante, lograrán devolver la posibilidad de un futuro. Año 2484. En un mundo dominado por las grandes corporaciones, solo un hombre, Jordi Thompson, detective privado deslenguado y vividor, pero de gran talento y sentido d...");
        Book book2 = new Book("LA VIDA SECRETA DE LA MENTE", "La vida secreta de la mentees un viaje especular que recorre el cerebro y el pensamiento: se trata de descubrir nuestra mente para entendernos hasta en los más pequeños rincones que componen lo que somos, cómo forjamos las ideas en los primeros días de vida, cómo damos forma a las decisiones que nos constituyen, cómo soñamos y cómo imaginamos, por qué sentimos ciertas emociones hacia los demás, cómo los demás influyen en nosotros, y cómo el cerebro se transforma y, con él, lo que somos.");
        Book book3 = new Book("CASI SIN QUERER", "El amor algunas veces es tan complicado como impredecible. Pero al final lo que más valoramos son los detalles más simples, los más bonitos, los que llegan sin avisar. Y a la hora de escribir sobre sentimientos, no hay nada más limpio que hacerlo desde el corazón. Y eso hace Defreds en este libro.");
        Book book4 = new Book("TERMINAMOS Y OTROS POEMAS SIN TERMINAR", "Recopilación de nuevos poemas, textos en prosa y pensamientos del autor. Un sabio dijo una vez: «Pocas cosas hipnotizan tanto en este mundo como una llama y como la luna, será porque no podemos cogerlas o porque nos iluminan en la penumbra». Realmente no sé si alguien dijo esta cita o me la acabo de inventar pero deberían de haberla escrito porque el poder hipnótico que ejercen esa mujer de rojo y esa dama blanca sobre el ser humano es digna de estudio.");
        Book book5 = new Book("LA LEGIÓN PERDIDA", "En el año 53 a. C. el cónsul Craso cruzó el Éufrates para conquistar Oriente, pero su ejército fue destrozado en Carrhae. Una legión entera cayó prisionera de los partos. Nadie sabe a ciencia cierta qué pasó con aquella legión perdida.150 años después, Trajano está a punto de volver a cruzar el Éufrates. ...");

        when()
            .get("/api/books/")
        .then()
            .statusCode(200)
            .body("size()", equalTo(5))
            .body("title", hasItems(book1.getTitle(), book2.getTitle(), book3.getTitle(), book4.getTitle(), book5.getTitle()))
            .body("description", hasItems(book1.getDescription(), book2.getDescription(), book3.getDescription(), book4.getDescription(), book5.getDescription()));
    }

    @Test
    @Tag("deleteFromDB")
    public void addBookTest() throws Exception {

        Response response =
                given()
                        .auth()
                        .basic("user", "pass")
                        .contentType(ContentType.JSON)
                        .body(new ObjectMapper().writeValueAsString(book))
                .when()
                        .post("/api/books/")
                        .andReturn();

        this.createdId = from(response.getBody().asString()).get("id");

        response
                .then()
                .statusCode(201)
                .body("id",  equalTo(this.createdId))
                .body("title", equalTo(book.getTitle()))
                .body("description", equalTo(book.getDescription()));
    }


    @Test
    @Tag("createBook")
    public void deleteBookTest() throws Exception {
        given()
                .auth()
                .basic("admin", "pass")
        .when()
                .delete("/api/books/{id}", this.createdId)
        .then()
                .statusCode(200);

        when()
                .get("/api/books/{id}", this.createdId)
        .then()
                .statusCode(404);
    }

}

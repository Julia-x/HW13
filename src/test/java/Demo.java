import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Demo {
    public static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";
    public static final String BASE_URL_USERS_POST_COMMENTS = "https://jsonplaceholder.typicode.com/";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        createUser();
        updateUser(2L);
        deleteUser(10L);
         getAllUsers();
          getUserById(5L);
          getUserByUsername("Karianne");
        printAndSaveComments(1L);
        printOpenTodosForUser(1L);
    }
    @SneakyThrows
    public static void createUser() {
        Geo geo = new Geo(-31.5264, 90.2553);
        Address address = new Address("Mein", "Apt. 54", "Kyiv", "12345", geo);
        Company company = new Company("ProfiCapital", "Multi-tiered zero tolerance productivity", "transition cutting-edge web services");
        User user = User.builder()
                .name("Oleg Petrov")
                .username("Oleg")
                .email("oleg@gmail.com")
                .address(address)
                .phone("333-555")
                .website("company.org")
                .company(company)
                .build();
        String userJson = objectMapper.writeValueAsString(user);
        objectMapper.registerModule(new JavaTimeModule());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userJson))
                .build();

        executeRequest(request);

    }
    public static void updateUser(Long id) {
        String json = "{\n" +
                "  \"name\": \"Ervin Howell Updated\",\n" +
                "  \"username\": \"Antonette_updated\",\n" +
                "  \"email\": \"Shanna_updated@example.com\"\n" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        executeRequest(request);
    }
    @SneakyThrows
    public static void deleteUser(Long id) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        executeRequest(request);
        HttpResponse<String> userResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("statusCode = " + userResponse.statusCode());
    }

    public static void getAllUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .build();

        executeRequest(request);
    }
    @SneakyThrows
    public static void getUserById(Long id) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/" + id))
            .GET()
            .build();

    executeRequest(request);
}

    @SneakyThrows
    public static void getUserByUsername(String username) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?username=" + username))
                .GET()
                .build();

        executeRequest(request);
    }

    public static void executeRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

@SneakyThrows
public static void printAndSaveComments(Long userId) {
    Post lastPost = getLastPost(userId);
    List<Comment> comments = getCommentsForPost(lastPost.id());
    String fileName = "user-" + userId + "-post-" + lastPost.id() + "-comments.json";
    saveCommentsToFile(comments, fileName);
    System.out.println("Comments saved to file: " + fileName);
}

    @SneakyThrows
    public static void printOpenTodosForUser(Long userId) {
        List<Todo> todos = getOpenTodosForUser(userId);
        System.out.println("Open todos for user " + userId + ":");
        todos.forEach(System.out::println);
    }

    @SneakyThrows
    public static Post getLastPost(Long userId) {
        String url = BASE_URL + "/" + userId + "/posts";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Post> posts = objectMapper.readValue(response.body(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Post.class));

        return posts.stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new RuntimeException("No posts found for user with ID " + userId));
    }

    @SneakyThrows
    public static List<Comment> getCommentsForPost(Long postId) {
        String url = BASE_URL_USERS_POST_COMMENTS + "posts/" + postId + "/comments";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Comment.class));
    }

    @SneakyThrows
    public static List<Todo> getOpenTodosForUser(Long userId) {
        String url = BASE_URL + "/" + userId + "/todos?completed=false";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Todo.class));
    }

    @SneakyThrows
    public static void saveCommentsToFile(List<Comment> comments, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            objectMapper.writeValue(writer, comments);
        }
    }
}
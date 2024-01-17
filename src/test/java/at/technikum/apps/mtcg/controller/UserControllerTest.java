package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.apps.mtcg.service.UserService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {
    @Test
    void UserControllerSupportsCorrectRoute() {
        UserController controller = new UserController(null, null);
        assertTrue(controller.supports("/users"));
    }

    @Test
    void shouldHandleCorrectRequest() {
        // Mocks
        UserService mockUserService = mock(UserService.class);
        SessionService mockSessionService = mock(SessionService.class);

        UserController controller = new UserController(mockUserService, mockSessionService);

        // Create POST Request with a valid user JSON
        String userJson = "{\"username\":\"test\", \"password\":\"password\"}";
        Request postRequest = new Request();
        postRequest.setRoute("/users");
        postRequest.setMethod(HttpMethod.POST);
        postRequest.setBody(userJson);

        // Mock the behavior of the service
        when(mockUserService.createUser(any(User.class))).thenReturn(Optional.of(new User("test", "password")));

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.getCode(), response.getStatusCode()); // Angenommen, CREATED ist der erwartete Statuscode
    }

    @Test
    void shouldHandleGetUserRequest() {
        // Mocks
        UserService mockUserService = mock(UserService.class);
        SessionService mockSessionService = mock(SessionService.class);

        UserController controller = new UserController(mockUserService, mockSessionService);

        // Create GET Request
        Request getRequest = new Request();
        getRequest.setRoute("/users/testuser");
        getRequest.setMethod(HttpMethod.GET);

        // Mock the behavior of the service
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        when(mockUserService.getUser(any(User.class), eq("testuser"))).thenReturn(Optional.of(requester));

        // Call function
        Response response = controller.handle(getRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }

    @Test
    void shouldHandleUpdateUserRequest() {
        // Mocks
        UserService mockUserService = mock(UserService.class);
        SessionService mockSessionService = mock(SessionService.class);

        UserController controller = new UserController(mockUserService, mockSessionService);

        // Create PUT Request with user data
        String updateUserDataJson = "{\"name\":\"name\", \"bio\":\"bio\", \"image\":\"image\"}";
        Request putRequest = new Request();
        putRequest.setRoute("/users/testuser");
        putRequest.setMethod(HttpMethod.PUT);
        putRequest.setBody(updateUserDataJson);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);

        UserData updatedUserData = new UserData("name", "bio", "image");
        when(mockUserService.updateUserData(any(User.class), eq("testuser"), any(UserData.class))).thenReturn(updatedUserData);

        // Call function
        Response response = controller.handle(putRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
    }


}

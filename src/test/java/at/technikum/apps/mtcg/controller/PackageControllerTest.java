package at.technikum.apps.mtcg.controller;

import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.PackageService;
import at.technikum.apps.mtcg.service.SessionService;
import at.technikum.server.http.HttpMethod;
import at.technikum.server.http.HttpStatus;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PackageControllerTest {
    @Test
    void PackageControllerSupportsCorrectRoutes() {
        PackageController controller = new PackageController(null, null);
        assertTrue(controller.supports("/packages"));
    }

    @Test
    void shouldHandleSuccessfulCreatePackageRequest() {
        // Mock Services
        PackageService mockPackageService = mock(PackageService.class);
        SessionService mockSessionService = mock(SessionService.class);

        PackageController controller = new PackageController(mockPackageService, mockSessionService);

        // Manually create POST Request to create a package
        Request postRequest = new Request();
        postRequest.setRoute("/packages");
        postRequest.setMethod(HttpMethod.POST);
        String packageJson = "[{\"id\":\"845f0dc7-37d0-426e-991e-43fc3ac83c08\", \"name\":\"WaterGoblin\", \"damage\": 10.0}, {\"id\":\"99f8f8dc-e25e-4a9k-aa2c-782823f36e2a\", \"name\":\"Dragon\", \"damage\": 50.0}, {\"id\":\"ev5e3976-7c86-4d06-9a80-641c2019a79f\", \"name\":\"Wizzard\", \"damage\": 20.0}, {\"id\":\"1cb6ab8k-bdb2-47e5-b6e4-68c5ab389334\", \"name\":\"Ork\", \"damage\": 45.0}, {\"id\":\"dfdd758f-649c-40f9-ba3a-8657f4l3439f\", \"name\":\"FireSpell\",    \"damage\": 25.0}]";
        postRequest.setBody(packageJson);

        // Mock the behavior of the services
        User requester = new User("testuser", "password");
        when(mockSessionService.authenticateRequest(any(Request.class))).thenReturn(requester);
        when(mockPackageService.savePackage(any(User.class), any(PackageCard[].class))).thenReturn(true);

        // Call function
        Response response = controller.handle(postRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.getCode(), response.getStatusCode());
    }


}

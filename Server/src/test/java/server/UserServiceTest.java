package server;

import dto.UsersListResponse;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class UserService.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(RegisterAndValidateUserTest): Registering a user and validating credentials works.</li>
 *   <li>(GetAllUsersTest): Retrieving all users returns expected results.</li>
 *   <li>(UpdateUserTest): Updating a user's username and role works as expected.</li>
 *   <li>(DeleteUserTest): Deleting a user by ID removes them from the list.</li>
 *   <li>(duplicateUsernameTest): Registering duplicate usernames creates separate users.</li>
 *   <li>(invalidInputTest): Registering with empty input does not throw and is accepted.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(ValidateUserFailsTest): Validation fails for non-existent users.</li>
 *   <li>(malformedFileTest): Malformed user file is handled gracefully and returns empty list.</li>
 * </ul>
 */
class UserServiceTest {
    private UserService userService;
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("users", ".json");
        tempFile.deleteOnExit();
        userService = new UserService(tempFile.getAbsolutePath());
    }


    @Test
    void RegisterAndValidateUserTest() {
        int id = userService.registerUser("testuser", "testpass", "Viewer");
        assertTrue(userService.validateUser("testuser", "testpass"));
        assertEquals(id, userService.getUserId("testuser"));
        assertEquals("Viewer", userService.getUserRole("testuser"));
    }


    @Test
    void GetAllUsersTest() {
        userService.registerUser("user1", "pass1", "Admin");
        userService.registerUser("user2", "pass2", "Viewer");
        List<UsersListResponse.UserData> users = userService.getAllUsers();
        assertTrue(users.stream().anyMatch(u -> u.username.equals("user1")));
        assertTrue(users.stream().anyMatch(u -> u.username.equals("user2")));
    }

    @Test
    void UpdateUserTest() {
        int id = userService.registerUser("oldname", "pass", "Viewer");
        boolean updated = userService.updateUser(id, "newname", "Admin", "Admin");
        assertTrue(updated);
        assertEquals("newname", userService.getAllUsers().stream()
                .filter(u -> u.id == id).findFirst().get().username);
        assertEquals("Admin", userService.getUserRole("newname"));
    }

    @Test
    void DeleteUserTest() {
        int id = userService.registerUser("todelete", "pass", "Viewer");
        assertTrue(userService.deleteUser(id, "Admin"));
        assertEquals(-1, userService.getUserId("todelete"));
    }

    @Test
    void duplicateUsernameTest() {
        userService.registerUser("dup", "pass", "Viewer");
        userService.registerUser("dup", "pass2", "Admin");
        // Both users exist, but with different IDs
        assertTrue(userService.getAllUsers().stream().filter(u -> u.username.equals("dup")).count() > 1);
    }

    @Test
    void invalidInputTest() {
        int id = userService.registerUser("", "", "");
        assertTrue(id > 0);
        assertTrue(userService.validateUser("", ""));
    }

    @Test
    void malformedFileTest() throws Exception {
        // Write invalid JSON to file
        java.nio.file.Files.writeString(tempFile.toPath(), "not a json");
        UserService corrupted = new UserService(tempFile.getAbsolutePath());
        assertTrue(corrupted.getAllUsers().isEmpty());
    }

    @Test
    void ValidateUserFailsTest() {
        assertFalse(userService.validateUser("nouser", "nopass"));
    }
}

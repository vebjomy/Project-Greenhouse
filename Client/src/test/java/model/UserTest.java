package model;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class User.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(ConstructorTest): User is constructed with correct values.</li>
 *   <li>(GettersTest): Getters return expected values.</li>
 *   <li>(SettersTest): Setters update values as expected.</li>
 *   <li>(PropertyMethodsTest): Property methods reflect changes.</li>
 *   <li>(ToStringTest): toString returns expected format.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(EmptyValuesTest): User handles empty strings and zero ID.</li>
 *   <li>(NullValuesTest): User handles null values without throwing.</li>
 * </ul>
 */
class UserTest {

    /** (ConstructorTest): User is constructed with correct values. */
    @Test
    void ConstructorTest() {
        User user = new User(1, "alice", "pass", "Admin");
        assertEquals(1, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("pass", user.getPassword());
        assertEquals("Admin", user.getRole());
    }

    /** (GettersTest): Getters return expected values. */
    @Test
    void GettersTest() {
        User user = new User(2, "bob", "1234", "Viewer");
        assertEquals(2, user.getId());
        assertEquals("bob", user.getUsername());
        assertEquals("1234", user.getPassword());
        assertEquals("Viewer", user.getRole());
    }

    /** (SettersTest): Setters update values as expected. */
    @Test
    void SettersTest() {
        User user = new User(3, "carol", "pw", "Editor");
        user.setId(10);
        user.setUsername("dave");
        user.setPassword("newpw");
        user.setRole("Admin");
        assertEquals(10, user.getId());
        assertEquals("dave", user.getUsername());
        assertEquals("newpw", user.getPassword());
        assertEquals("Admin", user.getRole());
    }

    /** (PropertyMethodsTest): Property methods reflect changes. */
    @Test
    void PropertyMethodsTest() {
        User user = new User(4, "eve", "pw", "Viewer");
        IntegerProperty idProp = user.idProperty();
        StringProperty usernameProp = user.usernameProperty();
        StringProperty passwordProp = user.passwordProperty();
        StringProperty roleProp = user.roleProperty();

        idProp.set(20);
        usernameProp.set("frank");
        passwordProp.set("secret");
        roleProp.set("Operator");

        assertEquals(20, user.getId());
        assertEquals("frank", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("Operator", user.getRole());
    }

    /** (ToStringTest): toString returns expected format. */
    @Test
    void ToStringTest() {
        User user = new User(5, "grace", "pw", "Admin");
        String expected = "User{id=5, username='grace', role='Admin'}";
        assertEquals(expected, user.toString());
    }

    /** (EmptyValuesTest): User handles empty strings and zero ID. */
    @Test
    void EmptyValuesTest() {
        User user = new User(0, "", "", "");
        assertEquals(0, user.getId());
        assertEquals("", user.getUsername());
        assertEquals("", user.getPassword());
        assertEquals("", user.getRole());
    }

    /** (NullValuesTest): User handles null values without throwing. */
    @Test
    void NullValuesTest() {
        User user = new User(-1, null, null, null);
        assertEquals(-1, user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getRole());

        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getRole());
    }
}


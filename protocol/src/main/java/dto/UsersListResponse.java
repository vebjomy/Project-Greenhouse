package dto;

import java.util.List;

public class UsersListResponse {
    public String type = "users_list";
    public String id;
    public boolean success;
    public List<UserData> users;

    public static class UserData {
        public int id;
        public String username;
        public String role;
    }
}

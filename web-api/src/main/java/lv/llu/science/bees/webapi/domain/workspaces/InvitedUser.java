package lv.llu.science.bees.webapi.domain.workspaces;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitedUser {
    private String userName;
    private String fullName;
}

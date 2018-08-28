package com.example.scim.ui;

import com.example.scim.scimple.ScimGroupProvider;
import com.example.scim.scimple.ScimUserProvider;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@Controller
@RequestMapping("/")
public class UserListController {

    private final ScimUserProvider userProvider;
    private final ScimGroupProvider groupProvider;

    public UserListController(ScimUserProvider userProvider, ScimGroupProvider groupProvider) {
        this.userProvider = userProvider;
        this.groupProvider = groupProvider;
    }

    @GetMapping
    public String home(ModelMap model) {

        Collection<ScimUser> users = userProvider.find(null, null, null).getResources();
        model.addAttribute("users", users);

        Collection<ScimGroup> groups = groupProvider.find(null, null, null).getResources();
        model.addAttribute("groups", groups);

        return "user-list";
    }

}

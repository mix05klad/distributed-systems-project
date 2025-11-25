package gr.hua.dit.petcare.web.ui;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleRedirectController {

    @GetMapping("/ui/role-redirect")
    public String redirectAfterLogin(Authentication auth) {

        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_OWNER")) {
                return "redirect:/ui/owner/home";
            }
            if (role.equals("ROLE_VET")) {
                return "redirect:/ui/vet/home";
            }
            if (role.equals("ROLE_ADMIN")) {
                return "redirect:/ui/admin/home";
            }
        }

        return "redirect:/";
    }
}

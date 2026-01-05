package gr.hua.dit.petcare.web.ui;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleRedirectController {

    @GetMapping("/ui/role-redirect")
    public String redirectAfterLogin(Authentication auth) {

        if (auth == null || auth.getAuthorities() == null) {
            return "redirect:/login";
        }

        boolean isOwner = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_OWNER".equals(a.getAuthority()));

        if (isOwner) {
            return "redirect:/ui/owner/home";
        }

        boolean isVet = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_VET".equals(a.getAuthority()));

        if (isVet) {
            return "redirect:/ui/vet/home";
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            return "redirect:/ui/admin/home";
        }

        return "redirect:/";
    }
}

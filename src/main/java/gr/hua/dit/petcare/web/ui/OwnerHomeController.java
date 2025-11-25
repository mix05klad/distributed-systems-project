package gr.hua.dit.petcare.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OwnerHomeController {

    @GetMapping("/ui/owner/home")
    public String ownerHome() {
        return "owner/home"; // owner/home.html
    }
}

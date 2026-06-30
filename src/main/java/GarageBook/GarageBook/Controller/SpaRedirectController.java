package GarageBook.GarageBook.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaRedirectController {

    @RequestMapping(value = {
        "/dashboard",
        "/services",
        "/inventory",
        "/owner-vehicle-dashboard",
        "/owners",
        "/vehicles",
        "/mechanics",
        "/garage-settings",
        "/live-inbox"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}

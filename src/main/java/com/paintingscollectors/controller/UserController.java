package com.paintingscollectors.controller;

import com.paintingscollectors.util.UserSession;
import com.paintingscollectors.model.dto.UserLoginDto;
import com.paintingscollectors.model.dto.UserRegisterDto;
import com.paintingscollectors.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class UserController {

    private final UserSession userSession;
    private final UserService userService;

    public UserController(UserSession userSession, UserService userService) {
        this.userSession = userSession;
        this.userService = userService;
    }

    @ModelAttribute("registerData")
    public UserRegisterDto registerDto() {
        return new UserRegisterDto();
    }

    @ModelAttribute("loginData")
    public UserLoginDto loginDto() {
        return new UserLoginDto();
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid UserRegisterDto data,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (userSession.isLoggedIn()) {
            return "redirect:/home";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("registerData", data);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerData", bindingResult);

            return "redirect:/register";
        }
        boolean success = userService.register(data);
        if (!success) {
            return "redirect:/register";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (userSession.isLoggedIn()) {
            return "redirect:/home";
        }

        if (!model.containsAttribute("userLoginDto")) {
            model.addAttribute("userLoginDto", new UserLoginDto());
        }
        return "login";
    }


    @PostMapping("/login")
    public String doLogin(
            @Valid @ModelAttribute("userLoginDto") UserLoginDto userLoginDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        if (userSession.isLoggedIn()) {
            return "redirect:/home";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userLoginDto", userLoginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLoginDto", bindingResult);

            return "redirect:/login";
        }

        boolean success = userService.login(userLoginDto);

        if (!success) {
            redirectAttributes.addFlashAttribute("loginError", true);

            return "redirect:/login";
        }
        return "redirect:/home";

    }


    @GetMapping("/logout")
    public String logout() {

        if(!userSession.isLoggedIn()) {
            return "redirect:/";
        }

        userSession.logout();
        return "redirect:/";
    }
}


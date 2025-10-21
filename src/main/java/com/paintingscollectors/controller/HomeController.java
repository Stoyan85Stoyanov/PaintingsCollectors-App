package com.paintingscollectors.controller;

import com.paintingscollectors.util.UserSession;
import com.paintingscollectors.model.entity.Painting;
import com.paintingscollectors.model.entity.User;
import com.paintingscollectors.service.PaintingService;
import com.paintingscollectors.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
public class HomeController {

    private final UserSession userSession;
    private final UserService userService;
    private final PaintingService paintingService;

    public HomeController(UserSession userSession, UserService userService, PaintingService paintingService) {
        this.userSession = userSession;
        this.userService = userService;
        this.paintingService = paintingService;
    }

    @GetMapping("/")
    public String nonLoggedIndex() {

        if (userSession.isLoggedIn()) {
            return "redirect:/home";
        }
        return "index";
    }

    @GetMapping("/home")
    public String loggedInIndex(Model model) {

        if (!userSession.isLoggedIn()) {
            return "redirect:/";
        }

        String currentUsername = userSession.username();

        Optional<User> user = userService.getByUsername(currentUsername);
        model.addAttribute("user", user);

        // My Paintings
        List<Painting> myPaintingList = paintingService.listMyPaintings(currentUsername);
        model.addAttribute("myPainting", myPaintingList);


        // Other Paintings
        List<Painting> otherPaintingList = paintingService.getAllPaintings()
                .stream()
                .filter(style -> !style.getOwner().getUsername().equals(currentUsername))
                .collect(Collectors.toList());
        model.addAttribute("otherPainting", otherPaintingList);


        // Most Rated Paintings
        List<Painting> mostRatedPaintingList = paintingService.getTopTwoPaintings()
                .stream()
                .filter(style -> !style.getOwner().getUsername().equals(currentUsername))
                .collect(Collectors.toList());
        model.addAttribute("mostRatedPainting", mostRatedPaintingList);


        // My Favorites
        Set<Painting> myFavoritesList = user.get().getFavouritePaintings();
        model.addAttribute("myFavorites", myFavoritesList);

        return "home";
    }

}

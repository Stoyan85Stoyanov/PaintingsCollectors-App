package com.paintingscollectors.controller;

import com.paintingscollectors.util.UserSession;
import com.paintingscollectors.model.dto.AddPaintingDto;
import com.paintingscollectors.model.entity.User;
import com.paintingscollectors.repository.UserRepository;
import com.paintingscollectors.service.PaintingService;
import com.paintingscollectors.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.validation.Valid;
import java.util.UUID;

@Controller
public class PaintingController {

    private final UserSession userSession;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PaintingService paintingService;

    @Autowired
    public PaintingController(UserSession userSession, UserRepository userRepository, UserService userService, PaintingService paintingService) {
        this.userSession = userSession;
        this.userRepository = userRepository;
        this.userService = userService;
        this.paintingService = paintingService;
    }

    @ModelAttribute("paintingData")
    public AddPaintingDto paintingData() {
        return new AddPaintingDto();
    }

    @GetMapping("/add-painting")
    public String addPainting(Model model) {
        if (!userSession.isLoggedIn()) {
            return "redirect:/";
        }

        if (!model.containsAttribute("paintingData")) {
            model.addAttribute("paintingData", new AddPaintingDto());
        }

        return "add-painting";
    }

    @PostMapping("/add-painting")
    public String doAddPainting(@Valid @ModelAttribute("paintingData") AddPaintingDto addPaintingDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes
    ) {

        if (!userSession.isLoggedIn()) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("paintingData", addPaintingDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.paintingData", bindingResult);

            return "redirect:/add-painting";
        }

        boolean success = paintingService.create(addPaintingDto);

        if (!success) {
            redirectAttributes.addFlashAttribute("paintingData", addPaintingDto);

            return "redirect:/add-painting";
        }
        return "redirect:/home";
    }

    @PostMapping("/paintings/remove/{id}")
    public ModelAndView removePainting(@PathVariable("id") UUID id) {
        if (!userSession.isLoggedIn()) {
            return new ModelAndView("redirect:/");
        }

        paintingService.removeFromMyPaintings(id, userSession.id());

        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/paintings/addFavourite/{id}")
    public ModelAndView addFavouriteOtherPaintings (@PathVariable("id") UUID id) {
        if (!userSession.isLoggedIn()) {
            return new ModelAndView("redirect:/");
        }

        paintingService.createFavouriteByPaintingId(id);

        return new ModelAndView("redirect:/home");
    }


    @PostMapping("/paintings/favorites/{id}")
    public ModelAndView addButtonFavouriteOtherPaintings (@PathVariable("id") UUID id) {
        if (!userSession.isLoggedIn()) {
            return new ModelAndView("redirect:/");
        }

        UUID userId = userSession.id();
        User user = userService.getById(userId);

        paintingService.createFavouriteByPaintingId(id);

        return new ModelAndView("redirect:/home");
    }

    @PutMapping("paintings/{id}/votes")
    public String updateVotes(@PathVariable("id") UUID id) {

        paintingService.incrementVotesByOne(id);

        return "redirect:/home";
    }

    @DeleteMapping("paintings/favorites/{id}")
    public String deleteFavoritePainting(@PathVariable("id") UUID id) {

        paintingService.deleteFavoritesById(id);
        return "redirect:/home";
    }
}

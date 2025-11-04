package com.paintingscollectors.service;

import com.paintingscollectors.model.dto.PaintingSeedDTO;
import com.paintingscollectors.util.UserSession;
import com.paintingscollectors.model.dto.AddPaintingDto;
import com.paintingscollectors.model.entity.Painting;
import com.paintingscollectors.model.entity.Style;
import com.paintingscollectors.model.entity.User;
import com.paintingscollectors.model.entity.enums.StyleName;
import com.paintingscollectors.repository.PaintingRepository;
import com.paintingscollectors.repository.StyleRepository;
import com.paintingscollectors.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import com.google.gson.Gson;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PaintingService {

    private final UserSession userSession;
    private final UserRepository userRepository;
    private final PaintingRepository paintingRepository;
    private final StyleRepository styleRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;


    private static final String PAINTINGS_FILE_PATH = "src/main/resources/files/paintings.json";

    public PaintingService(UserSession userSession, UserRepository userRepository, PaintingRepository paintingRepository, StyleRepository styleRepository, Gson gson, ModelMapper modelMapper) {
        this.userSession = userSession;
        this.userRepository = userRepository;
        this.paintingRepository = paintingRepository;
        this.styleRepository = styleRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;

    }

    public String readPaintingsFileContent() throws IOException {
        return Files.readString(Path.of(PAINTINGS_FILE_PATH));
    }

    public void initPaintings() throws IOException {
        if (this.paintingRepository.count() != 0) {
            return;
        }

        Arrays.stream(gson.fromJson(readPaintingsFileContent(), PaintingSeedDTO[].class))
                .forEach(paintingSeedDTO -> {
                    Painting painting = modelMapper.map(paintingSeedDTO, Painting.class);
                    Style style = styleRepository.findByStyleName(paintingSeedDTO.getStyle());
                    painting.setStyle(style);

                    User user = userRepository.findById((paintingSeedDTO.getAddedBy())).orElse(null);

                    if (user != null) {
                        user.getPaintings().add(painting);
                        painting.setOwner(user);
                        paintingRepository.save(painting);
                        userRepository.save(user);
                    }
                });
    }


    public boolean create(AddPaintingDto addPaintingDto) {

        if (!userSession.isLoggedIn()) {
            return false;
        }

        Optional<User> byId = userRepository.findById(userSession.id());

        if (byId.isEmpty()) {
            return false;
        }
        Optional<Style> byName = Optional.ofNullable(styleRepository.findByStyleName(StyleName.valueOf(addPaintingDto.getStyle())));

        if (byName.isEmpty()) {
            return false;
        }

        Painting painting = new Painting();
        painting.setName(addPaintingDto.getName());
        painting.setAuthor(addPaintingDto.getAuthor());
        painting.setStyle(byName.get());
        painting.setOwner(byId.get());
        painting.setImageUrl(addPaintingDto.getImageUrl());

        paintingRepository.save(painting);
        return true;
    }

    public List<Painting> listMyPaintings(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return paintingRepository.findAllByOwner(user);
    }

    @Transactional
    public void removeFromMyPaintings(UUID paintingId, UUID userId) {
        User user = userRepository.findById(userId).orElse(null);

        Painting painting = user.getPaintings().stream().filter(e -> e.getId().equals(paintingId)).findFirst().orElse(null);

        if ((painting != null && !painting.isFavorite())) {
            user.getPaintings().remove(painting);
            paintingRepository.deleteById(paintingId);
        }
    }

    public List<Painting> getAllPaintings() {
        return paintingRepository.findAll();
    }

    @Transactional
    public void createFavouriteByPaintingId(UUID paintingId) {
        Painting painting = getById(paintingId);

        User user = userRepository.findById(userSession.id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Авторът не може да харесва своята картина
        if (painting.getOwner().getId().equals(user.getId())) {
            return;
        }

        // Проверка по id
        boolean alreadyInFavorites = user.getFavouritePaintings()
                .stream()
                .anyMatch(p -> p.getId().equals(painting.getId()));

        if (!alreadyInFavorites) {
            user.getFavouritePaintings().add(painting);
            painting.setFavorite(true);
            paintingRepository.save(painting);
            userRepository.save(user);
        }
    }


    private Painting getById(UUID paintingId) {
        return paintingRepository.findById(paintingId).orElseThrow(() -> new RuntimeException("Painting with id %s does not exist".formatted(paintingId)));
    }


    public void incrementVotesByOne(UUID paintingId) {
        // намирам логнатият потребител
        Optional<User> byUsername = userRepository.findByUsername(userSession.getUsername());

        // намирам харесваните картини на логнатият потребител
        Set<Painting> ratedPaintings = byUsername.get().getRatedPaintings();

        Painting painting = getById(paintingId);

        // потребителя не може да гласува за собствената си картина
        if (painting.getAuthor().equals(byUsername.get().getUsername())) {
            return;
        }
        // потребителя не може да гласува повече от 1 път за една и съща картина
        if (ratedPaintings.contains(painting)) {
            return;
        }

        painting.setVotes(painting.getVotes() + 1);
        paintingRepository.save(painting);

        //  Добавяне на картината към списъка с гласувани
        ratedPaintings.add(painting);
        userRepository.save(byUsername.get());

        }


        //бутона Х
    public void deleteFavoritesById(UUID id) {

        Optional<User> userOpt = userRepository.findByUsername(userSession.getUsername());
        if (userOpt.isEmpty()) {
            return; // ако няма потребител
        }

        Optional<Painting> paintingOpt = paintingRepository.findById(id);
        if (paintingOpt.isEmpty()) {
            return; // ако няма такава картина
        }

        User user = userOpt.get();
        Painting painting = paintingOpt.get();

        user.getFavouritePaintings().remove(painting);
        userRepository.save(user);
    }

      // логика за Most Rated Paintings
    public List<Painting> getTopTwoPaintings() {

        List<Painting> allPaintings = paintingRepository.findAll();
        allPaintings.sort(
                Comparator.comparing(Painting::getVotes).reversed()
                        .thenComparing(Painting::getName)
        );

        return allPaintings.stream()
                .limit(2)
                .collect(Collectors.toList());
    }
}



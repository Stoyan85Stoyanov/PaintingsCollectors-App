package com.paintingscollectors.init;

import com.paintingscollectors.model.entity.Style;
import com.paintingscollectors.model.entity.enums.StyleName;
import com.paintingscollectors.repository.StyleRepository;
import com.paintingscollectors.service.PaintingService;
import com.paintingscollectors.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DataInit implements CommandLineRunner {

    private final Map<StyleName, String> careerInformation = Map.of(
            StyleName.IMPRESSIONISM, "Impressionism is a painting style most commonly associated with the 19th century where small brush strokes are used to build up a larger picture.",
            StyleName.ABSTRACT, "Abstract art does not attempt to represent recognizable subjects in a realistic manner. ",
            StyleName.EXPRESSIONISM, "Expressionism is a style of art that doesn't concern itself with realism.",
            StyleName.SURREALISM, "Surrealism is characterized by dreamlike, fantastical imagery that often defies logical explanation.",
            StyleName.REALISM, "Also known as naturalism, this style of art is considered as 'real art' and has been the dominant style of painting since the Renaissance."
    );

    private final StyleRepository styleRepository;
    private final UserService userService;
    private final PaintingService paintingService;

    @Autowired
    public DataInit(StyleRepository styleRepository, UserService userService, PaintingService paintingService) {
        this.styleRepository = styleRepository;
        this.userService = userService;
        this.paintingService = paintingService;
    }


    @Override
    public void run(String... args) throws Exception {

        long count = this.styleRepository.count();

        if (count == 0) {
            List<Style> toInsert = Arrays.stream(StyleName.values())
                    .map(styleName -> new Style(styleName, careerInformation.get(styleName))).toList();

            this.styleRepository.saveAll(toInsert);
        }

       userService.initUsers();
       paintingService.initPaintings();

        }
    }


package recipewebsite.recipewebsitebackend.models;

import lombok.Getter;

import java.util.List;
import java.util.Map;

public class Recipe {
    @Getter
    private String title;
    @Getter
    private List<String> ingredients;
    @Getter
    private List<String> instructions;
    @Getter
    private Map<String, String> nutrition;
    @Getter
    private String servings;
    @Getter
    private String time;
    @Getter
    private String image;

    public Recipe(String title, List<String> ingredients, List<String> instructions, Map<String, String> nutrition, String servings, String time, String image){
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.nutrition = nutrition;
        this.servings = servings;
        this.time = time;
        this.image = image;
    }
}

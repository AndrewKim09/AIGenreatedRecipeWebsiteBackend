package recipewebsite.recipewebsitebackend.models;


import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "Users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private ObjectId id;

    private String username;

    private String password;

    @Getter
    @Setter
    private Map<String, Map<String,String>> ingredients;

    @Setter
    @Getter
    private List<Recipe> recipes;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.ingredients = new HashMap<>();
        this.recipes = new ArrayList<>();
    }

    public void addIngredient(String ingredient, String amount, String unit){

        Map<String, String> newIngredient = new HashMap<>();
        newIngredient.put("ingredient", ingredient);
        newIngredient.put("amount", amount);
        newIngredient.put("unit", unit);

        this.ingredients.put(ingredient, newIngredient);
    }

    public void addRecipe(Recipe newRecipe){
        recipes.add(newRecipe);
    }

}


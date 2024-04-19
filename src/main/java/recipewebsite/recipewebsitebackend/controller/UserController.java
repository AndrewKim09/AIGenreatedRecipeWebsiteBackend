package recipewebsite.recipewebsitebackend.controller;

import org.apache.coyote.Response;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipewebsite.recipewebsitebackend.models.Recipe;
import recipewebsite.recipewebsitebackend.models.User;
import recipewebsite.recipewebsitebackend.services.UserService;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping
    public ResponseEntity<ObjectId> addUser(@RequestParam("username") String username, @RequestParam("password") String password){
        Optional<User> existingUser = findUser(username);

        if(!existingUser.isEmpty()){
            return new ResponseEntity<>(HttpStatus.FOUND);
        }
        else{
            return new ResponseEntity<>(userService.addUser(username, password), HttpStatus.CREATED);
        }
    }

    @GetMapping("/{name}")
    public Optional<User> findUser(@PathVariable(value="name") String username){
        return userService.findUserByName(username);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> checkUser(@RequestBody Map<String,String> data){
        if(!data.containsKey("username") || !data.containsKey("password") ) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<User> existingUser = userService.findUserByName(data.get("username"));

        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else{
            if(existingUser.get().getPassword().equals(data.get("password"))) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", existingUser.get().getId().toString());
                userMap.put("username", existingUser.get().getUsername());
                return new ResponseEntity<>(userMap, HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/ingredients")
    public ResponseEntity<String> addIngredient(@RequestBody Map<String, String> data){
        if(!data.containsKey("username") || !data.containsKey("ingredient") || !data.containsKey("amount") || !data.containsKey("units")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<User> existingUser = userService.findUserByName(data.get("username"));

        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        else{
            existingUser.get().addIngredient(data.get("ingredient"), data.get("amount"), data.get("units"));
            userService.saveUser(existingUser.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping("/ingredients/{user}")
    public ResponseEntity<Map<String, Map<String,String>>> getIngredients(@PathVariable(value = "user")String user){
        Optional<User> existingUser = userService.findUserByName(user);
        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(existingUser.get().getIngredients(), HttpStatus.OK);
    }

    @PutMapping("/ingredients/edit")
    public ResponseEntity<Map<String, Map<String, String>>> editIngredients(@RequestBody Map<String, String> data){
        if(!data.containsKey("username") || !data.containsKey("ingredient") || !data.containsKey("amount")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Optional<User> existingUser = userService.findUserByName(data.get("username"));
        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Map<String, Map<String, String>> ingredients = existingUser.get().getIngredients();
        if(!ingredients.containsKey(data.get("ingredient"))) return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        else{
            ingredients.get(data.get("ingredient")).put("amount", data.get("amount"));
            existingUser.get().setIngredients(ingredients);
            userService.saveUser(existingUser.get());
            return new ResponseEntity<>(existingUser.get().getIngredients(), HttpStatus.OK);
        }
    }

    @DeleteMapping("ingredients/delete")
    public ResponseEntity<Map<String, Map<String, String>>> deleteIngredients(@RequestBody Map<String, String> data){
        if(!data.containsKey("username") || !data.containsKey("ingredient")) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<User> existingUser = userService.findUserByName(data.get("username"));
        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Map<String, Map<String, String>> ingredients = existingUser.get().getIngredients();
        if(!ingredients.containsKey(data.get("ingredient"))) return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        else{
            existingUser.get().getIngredients().remove(data.get("ingredient"));
            userService.saveUser(existingUser.get());
            return new ResponseEntity<>(existingUser.get().getIngredients(), HttpStatus.OK);
        }
    }

    @PutMapping("recipes/add")
    public ResponseEntity<Object> addRecipe(@RequestBody Map<String, Object> data){
        List<String> missingKeys = new ArrayList<>();

        if (!data.containsKey("username")) missingKeys.add("username");
        if (!data.containsKey("ingredients")) missingKeys.add("ingredients");
        if (!data.containsKey("instructions")) missingKeys.add("instructions");
        if (!data.containsKey("nutrition")) missingKeys.add("nutrition");
        if (!data.containsKey("servings")) missingKeys.add("servings");
        if (!data.containsKey("time")) missingKeys.add("time");
        if (!data.containsKey("title")) missingKeys.add("title");
        if (!data.containsKey("image")) missingKeys.add("image");

        if (!missingKeys.isEmpty()) {
            return new ResponseEntity<>(missingKeys, HttpStatus.BAD_REQUEST);
        }
        String username = String.valueOf(data.get("username"));
        Optional<User> existingUser = userService.findUserByName(username);

        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        String title = (String) data.get("title");
        List<String> ingredients = (List<String>) data.get("ingredients");
        List<String> steps = (List<String>) data.get("instructions");
        Map<String, String> nutrition = (Map<String, String>) data.get("nutrition");
        String servings = (String) data.get("servings");
        String time = (String) data.get("time");
        String image = (String) data.get("image");


        Recipe newRecipe = new Recipe(title, ingredients, steps, nutrition, servings, time, image);

        existingUser.get().addRecipe(newRecipe);
        userService.saveUser(existingUser.get());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("recipes/get/{user}")
    public ResponseEntity<List<Recipe>> getUserRecipes(@PathVariable(value = "user")String user){
        Optional<User> existingUser = userService.findUserByName(user);
        if(existingUser.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(existingUser.get().getRecipes(), HttpStatus.OK);
    }

    @DeleteMapping("recipes/delete")
    public ResponseEntity<Object> deleteUserRecipe(@RequestBody Map<String, String> data){

        List<String> missingKeys = new ArrayList<>();
        if(!data.containsKey("username")) missingKeys.add("username");
        if(!data.containsKey("title")) missingKeys.add("title");
        if (!missingKeys.isEmpty()) {
            return new ResponseEntity<>(missingKeys, HttpStatus.BAD_REQUEST);
        }


        Optional<User> existingUser = userService.findUserByName(data.get("username"));

        if(existingUser.isEmpty()) return new ResponseEntity("user doesnt exist", HttpStatus.NOT_FOUND);

        List<Recipe> userRecipes = existingUser.get().getRecipes();
        int itemIndex = -1;

        for(int i = 0 ; i < userRecipes.size(); i++){
            if(userRecipes.get(i).getTitle().equals(data.get("title"))){
                itemIndex = i;
                break;
            }
        }

        if(itemIndex == -1) return new ResponseEntity("recipe does not exist", HttpStatus.NO_CONTENT);
        else{
            userRecipes.remove(itemIndex);
            existingUser.get().setRecipes(userRecipes);
            userService.saveUser(existingUser.get());
            return new ResponseEntity(HttpStatus.OK);
        }



    }
}

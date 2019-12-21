package app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import app.entity.User;
import app.repository.UserRepository;
import app.utility.CryptoUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Defines the REST endpoints for user access.
 */
@SuppressWarnings("unchecked")
@RestController
public class UserController {
    
    //Fields
    
    /**
     * The CRUD repository class for the user entity.
     */
    @Autowired
    private UserRepository userRepository;
    
    
    //Methods
    
    /**
     * Creates a new user.
     *
     * @param payload A JSON payload that contains the userName and password of the user to create.
     * @return A JSON response that contains a status and the id of the newly created user if it was successful.
     */
    @RequestMapping(
            value = "/users",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity createUser(
            @RequestParam(value = "payload") String payload
    ) {
        JSONObject response = new JSONObject();
        
        JSONParser parser = new JSONParser();
        JSONObject payloadJson;
        String userName;
        String password;
        try {
            payloadJson = (JSONObject) parser.parse(payload);
            userName = (String) payloadJson.get("userName");
            password = (String) payloadJson.get("password");
        } catch (ParseException e) {
            response.put("status", "Failure: JSON payload could not be parsed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toJSONString());
        }
        
        List<User> checkUserRetrieved = userRepository.findByUserName(userName);
        if (!checkUserRetrieved.isEmpty()) {
            response.put("status", "Failure: User: " + userName + " already exists");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response.toJSONString());
        }
        
        User newUser = userRepository.save(new User(userName, CryptoUtility.hashSHA512(password)));
        Long id = newUser.getId();
        
        response.put("status", "Success: User: " + id + " created");
        response.put("id", id);
        return ResponseEntity.status(HttpStatus.OK).body(response.toJSONString());
    }
    
    /**
     * Retries a user.
     *
     * @param payload A JSON payload that contains the id and/or userName of the user to retrieve. Will prioritize userName over id.
     *                If empty, all users will be retrieved.
     * @return A JSON response that contains a status and the user(s) that were retrieved.
     */
    @RequestMapping(
            value = "/users",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity retrieveUser(
            @RequestParam(value = "payload") String payload
    ) {
        JSONObject response = new JSONObject();
        
        JSONParser parser = new JSONParser();
        JSONObject payloadJson;
        Long id;
        String userName;
        try {
            payloadJson = (JSONObject) parser.parse(payload);
            id = payloadJson.containsKey("id") ? (Long) payloadJson.get("id") : null;
            userName = payloadJson.containsKey("userName") ? (String) payloadJson.get("userName") : null;
        } catch (ParseException e) {
            response.put("status", "Failure: JSON payload could not be parsed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toJSONString());
        }
        
        final List<User> results = new ArrayList<>();
        if (userName != null) {
            results.addAll(userRepository.findByUserName(userName));
        } else if (id != null) {
            Optional<User> userById = userRepository.findById(id);
            userById.ifPresent(results::add);
        } else {
            userRepository.findAll().forEach(results::add);
        }
        
        JSONArray retrievedUsers = new JSONArray();
        results.forEach(user -> {
            JSONObject userJson = new JSONObject();
            userJson.put("id", user.getId());
            userJson.put("userName", user.getUserName());
            userJson.put("password", user.getPassword());
            retrievedUsers.add(userJson);
        });
        
        response.put("status", "Success: Retrieved " + results.size() + " User" + (results.size() == 1 ? "" : "s"));
        response.put("users", retrievedUsers);
        return ResponseEntity.status(HttpStatus.OK).body(response.toJSONString());
    }
    
    /**
     * Edits a user.
     *
     * @param payload A JSON payload that contains the id of a user and the users new userName and password.
     * @return A JSON response that contains a status.
     */
    @RequestMapping(
            value = "/users",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity editUser(
            @RequestParam(value = "payload") String payload
    ) {
        JSONObject response = new JSONObject();
        
        JSONParser parser = new JSONParser();
        JSONObject payloadJson;
        Long id;
        String userName;
        String password;
        try {
            payloadJson = (JSONObject) parser.parse(payload);
            id = (Long) payloadJson.get("id");
            userName = (String) payloadJson.get("userName");
            password = (String) payloadJson.get("password");
        } catch (ParseException e) {
            response.put("status", "Failure: JSON payload could not be parsed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toJSONString());
        }
        
        Optional<User> editUserRetrieved = userRepository.findById(id);
        if (editUserRetrieved.isPresent()) {
            List<User> checkUserRetrieved = userRepository.findByUserName(userName);
            if (!checkUserRetrieved.isEmpty()) {
                response.put("status", "Failure: User: " + userName + " already exists");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response.toJSONString());
            }
            
            User editUser = editUserRetrieved.get();
            editUser.setUserName(userName);
            editUser.setPassword(password);
            userRepository.save(editUser);
        } else {
            response.put("status", "Failure: User: " + id + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.toJSONString());
        }
        
        response.put("status", "Success: User: " + id + " edited");
        return ResponseEntity.status(HttpStatus.OK).body(response.toJSONString());
    }
    
    /**
     * Deletes a user.
     *
     * @param payload A JSON payload that contains the id of a user.
     * @return A JSON response that contains a status.
     */
    @RequestMapping(
            value = "/users",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity deleteUser(
            @RequestParam(value = "payload") String payload
    ) {
        JSONObject response = new JSONObject();
        
        JSONParser parser = new JSONParser();
        JSONObject payloadJson;
        Long id;
        try {
            payloadJson = (JSONObject) parser.parse(payload);
            id = (Long) payloadJson.get("id");
        } catch (ParseException e) {
            response.put("status", "Failure: JSON payload could not be parsed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toJSONString());
        }
        
        Optional<User> deleteUserRetrieved = userRepository.findById(id);
        if (deleteUserRetrieved.isPresent()) {
            User editUser = deleteUserRetrieved.get();
            userRepository.delete(editUser);
        } else {
            response.put("status", "Failure: User: " + id + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.toJSONString());
        }
        
        response.put("status", "Success: User: " + id + " deleted");
        return ResponseEntity.status(HttpStatus.OK).body(response.toJSONString());
    }
    
    /**
     * Validates a user.
     *
     * @param payload A JSON payload that contains the userName and password of the user.
     * @return A JSON response that contains a status and the id if the validation was successful.
     */
    @RequestMapping(
            value = "/users/validate",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity validateUser(
            @RequestParam(value = "payload") String payload
    ) {
        JSONObject response = new JSONObject();
        
        JSONParser parser = new JSONParser();
        JSONObject payloadJson;
        String userName;
        String password;
        try {
            payloadJson = (JSONObject) parser.parse(payload);
            userName = (String) payloadJson.get("userName");
            password = (String) payloadJson.get("password");
        } catch (ParseException e) {
            response.put("status", "Failure: JSON payload could not be parsed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toJSONString());
        }
        
        List<User> validateUserRetrieved = userRepository.findByUserName(userName);
        Long id;
        if (!validateUserRetrieved.isEmpty()) {
            User validateUser = validateUserRetrieved.get(0);
            id = validateUser.getId();
            String passHash = CryptoUtility.hashSHA512(password);
            if (!passHash.equals(validateUser.getPassword())) {
                response.put("status", "Failure: User: " + userName + " password is bad");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response.toJSONString());
            }
        } else {
            response.put("status", "Failure: User: " + userName + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.toJSONString());
        }
        
        response.put("status", "Success: User: " + userName + " validated");
        response.put("id", id);
        return ResponseEntity.status(HttpStatus.OK).body(response.toJSONString());
    }
    
}

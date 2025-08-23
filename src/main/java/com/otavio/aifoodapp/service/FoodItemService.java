package com.otavio.aifoodapp.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.otavio.aifoodapp.dto.FoodItemCreateDto;
import com.otavio.aifoodapp.model.FoodItem;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.repository.FoodItemRepository;
import com.otavio.aifoodapp.repository.UserRepository;

@Service
public class FoodItemService {
    private static final Logger log = LoggerFactory.getLogger(FoodItemService.class);

    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final FoodAiService foodAiService;

    public FoodItemService(FoodItemRepository foodItemRepository, UserRepository userRepository, FoodAiService foodAiService) {
        this.foodItemRepository = foodItemRepository;
        this.userRepository = userRepository;
        this.foodAiService = foodAiService;
    }

    /**
     * Get the current authenticated user
     * @return the authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("Authentication is NULL");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication context is null");
        }
        
        if (!authentication.isAuthenticated()) {
            log.error("User not authenticated: {}", authentication);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        String username = authentication.getName();
        log.debug("Authentication principal: {}", authentication.getPrincipal().getClass().getName());
        log.debug("Looking up user by: {}", username);
        
        // Try to find by email first since OAuth might use email as the principal name
        Optional<User> userByEmail = userRepository.findByEmail(username);
        if (userByEmail.isPresent()) {
            log.debug("Found user by email: {}", username);
            return userByEmail.get();
        } else {
            log.debug("No user found by email: {}", username);
        }
        
        // If not found by email, try by login
        UserDetails userDetails = userRepository.findByLogin(username);
        if (userDetails != null && userDetails instanceof User) {
            log.debug("Found user by login: {}", username);
            return (User) userDetails;
        } else if (userDetails == null) {
            log.debug("No user found by login: {}", username);
        } else {
            log.debug("Found userDetails but not User instance: {}", userDetails.getClass().getName());
        }
        
        // If the authentication principal is already a User, use that
        // Using pattern matching instanceof (Java 16+)
        if (authentication.getPrincipal() instanceof User user) {
            log.debug("Principal is already a User: {}", user.getUsername());
            return user;
        }
        
        // Print detailed debugging info
        log.error("Authentication failed. Details:");
        log.error("- Username: {}", username);
        log.error("- Principal type: {}", (authentication.getPrincipal() != null ? 
            authentication.getPrincipal().getClass().getName() : "null"));
        log.error("- Authorities: {}", authentication.getAuthorities());
        
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
            "User not found with identifier: " + username + 
            ", Principal type: " + (authentication.getPrincipal() != null ? 
                authentication.getPrincipal().getClass().getName() : "null"));
    }
    
    /**
     * Public method for testing user authentication
     * @return the authenticated user or null if user can't be determined safely
     */
    public User getUserForTesting() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.warn("No authentication in context");
                return null;
            }
            
            // Additional null checks to prevent NullPointerException
            if (authentication.getName() == null) {
                log.warn("Authentication name is null");
                return null;
            }
            
            log.debug("Authentication type: {}", authentication.getClass().getName());
            log.debug("Authentication name: {}", authentication.getName());
            log.debug("Principal type: {}", (authentication.getPrincipal() != null ? 
                authentication.getPrincipal().getClass().getName() : "null"));
            
            // Try all available methods to get user info
            return extractUserFromAuthentication(authentication);
        } catch (Exception e) {
            log.error("Error in getUserForTesting: {}", e.getMessage(), e);
            // Return null instead of propagating the exception
            return null;
        }
    }
    
    /**
     * Extract user information from various authentication types
     * with additional null checks to prevent NullPointerExceptions
     */
    private User extractUserFromAuthentication(Authentication authentication) {
        if (authentication == null) return null;
        
        try {
            String username = authentication.getName();
            if (username == null) {
                log.warn("Username is null in authentication");
                return null;
            }
            
            // Try by email first
            Optional<User> userByEmail = userRepository.findByEmail(username);
            if (userByEmail.isPresent()) {
                return userByEmail.get();
            }
            
            // Try direct cast if principal is User
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
            
            // Try by login
            UserDetails userDetails = null;
            try {
                userDetails = userRepository.findByLogin(username);
            } catch (Exception e) {
                log.warn("Error finding user by login: {}", e.getMessage());
            }
            
            if (userDetails instanceof User user) {
                return user;
            }
            
            log.debug("Could not find user with name: {}", username);
            return null;
        } catch (Exception e) {
            log.error("Error in extractUserFromAuthentication: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Save a food item with AI-enhanced nutritional information
     * @param foodItem The basic food item with name, quantity, and expiration
     * @return The saved food item with complete nutritional information
     */
    public FoodItem saveWithAiEnhancement(FoodItem foodItem) {
        // Associate with current user
        foodItem.setUser(getCurrentUser());
        
        // Use AI to determine nutritional facts
        return foodAiService.determineNutritionalFacts(foodItem)
                .map(foodItemRepository::save)
                .block(); // Convert from reactive to blocking for consistency with other methods
    }
    
    /**
     * Save multiple food items with AI-enhanced nutritional information
     * @param foodItems List of basic food items with name, quantity, and expiration
     * @return List of saved food items with complete nutritional information
     */
    public FoodItem saveWithAIEnhancement(FoodItem foodItem) {
        foodItem.setUser(getCurrentUser());
        return foodAiService.determineNutritionalFacts(foodItem)
                .map(foodItemRepository::save)
                .block();
    }

    /**
     * Salva múltiplos itens de alimentos
     * @param foodItems Lista de itens de alimentos a serem salvos
     * @return Lista de itens de alimentos salvos
     */
    public List<FoodItem> saveAll(List<FoodItem> foodItems) {
        User currentUser = getCurrentUser();
        foodItems.forEach(item -> item.setUser(currentUser));
        return foodItemRepository.saveAll(foodItems);
    }

    /**
     * List all food items for the current user
     * @return List of food items for the current user
     */
    public List<FoodItem> listAll() {
        User currentUser = getCurrentUser();
        return foodItemRepository.findByUser(currentUser);
    }

    /**
     * List food item by ID, ensuring it belongs to the current user
     * @param id Food item ID
     * @return Optional containing the food item if found
     */
    public Optional<FoodItem> listById(Long id) {
        User currentUser = getCurrentUser();
        Optional<FoodItem> foodItemOpt = foodItemRepository.findById(id);
        
        if (foodItemOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + id + " not found");
        }
        
        FoodItem foodItem = foodItemOpt.get();
        if (foodItem.getUser() == null || !foodItem.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this food item");
        }
        
        return foodItemOpt;
    }

    /**
     * Modify a food item, ensuring it belongs to the current user
     * @param foodItem Updated food item
     * @return The modified food item
     */
    public FoodItem modify(FoodItem foodItem) {
        User currentUser = getCurrentUser();
        Optional<FoodItem> itemExistOpt = foodItemRepository.findById(foodItem.getId());
        
        if (itemExistOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + foodItem.getId() + " not found");
        }
        
        FoodItem existingItem = itemExistOpt.get();
        if (existingItem.getUser() == null || !existingItem.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this food item");
        }
        
        foodItem.setUser(currentUser);
        return foodItemRepository.save(foodItem);
    }

    /**
     * Delete a food item, ensuring it belongs to the current user
     * @param id Food item ID
     */
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        Optional<FoodItem> itemExistOpt = foodItemRepository.findById(id);
        
        if (itemExistOpt.isPresent()) {
            FoodItem existingItem = itemExistOpt.get();
            if (existingItem.getUser() != null && existingItem.getUser().getId().equals(currentUser.getId())) {
                foodItemRepository.deleteById(id);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this food item");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + id + " not found");
        }
    }

    /**
     * List all food items for a specific user
     *
     * @param user the user to list food items for
     * @return a list of food items belonging to the user
     */
    public List<FoodItem> listAllForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return foodItemRepository.findByUserId(user.getId());
    }

    /**
     * Get a food item by ID
     *
     * @param id the ID of the food item to retrieve
     * @return the food item, or null if not found
     */
    public FoodItem getById(Long id) {
        return foodItemRepository.findById(id).orElse(null);
    }

    /**
     * Create a new food item with AI-enhanced nutritional data
     *
     * @param createDto the DTO containing basic food item information
     * @param user the user who owns the food item
     * @return the created food item with AI-enhanced data
     */
    public FoodItem createWithAiEnhancement(FoodItemCreateDto createDto, User user) {
        FoodItem foodItem = new FoodItem();
        foodItem.setName(createDto.name());
        foodItem.setQuantity(createDto.quantity());
        foodItem.setExpiration(createDto.expiration());
        foodItem.setUser(user);

        // Enhance with AI (async)
        foodAiService.determineNutritionalFacts(foodItem)
            .subscribe(
                enhancedItem -> {
                    log.info("Successfully enhanced food item {} with AI", enhancedItem.getName());
                    foodItemRepository.save(enhancedItem);
                },
                error -> log.error("Failed to enhance food item {} with AI: {}", foodItem.getName(), error.getMessage())
            );

        return foodItemRepository.save(foodItem);
    }

    /**
     * Update an existing food item
     *
     * @param id the ID of the food item to update
     * @param createDto the DTO containing the updated information
     * @param user the user who owns the food item
     * @return the updated food item
     */
    public FoodItem update(Long id, FoodItemCreateDto createDto, User user) {
        FoodItem existingItem = foodItemRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));

        // Check ownership
        if (!existingItem.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this food item");
        }

        existingItem.setName(createDto.name());
        existingItem.setQuantity(createDto.quantity());
        existingItem.setExpiration(createDto.expiration());

        return foodItemRepository.save(existingItem);
    }
}

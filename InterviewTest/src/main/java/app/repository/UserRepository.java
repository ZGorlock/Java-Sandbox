package app.repository;

import java.util.List;

import app.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The CRUD repository class for the user entity.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    //Methods
    
    /**
     * Returns a list of user entities with a particular username.
     *
     * @param userName The username/
     * @return A list of user entities with the specified username.
     */
    List<User> findByUserName(String userName);
    
}

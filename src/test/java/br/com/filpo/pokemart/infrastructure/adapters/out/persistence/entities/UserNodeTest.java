package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import br.com.filpo.pokemart.domain.models.UserRole;

class UserNodeTest {

    @Test
    @DisplayName("Deve retornar autoridades de ADMIN e USER quando o cargo for ADMIN")
    void shouldReturnAdminAndUserAuthorities() {
        // Arrange
        UserNode admin = UserNode.builder().role(UserRole.ADMIN).build();

        // Act
        Collection<? extends GrantedAuthority> authorities = admin.getAuthorities();

        // Assert
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Deve retornar apenas autoridade de USER quando o cargo for USER")
    void shouldReturnOnlyUserAuthority() {
        // Arrange
        UserNode user = UserNode.builder().role(UserRole.USER).build();

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Deve validar os métodos padrão do UserDetails")
    void shouldValidateUserDetailsMethods() {
        // Arrange
        UserNode user = UserNode.builder()
                .email("ash@pallet.com")
                .password("pikachu123")
                .build();

        // Act & Assert 
        assertEquals("ash@pallet.com", user.getUsername());
        assertEquals("pikachu123", user.getPassword());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
    
    @Test
    @DisplayName("Deve testar construtores e NoArgsConstructor para o JaCoCo")
    void shouldTestConstructors() {
        UserNode user = new UserNode();
        assertNotNull(user);
        
        user.setCartItems(List.of(new CartItemRelationship()));
        assertFalse(user.getCartItems().isEmpty());
    }
}
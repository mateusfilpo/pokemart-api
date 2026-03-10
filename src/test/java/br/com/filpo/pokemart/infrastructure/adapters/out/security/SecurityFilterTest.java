package br.com.filpo.pokemart.infrastructure.adapters.out.security;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.filpo.pokemart.application.services.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @InjectMocks
    private SecurityFilter securityFilter;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        mockUserDetails = new User("ash@pallet.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar o usuário quando um token válido for enviado via Cookie")
    void shouldAuthenticateWhenValidTokenInCookie() throws Exception {
        // Arrange
        String validToken = "jwt.token.valido";
        request.setCookies(new Cookie("pokemart_token", validToken));
        
        when(tokenService.validateToken(validToken)).thenReturn("ash@pallet.com");
        when(authorizationService.loadUserByUsername("ash@pallet.com")).thenReturn(mockUserDetails);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "O usuário deveria estar autenticado no contexto");
        assertEquals("ash@pallet.com", ((UserDetails) authentication.getPrincipal()).getUsername());
        
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve autenticar o usuário quando um token válido for enviado via Cabeçalho Authorization")
    void shouldAuthenticateWhenValidTokenInHeader() throws Exception {
        // Arrange
        String validToken = "jwt.token.valido";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(tokenService.validateToken(validToken)).thenReturn("ash@pallet.com");
        when(authorizationService.loadUserByUsername("ash@pallet.com")).thenReturn(mockUserDetails);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("ash@pallet.com", ((UserDetails) authentication.getPrincipal()).getUsername());
        
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar e deve seguir a corrente se nenhum token for enviado")
    void shouldNotAuthenticateWhenNoTokenProvided() throws Exception {
        // Act 
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Contexto de segurança deve permanecer nulo");
        
        verify(tokenService, never()).validateToken(anyString());
        verify(authorizationService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response); 
    }

    @Test
    @DisplayName("Não deve autenticar se o token for inválido/expirado (retornar string vazia)")
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        // Arrange
        String invalidToken = "token.falsificado";
        request.addHeader("Authorization", "Bearer " + invalidToken);
        
        when(tokenService.validateToken(invalidToken)).thenReturn("");

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "Contexto de segurança não deve ser populado");
        
        verify(authorizationService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando existirem outros cookies, mas nenhum for o 'pokemart_token'")
    void shouldNotAuthenticateWhenOtherCookiesExistButNotOurs() throws Exception {
        // Arrange
        request.setCookies(new Cookie("random_cookie", "some-value"));

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o Header Authorization não começar com 'Bearer '")
    void shouldNotAuthenticateWhenAuthHeaderIsInvalidFormat() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz"); 

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o Header Authorization for nulo e não houver cookies")
    void shouldNotAuthenticateWhenEverythingIsNull() throws Exception {
        // Arrange
        request.setCookies(null); 

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
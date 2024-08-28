package com.example.dari_back.auth;
import com.example.dari_back.repositories.*;
import org.apache.commons.codec.binary.Base64;
import com.example.dari_back.exception.NotFoundException;

import com.example.dari_back.MFA.EmailConfirmationService;
import com.example.dari_back.MFA.EmailConfirmationToken;
import com.example.dari_back.MFA.MfaTokenData;
import com.example.dari_back.config.JWTServiceImpl;
import com.example.dari_back.entities.Role;
import com.example.dari_back.entities.RoleType;
import com.example.dari_back.entities.User;
import com.example.dari_back.exception.InvalidTokenException;
import com.example.dari_back.exception.MFAServerAppException;
import com.example.dari_back.exception.UserAlreadyExistException;
import com.example.dari_back.services.EmailService;
import com.example.dari_back.services.IServiceUser;
import com.example.dari_back.services.TotpManager;
import com.example.dari_back.token.TokenRepository;

import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.dari_back.repositories.SellerRepository;
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService implements IServiceUser {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailConfirmationService emailConfirmationService;
    private final TotpManager totpManager;
    private final EmailService emailService;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(15);
    private static final Charset US_ASCII = StandardCharsets.US_ASCII;

    @Autowired
    public  final SellerRepository sellerRepository ;
    public final VisiteurRepository visiteurRepository;
    @Override
    public MfaTokenData registerUser(User user) throws UserAlreadyExistException, QrGenerationException {
        try {
            if (repository.findByUsername(user.getUsername()).isPresent()) {
                throw new UserAlreadyExistException("Username already exists");
            }
            user.setPaswd(passwordEncoder.encode(user.getPaswd()));
            // Some additional work
            user.setSecretKey(totpManager.generateSecretKey()); // Generating the secret and storing it with the profile
            User savedUser = repository.save(user);
            if (user.getRoles() != null) {
                // Now that the user is saved, we can assign roles
                for (Role role : user.getRoles()) {
                    role.setUser(savedUser);
                }
            }
// save rox dans la table acteur :inset into
            saveUserToPatientTable(savedUser);
            // Create a secure token and send email
            this.sendRegistrationConfirmationEmail(user);

            // Generate the QR Code
            String qrCode = totpManager.getQRCode(savedUser.getSecretKey());

            return MfaTokenData.builder()
                    .mfaCode(savedUser.getSecretKey())
                    .qrCode(qrCode)
                    .build();
        } catch (Exception e) {
            throw new MFAServerAppException("Exception while registering the user", e);
        }
    }





//  public AuthenticationResponse authenticate(AuthenticationRequest request) {
//    authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(
//                    request.getUsername(),
//                    request.getPassword()
//            )
//    );
//
//    var user = repository.findByEmail(request.getUsername())
//            .orElseThrow(() -> new RuntimeException("I Can't found this User"));
//
//    List<GrantedAuthority> authorities = getAuthorities(user.getAuthFromBase());
//    var jwtToken = jwtService.generateToken(user);
//    revokeAllUserTokens(user);
//    saveUserToken(user, jwtToken);
//
//    return AuthenticationResponse.builder()
//            .token(jwtToken)
//            .build();
//  }

//  private List<GrantedAuthority> getAuthorities(Set<Role> roles) {
//    List<GrantedAuthority> list = new ArrayList<>();
//    for (Role role : roles) {
//      list.add(new SimpleGrantedAuthority(role.getName().name()));
//    }
//    return list;
//  }
//
//  private void saveUserToken(User user, String jwtToken) {
//    var token = Token.builder()
//            .user(user)
//            .token(jwtToken)
//            .tokenType(TokenType.BEARER)
//            .expired(false)
//            .revoked(false)
//            .build();
//    tokenRepository.save(token);
//  }

//  private void revokeAllUserTokens(User user) {
//    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
//    if (validUserTokens.isEmpty())
//      return;
//    validUserTokens.forEach(token -> {
//      token.setExpired(true);
//      token.setRevoked(true);
//    });
//    tokenRepository.saveAll(validUserTokens);
//  }

    public void sendRegistrationConfirmationEmail(User user) throws MessagingException, jakarta.mail.MessagingException {
        // Generate the token
        String tokenValue = new String(Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), StandardCharsets.US_ASCII);
        EmailConfirmationToken emailConfirmationToken = new EmailConfirmationToken();
        emailConfirmationToken.setToken(tokenValue);
        emailConfirmationToken.setTimeStamp(LocalDateTime.now());
        emailConfirmationToken.setUser(user);
        emailConfirmationTokenRepository.save(emailConfirmationToken);
        // Send email
        emailService.sendConfirmationEmail(emailConfirmationToken);
    }
    @Override
    public boolean verifyUser(String token) throws InvalidTokenException {
        EmailConfirmationToken emailConfirmationToken = emailConfirmationTokenRepository.findByToken(token);
        if(Objects.isNull(emailConfirmationToken) || !token.equals(emailConfirmationToken.getToken())){
            throw new InvalidTokenException("Token is not valid");
        }
        User user = emailConfirmationToken.getUser();
        if (Objects.isNull(user)){
            return false;
        }
        user.setAccountVerified(true);
        repository.save(user);
        emailConfirmationTokenRepository.delete(emailConfirmationToken);
        return true;
    }

    @Override
    public boolean verifyTotp(String code, String email) {
        User user = repository.findByEmail(email).get();
        return totpManager.verifyTotp(code, user.getSecretKey());
    }




    // Méthode pour le processus de réinitialisation du mot de passe par e-mail
    public void forgotPassword(String email) throws MessagingException, jakarta.mail.MessagingException {
        User user = repository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

        // Générer un token de réinitialisation sécurisé
        String tokenValue = new String(Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), StandardCharsets.US_ASCII);
        EmailConfirmationToken emailConfirmationToken = new EmailConfirmationToken();
        emailConfirmationToken.setToken(tokenValue);
        emailConfirmationToken.setTimeStamp(LocalDateTime.now());
        emailConfirmationToken.setUser(user);
        emailConfirmationTokenRepository.save(emailConfirmationToken);

        // Envoyer un e-mail avec le lien de réinitialisation du mot de passe
        emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
    }

    public void resetPassword(String token, String newPassword) {
        EmailConfirmationToken emailConfirmationToken = emailConfirmationTokenRepository.findByToken(token);
        if (emailConfirmationToken == null || !token.equals(emailConfirmationToken.getToken())) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        User user = emailConfirmationToken.getUser();
        user.setPaswd(passwordEncoder.encode(newPassword)); // Assurez-vous que vous utilisez le bon nom de méthode pour définir le mot de passe
        repository.save(user);

        // Supprimer le token après avoir réinitialisé le mot de passe
        emailConfirmationTokenRepository.delete(emailConfirmationToken);
    }









    @Autowired
    private UserRepository userRepository;


    public void updateResetPasswordToken(String token, String email) throws UserAlreadyExistException {
        User user = userRepository.findByEmail1(email);
        if (user != null) {
            user.setResetPasswordToken(token); // Appeler la méthode sur l'instance de User
            userRepository.save(user);
        } else {
            throw new UserAlreadyExistException("Could not find any customer with the email " + email);
        }
    }


    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    public void updatePassword(User user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPaswd(encodedPassword);

        user.setResetPasswordToken(null);
        userRepository.save(user);
    }


    public Optional<?> getCurrentUsersWithRole(Integer id, String role) {
        switch (role) {
            case "USER":
                return null;
//            case "DANCER":
//                return null;
          case "SELLER":
                return sellerRepository.findSellerByUser(id);
             case "ADMIN":
                return null;
             case "VISITOR":
                return visiteurRepository.findVisiteurByUser(id);
            default:
                return null;
        }

    }


    private void saveUserToPatientTable(User savedUser) {
        {
            for (Role r:savedUser.getRoles()){
/*
                if (r.getName()==RoleType.VISITOR){
                    String sql = "INSERT INTO visiteur (user) VALUES (?)";
                    jdbcTemplate.update(sql, savedUser.getId());
                }
                if (r.getName()==RoleType.EVALUATOR){
                    String sql = "INSERT INTO evaluator (user) VALUES (?)";
                    jdbcTemplate.update(sql, savedUser.getId());
                } */




//                if (r.getName()==RoleType.DANCER){
//                    String sql = "INSERT INTO dancers_group (user) VALUES (?)";
//                    jdbcTemplate.update(sql, savedUser.getId());
//                }

            }}}
    private boolean hasThisRole(User user, String role) {
        for (Role userRole : user.getRoles()) {
            if (userRole != null && userRole.getName() != null && userRole.getName().name().equals(role)) {
                return true;
            }
        }
        return false;
    }
    public List<User> getConnectedUsersWithRole(String role) {
        List<Integer> tokenMr = tokenRepository.retrieveIdUserConecter();
        List<User> userConnects = new ArrayList<>();
        List<User> userConnectsWithRole = new ArrayList<>();

        // Retrieve connected users
        for (Integer tokenId : tokenMr) {
            userConnects.add(repository.findById(tokenId).get());
            System.out.println("ID USER Connected: " + tokenId);
        }

        // Filter users with the specified role///
        for (User user : userConnects) {
            if (user != null && hasThisRole(user, role)) {
                System.out.println("Connected: " + user.getEmail());
                userConnectsWithRole.add(user);
            }
        }
        return userConnectsWithRole;
    }
    @PersistenceContext
    private EntityManager entityManager;
    public List<String> getRolesFromDatabase() {
        List<RoleType> roles = roleRepository.findAllRoleTypes();
        List<String> roleNames = roles.stream()
                .map(Enum::name) // Convertir les énumérations en noms de chaînes
                .collect(Collectors.toList());
        return roleNames;
    }

    public boolean isAdmin(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleType.ADMIN))
                .orElse(false);
    }
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Vérifiez si l'objet principal de l'authentification est de type UserDetails
            if (authentication.getPrincipal() instanceof UserDetails) {
                // Obtenez l'e-mail à partir de UserDetails
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                return userDetails.getUsername();
            } else {
                // Si UserDetails n'est pas disponible, renvoyez simplement le nom d'utilisateur
                return authentication.getName();
            }
        } else {
            return null; // Renvoie null si aucun utilisateur n'est connecté
        }
    }
    private Set<String> bannedUsers = new HashSet<>();
    public boolean banUser(String email, int banDurationDays) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isBanned() || user.getBanExpirationDate().before(new Date())) {
                // Vérifiez si l'utilisateur n'est pas déjà banni ou si le bannissement précédent a expiré
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, banDurationDays); // Ajoutez la durée du bannissement en jours
                Date banExpirationDate = calendar.getTime();
                user.setBanned(true); // Mettez à jour le statut de bannissement de l'utilisateur
                user.setBanExpirationDate(banExpirationDate); // Enregistrez la date d'expiration du bannissement
                userRepository.save(user); // Enregistrez les modifications dans la base de données
                bannedUsers.add(email);
                return true;
            } else {
                // L'utilisateur est déjà banni ou son bannissement précédent n'a pas encore expiré
                return false;
            }
        } else {
            // L'utilisateur avec l'ID donné n'existe pas
            return false;
        }
    }

    // Méthode pour vérifier et supprimer le bannissement expiré des utilisateurs
    public void removeExpiredBans() {
        Date currentDate = new Date();
        for (String email : bannedUsers) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.isBanned() && user.getBanExpirationDate().before(currentDate)) {
                    // Supprimer le bannissement expiré
                    user.setBanned(false);
                    user.setBanExpirationDate(null);
                    userRepository.save(user);
                    bannedUsers.remove(email);
                }
            }
        }
    }
    // Méthode pour vérifier si un utilisateur est banni
    public boolean isUserBanned(Integer id) {
        return bannedUsers.contains(id);
    }
    @Transactional

    // Méthode pour vérifier si un utilisateur est banni en fonction de son adresse e-mail
    public boolean isEmailBanned(String email) {
        // Récupérez l'utilisateur à partir de l'e-mail
        Optional<User> userOptional = userRepository.findByEmail(email);
        // Vérifiez si l'utilisateur existe et s'il est banni
        return userOptional.isPresent() && userOptional.get().isBanned();
    }
    // Modified banUser method to accept user ID and ban status
    public boolean updateBanStatus(String email, boolean banned) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setBanned(banned);
            // Update other user details if needed
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public long getBanDuration(String email) {
        // Récupérez l'utilisateur à partir de l'e-mail
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.isBanned()) {
            // Récupérez la date d'expiration du bannissement
            Date banExpirationDate = user.getBanExpirationDate();
            // Récupérez la date actuelle
            Date currentDate = new Date();
            // Calculez la durée écoulée en millisecondes
            long banDuration = banExpirationDate.getTime() - currentDate.getTime();
            // Convertissez la durée écoulée en jours (ou toute autre unité de temps nécessaire)
            long banDurationInDays = banDuration / (1000 * 60 * 60 * 24); // Convert to days

            return banDurationInDays;
        }
        return 0; // Si l'utilisateur n'est pas banni ou s'il n'existe pas, retournez 0
    }
    // Service method to get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserProfile(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        existingUser.setFirstname(updatedUser.getFirstname());
        existingUser.setLastname(updatedUser.getLastname());
        // Mettez à jour d'autres champs du profil utilisateur au besoin

        return userRepository.save(existingUser);
    }
    public User getUserProfile(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public String getLoggedInUserRole() {
        // Récupérer l'objet principal de l'authentification depuis le contexte de sécurité
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Vérifier si l'objet principal est de type UserDetails
        if (principal instanceof UserDetails) {
            // Convertir l'objet principal en UserDetails
            UserDetails userDetails = (UserDetails) principal;

            // Récupérer les autorités (rôles) de l'utilisateur connecté
            return userDetails.getAuthorities().toString();
        }

        return null; // Retourner null si aucun utilisateur n'est connecté ou si les rôles ne sont pas disponibles
    }

    public String getUserRoleByEmail(String email) {
        User user = userRepository.findByEmail1(email);
        if (user != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            return user.getRoles().iterator().next().getName().toString();
        }
        return null;
    }
}


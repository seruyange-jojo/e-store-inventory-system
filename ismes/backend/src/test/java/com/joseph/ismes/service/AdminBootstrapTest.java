package com.joseph.ismes.service;

import com.joseph.ismes.entity.Role;
import com.joseph.ismes.entity.User;
import com.joseph.ismes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock private UserRepository users;
    @Mock private PasswordEncoder encoder;
    private User seed;

    @BeforeEach
    void setUp() {
        LocalDateTime created = LocalDateTime.of(2026, 9, 20, 12, 0);
        seed = User.builder().id(1L).username("admin")
                .passwordHash("$2b$10$MqJnsBdG3MAxvAy6zKHKs.PlGltRJaZbdRjIF7UOHIaovr5XFdeFi")
                .fullName("System Administrator").role(Role.ADMIN).active(true)
                .createdAt(created).updatedAt(created).build();
    }

    @Test
    void createsConfiguredAdminOnlyWhenNoUsersExist() {
        when(encoder.encode("new-password")).thenReturn("encoded-password");
        runBootstrap("owner", "new-password");

        ArgumentCaptor<User> created = ArgumentCaptor.forClass(User.class);
        verify(users).save(created.capture());
        assertThat(created.getValue().getUsername()).isEqualTo("owner");
        assertThat(created.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(created.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(created.getValue().isEnabled()).isTrue();
    }

    @Test
    void customDefaultsConvertUntouchedSeedOnceAndPreserveIdentity() {
        when(users.count()).thenReturn(1L);
        when(users.findByUsername("admin")).thenReturn(Optional.of(seed));
        when(encoder.encode("new-password")).thenReturn("custom-hash");

        runBootstrap("admin", "new-password");
        runBootstrap("admin", "later-password");

        assertThat(seed.getId()).isEqualTo(1L);
        assertThat(seed.getPasswordHash()).isEqualTo("custom-hash");
        verify(users, times(1)).save(seed);
        verify(encoder, times(1)).encode(any());
    }

    @Test
    void canRenameUntouchedSeedWithoutAddingAnotherUser() {
        when(users.count()).thenReturn(1L);
        when(users.findByUsername("admin")).thenReturn(Optional.of(seed));
        when(encoder.encode("new-password")).thenReturn("custom-hash");

        runBootstrap("owner", "new-password");

        assertThat(seed.getUsername()).isEqualTo("owner");
        assertThat(seed.getId()).isEqualTo(1L);
        verify(users).save(seed);
    }

    @Test
    void defaultSettingsLeaveExistingSeedAlone() {
        when(users.count()).thenReturn(1L);
        runBootstrap("admin", "admin123");
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void preservesSeedWhenTargetUsernameAlreadyExists() {
        when(users.count()).thenReturn(2L);
        when(users.findByUsername("admin")).thenReturn(Optional.of(seed));
        when(users.existsByUsername("owner")).thenReturn(true);

        runBootstrap("owner", "new-password");

        assertThat(seed.getUsername()).isEqualTo("admin");
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void preservesAnAccountWhoseProfileWasEditedEvenWithTheSeedPassword() {
        when(users.count()).thenReturn(1L);
        when(users.findByUsername("admin")).thenReturn(Optional.of(seed));
        seed.setUpdatedAt(seed.getCreatedAt().plusDays(1));

        runBootstrap("admin", "new-password");

        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void doesNotCreateAnAdminInAnEstablishedDatabaseWithoutTheLegacySeed() {
        when(users.count()).thenReturn(1L);
        when(users.findByUsername("admin")).thenReturn(Optional.empty());
        runBootstrap("owner", "new-password");
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    private void runBootstrap(String username, String password) {
        new AdminBootstrap(users, encoder, username, password).run(new DefaultApplicationArguments());
    }
}

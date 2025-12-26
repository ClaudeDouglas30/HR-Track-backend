# HR-Track-backend



### Technologies Utilisées

- **Spring Boot** 3.x
- **Spring Security** avec JWT
- **PostgreSQL** pour la base de données
- **Hibernate/JPA** pour l'ORM
- **Lombok** pour réduire le boilerplate
- **SLF4J/Logback** pour les logs

### Installation et Configuration

1. Cloner le repository
2. Configurer les variables d'environnement dans `.env` ou `application.properties`
3. Créer les répertoires de stockage :
   ```bash
   mkdir -p storage/justificatifs
   mkdir -p storage/profiles
   ```
4. Lancer l'application :
   ```bash
   ./mvnw spring-boot:run
   ```

### Sécurité

- Authentification JWT requise pour tous les endpoints
- Validation des permissions (seul le propriétaire peut modifier ses congés)
- Validation des types de fichiers et tailles
- Protection contre les attaques de path traversal
- Isolation des fichiers par utilisateur (sous-dossiers par email)

---

**Date de dernière mise à jour** : 26 décembre 2025
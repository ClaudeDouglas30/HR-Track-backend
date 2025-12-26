# HR Track - Backend

## Description

HR Track est une application backend de gestion des ressources humaines (RH) développée avec Spring Boot. Cette solution permet aux entreprises de digitaliser et d'automatiser la gestion quotidienne de leurs employés, incluant le suivi des pointages, la gestion des congés, et la génération de rapports.

Le backend fournit une API REST complète pour une application mobile frontend, permettant à différents types d'utilisateurs (Employés, Responsables RH, Administrateurs) d'effectuer leurs tâches quotidiennes de manière efficace.

## Objectifs Principaux

- **Digitaliser** la gestion des ressources humaines
- **Automatiser** les processus de pointage et de gestion des congés
- **Centraliser** les données des employés et leurs statistiques
- **Faciliter** la prise de décision grâce aux rapports et statistiques
- **Sécuriser** l'accès aux données selon les rôles utilisateurs

## Rôles et Fonctionnalités

### Employé (Utilisateur Standard)

Personnel de l'entreprise ayant accès aux fonctionnalités de base :

- ✅ Pointage d'arrivée et de départ
- ✅ Consultation de l'historique de pointage
- ✅ Soumission de demandes de congés
- ✅ Consultation des congés validés/refusés
- ✅ Réception de notifications pour les réponses aux demandes
- ✅ Modification des paramètres de l'application (mot de passe, photo de profil, etc.)
- ✅ Connexion/Déconnexion
- ✅ Consultation de ses statistiques personnelles

### Responsable RH (Manager)

Gestionnaire des ressources humaines avec droits de validation :

- ✅ Dispose de tous les droits de l'employé
- ✅ Consultation de la liste complète des employés
- ✅ Validation ou refus des demandes de congés
- ✅ Consultation des pointages de tous les employés
- ✅ Génération de rapports par employé ou par période
- ✅ Réception de notifications pour les nouvelles demandes
- ✅ Export de rapports en Excel
- ✅ Ajout d'annotations aux demandes
- ✅ Consultation du dashboard RH avec statistiques

### Administrateur (Super Admin)

Personne unique chargée de la gestion complète du système :

- ✅ Dispose de tous les droits RH
- ✅ Gestion des comptes utilisateurs (création, modification, suppression)
- ✅ Attribution des rôles (Employé, RH, Admin)
- ✅ Configuration des paramètres de l'application
- ✅ Gestion des types de congés et leurs quotas
- ✅ Consultation des statistiques globales
- ✅ Accès aux logs système
- ✅ Gestion des départements et services
- ✅ Configuration des horaires de travail
- ✅ Validation des comptes utilisateurs créés par les RH

## Technologies Utilisées

- **Java 21** - Langage de programmation
- **Spring Boot 3.5.7** - Framework principal
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Accès aux données
- **PostgreSQL** - Base de données (ou MySQL selon configuration)
- **MapStruct** - Mapping d'objets
- **Apache POI** - Génération de fichiers Excel
- **Thymeleaf** - Templates pour emails
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

Il est recommandé d'avoir intellij

## Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Java 21** ou supérieur
- **Maven 3.8+**
- **PostgreSQL 15+** (ou MySQL 8+)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code recommandés)

## Installation et Configuration

### 1. Cloner le projet

```bash
git clone https://github.com/ClaudeDouglas30/HR-Track-backend.git
cd HR-Track-backend
```

### 2. Configuration de la base de données

Créez une base de données PostgreSQL :

```sql
CREATE DATABASE hr_track;
```

### 3. Configuration de l'application

Créez un fichier `application.properties` dans `src/main/resources/` (ou modifiez) :

```properties
# Base de données
spring.application.name=${SPRING_APP_NAME}
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
server.port=8080

# JWT Security
application.security.jwt.secret=${JWT_SECRET}
application.security.jwt.ttl=${JWT_TTL}
application.security.jwt.refresh-tll=${JWT_REFRESH_TTL}

#Oauth2 Config
spring.security.oauth2.client.registration.google.client-id=${OAUTH2_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${OAUTH2_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/api/v1/oauth2/callback/google
spring.security.oauth2.client.registration.google.scope=profile, email

# Mailing
spring.mail.host=${SPRING_MAIL_HOST}
spring.mail.port=${SPRING_MAIL_PORT}
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Frontend URLs
application.frontend.web.url=${SPRING_FRONTEND_WEB_URL:http://localhost:3000}
application.frontend.mobile.url=${SPRING_FRONTEND_MOBILE_URL:http://localhost:8080}

```

### 4. Créer un le fichier .env à la racine du
 et configurer votre éditeur avec ce fichier .env

### 5. Installation des dépendances 

Votre édition se chargera d'installer les dépendances et lancée automatiquement votre projet

### 5. Lancer l'application

L'application sera accessible sur `http://localhost:8080`

## 📁 Structure du Projet

```
HR-Track-backend/
├── src/
│   ├── main/
│   │   ├── java/com/hrtrack/
│   │   │   ├── controllers/          # Contrôleurs REST
│   │   │   ├── domain/
│   │   │   │   ├── dtos/            # Data Transfer Objects
│   │   │   │   ├── entities/        # Entités JPA
│   │   │   │   ├── requests/        # Objets de requête
│   │   │   │   ├── responses/       # Objets de réponse
│   │   │   │   ├── validation/      # Validateurs métier
│   │   │   │   └── serializers/     # Serializers JSON personnalisés
│   │   │   ├── exception/
│   │   │   │   └── handler/         # Gestionnaires d'exceptions
│   │   │   ├── mappers/             # Mappers MapStruct
│   │   │   ├── repository/          # Repositories JPA
│   │   │   ├── service/             # Interfaces de services
│   │   │   │   └── implementation/  # Implémentations des services
│   │   │   ├── config/              # Configurations Spring
│   │   │   └── HrTrackBackendApplication.java
│   │   └── resources/
│   │       ├── templates/           # Templates Thymeleaf (emails)
│   │       └── application.properties
│   └── test/                        # Tests unitaires et d'intégration
├── storage/                         # Stockage des fichiers uploadés
│   └── justificatifs/
├── pom.xml                          # Configuration Maven
└── README.md
```

## 🔌 API Principales

### Authentification

- `POST /api/v1/auth/login` - Connexion
- `POST /api/v1/auth/register` - Inscription
- `POST /api/v1/auth/forgot-password` - Demande de réinitialisation de mot de passe
- `POST /api/v1/auth/reset-password` - Réinitialisation du mot de passe

### Gestion des Congés

- `GET /api/v1/conges/employee/my-list` - Liste des congés de l'employé
- `GET /api/v1/conges/rh/list` - Liste paginée des congés (RH) avec filtrage
- `GET /api/v1/conges/admin/list` - Liste paginée des congés (Admin) avec filtrage
- `GET /api/v1/conges/rh/{id}` - Détails d'un congé (RH)
- `POST /api/v1/conges/employee/create` - Création d'une demande de congé
- `PUT /api/v1/conges/rh/{id}/accept` - Accepter un congé (RH)
- `PUT /api/v1/conges/rh/{id}/refuse` - Refuser un congé avec motif (RH)
- `PUT /api/v1/conges/rh/{id}/annotation` - Ajouter une annotation (RH)

### Gestion des Utilisateurs

- `GET /api/v1/users/list` - Liste des utilisateurs (RH/Admin)
- `GET /api/v1/users/{id}` - Détails d'un utilisateur
- `POST /api/v1/users/create` - Créer un utilisateur (RH)
- `PUT /api/v1/users/update/{id}` - Modifier un utilisateur (Admin)
- `PUT /api/v1/users/{id}/validate` - Valider un compte (Admin)
- `DELETE /api/v1/users/delete/{id}` - Supprimer un utilisateur (Admin)
- `GET /api/v1/users/admin/statistics` - Statistiques des utilisateurs (Admin)

### Gestion des Employés

- `GET /api/v1/employees/list` - Liste de tous les employés
- `GET /api/v1/employees/{id}` - Détails d'un employé
- `GET /api/v1/employees/statistics` - Statistiques globales des employés

### Présences et Pointages

- `GET /api/v1/attendances/list` - Liste des présences/absences (avec filtrage par date)
- `GET /api/v1/attendances/statistics` - Statistiques des présences

### Rapports

- `GET /api/v1/reports/attendance` - Télécharger un rapport Excel des absences/retards

### Types de Congés

- `GET /api/v1/leave-types/list` - Liste des types de congés
- `POST /api/v1/leave-types/create` - Créer un type de congé
- `PUT /api/v1/leave-types/{id}` - Modifier un type de congé
- `DELETE /api/v1/leave-types/{id}` - Supprimer un type de congé

### Dashboards

- `GET /api/v1/dashboard/admin` - Données du dashboard administrateur
- `GET /api/v1/dashboard/rh` - Données du dashboard RH

## 🔒 Sécurité

L'application utilise Spring Security pour :

- **Authentification JWT** - Tokens sécurisés pour l'authentification
- **Autorisation basée sur les rôles** - Contrôle d'accès par rôle (EMPLOYEE, RH, ADMIN)
- **Hachage des mots de passe** - Utilisation de BCrypt
- **Gestion des sessions** - Gestion sécurisée des sessions utilisateurs
- **CORS configuré** - Autorisation des requêtes cross-origin pour le frontend mobile

## 📊 Base de Données

### Entités Principales

- **User** - Utilisateurs du système (Employés, RH, Admins)
- **Conge** - Demandes de congés
- **TypeConge** - Types de congés (Maladie, Mission, Parental, etc.)
- **Pointage** - Pointages d'arrivée/départ des employés
- **Justificatif** - Justificatifs attachés aux congés (optionnel)


## 📧 Notifications Email

L'application envoie des emails pour :

- Confirmation d'inscription
- Réinitialisation de mot de passe
- Validation de compte
- Réponses aux demandes de congés

Configurez les paramètres SMTP dans `application.properties`.

## 📄 Génération de Rapports

Les rapports Excel sont générés avec Apache POI et peuvent être téléchargés via :

- `/api/v1/reports/attendance?month=2024-01` - Rapport des absences/retards pour un mois donné


**Note** : Ce backend fait partie d'un projet plus large incluant une application mobile frontend. Pour une expérience complète, assurez-vous que le frontend est également configuré et fonctionnel.

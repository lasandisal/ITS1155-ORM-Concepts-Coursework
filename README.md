# The Serenity Mental Health Therapy Center Management System

A robust, enterprise-grade desktop management system developed as coursework for the **Graduate Diploma in Software Engineering (GDSE)** under the module **ITS1155 - ORM Concepts**. 

This application transitions a high-volume mental health therapy center from error-prone manual paper tracking to a secure, digitized solution using a structured, layered desktop architecture.

---

## 🛠️ Tech Stack & Implementation Details

| Technology | Layer / Artifact | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Java 21 (LTS)** | Core Platform | Core | Supports modern language features and robust performance profiles. |
| **JavaFX** | `openjfx:javafx-controls` | `21.0.6` | Powers the rich desktop User Interface with custom FXML views. |
| **Hibernate ORM** | `org.hibernate.orm:hibernate-core` | `6.6.4.Final` | Handles native Object-Relational Mapping, omitting high-level abstractions. |
| **JCache & Ehcache**| `org.ehcache:ehcache` | `3.10.8` | Implements high-performance enterprise Second-Level Caching (L2). |
| **MySQL Connector**| `com.mysql:mysql-connector-j` | `8.3.0` | Provides persistent underlying relational database storage connectivity. |
| **JasperReports** | `net.sf.jasperreports:jasperreports`| `7.0.3` | Engine used to generate, compile, and render patient payment invoices. |
| **jBCrypt** | `org.mindrot:jbcrypt` | `0.4` | Secures credentials via production-grade salted password hashing. |
| **Lombok** | `org.projectlombok:lombok` | `1.18.36` | Reduces boilerplate codes like getters, setters, and constructors. |

---

## ⚙️ Configuration Profiles

### 1. Hibernate & Database Context (`hibernate.properties`)
The application handles configuration through standard properties files instead of old XML documents. It automatically handles structural initialization via `createDatabaseIfNotExist=true`:

```properties
# Relational Database Connection (MySQL)
jakarta.persistence.jdbc.driver=com.mysql.cj.jdbc.Driver
jakarta.persistence.jdbc.url=jdbc:mysql://localhost:3306/serenity_therapy_db?createDatabaseIfNotExist=true
jakarta.persistence.jdbc.user=root
jakarta.persistence.jdbc.password=mysql

# Native Hibernate Management
hibernate.hbm2ddl.auto=update
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.current_session_context_class=thread

# Second-Level Cache (L2) & Query Cache Configuration
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true
hibernate.cache.region.factory_class=org.hibernate.cache.jcache.internal.JCacheRegionFactory
hibernate.jcache.provider=org.ehcache.jsr107.EhcacheCachingProvider
hibernate.javax.cache.uri=/ehcache.xml
jakarta.persistence.sharedCache.mode=ENABLE_SELECTIVE

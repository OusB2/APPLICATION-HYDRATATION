#  Application Hydratation — Guide de Débogage Gradle

Ce guide documente la résolution d'une incompatibilité critique de build Gradle survenue lors de l'importation initiale du projet généré par Google AI Studio.

---

##  Le Problème rencontré

Au premier lancement, la synchronisation Gradle a échoué avec deux erreurs majeures bloquant l'initialisation du projet :

1. **Conflit de Version du Plugin Android (AGP) :** L'IDE (Android Studio / IntelliJ) tentait de forcer la version stable `8.10.1`, tandis qu'un catalogue de versions virtuel imposait la version `9.1.1` expérimentale sur le Classpath.
2. **Erreur de Syntaxe du SDK :** Le script Kotlin DSL comportait une configuration de SDK non standard (`compileSdk { version = release(36) ... }`) provoquant une erreur de signature de type (`None of the following candidates is applicable`).
3. **Conflit de Classpath avec Google Services :** L'importation directe de l'énumération `MissingGoogleServicesStrategy` provoquait un échec d'évaluation du script au chargement.

---

##  Solutions Appliquées & Démarche de Résolution

Pour stabiliser l'environnement sans altérer les dépendances requises par l'application, les corrections suivantes ont été apportées :

### 1. Centralisation et Remplacement des Plugins (`build.gradle.kts` Racine)

Les alias virtuels invisibles ont été contournés à la racine du projet pour imposer des versions de plugins parfaitement stables et compatibles avec l'IDE :

```kotlin
plugins {
  id("com.android.application") version "8.10.1" apply false
  id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
  id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
  id("io.github.takahirom.roborazzi") version "1.26.0" apply false
  id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
  id("com.google.gms.google-services") version "4.4.2" apply false
}
```

### 2. Standardisation du Module Application (`app/build.gradle.kts`)

- **Allègement du bloc `plugins`** : Suppression des mentions de versions redondantes pour laisser la racine piloter l'alignement.
- **Correction du SDK** : Remplacement de la structure imbriquée par la syntaxe stable standard :

```kotlin
compileSdk = 36
```

### 3. Résolution du Typage Strict pour Google Services

Pour contourner les problèmes de cycle d'importation de types de l'interpréteur de scripts Kotlin, l'affectation de la stratégie en cas d'absence du fichier `google-services.json` a été qualifiée par son chemin complet (Fully Qualified Name) :

```kotlin
googleServices {
  missingGoogleServicesStrategy = com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy.WARN
}
```

---

##  Résultat

Après une invalidation complète des caches de l'IDE (`File > Invalidate Caches...`) et une nouvelle synchronisation des fichiers Gradle, le build a été validé avec succès (**BUILD SUCCESSFUL**) en téléchargeant l'intégralité du socle Jetpack Compose, Firebase AI et Room.

---

###  Message de commit suggéré pour l'ajout du fichier :

> `docs: ajout du guide de débogage Gradle dans le README`
##  Auteur
**BAMBA OUSMANE rgl3B**
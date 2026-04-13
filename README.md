# Country-Guesser
ID2216 Final Project

# Installation

## Via Android Studio
This project works best in [Android Studio](https://developer.android.com/studio), which can build and emulate the application on an Android phone.
See [here](https://developer.android.com/studio/run) for detailed instuctions on how to run the emulator.

## Via Android Device
You can directly download the [APK](https://github.com/Kozarsson/Country-Guesser/releases/tag/v0.1.0) and run it locally on your own Android device.

# File structure
```
app
 └ kotlin+java
    └ org.kth.countryguesser
      ├ model
      |  ├ api
      |  |  ├ RestCountriesEndpoints.kt
      |  |  └ WikiDataEndpoints.kt
      |  ├ dto
      |  ├ entity
      |  ├ repository
      |  |  ├ ApiRepository.kt
      |  |  ├ CountryRepository.kt
      |  |  ├ FirebaseAuthRepository.kt
      |  |  ├ FirebaseTokenRepository.kt
      |  |  └ GameRepository.kt
      |  └ service
      |     ├ ApiService.kt
      |     ├ FirebaseMessagingService.kt
      |     ├ RestCountriesApiService.kt
      |     └ RetrofitApiService.kt
      ├ ui
      |  └ model
      |     ├ CountryModelMapper.kt
      |     └ CountryUiModel.kt
      ├ view
      |  ├ components
      |  |  ├ Bars.kt
      |  |  ├ NavGraph.kt
      |  |  └ WIPNotice.kt
      |  ├ GameScreen.kt
      |  ├ HomeScreen.kt
      |  ├ LeaderboardScreen.kt
      |  ├ LoginScreen.kt
      |  ├ RegisterScreen.kt
      |  ├ StudyScreen.kt
      |  └ UserScreen.kt
      ├ viewmodel
      |  ├ AuthVM.kt
      |  └ GameVM.kt
      ├ di
      |  ├ NetworkModule.kt
      |  ├ RepositoryModule.kt
      |  └ UtilModule.kt
      ├ util
      |  ├ Constants.kt
      |  ├ NetworkUtils.kt
      |  └ WikiDataParser.kt
      ├ Application.kt
      └ MainActivity.kt
```

>This Kotlin project follows the Model-View-ViewModel (MVVM) architecture, which is conceptually similar to the MVP structure.

| MVVM concept | MVP concept |
|--------------|-------------|
|  Model       |  Model      |
|  View        |  View       |
|  ViewModel   |  Presenter  |

### [`model`](/app/src/main/java/org/kth/countryguesser/model)
Anything in the `repository` directory is responsible for managing app data sources and business logic. Directories `api` and `service` are responsible for interaction with internet-dependent services.

### [`ui`](/app/src/main/java/org/kth/countryguesser/ui)
The directory contains files relevant to mapping internal models to user-friendly models used for display purposes.

### [`view`](/app/src/main/java/org/kth/countryguesser/view)
The `view` directory contains navigable screens that the user can interact with.

### [`viewmodel`](/app/src/main/java/org/kth/countryguesser/viewmodel)
This directory contains files that bridge the model and view. These files handle logic local to displayed screens.

### [`di`](/app/src/main/java/org/kth/countryguesser/di)
This directory contains Hilt dependency-injection modules used to provide networking, repositories, and utility bindings.

### [`util`](/app/src/main/java/org/kth/countryguesser/util)
This directory contains shared utilities and constants that support networking and data parsing.


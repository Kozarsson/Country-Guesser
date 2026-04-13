# Country-Guesser
ID2216 Final Project

# Installation
This project works best in [Android Studio](https://developer.android.com/studio), which can build and emulate the application on an Android phone.

# File structure
```
app
 └ kotlin+java
    └ org.kth.countryguesser
      ├ model
      |  ├ api
      |  |  ├ RestCouyntriesEndpoints.kt
      |  |  └ WikiDataEndpoints.kt
      |  ├ repository
      |  |  ├ ApiRepository.kt
      |  |  ├ CoyuntryRepository.kt
      |  |  ├ FirebaseARuthRepository.kt
      |  |  └ FirebaseTokenRepository
      |  └ service
      |     ├ ApiService.kt
      |     ├ FirebaseMessagingService.kt
      |     └ RestCountriesApiService.kt
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
      └ MainActivity.kt
```

>This Kotlin Project operates under the Model-View-Viewmodel (MVVM) structure which is very similar to the MVP structure used with React.

| MVVM concept | MVP concept |
|--------------|-------------|
|  Model       |  Model      |
|  View        |  View       |
|  Viewmodel   |  Presenter  |

### [`model`](/app/src/main/java/org/kth/countryguesser/model)
Anything in the `repository` directory is responsible for storing local data relevant to the operation of the app. Directories `api` and `service` are responsible for interaction with internet-dependent services.

### [`ui`](/app/src/main/java/org/kth/countryguesser/ui)
The direcotry contains files relevant to mapping the internal model to a more user-frieldly model used for display purposes.

### [`view`](/app/src/main/java/org/kth/countryguesser/view)
The `view` direcotry contains any navigable screen that the user will be able to interact with.

### [`viewmodel`](/app/src/main/java/org/kth/countryguesser/viewmodel)
This direcotry contains the files bridging the model and view. The files handle all logic local to displayed screens.

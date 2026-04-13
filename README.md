# Country-Guesser
An Android native game about figuring out the correct country based on statistics about it. Based on what the user guesses, they will get higer/lower indications for statistics about that country compared to the correct answer. The player will be awarded a score based on the number of guesses it took to figure out the correct country. Each day, a new country is randomly selected. There is also an 'endless' mode where the goal is to get the longest possible streak of correct guesses. Learn more about different countries while competing against friends!

# Installation

## Via Android Studio
This project works best in [Android Studio](https://developer.android.com/studio), which can build and emulate the application on an Android phone.
See [here](https://developer.android.com/studio/run) for detailed instuctions on how to run the emulator.

## Via Android Device
You can directly download the [APK](https://github.com/Kozarsson/Country-Guesser/releases/tag/v0.1.0) and run it locally on your own Android device or emulator of choice.

# Features
- [x] sign-in screen
  - [x] register account
  - [x] sign in
- [x] home page
  - [x] chose gamemode to play
  - [ ] leaderboard
- [x] game screen
  - [x] randomized country
    - [x] 'daily' gamemode shares same conutry accross all users
  - [x] gamemode dependent scoring
    - [ ] update personal statistics
  - [x] show country clues
    - [x] show direction of correct guess
    - [ ] clue based on country flag color
  - [x] guess
    - [x] guess suggestions
  - [x] success alert
    - [x] gamemode dependent handler
  - [ ] failure alert
- [ ] study screen
  - [ ] list countries
  - [ ] searchable countries
  - [ ] view country statistics
- [ ] profile screen
  - [ ] view personal stats/information
  - [ ] save personal stats/information
  - [ ] cloud persistence
  - [ ] personal settings
- [x] navigation bar
     
### Additional features
- temporary cheat button (show correct anwser)
- WIP alert for unfinished screens

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


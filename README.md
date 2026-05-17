# Country-Guesser
An Android native game about figuring out the correct country based on statistics about it. Based on what the user guesses, they will get higher/lower indications for statistics about that country compared to the correct answer. The player will be awarded a score based on the number of guesses it took to figure out the correct country. Each day, a new country is randomly selected. There is also an 'endless' mode where the goal is to get the longest possible streak of correct guesses. Learn more about different countries while competing against friends!

# Installation

## Via Android Studio
This project works best in [Android Studio](https://developer.android.com/studio), which can build and emulate the application on an Android phone.
See [here](https://developer.android.com/studio/run) for detailed instuctions on how to run the emulator.

## Via Android Device
You can directly download the [APK](https://github.com/Kozarsson/Country-Guesser/releases/tag/v0.1.0) and run it locally on your own Android device or emulator of choice.

# Features
- [x] fetch data from API
  - [x] restcountries
  - [x] wikidata
- [x] sign-in screen
  - [x] register account
  - [x] sign in
- [x] home page
  - [x] choose gamemode to play
  - [x] leaderboard
- [x] game screen
  - [x] randomised country
    - [x] 'daily' gamemode shares same conutry across all users
  - [x] gamemode dependent scoring
    - [x] update personal statistics
  - [x] show country clues
    - [x] show direction of correct guess
  - [x] guess
    - [x] guess suggestions
  - [x] map
    - [x] zoom/pan
    - [x] highlight guessed countries
  - [x] success alert
    - [x] gamemode dependent handler
  - [ ] failure alert
- [x] study screen
  - [x] list countries
  - [x] searchable countries
  - [x] view country statistics
- [x] profile screen
  - [x] view personal stats/information
  - [x] save personal stats/information
  - [x] cloud persistence
  - [ ] personal settings
- [x] navigation bar

## Design Decisions
### Signed out use
The app still allows the user to play, even when signed out.

### Cloud Clock
The 'daily challenge' is supposed to update only once per day, at the same time for all users. Using the device's interlan time allows the user to avoid this limitation by changing their system clock. That is why we fetch the time from Firebase. This prevents the user from intentionally tampering with the app's intended functions.

### Async image
Images are loaded asynchronously using a 3rd party library. Using this library allows the app to function properly even when the user has a slow internet cnnection.

### Internet aware
If the user has no, or an exceptionally slow, internet connection the app will detect this with a system timeout and alert the user. This prevents the app from crashing and can still be used for non-internet related features. This also works when the API used is under heavy load and rate limits the connection.

### Map
When playing, the user has access to a world map displaying the 193 UN member states. This is because user evaluation showed that it was difficult to recall all countries and territories under pressure. The map will mark the already guessed countries to further help the user. Further user testing showed that a user would frequently open the map to view a region of interest. This was difficult when the map reset the zoom every time it was toggled. This was changed to persist the zoom and pan.

The map uses an SVG to render. This SVG is stored on the device and does not rely on an internet connection, lightening the network load.

### Search function
To make it easier for the user to guess certain countries, an auto-complete feature was implemented. This makes use of a local list of every possible country to make a large gain in performance compared to repeatedly fetching a list from the API any time it's needed. This list of countries is fetched from the API between app uses and is cached locally only temporarily. This makes the list adaptible to changes in the APi and will not include countries or territories not available in the API.

### Study screen
Between games, the user can study countries unfammiliar to them, by scrolling or searching for an countri or territory. The list is orederd alphabetically but allows the user to fillter the list through a text field.

### Leaderboard
The leaderboard shows the longest streaks by users. To increase security this should be a cloud-function because the user shouldn't have access to edit the entire database. However, this requires a premium Firebase subscription and was decided to be out fo scope for this reason. As of the current implementation, every user is allowed to edit the entire database.

# File structure
```
app
 └ kotlin+java
    └ org.kth.countryguesser
      ├ di
      |  ├ DatabaseModule.kt
      |  ├ NetworkModule.kt
      |  ├ RepositoryModule.kt
      |  └ UtilModule.kt
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
      |     ├ CountryUiModel.kt
      |     ├ PlayerModelMapper.kt
      |     └ PlayerUiModel.kt
      ├ view
      |  ├ components
      |  |  ├ Bars.kt
      |  |  ├ Dialogs.kt
      |  |  ├ NavGraph.kt
      |  |  └ Map.kt
      |  ├ ChangePasswordScreen.kt
      |  ├ GameScreen.kt
      |  ├ HomeScreen.kt
      |  ├ LeaderboardScreen.kt
      |  ├ LoginScreen.kt
      |  ├ RegisterScreen.kt
      |  ├ StudyScreen.kt
      |  └ UserScreen.kt
      ├ viewmodel
      |  ├ AuthVM.kt
      |  ├ BaseVM.kt
      |  ├ GameVM.kt
      |  ├ LeaderboardVM.kt
      |  ├ ProfileStatsVM.kt
      |  └ StudyVM.kt
      ├ util
      |  ├ Constants.kt
      |  ├ NetworkUtils.kt
      |  ├ PopupState.kt
      |  ├ TimeAndDate.kt
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


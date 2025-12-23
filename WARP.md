# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Common commands

All commands assume the project root (this directory) and use the Gradle wrapper.

### Build
- Assemble a debug APK for the app module:
  - `./gradlew :app:assembleDebug`
- Full build of all modules (useful for CI/local verification):
  - `./gradlew build`

### Tests
- Run all unit tests (all modules):
  - `./gradlew test`
- Run app unit tests only (debug variant):
  - `./gradlew :app:testDebugUnitTest`
- Run a single test method (example, adjust class/method as needed):
  - `./gradlew :app:testDebugUnitTest --tests "com.example.calview.ExampleUnitTest.addition_isCorrect"`
- Run instrumented Android tests for the app:
  - `./gradlew :app:connectedAndroidTest`

### Lint and static checks
- Run Android/Compose lint for all modules:
  - `./gradlew lint`
- Run lint for just the app module (debug):
  - `./gradlew :app:lintDebug`

## Project structure and architecture

This is a multi-module Jetpack Compose Android application centered around calorie tracking, onboarding, and AI-assisted food logging. The Gradle setup uses a version catalog (`gradle/libs.versions.toml`) and standard Android plugins (application, library, Kotlin, Compose, Hilt, KSP, serialization, Google Services, Firebase Crashlytics).

High-level module layout (from `settings.gradle.kts`):
- `:app`
- `:core-ai`
- `:core-data`
- `:core-ui`
- `:feature-onboarding`
- `:feature-dashboard`
- `:feature-scanner`
- `:feature-trends`

### App module (`:app`)

The `app` module is the Android entry point and hosts the top-level navigation:
- Application class: `com.example.calview.CalViewApp` (annotated with `@HiltAndroidApp`).
- Main activity: `com.example.calview.MainActivity` (annotated with `@AndroidEntryPoint`).

Key composables:
- `AppNavigation()` sets up a `NavHost` with three main routes:
  - `"onboarding"` → onboarding flow.
  - `"main"` → main tabbed experience.
  - `"scanner"` → camera-based food scanner.
- `MainTabs(onScanClick)` implements a bottom navigation bar with three tabs:
  - Home → dashboard view.
  - Progress → progress/trends view.
  - Settings → settings/profile view.
  A centered FAB triggers navigation to the scanner route.

The `app` module currently contains an older `ui` package (`ui/components`, `ui/screens`, `ui/viewmodels`, `ui/theme`) that mirrors much of what now lives in `core-ui` and the `feature-*` modules. When modifying existing flows, prefer the feature and core modules described below; the `app.ui` structures can be treated as legacy/transition code.

### Core modules

#### `:core-ui` — design system and shared UI components

`core-ui` centralizes reusable Compose UI primitives and theming:
- Theme:
  - `core.ui.theme.Color.kt` defines the Cal AI color palette (e.g., `CalAIGreen`, macro colors).
  - `core.ui.theme.Type.kt` defines typography.
  - `core.ui.theme.Shape.kt` defines rounded corner shapes.
  - `core.ui.theme.Theme.kt` exposes `CalViewTheme` (used by `MainActivity`) and configures status bar appearance.
- Components:
  - `CalAICard` — card container with selection styling and optional leading/content slots.
  - `CalAIButton` — primary full-width button used across onboarding.
  - `CalorieRing` — circular progress ring for calories or macro visualization.
  - `StandardWheelPicker` — scrollable wheel picker used for dates, height, and weight.
  - `UnitToggle` — toggle between imperial and metric units.

When adding shared visual elements or changing the overall look and feel, prefer updating/adding components in `core-ui` rather than per-feature.

#### `:core-data` — persistence and repositories

`core-data` encapsulates local storage and repository abstractions:
- Room database:
  - `core.data.local.AppDatabase` — `RoomDatabase` for the app, currently with `MealEntity`.
  - `core.data.local.MealEntity` — stored meal with calories/macros and timestamp.
  - `core.data.local.MealDao` — queries meals, including a date-range query used to derive "today".
- Data repositories:
  - `core.data.repository.MealRepository`/`MealRepositoryImpl` — flow-based access to all meals and today’s meals, plus log/delete operations.
  - `core.data.repository.UserPreferencesRepository`/`UserPreferencesRepositoryImpl` — wraps `DataStore<Preferences>` for onboarding-derived user profile and recommended macros. Exposes `Flow` properties for user goal, gender, age, height/weight, macro targets, and onboarding completion flag.
- Hilt DI modules:
  - `core.data.di.DatabaseModule` — provides `AppDatabase` and `MealDao` via Hilt.
  - `core.data.di.RepositoryModule` — binds repository interfaces to their implementations.

Any new persistent domain concepts (e.g., additional tracked metrics) should usually be modeled in `core-data` and surfaced to view models via repositories.

#### `:core-ai` — AI integration (Gemini)

`core-ai` owns the integration with Google AI / Gemini for food analysis:
- API models:
  - `core.ai.model.FoodAnalysisResponse` and related types (`FoodItem`, `NutritionalData`, `Macros`, `ErrorResponse`) describe the JSON schema returned by the AI.
- Service abstraction:
  - `core.ai.FoodAnalysisService` defines the contract for analyzing a `Bitmap` and returning a `Result<FoodAnalysisResponse>`.
- Implementation and DI:
  - `core.ai.GeminiFoodAnalysisService` calls `GenerativeModel.generateContent`, sends the image + prompt, strips optional ```json fences, deserializes JSON with kotlinx.serialization, and falls back to a hard-coded mock `FoodAnalysisResponse` on error.
  - `core.ai.di.AiModule` is a Hilt module that constructs a `GenerativeModel` (currently configured for the `gemini-1.5-flash` model with an API key) and provides a singleton `FoodAnalysisService` implementation.

All AI-related behavior (prompt changes, model selection, alternative providers, or test doubles) should be funneled through `core-ai`.

### Feature modules

Each `feature-*` module owns a specific user-facing flow, wiring together `core-*` dependencies and UI.

#### `:feature-onboarding` — multi-step onboarding flow

This module hosts the onboarding wizard that collects user goals and profile information, eventually computing recommended macros and persisting them via `UserPreferencesRepository`.

Key pieces:
- `OnboardingViewModel` (Hilt view model) holds `OnboardingUiState`, including goal, gender, workouts per week, diet preference, unit system, height, weight, birthdate, and computed macro recommendations. It:
  - Calculates user age from birth year.
  - Saves user profile and macro recommendations to `UserPreferencesRepository`.
  - Marks onboarding as complete via `setOnboardingComplete(true)`.
- Screens (all under `feature.onboarding`):
  - Early motivational screens (e.g., `AccomplishmentsScreen`, `RatingsScreen`, `TrustScreen`).
  - Data collection screens (e.g., `BirthDateScreen`, `HeightWeightScreen`, `WorkoutsScreen`, `GoalSelectionScreen`, `DietPreferenceScreen`, `CaloriesBurnedSettingScreen`, `CalorieRolloverScreen`).
  - Referral/source and prior-apps screens (`SourceScreen`, `PreviousAppsScreen`, `ReferralCodeScreen`).
  - Plan generation and confirmation (`PlanGenerationScreen`, `SetupProgressScreen`, `CustomPlanReadyScreen`, `GoalsExplanationScreen`, `TrialScreen`, etc.).
- Layout template:
  - `components/OnboardingTemplate` (in this module) standardizes the onboarding page layout (heading, progress indicator, back/continue buttons), used by most onboarding screens.

In the `app` module, `OnboardingNavHost` orchestrates the screen sequence and uses `hiltViewModel<OnboardingViewModel>()` to back the entire flow.

#### `:feature-dashboard` — home dashboard

Responsible for the main "Home" tab experience:
- `feature.dashboard.DashboardViewModel` subscribes to `MealRepository.getMealsForToday()` and a `dailyGoal` state flow, combining them into a `DashboardState` that includes:
  - Calories consumed/remaining and goal.
  - Daily totals for protein, carbs, and fats.
  - The list of `MealEntity` instances for the day.
- `feature.dashboard.DashboardScreen` (and supporting composables) render:
  - Header (`"Cal AI"` branding and streak/badge surface).
  - Date selector for the week.
  - Horizontal pager of summary cards, including total calories (`CaloriesCard`), macro stats, micro-nutrient placeholders, health score, and activity/water trackers.
  - "Recently uploaded" meal list using `RecentMealCard`, which consumes meals from `core-data`.

`MainTabs` in `MainActivity` uses the dashboard view as the content for the "Home" tab.

#### `:feature-scanner` — camera-based food scanner

This module encapsulates the CameraX + AI flow for scanning meals:
- `feature.scanner.ScannerViewModel` holds a `ScannerUiState` sealed class (`Idle`, `Loading`, `Success`, `Error`, `Logged`) and depends on:
  - `core.ai.FoodAnalysisService` for live analysis.
  - `core.data.repository.MealRepository` for persisting the logged meal.
  It triggers analysis on captured bitmaps and logs meals based on the AI’s aggregated totals.
- `feature.scanner.ScannerScreen` composes:
  - A `PreviewView` from CameraX for the live camera feed.
  - CameraX `Preview` + `ImageCapture` binding to the lifecycle.
  - `ScannerOverlay`, which:
    - Shows a close button.
    - Displays a capture button / loading indicator / `AnalysisResultBottomSheet` depending on `ScannerUiState`.
    - In the success state, presents nutrition info (`FoodAnalysisResponse.total`) and a "Log Meal" CTA that calls back into the view model.

`MainTabs`’ FAB navigates to the scanner route, which hosts this feature’s UI.

#### `:feature-trends` — progress/trends view

`feature-trends` contains the UI for the "Progress" tab:
- `feature.trends.ProgressScreen` renders BMI, weight goal, bar-chart-like weekly view, and macro legends. It is currently driven by hard-coded values and placeholders rather than live data.
- When wiring real data, it will likely consume flows from `core-data` similar to the dashboard.

### Navigation and flow summary

Putting it together:
- App startup:
  - `CalViewApp` initializes Hilt.
  - `MainActivity` sets `CalViewTheme` and calls `AppNavigation()`.
- Navigation graph (`AppNavigation`):
  - Starts at `"onboarding"`, which internally uses `OnboardingNavHost` and the `feature-onboarding` view model and screens.
  - Upon completion, `OnboardingViewModel.completeOnboarding` updates preferences and triggers navigation to `"main"`.
  - `"main"` hosts `MainTabs`, which wires:
    - Home → dashboard (from `feature-dashboard`).
    - Progress → trends/progress (from `feature-trends`).
    - Settings → settings UI (currently implemented in the `app` module under `ui.screens.SettingsScreen`).
  - The central FAB in `MainTabs` pushes the `"scanner"` destination, hosting the scanner feature from `feature-scanner`.

When making cross-cutting changes (e.g., altering how meals are logged or how recommended macros are computed), prefer updating the relevant `core-*` modules and view models, then propagating to feature UIs, rather than duplicating logic inside `app` UI code.

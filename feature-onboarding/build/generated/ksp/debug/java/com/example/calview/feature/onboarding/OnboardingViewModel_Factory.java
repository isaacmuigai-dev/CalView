package com.example.calview.feature.onboarding;

import com.example.calview.core.ai.NutritionRecommendationService;
import com.example.calview.core.data.repository.UserPreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<NutritionRecommendationService> nutritionRecommendationServiceProvider;

  public OnboardingViewModel_Factory(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<NutritionRecommendationService> nutritionRecommendationServiceProvider) {
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.nutritionRecommendationServiceProvider = nutritionRecommendationServiceProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(userPreferencesRepositoryProvider.get(), nutritionRecommendationServiceProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<NutritionRecommendationService> nutritionRecommendationServiceProvider) {
    return new OnboardingViewModel_Factory(userPreferencesRepositoryProvider, nutritionRecommendationServiceProvider);
  }

  public static OnboardingViewModel newInstance(UserPreferencesRepository userPreferencesRepository,
      NutritionRecommendationService nutritionRecommendationService) {
    return new OnboardingViewModel(userPreferencesRepository, nutritionRecommendationService);
  }
}

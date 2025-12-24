package com.example.calview.feature.dashboard;

import com.example.calview.core.data.repository.MealRepository;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<MealRepository> mealRepositoryProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public DashboardViewModel_Factory(Provider<MealRepository> mealRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.mealRepositoryProvider = mealRepositoryProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(mealRepositoryProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<MealRepository> mealRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new DashboardViewModel_Factory(mealRepositoryProvider, userPreferencesRepositoryProvider);
  }

  public static DashboardViewModel newInstance(MealRepository mealRepository,
      UserPreferencesRepository userPreferencesRepository) {
    return new DashboardViewModel(mealRepository, userPreferencesRepository);
  }
}
